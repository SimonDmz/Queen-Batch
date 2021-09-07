package fr.insee.queen.batch.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import fr.insee.lunatic.conversion.data.JSONLunaticDataToXML;
import fr.insee.queen.batch.dao.CampaignDao;
import fr.insee.queen.batch.dao.DataDao;
import fr.insee.queen.batch.dao.ParadataEventDao;
import fr.insee.queen.batch.dao.PersonalizationDao;
import fr.insee.queen.batch.dao.StateDataDao;
import fr.insee.queen.batch.dao.SurveyUnitDao;
import fr.insee.queen.batch.enums.BatchErrorCode;
import fr.insee.queen.batch.enums.BatchOption;
import fr.insee.queen.batch.exception.BatchException;
import fr.insee.queen.batch.exception.DataBaseException;
import fr.insee.queen.batch.object.Campaign;
import fr.insee.queen.batch.object.Personalization;
import fr.insee.queen.batch.utils.PathUtils;

/**
 * Extraction Service : this service contains all functions used to extract datas
 * 
 * @author Claudel Benjamin
 * 
 */
@Service
public class ExtractionService {

	@Autowired
	CampaignDao campaignDao;
	@Autowired
	SurveyUnitDao surveyUnitDao;
	@Autowired
	DataDao dataDao;
	@Autowired
	PersonalizationDao personalizationDao;
	@Autowired
	ParadataEventDao paradataEventDao;
	@Autowired
	StateDataDao stateDataDao;
	
	@Autowired
	DatabaseService databaseService;
	
	@Autowired(required=false)
	@Qualifier("connection")
	Connection connection;
	
	BatchOption batchOption;
	File fileData;
	FileWriter fileWriterData;
	
	private static final Logger logger = LogManager.getLogger(ExtractionService.class);

	/**
	 * Main method for extraction
	 * @param batchOption
	 * @param out
	 * @return
	 */
	public BatchErrorCode extract(BatchOption batchOption, String out) {
		BatchErrorCode batchErrorCode = BatchErrorCode.OK;
		this.batchOption = batchOption;
		List<Campaign> lstCampaign;
		lstCampaign = campaignDao.findAll();
		List<String> lstCampaignError = new ArrayList<>();
		List<String> lstCampaignSuccess = new ArrayList<>();
		for (Campaign c : lstCampaign) {
			try {
				extractCampaign(batchOption, c, List.of(), out);
				lstCampaignSuccess.add(c.getId());
			}catch(Exception e) {
				logger.log(Level.WARN, "Error occured durring extraction of campaign {}", c.getId());
				lstCampaignError.add(c.getId());
			}
		}
		logger.log(Level.INFO, "Number of campaigns treated : {}", lstCampaign.size());
		logger.log(Level.INFO, "{} campaigns extracted with success : [{}]", lstCampaignSuccess.size(), StringUtils.join(lstCampaignSuccess, ", "));
		logger.log(Level.INFO, "{} campaigns not extracted : [{}]", lstCampaignError.size(), StringUtils.join(lstCampaignError, ", "));
		if(!lstCampaignError.isEmpty()) {
			if(lstCampaignError.size()==lstCampaign.size()) {
				batchErrorCode=BatchErrorCode.KO_TECHNICAL_ERROR;
			} else {
				batchErrorCode=BatchErrorCode.OK_TECHNICAL_WARNING;
			}
		}
		return batchErrorCode;
	}

	/**
	 * Method use to extract a campaign
	 * @param c
	 * @param lstSu
	 * @param out
	 * @throws BatchException
	 * @throws IOException
	 * @throws SQLException
	 * @throws DataBaseException
	 */
	public void extractCampaign(BatchOption batchOption, Campaign c, List<String> lstSu, String out) throws BatchException, IOException, SQLException, DataBaseException  {
		if(this.batchOption == null)
			this.batchOption = batchOption;
		PathUtils.createFolderTreeExtract(this.batchOption, out, c.getId());
		StringBuilder fileName = new StringBuilder(out)
		.append("/extractdata/")
		.append(c.getId());
		if(this.batchOption.equals(BatchOption.EXTRACTDATA)) {
			lstSu = surveyUnitDao.findSurveyUnitsValidatedIdsByCampaignId(c.getId());
			fileName.append("/differential/data/data.diff.");
		}
		if(this.batchOption.equals(BatchOption.EXTRACTDATACOMPLETE)) {
			lstSu = surveyUnitDao.getAllSurveyUnitByCamapignId(c.getId());
			fileName.append("/complete/data/data.complete.");
		}
		fileName.append(c.getId())
		.append(".")
		.append(PathUtils.getTimestampForPath())
		.append(".xml");
		extractParadata(this.batchOption, c, out, lstSu);
		Document doc = new Document();
		File file = new File(fileName.toString());
		Element campaign = new Element("Campaign");
		try(FileWriter fileWriter = new FileWriter(file);) {
			campaign
					.addContent(new Element("Id").addContent(c.getId()))
					.addContent(new Element("Label").addContent(c.getLabel()))
					.addContent(getSureyUnitsElement(lstSu));
			doc.setRootElement(campaign);
			// Create the XML
			XMLOutputter outter = new XMLOutputter();
			outter.setFormat(Format.getPrettyFormat());
			outter.output(doc, fileWriter);
			logger.log(Level.INFO, "campaign {} extracted succefully in file {}", c.getId(), fileName);
		} catch (Exception e) {
			throw new BatchException(String.format("Error during extract campaign %s", c.getId()));
		}
	}

	/**
	 * This method extract the paradata for a campaign
	 * and a list of SurveyUnit
	 * @param batchOption
	 * @param c
	 * @param out
	 * @param lstSu
	 * @throws IOException
	 * @throws SQLException
	 * @throws DataBaseException
	 */
	@SuppressWarnings("resource")
	public void extractParadata(BatchOption batchOption, Campaign c, String out, List<String> lstSu) throws IOException, SQLException, DataBaseException {
		if(databaseService.isJpaDatabase()) {
			connection.setAutoCommit(false);
		}
		try {
			lstSu.stream().forEach(id -> stateDataDao.updateSurveyUnitStateById(id, "TOEXTRACT"));
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			for(String id : lstSu) {
				StringBuilder fileName = new StringBuilder(out)
				.append("/extractdata/")
				.append(c.getId());
				if(batchOption.equals(BatchOption.EXTRACTDATA)) {
					fileName.append("/differential/paradata/paradata.diff.");
				}
				if(batchOption.equals(BatchOption.EXTRACTDATACOMPLETE)) {
					fileName.append("/complete/paradata/paradata.complete.");
				}
				fileName.append(c.getId())
				.append(".")
				.append(id)
				.append(".json");
				if(batchOption.equals(BatchOption.EXTRACTDATACOMPLETE) && PathUtils.isFileExist(fileName.toString())) {
					new File(fileName.toString()).delete();
				}
				FileWriter fileWriter = new FileWriter(fileName.toString(), true);
				fileWriter.write(gson.toJson(JsonParser.parseString(paradataEventDao.findBySurveyUnitId(id).toJSONString())));
				fileWriter.flush();
				fileWriter.close();
			}
			lstSu.stream().forEach(id -> stateDataDao.updateSurveyUnitStateById(id, "EXTRACTED"));
		} catch (Exception e) {
			if(databaseService.isJpaDatabase()) {
				connection.rollback();
				connection.setAutoCommit(true);
			}
			throw new DataBaseException("Error during update state of SU in DB ... Rollback : " + e.getMessage());
		} finally {
			if(databaseService.isJpaDatabase()) {
				connection.setAutoCommit(true);
			}
		}
	}
	
	/**
	 * Construct the <SurveyUnits> tag
	 * @param c
	 * @param lstSu
	 * @param out
	 * @return
	 * @throws Exception
	 */
	private Element getSureyUnitsElement(List<String> lstSu) throws Exception {
		Element surveyUnits = new Element("SurveyUnits");
		for (String suId : lstSu) {
			String qmId = surveyUnitDao.findQuestionnaireIdBySurveyUnitId(suId);
			surveyUnits.addContent(getSurveyUnitContent(suId, qmId));
		}
		return surveyUnits;

	}

	/**
	 * Construct the <SurveyUnit> tag
	 * @param suId
	 * @param qmId
	 * @param out
	 * @param campaignId
	 * @return
	 * @throws Exception
	 */
	private Element getSurveyUnitContent(String suId, String qmId) throws Exception {
		return new Element("SurveyUnit")
					.addContent(new Element("Id").addContent(suId))
					.addContent(new Element("QuestionnaireModelId").addContent(qmId))
					.addContent(getDataContent(suId))
					.addContent(getPersonalizationContent(suId));
	}

	/**
	 * Construct the <Data> tag
	 * @param suId
	 * @return
	 * @throws Exception
	 */
	private Element getDataContent(String suId) throws Exception {
		JSONLunaticDataToXML jsonLunaticDataToXML = new JSONLunaticDataToXML();
		JSONObject dataJson = dataDao.getDataBySurveyUnitId(suId);
		// create a temporary file
		Path tempFile = Files.createTempFile(null, null);
		try (FileWriter file = new FileWriter(tempFile.toFile())) {
			file.write(dataJson.toJSONString());
		} catch (IOException e) {
			logger.log(Level.ERROR, "Error during extract Data content for Survey-Unit {}", suId);
		}
		File xmlData = jsonLunaticDataToXML.transform(tempFile.toFile());
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(xmlData);
		
		return document.getRootElement().detach();
	}
	
	/**
	 * Construct the <Personalization> tag
	 * @param suId
	 * @return
	 * @throws ParseException
	 */
	@SuppressWarnings("rawtypes")
	private List<Element> getPersonalizationContent(String suId) throws ParseException{
		List<Element> personalizationsElements = new ArrayList<>();
		List<Personalization> personalizations = personalizationDao.findBySurveyUnitId(suId);
		for (Personalization personalization : personalizations) {
			JSONArray jsonArray = personalizationDao.getValueById(personalization.getId());
			if(!jsonArray.isEmpty()) {
				Element personalizationElement = new Element("Personalization");
				for(int i= 0; i<jsonArray.size(); i++) {
					Element variableElement = new Element("Variable");
					JSONObject json = new JSONObject((Map) jsonArray.get(i));
					variableElement.addContent(new Element("Name").addContent(json.get("name").toString()));
					variableElement.addContent(new Element("Value").addContent(json.get("value").toString()));
					personalizationElement.addContent(variableElement);
				}
				personalizationsElements.add(personalizationElement);
			}
		}
		return personalizationsElements;
	}
}
