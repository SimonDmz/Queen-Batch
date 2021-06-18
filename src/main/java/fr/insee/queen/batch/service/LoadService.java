package fr.insee.queen.batch.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fr.insee.queen.batch.dao.CampaignDao;
import fr.insee.queen.batch.dao.CommentDao;
import fr.insee.queen.batch.dao.DataDao;
import fr.insee.queen.batch.dao.NomenclatureDao;
import fr.insee.queen.batch.dao.PersonalizationDao;
import fr.insee.queen.batch.dao.QuestionnaireModelDao;
import fr.insee.queen.batch.dao.RequiredNomenclatureDao;
import fr.insee.queen.batch.dao.SurveyUnitDao;
import fr.insee.queen.batch.enums.BatchErrorCode;
import fr.insee.queen.batch.exception.BatchException;
import fr.insee.queen.batch.exception.DataBaseException;
import fr.insee.queen.batch.exception.ValidateException;
import fr.insee.queen.batch.object.Nomenclature;
import fr.insee.queen.batch.object.QuestionnaireModel;
import fr.insee.queen.batch.object.Sample;
import fr.insee.queen.batch.object.SurveyUnit;
import fr.insee.queen.batch.utils.PathUtils;
import fr.insee.queen.batch.utils.XmlUtils;

/**
 * Load Service : this service contains all functions used to load datas
 * 
 * @author Claudel Benjamin
 * 
 */
@Service
public class LoadService {

	private static final Logger logger = LogManager.getLogger(LoadService.class);
	
	QuestionnaireModelDao questionnaireModelDao;
	CampaignDao campaignDao;
	SurveyUnitDao surveyUnitDao;
	DataDao dataDao;
	CommentDao commentDao;
	RequiredNomenclatureDao requiredNomenclatureDao;
	PersonalizationDao personalizationDao;
	
	@Autowired(required=false)
	Connection connection;
	
	@Autowired
	Environment env;
	
	@Autowired
	AnnotationConfigApplicationContext context;
	
	@Autowired
	XmlUtils xmlUtils;
	
	@Autowired
	DatabaseService databaseService;
	
	/**
	 * This method Load the Nomenctlature.xml file in folder IN, validate it content
	 * and insert in database if needed All error are logged and are write in a log
	 * File
	 * 
	 * @param context the context of batch
	 * @throws SQLException 
	 * @throws DataBaseException 
	 * @throws ValidateException 
	 * @throws IOException 
	 * @throws DOMException 
	 * @throws Exception 
	 */
	public BatchErrorCode loadNomenclature(String pathToNomenclature,
			String pathToJson, BatchErrorCode returnCode) throws BatchException, SQLException, DataBaseException {
		if(databaseService.isJpaDatabase()) {
			connection.setAutoCommit(false);
		}
		NomenclatureDao nomenclatureDao = context.getBean(NomenclatureDao.class);
		List<Nomenclature> lstNomenclature;
		try {
			lstNomenclature = XmlUtils.xmlToNomenclature(true, pathToNomenclature, pathToJson);
		} catch (Exception e) {
			throw new BatchException(e.getMessage());
		}
		try {
			for(Nomenclature nomenclature : lstNomenclature) {
				if (nomenclatureDao.exist(nomenclature.getId())) {
					nomenclatureDao.update(nomenclature);
					logger.log(Level.INFO, "Nomenclature {} succesfully updated in database", nomenclature.getId());
				} else {
					nomenclatureDao.create(nomenclature);
					logger.log(Level.INFO, "Nomenclature {} succesfully created in database", nomenclature.getId());
				}
			}
		} catch (Exception e) {
			if(databaseService.isJpaDatabase()) {
				connection.rollback();
				connection.setAutoCommit(true);
			}
			throw new DataBaseException("Error during create or update nomenclature in DB ... Rollback : " + e.getMessage());
		} finally {
			if(databaseService.isJpaDatabase()) {
				connection.setAutoCommit(true);
			}
		}
		if (returnCode == BatchErrorCode.OK) {
			logger.info("Success to load nomenclatures.xml");
		}
		return returnCode;
	}

	/**
	 * This method Load the Sample.xml file in folder IN, validate it content and
	 * insert in database if needed All error are logged and are write in a log File
	 * 
	 * @param context the context of batch
	 * @throws BatchException
	 * @throws SQLException 
	 * @throws DataBaseException 
	 */
	public BatchErrorCode loadSample(String pathSampleIn, String pathSampleOut, BatchErrorCode returnCode) throws BatchException, SQLException {
		if(databaseService.isJpaDatabase()) {
			connection.setAutoCommit(false);
		}
		try {
			questionnaireModelDao = context.getBean(QuestionnaireModelDao.class);
			campaignDao = context.getBean(CampaignDao.class);
			surveyUnitDao = context.getBean(SurveyUnitDao.class);
			dataDao = context.getBean(DataDao.class);
			commentDao = context.getBean(CommentDao.class);
			personalizationDao = context.getBean(PersonalizationDao.class);
			if(databaseService.isJpaDatabase())
				requiredNomenclatureDao = context.getBean(RequiredNomenclatureDao.class);
			// Retrieve complete xml Objects
			Sample sample = xmlUtils.createSample(pathSampleIn);
			returnCode = createOrUpdateCampaign(sample, pathSampleOut, returnCode);
		} catch (Exception e) {
			throw new BatchException(e.getMessage());
		}
		if (returnCode == BatchErrorCode.OK) {
			logger.info("Success to load sample.xml");
		}
		return returnCode;
	}

	private BatchErrorCode createOrUpdateCampaign(Sample sample, String pathSampleOut, BatchErrorCode returnCode) throws DataBaseException, SQLException {
		try {
			// Forcing uppercase
			sample.getCampaign().setId(sample.getCampaign().getId().toUpperCase());
			
			if(databaseService.isJpaDatabase()) {
				connection.setAutoCommit(false);
			} 
			if (campaignDao.exist(sample.getCampaign().getId())) {
				logger.log(Level.INFO, "Campaign {} already exist in database", sample.getCampaign().getId());
				returnCode = BatchErrorCode.OK_FONCTIONAL_WARNING;
			}else {
				// Create Campaign
				campaignDao.create(sample.getCampaign());
			}
			// Create associations btwn campaign and qm
			for(QuestionnaireModel qm : sample.getCampaign().getQuestionnaireModels()) {
				questionnaireModelDao.updateCampaignId(qm);
			}
			returnCode = createOrUpdateSurveyUnit(sample, pathSampleOut+"sample", returnCode);
		} catch (Exception e) {
			if(databaseService.isJpaDatabase()) {
				connection.rollback();
				connection.setAutoCommit(true);
			}
			throw new DataBaseException("Error during create or update campaign in DB ... Rollback : " + e.getMessage());
		} finally {
			if(databaseService.isJpaDatabase()) {
				connection.setAutoCommit(true);
			}
		}
		return returnCode;
	}

	private BatchErrorCode createOrUpdateSurveyUnit(Sample sample, String pathSampleOut, BatchErrorCode returnCode) throws IOException, ParserConfigurationException, SAXException, 
	SQLException, XPathExpressionException, TransformerFactoryConfigurationError, TransformerException, ParseException {
		// Duplicate sample xml file
		String sampleErrorList = pathSampleOut + "/sample." + sample.getCampaign().getId() + PathUtils.getTimestampForPath() + ".error.list.xml";
		FileUtils.copyFile(new File(sample.getFileName()), new File(sampleErrorList));
		File file = new File(sampleErrorList);
		InputStream inputStream = new FileInputStream(file);
		Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
		InputSource is = new InputSource(reader);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    DocumentBuilder db = dbf.newDocumentBuilder();
	    Document doc = db.parse(is);
	    StreamResult sr = null;
		int nbSurveyUnitError=0;
		boolean errorList = false;
		List<String> lstSurveyUnitError = new ArrayList<>();
		List<String> lstSurveyUnitUpdated = new ArrayList<>();
		List<String> lstSurveyUnitSuccess =new ArrayList<>();
		// For each survey Unit
		for (SurveyUnit surveyUnit : sample.getSurveyUnits()) {
			try {
				if (!surveyUnitDao.existSurveyUnit(surveyUnit.getId())) {
					logger.log(Level.WARN, "Create Survey Unit {}", surveyUnit.getId());
					// Create Survey Unit
					surveyUnitDao.createSurveyUnit(surveyUnit);
					// Create Data
					dataDao.createData(surveyUnit);
					if(surveyUnit.getPersonalization() != null) {
						// Create Personalization
						personalizationDao.createPersonalization(surveyUnit);
					}
					// Create Empty Comment
					commentDao.createComment(surveyUnit);
					// Remove SU from file error.list
					sr = XmlUtils.removeSurveyUnitNode(doc, surveyUnit.getId());
					lstSurveyUnitSuccess.add(surveyUnit.getId());
				} else {
					//if the SU already exist we update
					logger.log(Level.WARN, "Update Survey Unit {}", surveyUnit.getId());
					surveyUnitDao.updateSurveyUnit(surveyUnit);
					dataDao.updateData(surveyUnit);
					if(surveyUnit.getPersonalization() != null) {
						personalizationDao.updatePersonalization(surveyUnit);
					}
					lstSurveyUnitUpdated.add(surveyUnit.getId());
				}
			} catch (Exception e) {
				nbSurveyUnitError++;
				errorList = true;
				lstSurveyUnitError.add(surveyUnit.getId());
			}
			
		}
		logger.log(Level.WARN, "Total survey-units treated : {}", sample.getSurveyUnits().size());
		logger.log(Level.WARN, "There is {} survey-units with error : [{}]", lstSurveyUnitError.size(), StringUtils.join(lstSurveyUnitError, ","));
		logger.log(Level.WARN, "There is {} survey-units updated : [{}]", lstSurveyUnitUpdated.size(), StringUtils.join(lstSurveyUnitUpdated, ","));
		logger.log(Level.WARN, "There is {} survey-units with success : [{}]", lstSurveyUnitSuccess.size(), StringUtils.join(lstSurveyUnitSuccess, ","));
		if(errorList) {
			returnCode = BatchErrorCode.OK_FONCTIONAL_WARNING;
			if(nbSurveyUnitError<sample.getSurveyUnits().size()) {
				// At least 1 error on survey units : file must be created
				XmlUtils.updateSampleFileErrorList(sr, sampleErrorList);
			}
		} else {
			// No error in survey units : error file do not have to be created
			FileUtils.forceDelete(new File(sampleErrorList));
		}
		return returnCode;
					
	}
}
