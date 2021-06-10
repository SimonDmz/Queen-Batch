package fr.insee.queen.batch;

import java.io.IOException;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.xml.sax.SAXException;

import fr.insee.queen.batch.config.ApplicationContext;
import fr.insee.queen.batch.enums.BatchErrorCode;
import fr.insee.queen.batch.enums.BatchOption;
import fr.insee.queen.batch.exception.ArgumentException;
import fr.insee.queen.batch.exception.BatchException;
import fr.insee.queen.batch.exception.DataBaseException;
import fr.insee.queen.batch.exception.FolderException;
import fr.insee.queen.batch.exception.ValidateException;
import fr.insee.queen.batch.service.DatabaseService;
import fr.insee.queen.batch.service.ExtractionService;
import fr.insee.queen.batch.service.FolderService;
import fr.insee.queen.batch.service.LauncherService;
import fr.insee.queen.batch.utils.PathUtils;
import fr.insee.queen.batch.utils.XmlUtils;

/**
 * Launcher : Queen Batch main class. 4 steps : 
 * - Load Nomenclature 
 * - Validate Sample XML 
 * - Load Sample XML 
 * - Clean & reset contents
 * 
 * @author Claudel Benjamin
 * 
 */
public abstract class Launcher {
	/**
	* The folder in use to insert datas 
	*/
	public static String FOLDER_IN;
	/**
	* The folder out use to store logs and file treated 
	*/
	public static String FOLDER_OUT;
	
	static AnnotationConfigApplicationContext context;

	static DatabaseService databaseService;
	static FolderService folderService;
	static LauncherService launcherService;
	static ExtractionService extractionService;
	static XmlUtils xmlUtils;
	
	
	private static final Logger logger = LogManager.getLogger(Launcher.class);
	
	public static void main(String[] args) throws IOException, ValidateException, XMLStreamException, ParserConfigurationException, SAXException {
		context = new AnnotationConfigApplicationContext(ApplicationContext.class);
		launcherService = context.getBean(LauncherService.class);
		folderService = context.getBean(FolderService.class);
		databaseService = context.getBean(DatabaseService.class);
		extractionService = context.getBean(ExtractionService.class);
		xmlUtils = context.getBean(XmlUtils.class);
		BatchErrorCode batchErrorCode = BatchErrorCode.OK;
		try{
			initBatch();
			checkFolderTree();
			batchErrorCode = runBatch(args);
			logger.log(Level.INFO, Constants.MSG_RETURN_CODE, batchErrorCode);
		}catch (ArgumentException|FolderException|ValidateException|DataBaseException|SQLException e) {
			logger.log(Level.ERROR, e.getMessage(), e);
			batchErrorCode = BatchErrorCode.KO_TECHNICAL_ERROR;
			logger.log(Level.ERROR, Constants.MSG_RETURN_CODE, batchErrorCode);
		}catch (BatchException be) {
			logger.log(Level.ERROR,be.getMessage(), be);
			cleanWhenError();
			batchErrorCode = BatchErrorCode.KO_FONCTIONAL_ERROR;
			logger.log(Level.ERROR, Constants.MSG_RETURN_CODE, batchErrorCode);
		} finally {
			context.close();
			System.exit(batchErrorCode.getCode());
		}
	}

	protected static void initBatch() throws FolderException, DataBaseException, SQLException {
		// Check folder properties
		FOLDER_IN = folderService.getFolderIn();
		FOLDER_OUT = folderService.getFolderOut();
		if (StringUtils.isBlank(FOLDER_IN) || "${fr.insee.queen.folder.in}".equals(FOLDER_IN)) {
			throw new FolderException("property fr.insee.queen.batch.folder.in is not define in properties");
		}
		if (StringUtils.isBlank(FOLDER_OUT) || "${fr.insee.queen.folder.out}".equals(FOLDER_OUT)) {
			throw new FolderException("property fr.insee.queen.batch.folder.out is not define in properties");
		}
		logger.log(Level.INFO, "Folder properties are OK");
		
		// Check database
		try {
			databaseService.checkDatabaseAccess();
		} catch (DataBaseException e) {
			throw e;
		}
		logger.log(Level.INFO, "Database is OK");
	}

	private static void checkFolderTree() throws FolderException {
		// Create the folder tree for "in"
		PathUtils.createFolderTreeIn(FOLDER_IN);
		logger.log(Level.INFO, "Folder tree '{}' is OK", FOLDER_IN);
		// Create the folder tree for "out"
		PathUtils.createFolderTreeOut(FOLDER_OUT);
		logger.log(Level.INFO, "Folder tree '{}' is OK", FOLDER_OUT);
	}

	public static BatchErrorCode runBatch(String[] options) throws BatchException, ValidateException, IOException, ArgumentException, XMLStreamException, SQLException, DataBaseException, ParserConfigurationException, SAXException{
		if (options.length == 0) {
			throw new ArgumentException("Batch type is empty, you must choose between [LOADDATA], [DELETEDATA], [EXTRACTDATA] or [EXTRACTDATACOMPLETE]");
		}
		BatchOption batchOption = null;
		try {	
			batchOption = BatchOption.valueOf(options[0].trim());
		}catch(Exception e) {
			throw new ArgumentException("Wrong batch type, you must choose between [LOADDATA], [DELETEDATA], [EXTRACTDATA] or [EXTRACTDATACOMPLETE]");
		}
		switch(batchOption) {
			case EXTRACTDATA :
			case EXTRACTDATACOMPLETE :
				return extractionService.extract(batchOption, FOLDER_OUT);
			case LOADDATA :
			case DELETEDATA :
				return launcherService.validateTreatCleanBatch(batchOption, FOLDER_IN, FOLDER_OUT);
			default :
				throw new ArgumentException("Wrong batch type, you must choose between [LOADDATA], [DELETEDATA], [EXTRACTDATA] or [EXTRACTDATACOMPLETE]");
		}
	}
	
	/**
	 * This method is executed when a BatchException is thrown
	 * @throws IOException
	 * @throws ValidateException 
	 */
	public static void cleanWhenError() throws IOException, ValidateException {
		launcherService.cleanAndReset(Model.NOMENCLATURE, FOLDER_IN, FOLDER_OUT, BatchErrorCode.KO_FONCTIONAL_ERROR);
		launcherService.cleanAndReset(Model.SAMPLE, FOLDER_IN, FOLDER_OUT, BatchErrorCode.KO_FONCTIONAL_ERROR);
	}
}
