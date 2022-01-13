package com.robotikflow.pipeline.server.services.docs.external;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import com.robotikflow.core.models.entities.DocumentExt;
import com.robotikflow.core.interfaces.IStorageProviderService;
import com.robotikflow.core.interfaces.props.DocProps;
import com.robotikflow.core.models.queue.DocExtCreatedMessage;
import com.robotikflow.core.util.DocumentUtil;
import com.robotikflow.pipeline.server.services.BaseTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocExtTask 
{
	protected final Logger logger = LoggerFactory.getLogger(BaseTask.class);
	private final DocExtContext context;
	
	public DocExtTask(DocExtContext context)
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

	public void handleOnDocCreated(
		final DocExtCreatedMessage mensagem) 
	{
		var doc = DocumentUtil.toExterno(
			context.getDocumentRepo().findById(mensagem.getId()).get());
		
		List<Consumer<DocExtCreatedMessage>> funcs = List.of(
				(msg) -> enviarParaCloud(doc, msg.getPath())
			);
		
		startThreadsAndWait(funcs, mensagem);

		context.getDocumentRepo().save(doc);
		
		removerTemporario(mensagem);
	}
	
	private void enviarParaCloud(
		final DocumentExt doc, 
		final String caminhoCompleto) 
	{
		try 
		{
			var provider = doc.getProvider();

			var providerService = (IStorageProviderService)context
				.getProviderServiceFactory()	
					.build(provider.getSchema().getName(), provider);

			var file = Paths.get(caminhoCompleto).toFile();

			var parentDoc = doc.getParent() != null? 
				doc.getParent():
				context.getDocumentRepo().findByPubIdAndWorkspace(
					provider.getFields().getString("root"), 
					doc.getWorkspace().getId());
			
			var target = DocumentUtil.toExterno(parentDoc);
			
			var res = providerService.createFile(
				new FileInputStream(file), 
				file.length(), 
				doc.getWorkspace().getPubId(),
				doc.getName(), 
				doc.getExtension(),
				new DocProps(target.getFileId(), target.getFilePath()));

			doc.setFileId(res.getId());
			doc.setFilePath(res.getPath());
			doc.setMimeType(res.getMimeType());
			doc.setUrlDownload(res.getUrlDownload());
		} 
		catch (Exception e) 
		{
			context.getDocLogger().error(
				doc, String.format("Envio para a cloud falhou para o document %s", doc.getName()), e);
		}
	}

	private void removerTemporario(
		final DocExtCreatedMessage msg) 
	{
		Path caminho = null;
		try 
		{
			if(msg.isUpload())
			{
				caminho = Paths.get(msg.getPath()).getParent();
				Files.deleteIfExists(Paths.get(caminho.toString(), "data").normalize());
				Files.deleteIfExists(Paths.get(caminho.toString(), "info").normalize());
			}
			else
			{
				caminho = Paths.get(msg.getPath());
			}
			
			Files.deleteIfExists(caminho.normalize());
		} 
		catch (IOException e) 
		{
			logger.error(String.format("Remoção dos arquivos temporários falhou para %s", caminho != null? caminho.toString(): ""), e);
		}
	}
}
