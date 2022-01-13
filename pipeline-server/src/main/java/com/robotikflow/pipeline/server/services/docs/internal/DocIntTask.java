package com.robotikflow.pipeline.server.services.docs.internal;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import com.robotikflow.core.interfaces.IActivityService;
import com.robotikflow.core.interfaces.props.FileProps;
import com.robotikflow.core.models.entities.DocumentInt;
import com.robotikflow.core.models.entities.DocumentType;
import com.robotikflow.core.models.entities.DocumentOperationType;
import com.robotikflow.core.models.queue.DocIntCopiedMessage;
import com.robotikflow.core.models.queue.DocIntCreatedMessage;
import com.robotikflow.core.models.queue.DocIntDeletedMessage;
import com.robotikflow.core.models.queue.DocIntUpdatedMessage;
import com.robotikflow.core.models.queue.Message;
import com.robotikflow.core.util.DocumentUtil;
import com.robotikflow.core.util.IdUtil;
import com.robotikflow.pipeline.server.services.BaseTask;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaMetadataKeys;
import org.apache.tika.sax.BodyContentHandler;
import org.artofsolving.jodconverter.office.OfficeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import net.coobird.thumbnailator.Thumbnails;

public class DocIntTask 
{
	protected final Logger logger = LoggerFactory.getLogger(BaseTask.class);
	private final DocIntContext context;
	
	public DocIntTask(DocIntContext context)
	{
		this.context = context;
	}
	
	private <T> void startThreadsAndWait(
		final List<Consumer<T>> funcs, 
		final T msg)
	{
		try 
		{
			var latch = new CountDownLatch(funcs.size());
			
			funcs.forEach(f -> 
			{
				context.getThreadPool().submit(() -> 
				{
					f.accept(msg);
					latch.countDown();
				});
			});

			latch.await();
		} 
		catch (InterruptedException e) 
		{
			logger.error(e.getMessage(), e);
		}
	}

	public void handleOnDocUpdated(
		final DocIntUpdatedMessage mensagem) 
	{
		var doc = DocumentUtil.toInterno(context.getDocumentRepo().findById(mensagem.getId()).get());
		
		List<Consumer<DocIntUpdatedMessage>> funcs = List.of(
			(msg) -> atualizarIndice(doc),
			(msg) -> executarIntegracao(doc, msg, DocumentOperationType.UPDATED)
		);
			
		startThreadsAndWait(funcs, mensagem);
	}
	
	private void atualizarIndice(
		final DocumentInt doc) 
	{
		try 
		{
			if(doc.getType() == DocumentType.FOLDER || doc.getExtension() == null || 
				!context.getConfig().extensoesIndexaveis.contains(doc.getExtension().toLowerCase()))
			{
				return;
			}
			
			context.getIndexador().atualizar(
				doc.getPubId(), 
				doc.getParent().getPubId(), 
				doc.getSize(), 
				Date.from(doc.getUpdatedAt().toInstant()), 
				doc.getWorkspace().getPubId());
		} 
		catch (Exception e) 
		{
			context.getDocLogger().error(
				doc, String.format("Atualização de índice falhou para o document %s", doc.getName()), e);
		}
	}

	public void handleOnDocCopied(
		final DocIntCopiedMessage mensagem) 
	{
		var doc = DocumentUtil.toInterno(context.getDocumentRepo().findById(mensagem.getId()).get());
		var fonte = DocumentUtil.toInterno(context.getDocumentRepo().findById(mensagem.getFonteId()).get());
		
		List<Consumer<DocIntCopiedMessage>> funcs = List.of(
				(msg) -> copiarThumb(fonte, doc),
				(msg) -> copiarIndice(fonte, doc),
				(msg) -> copiarNaCloud(fonte, doc),
				(msg) -> executarIntegracao(doc, msg, DocumentOperationType.MOVED)
			);
			
		startThreadsAndWait(funcs, mensagem);
		
		context.getDocumentRepo().save(doc);
	}

	private void copiarThumb(
		final DocumentInt fonte, 
		final DocumentInt doc) 
	{
		try 
		{
			if(doc.getType() == DocumentType.FOLDER || doc.getExtension() == null ||
				!context.getConfig().extensoesThumbiaveis.contains(doc.getExtension().toLowerCase()))
			{
				return;
			}
			
			var bucket = doc.getWorkspace().getPubId() + DocIntContext.getSufixoRepoPublico();
			
			var thumbId = IdUtil.genId();
			doc.setThumbId(thumbId);
			context.getStorage().copyContents(bucket, fonte.getThumbId() + ".jpg", bucket, thumbId + ".jpg");
			
			var previewId = IdUtil.genId();
			doc.setPreviewId(previewId);
			context.getStorage().copyContents(bucket, fonte.getPreviewId() + ".jpg", bucket, previewId + ".jpg");
			
		}
		catch (Exception e) 
		{
			context.getDocLogger().error(
				doc, String.format("Cópia de thumb falhou para o document %s", doc.getName()), e);
		}
	}
	
	private void copiarIndice(
		final DocumentInt fonte, 
		final DocumentInt doc) 
	{
		try 
		{
			if(doc.getType() == DocumentType.FOLDER || doc.getExtension() == null ||
				!context.getConfig().extensoesIndexaveis.contains(doc.getExtension().toLowerCase()))
			{
				return;
			}
			
			context.getIndexador().copiar(fonte.getPubId(), doc.getName(), doc.getExtension(), 
					doc.getPubId(), doc.getParent().getPubId(), doc.getSize(), 
					Date.from(doc.getCreatedAt().toInstant()), doc.getWorkspace().getPubId());
		} 
		catch (Exception e) 
		{
			context.getDocLogger().error(
				doc, String.format("cópia de índice falhou para o document %s", doc.getName()), e);
		}
	}

	private void copiarNaCloud(
		final DocumentInt fonte, 
		final DocumentInt doc) 
	{
		try 
		{
			var blobId = IdUtil.genId();
			doc.setBlobId(blobId);
			context.getStorage()
				.copyContents(doc.getWorkspace().getPubId(), fonte.getBlobId(), doc.getWorkspace().getPubId(), blobId);
		} 
		catch (Exception e) 
		{
			context.getDocLogger().error(
				fonte, String.format("Cópia na cloud falhou para o document %s", doc.getName()), e);
		}
	}
	
	public void handleOnDocDeleted(
		final DocIntDeletedMessage mensagem) 
	{
		var doc = DocumentUtil.toInterno(context.getDocumentRepo().findById(mensagem.getId()).get());
		
		List<Consumer<DocIntDeletedMessage>> funcs = List.of(
				(msg) -> removerThumb(doc, msg),
				(msg) -> removerIndice(doc, msg),
				(msg) -> removerDaCloud(doc, msg),
				(msg) -> executarIntegracao(doc, msg, DocumentOperationType.REMOVED)
			);

		startThreadsAndWait(funcs, mensagem);
	}
	
	private void removerThumb(
		final DocumentInt doc, 
		final DocIntDeletedMessage msg) 
	{
		try 
		{
			if(doc.getType() == DocumentType.FOLDER || doc.getExtension() == null ||
				!context.getConfig().extensoesThumbiaveis.contains(doc.getExtension().toLowerCase()))
			{
				return;
			}
			
			var bucket = doc.getWorkspace().getPubId() + DocIntContext.getSufixoRepoPublico();
			context.getStorage().deleteContents(bucket, doc.getThumbId() + ".jpg");
			context.getStorage().deleteContents(bucket, doc.getPreviewId() + ".jpg");
		}
		catch (Exception e) 
		{
			context.getDocLogger().error(
				doc, String.format("Remoção de thumb falhou para o document %s", doc.getName()), e);
		}
	}
	
	private void removerIndice(
		final DocumentInt doc, 
		final DocIntDeletedMessage msg) 
	{
		try 
		{
			if(doc.getType() == DocumentType.FOLDER || doc.getExtension() == null ||
				!context.getConfig().extensoesIndexaveis.contains(doc.getExtension().toLowerCase()))
			{
				return;
			}
			
			context.getIndexador().remover(doc.getPubId(), doc.getWorkspace().getPubId());
		} 
		catch (Exception e) 
		{
			context.getDocLogger().error(
				doc, String.format("Remoção de índice falhou para o document %s", doc.getName()), e);
		}
	}
	
	private void removerDaCloud(
		final DocumentInt doc, 
		final DocIntDeletedMessage msg) 
	{
		try 
		{
			context.getStorage().deleteContents(doc.getWorkspace().getPubId(), doc.getBlobId());
		} 
		catch (Exception e) 
		{
			context.getDocLogger().error(
				doc, String.format("remoção da cloud falhou para o document %s", doc.getName()), e);
		}
	}

	public void handleOnDocCreated(
		final DocIntCreatedMessage mensagem) 
	{
		var doc = DocumentUtil.toInterno(context.getDocumentRepo().findById(mensagem.getId()).get());
		
		List<Consumer<DocIntCreatedMessage>> funcs = List.of(
				(msg) -> criarThumb(doc, msg.getCaminhoCompleto()),
				(msg) -> criarIndice(doc, msg.getCaminhoCompleto()),
				(msg) -> enviarParaCloud(doc, msg.getCaminhoCompleto()),
				(msg) -> executarIntegracao(doc, msg, DocumentOperationType.CREATED)
			);
		
		startThreadsAndWait(funcs, mensagem);

		context.getDocumentRepo().save(doc);
		
		removerTemporario(mensagem);
	}
	
	private void criarThumb(
		final DocumentInt doc, 
		final String caminhoCompleto) 
	{
		try 
		{
			if(doc.getType() == DocumentType.FOLDER || doc.getExtension() == null)
			{
				return;
			}
			
			var extensao = doc.getExtension().toLowerCase();
			if(!context.getConfig().extensoesThumbiaveis.contains(extensao))
			{
				return;
			}
			
			var arquivo = new File(caminhoCompleto);

			BufferedImage preview = null;
			BufferedImage thumb;
			
			// document do Office? converter para PDF e então gerar thumb
			if(context.getConfig().extensoesDoOffice.contains(extensao))
			{
				preview = converterOfficeToPdf(arquivo, extensao); 
				
				thumb = Thumbnails.of(preview)
						.size(context.getConfig().THUMB_WIDTH, context.getConfig().THUMB_HEIGHT)
							.asBufferedImage();
			}
			// pdf? renderizar primeiro
			else if(extensao.equals("pdf"))
			{
				preview = renderizarPdf(arquivo);
				
				thumb = Thumbnails.of(preview)
						.size(context.getConfig().THUMB_WIDTH, context.getConfig().THUMB_HEIGHT)
							.asBufferedImage();
			}
			// imagens.. só converter para o tamanho e formato de thumb
			else
			{
				preview = Thumbnails.of(arquivo)
						.size(context.getConfig().PREVIEW_WIDTH, context.getConfig().PREVIEW_HEIGHT)
							.asBufferedImage();

				thumb = Thumbnails.of(arquivo)
						.size(context.getConfig().THUMB_WIDTH, context.getConfig().THUMB_HEIGHT)
							.asBufferedImage();
			}
			
			var bucket = doc.getWorkspace().getPubId() + DocIntContext.getSufixoRepoPublico();
			
			// thumb
			var thumbId = IdUtil.genId();
			context.getStorage().sendContents(thumb, "jpg", bucket, thumbId + ".jpg");
			thumb.flush();
			doc.setThumbId(thumbId);
			
			// preview
			var previewId = IdUtil.genId();
			context.getStorage().sendContents(preview, "jpg", bucket, previewId + ".jpg");
			preview.flush();
			doc.setPreviewId(previewId);
		} 
		catch (Exception e) 
		{
			context.getDocLogger().error(
				doc, String.format("geração de thumb falhou para o document %s", doc.getName()), e);
		}
	}

	private BufferedImage converterOfficeToPdf(
		final File arquivo, 
		final String extensao) 
		throws OfficeException, IOException 
	{
		try(var officeInput = FileUtils.openInputStream(arquivo))
		{
			var pdfOutput = new ByteArrayOutputStream();
			context.getOfficeConverter().convert(
				officeInput, 
				context.getOfficeConverter().getFormatRegistry().getFormatByExtension(extensao), 
				pdfOutput, 
				context.getOfficeConverter().getFormatRegistry().getFormatByExtension("pdf"), 
				"1");
			
			PDDocument pdf = null;
			try
			{
				pdf = PDDocument.load(pdfOutput.toByteArray());
				
				pdfOutput.close();
			
				return converterPdfToImg(pdf);
			}
			finally 
			{
				if(pdf != null) 
				{
					pdf.close();
				}
			}
		}
	}

	private BufferedImage converterPdfToImg(
		final PDDocument pdf) 
		throws IOException 
	{
		var renderer = new PDFRenderer(pdf);
	
		return renderer.renderImageWithDPI(0, 96, ImageType.RGB);
	}

	private BufferedImage renderizarPdf(
		final File arquivo) 
		throws InvalidPasswordException, IOException 
	{
		PDDocument pdf = null; 
		try
		{
			pdf = PDDocument.load(arquivo);
		
			return converterPdfToImg(pdf);
		}
		finally 
		{
			if(pdf != null) 
			{
				pdf.close();
			}
			
		}
	}
	
	private void criarIndice(
		final DocumentInt doc, 
		final String caminhoCompleto) 
	{
		try 
		{
			if(doc.getType() == DocumentType.FOLDER || doc.getExtension() == null ||   
				!context.getConfig().extensoesIndexaveis.contains(doc.getExtension().toLowerCase()))
			{
				return;
			}

			var conteudo = extrairConteudo(doc, caminhoCompleto);
			
			context.getIndexador().indexar(
				doc.getName(), 
				doc.getExtension(), 
				doc.getPubId(), 
				doc.getParent().getPubId(), 
				doc.getSize(), 
				Date.from(doc.getCreatedAt().toInstant()), 
				doc.getWorkspace().getPubId(), 
				conteudo);
		} 
		catch (Exception e) 
		{
			context.getDocLogger().error(
				doc, String.format("Criação de índice falhou para o document %s", doc.getName()), e);
		}
	}

	private String extrairConteudo(
		final DocumentInt doc, 
		final String caminhoCompleto) 
		throws FileNotFoundException, IOException, SAXException, TikaException 
	{
	    var handler = new BodyContentHandler(100000000);
	    var metadata = new Metadata();
	    metadata.set(TikaMetadataKeys.RESOURCE_NAME_KEY, doc.getName());
	    try (var stream = new FileInputStream(Paths.get(caminhoCompleto).normalize().toFile())) 
	    {
	    	context.getParser().parse(stream, handler, metadata);
			return handler.toString();
	    } 
	}

	private void enviarParaCloud(
		final DocumentInt doc, 
		final String caminhoCompleto) 
	{
		try 
		{
			var blobId = IdUtil.genId();
			context.getStorage().sendContents(
				Paths.get(caminhoCompleto), 
				doc.getWorkspace().getPubId(), 
				blobId, 
				doc.getExtension());
			doc.setBlobId(blobId);
		} 
		catch (Exception e) 
		{
			context.getDocLogger().error(
				doc, String.format("Envio para a cloud falhou para o document %s", doc.getName()), e);
		}
	}

	private void removerTemporario(
		final DocIntCreatedMessage msg) 
	{
		Path caminho = null;
		try 
		{
			if(msg.isUpload())
			{
				caminho = Paths.get(msg.getCaminhoCompleto()).getParent();
				Files.deleteIfExists(Paths.get(caminho.toString(), "data").normalize());
				Files.deleteIfExists(Paths.get(caminho.toString(), "info").normalize());
			}
			else
			{
				caminho = Paths.get(msg.getCaminhoCompleto());
			}
			
			Files.deleteIfExists(caminho.normalize());
		} 
		catch (IOException e) 
		{
			logger.error(String.format("remoção dos arquivos temporários falhou para %s", caminho != null? caminho.toString(): ""), e);
		}
	}

	private void executarIntegracao(
		final DocumentInt doc, 
		final Message msg, 
		final DocumentOperationType op) 
	{
		var collectionService = context.getCollectionService();

		var fileProps = new FileProps(
			doc.getProvider(),
			doc.getName(), 
			doc.getPubId(), 
			context.getDocumentRepo().getPathById(doc.getId()), 
			doc.getExtension(), 
			null, 
			doc.getSize(), 
			doc.getCreatedAt(), 
			doc.getUpdatedAt(), 
			doc.getCreatedBy().getEmail(), 
			null);
		
		var files = new ArrayList<FileProps>() {{
			add(fileProps);
		}};

		Map<String, Object> item = Map.of(
			"name", fileProps.name,
			"path", fileProps.path,
			"creator", fileProps.creator,
			"files", files
		);
		
		var scriptContext = collectionService.configScriptContext(null, null);
		scriptContext.put("src", item);
		
		try
		{
			var parent = DocumentUtil.toInterno(doc.getParent());
			if(parent == null)
			{
				return;
			}

			var integrations = parent.getIntegrations();
			if(integrations.isEmpty())
			{
				return;
			}

			for(var integ : integrations)
			{
				try
				{
					scriptContext.put("src", item);
					
					var integration = integ.getIntegration();
					for(var act : integration.getActivities())
					{
						var activity = act.getActivity();
						var activityService = (IActivityService)context.getActivityServiceFactory().build(
							activity.getSchema().getName(),
							activity);

						var start = ManagementFactory.getThreadMXBean().getThreadCpuTime(Thread.currentThread().getId());				
			
						activityService.run(
							integration.getCollection(),
							item,
							scriptContext);

						var total = ManagementFactory.getThreadMXBean().getThreadCpuTime(Thread.currentThread().getId()) - start;

						context.getIntegrationLogger().info(
							integration, 
							"Activity %s(%s)::run() took %d ns", 
							activity.getPubId(),
							activity.getSchema().getName(),
							total);
					}
				}
				catch (Exception e) 
				{
					context.getDocLogger().error(
						doc, String.format("Execução de integração falhou para document %s", doc.getName()), e);
				}
			}
		}
		catch (Exception e) 
		{
			context.getDocLogger().error(
				doc, String.format("Execução de workflow falhou para document %s", doc.getName()), e);
		}
	}
}
