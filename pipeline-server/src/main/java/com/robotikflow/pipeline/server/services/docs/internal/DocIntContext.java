package com.robotikflow.pipeline.server.services.docs.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.parser.AutoDetectParser;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.robotikflow.core.factories.ActivityServiceFactory;
import com.robotikflow.core.interfaces.IStorageProviderService;
import com.robotikflow.core.models.repositories.DocumentRepository;
import com.robotikflow.core.services.collections.CollectionService;
import com.robotikflow.core.services.indexing.DocumentIndexService;
import com.robotikflow.core.services.log.CollectionIntegrationLogger;
import com.robotikflow.core.services.log.DocumentLogger;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class DocIntContext 
{
	private static Logger logger = LoggerFactory.getLogger(DocIntContext.class);
	@Autowired
	private IStorageProviderService internalStorageProvider;
	@Autowired
	private DocumentIndexService indexador;
	@Autowired
	private CollectionService collectionService;
	@Autowired
	private DocumentRepository documentRepo;
	@Autowired
	private DocumentLogger docLogger;
	@Autowired
	private CollectionIntegrationLogger integrationLogger;
	@Autowired
	private ActivityServiceFactory activityServiceFactory;
	
	private AutoDetectParser parser;
	private OfficeManager officeManager;
	private OfficeDocumentConverter officeConverter;
	private final ExecutorService threadPool;
	private final DocIntConfig config;
	private final ObjectMapper mapper = new ObjectMapper()
		.findAndRegisterModules();
	private static String sufixoRepoPublico = "-pub";
	private static String tikaConfigXmlFileName = "tika-config.xml";

	@Autowired
	public DocIntContext(Environment env)
	{
		this.config = new DocIntConfig();
		config.extensoesThumbiaveis = new HashSet<String>(Arrays.asList(env.getProperty("docs.thumbable-extensions").split(",")));
		config.extensoesIndexaveis = new HashSet<String>(Arrays.asList(env.getProperty("docs.indexable-extensions").split(",")));
		config.extensoesDoOffice = new HashSet<String>(Arrays.asList(env.getProperty("docs.office-extensions").split(",")));
		config.THUMB_WIDTH = Integer.parseInt(env.getProperty("thumb.width"));
		config.THUMB_HEIGHT = Integer.parseInt(env.getProperty("thumb.height"));
		config.PREVIEW_WIDTH = Integer.parseInt(env.getProperty("preview.width"));
		config.PREVIEW_HEIGHT = Integer.parseInt(env.getProperty("preview.height"));
		
		// configurar o extrator de texto dos documentos
		try
		{
			var tikaConfigXml = ClassLoader.getSystemClassLoader().getResourceAsStream(tikaConfigXmlFileName);
			if(tikaConfigXml == null)
			{
				tikaConfigXml = this.getClass().getClassLoader().getResourceAsStream(tikaConfigXmlFileName);
			}
			var tikaConfig = new TikaConfig(tikaConfigXml);
			parser = new AutoDetectParser(tikaConfig);
		}
		catch (Exception e) 
		{
			logger.error("Erro ao iniciar o Tika", e);
		}
		
		// configurar conversor de Office para PDF
		try
		{
			var configuration = new DefaultOfficeManagerConfiguration();
			configuration.setOfficeHome(env.getProperty("office.home-dir"));
			configuration.setPortNumber(Integer.parseInt(env.getProperty("office.port")));
			
			officeManager = configuration.buildOfficeManager();
			
			officeConverter = new OfficeDocumentConverter(officeManager);
		}
		catch (Exception e) 
		{
			logger.error("Erro ao instanciar o jodconverter", e);
		}
		
		//
		threadPool = Executors.newFixedThreadPool(10);
	}

	public IStorageProviderService getStorage() {
		return internalStorageProvider;
	}

	public DocumentIndexService getIndexador() {
		return indexador;
	}

	public AutoDetectParser getParser() {
		return parser;
	}

	public OfficeManager getOfficeManager() {
		return officeManager;
	}

	public OfficeDocumentConverter getOfficeConverter() {
		return officeConverter;
	}

	public ExecutorService getThreadPool() {
		return threadPool;
	}

	public DocIntConfig getConfig() {
		return config;
	}

	public DocumentRepository getDocumentRepo() {
		return documentRepo;
	}

	public DocumentLogger getDocLogger() {
		return docLogger;
	}

	public CollectionIntegrationLogger getIntegrationLogger() {
		return integrationLogger;
	}

	public static String getSufixoRepoPublico() {
		return sufixoRepoPublico;
	}

	public String getTikaConfigXmlFileName() {
		return tikaConfigXmlFileName;
	}

	public ObjectMapper getMapper() {
		return mapper;
	}

	public CollectionService getCollectionService() {
		return collectionService;
	}

	public ActivityServiceFactory getActivityServiceFactory() {
		return activityServiceFactory;
	}
}
