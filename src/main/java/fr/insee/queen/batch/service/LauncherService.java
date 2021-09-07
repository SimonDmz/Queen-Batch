package fr.insee.queen.batch.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.insee.queen.batch.Constants;
import fr.insee.queen.batch.Model;
import fr.insee.queen.batch.dao.NomenclatureDao;
import fr.insee.queen.batch.dao.QuestionnaireModelDao;
import fr.insee.queen.batch.enums.BatchErrorCode;
import fr.insee.queen.batch.enums.BatchOption;
import fr.insee.queen.batch.exception.BatchException;
import fr.insee.queen.batch.exception.DataBaseException;
import fr.insee.queen.batch.exception.ValidateException;
import fr.insee.queen.batch.object.Nomenclature;
import fr.insee.queen.batch.utils.PathUtils;
import fr.insee.queen.batch.utils.XmlUtils;

/**
 * Launcher Service : this service contains all steps of Batch : - Load
 * Nomenclature - Validate Sample XML - Load Sample XML - Clean & reset contents
 * 
 * @author Claudel Benjamin
 * 
 */
@Service
public class LauncherService {

	private static final Logger logger = LogManager.getLogger(LauncherService.class);
	
	@Autowired
	AnnotationConfigApplicationContext context;
	
	static FolderService folderService;
	
	private String fileName;

	public BatchErrorCode validateTreatCleanBatch(BatchOption batchOption, String in, String out) throws ValidateException, BatchException, IOException, XMLStreamException, SQLException, DataBaseException, ParserConfigurationException, SAXException {
		BatchErrorCode returnCode= BatchErrorCode.OK;
		returnCode = validateTreatClean(batchOption,Model.NOMENCLATURE, in, out, returnCode);
		returnCode = validateTreatClean(batchOption, Model.SAMPLE, in, out, returnCode);
		return returnCode;
	}
	
	public BatchErrorCode validateTreatClean(BatchOption batchOption, Model model, String in, String out, BatchErrorCode returnCode) throws ValidateException, BatchException, IOException, XMLStreamException, SQLException, DataBaseException, ParserConfigurationException, SAXException {
		if (PathUtils.isDirContainsFileExtension(Path.of(in + "/"+model.getLabel()), model.getLabel()+".xml")) {
			// Validate
			switch(model) {
				case NOMENCLATURE:
					XmlUtils.validateXMLSchema(Constants.MODEL_NOMENCLATURE, in + "/"+model.getLabel()+"/"+model.getLabel()+".xml");
					break;
				case SAMPLE:
					// Moving the file to in/processing
					folderService = context.getBean(FolderService.class);
					folderService.setFilename(model, in + "/sample/sample.xml");
					fileName = folderService.getFilename();
					PathUtils.moveFile(in + "/sample/sample.xml", in + "/processing/" + fileName);
					// Validation of the xml schema
					XmlUtils.validateXMLSchema(Constants.MODEL_SAMPLE, in + "/processing/" + fileName);
					// Check if the questionnaire exist in database
					validQuestionnaireModel(context, in + "/processing/" + fileName);
					break;
				default:
					throw new ValidateException("Unknown Model");
			}
			switch(model) {
				case NOMENCLATURE:
					returnCode = treatNomenclature(batchOption, in+"/"+model.getLabel()+"/"+model.getLabel()+".xml", in+"/"+model.getLabel()+"/json/", returnCode);
					break;
				case SAMPLE:	
					returnCode = treatSample(batchOption, in + "/processing/" +fileName, out+"/", returnCode);
					break;
				default:
					throw new ValidateException(Constants.MSG_UNKNOWN_MODEL);
			}
			//Clean & reset
			try {
				cleanAndReset(model, in, out, returnCode);
			} catch (IOException e) {
				logger.log(Level.ERROR, "Error during process, error files have been created");
			}
		}else {
			logger.log(Level.INFO, "No {} file to treat in '{}/{}'", model, in, model);
		}
		return returnCode;
	}
	
	private BatchErrorCode treatSample(BatchOption batchOption, String in, String out, BatchErrorCode returnCode) throws BatchException, SQLException, DataBaseException {
		switch(batchOption) {
			case LOADDATA :
				LoadService loadService = context.getBean(LoadService.class);
				returnCode = loadService.loadSample(in, out, returnCode);
				break;
			case DELETEDATA : 
				DeleteService deleteService = context.getBean(DeleteService.class);
				returnCode = deleteService.deleteSample(in, out, returnCode);
				break;
			default : 
				break;
		}
		return returnCode;
	}

	private BatchErrorCode treatNomenclature(BatchOption batchOption, String in, String out, BatchErrorCode returnCode) throws BatchException, SQLException, DataBaseException {
		switch(batchOption) {
		case LOADDATA :
			LoadService loadService = context.getBean(LoadService.class);
			returnCode = loadService.loadNomenclature(in, out, returnCode);
			break;
		case DELETEDATA : 
			DeleteService deleteService = context.getBean(DeleteService.class);
			returnCode = deleteService.deleteNomenclature(in, out, returnCode);
			break;
		default : 
			break;
	}
	return returnCode;
	}

	public boolean validRequiredNomenclature(AnnotationConfigApplicationContext context, String fileName) throws ValidateException, IOException {
		NomenclatureDao nomenclatureDao = context.getBean(NomenclatureDao.class);
		NodeList lstNodeNomenclature = XmlUtils.getXmlNodeFile(fileName, "NomenclatureId");
		Nomenclature nomenclature = null;
		if (lstNodeNomenclature != null) {
			for (int itr = 0; itr < lstNodeNomenclature.getLength(); itr++) {
				Node nodeQuestionnaireModel = lstNodeNomenclature.item(itr);
				if (nodeQuestionnaireModel.getNodeType() == Node.ELEMENT_NODE) {
					Element elt = (Element) nodeQuestionnaireModel;
					try {
						nomenclature = nomenclatureDao.findById(elt.getTextContent());
					} catch (Exception e) {
						throw new ValidateException(e.getMessage());
					}
					if(nomenclature==null) {
						throw new ValidateException(String.format("Nomenclature %s does not exist in database", elt.getTextContent()));
					}
				}
			}
		}
		return false;
	}
	
	public boolean validQuestionnaireModel(AnnotationConfigApplicationContext context, String fileName) throws ValidateException, IOException {
		QuestionnaireModelDao questionaireModelDao = context.getBean(QuestionnaireModelDao.class);
		NodeList lstQuestionnaireModel = XmlUtils.getXmlNodeFile(fileName, "QuestionnaireModelId");
		if (lstQuestionnaireModel != null) {
			for (int itr = 0; itr < lstQuestionnaireModel.getLength(); itr++) {
				Node nodeQuestionnaireModel = lstQuestionnaireModel.item(itr);
				if (nodeQuestionnaireModel.getNodeType() == Node.ELEMENT_NODE) {
					Element elt = (Element) nodeQuestionnaireModel;
					if(!questionaireModelDao.exist(elt.getTextContent())) {
						throw new ValidateException(String.format("Questionnaire %s does not exist in database", elt.getTextContent()));
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * This method clean and reset the batch by deleting the file nomenclaure.xml in the folder IN.
	 * It also creates an error file in the OUT folder if an error has occured durring the steps 
	 * of validation or loading for the nomenclature. Otherwise if there is no error, it creates 
	 * a ".done" file from the nomenclature.xml file in the OUT folder.
	 * @param model
	 * @param in
	 * @param out
	 * @param returnCode
	 * @throws IOException
	 * @throws ValidateException 
	 */
	public BatchErrorCode cleanAndReset(Model model, String in, 
			String out, BatchErrorCode returnCode)
			throws IOException, ValidateException {
			String filename = "";
			switch(returnCode) {
			case KO_TECHNICAL_ERROR: 
			case KO_FONCTIONAL_ERROR: 
				if(this.fileName != null) {
					filename = model.getLabel() + "." + folderService.getCampaignName() + "." + PathUtils.getTimestampForPath() + ".error.xml";
				} else {
					filename = model.getLabel() + PathUtils.getTimestampForPath() + ".error.xml";
				}
				break;
			case OK_TECHNICAL_WARNING:
			case OK_FONCTIONAL_WARNING:
				if(this.fileName != null) {
					filename = model.getLabel() +  "." + folderService.getCampaignName() + "." + PathUtils.getTimestampForPath() + ".warning.xml";
				} else {
					filename = model.getLabel() + PathUtils.getTimestampForPath() + ".warning.xml";
				}
				break;
			case OK:
				if(this.fileName != null) {
					filename = model.getLabel() +  "." + folderService.getCampaignName() + "." + PathUtils.getTimestampForPath() + ".done.xml";
				} else {
					filename = model.getLabel() + PathUtils.getTimestampForPath() + ".done.xml";
				}
				break;
			default:
				throw new ValidateException("Unknown return code");
			}
			File file = null;
			if(model.getLabel().equals(Model.SAMPLE.getLabel()) && this.fileName != null){
				file = new File(in + "/processing/" + this.fileName);
			} else {
				file = new File(in);
			}
			
			if(file.exists()) {
				Path temp = null;
				if(model.getLabel().equals(Model.SAMPLE.getLabel()) && PathUtils.isFileExist(in + "/processing/" + this.fileName)) {
					temp = Files.move(Paths.get(in+ "/processing/" + this.fileName), Paths.get(out + "/"+model.getLabel()+"/" + filename));
				} else if(PathUtils.isFileExist(in+ "/"+model.getLabel()+"/"+model.getLabel()+".xml")){
					temp = Files.move(Paths.get(in+ "/"+model.getLabel()+"/"+model.getLabel()+".xml"), Paths.get(out + "/"+model.getLabel()+"/" + filename));
				}
				if (temp != null) {
					logger.log(Level.INFO, Constants.MSG_FILE_MOVE_SUCCESS, filename);
				} else {
					logger.log(Level.WARN,Constants.MSG_FAILED_MOVE_FILE, filename);
					if(returnCode != BatchErrorCode.KO_FONCTIONAL_ERROR) {
						returnCode = BatchErrorCode.KO_TECHNICAL_ERROR;
					}
				}
			} else {
				logger.log(Level.WARN, Constants.MSG_FAILED_MOVE_FILE, filename);
				if(returnCode != BatchErrorCode.KO_FONCTIONAL_ERROR) {
					returnCode = BatchErrorCode.KO_TECHNICAL_ERROR;
				}
			}
			if(model==Model.NOMENCLATURE) {
				returnCode=cleanAndResetJson(in+"/nomenclatures/json", out+"/nomenclatures/json/", returnCode);
			}
		return returnCode;
	}
	

	private static BatchErrorCode cleanAndResetJson(String inJson, String outJson, BatchErrorCode returnCode) throws IOException, ValidateException {
		List<String> jsonFileNames = PathUtils.getListFileName(Path.of(inJson));
		for(String jsonFileName : jsonFileNames) {
			File json = new File(inJson + "/" + jsonFileName);
			String jsonOutFileName = "";
			
			switch(returnCode) {
			case KO_TECHNICAL_ERROR: 
			case KO_FONCTIONAL_ERROR: 
				jsonOutFileName = PathUtils.getFileNameWithoutExtension(jsonFileName) + "." + PathUtils.getTimestampForPath() + ".error.xml";
				break;
			case OK_TECHNICAL_WARNING:
			case OK_FONCTIONAL_WARNING:
				jsonOutFileName = PathUtils.getFileNameWithoutExtension(jsonFileName) + "." + PathUtils.getTimestampForPath() + ".warning.xml";
				break;
			case OK:
				jsonOutFileName = PathUtils.getFileNameWithoutExtension(jsonFileName) + "." + PathUtils.getTimestampForPath() + ".done.xml";
				break;
			default:
				throw new ValidateException("Unknown return code");
			}

			if(json.exists()) {
				Path tempJson = Files.move(Paths.get(inJson + "/" + jsonFileName),
				Paths.get(outJson + jsonOutFileName));
				if (tempJson != null) {
					logger.log(Level.INFO, Constants.MSG_FILE_MOVE_SUCCESS, jsonFileName);
				} else {
					logger.log(Level.WARN, Constants.MSG_FAILED_MOVE_FILE, jsonFileName);
					if(returnCode != BatchErrorCode.KO_FONCTIONAL_ERROR) {
						logger.log(Level.WARN, Constants.MSG_FAILED_MOVE_FILE, jsonFileName);
						returnCode = BatchErrorCode.KO_TECHNICAL_ERROR;
					}
				}
			}
		}
		return returnCode;
	}
}
