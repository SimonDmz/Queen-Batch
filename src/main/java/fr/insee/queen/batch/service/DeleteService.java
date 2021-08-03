package fr.insee.queen.batch.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import fr.insee.queen.batch.dao.CampaignDao;
import fr.insee.queen.batch.dao.CommentDao;
import fr.insee.queen.batch.dao.DataDao;
import fr.insee.queen.batch.dao.NomenclatureDao;
import fr.insee.queen.batch.dao.PersonalizationDao;
import fr.insee.queen.batch.dao.QuestionnaireModelDao;
import fr.insee.queen.batch.dao.RequiredNomenclatureDao;
import fr.insee.queen.batch.dao.StateDataDao;
import fr.insee.queen.batch.dao.SurveyUnitDao;
import fr.insee.queen.batch.enums.BatchErrorCode;
import fr.insee.queen.batch.enums.BatchOption;
import fr.insee.queen.batch.exception.BatchException;
import fr.insee.queen.batch.exception.DataBaseException;
import fr.insee.queen.batch.object.Nomenclature;
import fr.insee.queen.batch.object.Sample;
import fr.insee.queen.batch.object.SurveyUnit;
import fr.insee.queen.batch.utils.XmlUtils;

/**
 * Delete Service : this service contains all functions used to delete datas
 * 
 * @author Claudel Benjamin
 * 
 */
@Service
public class DeleteService {

	private static final Logger logger = LogManager.getLogger(DeleteService.class);
	
	@Autowired
	SurveyUnitDao surveyUnitDao;
	@Autowired
	DataDao dataDao;
	@Autowired
	CommentDao commentDao;
	@Autowired
	CampaignDao campaignDao;
	@Autowired
	NomenclatureDao nomenclatureDao;
	@Autowired
	QuestionnaireModelDao questionnaireModelDao;
	@Autowired
	PersonalizationDao personalizationDao;
	@Autowired
	StateDataDao stateDataDao;
	RequiredNomenclatureDao requiredNomenclatureDao;
	@Autowired
	DatabaseService databaseService;
	
	@Autowired
	ExtractionService extractionService;
	
	@Autowired
	XmlUtils xmlUtils;
	
	@Autowired(required=false)
	@Qualifier("connection")
	Connection connection;
	
	@Autowired
	AnnotationConfigApplicationContext context;
	
	@Autowired
	Environment env;

	public BatchErrorCode deleteSample(String in, String out, BatchErrorCode returnCode) throws SQLException, BatchException, DataBaseException {
		List<String> lstSu = null;
		Sample sample = null;
		try {
			sample = xmlUtils.createSample(in);
		} catch (Exception e) {
			throw new BatchException(e.getMessage());
		}
		if(!campaignDao.exist(sample.getCampaign().getId())) {
			throw new BatchException(String.format("Campaign %s does not exist in database", sample.getCampaign().getId()));			
		}
		if(databaseService.isJpaDatabase()) {
			connection.setAutoCommit(false);
		}
		if(sample.getSurveyUnits() ==null || sample.getSurveyUnits().isEmpty()) {
			try {
				// Delete Campaign
				extractionService.extractCampaign(BatchOption.EXTRACTDATACOMPLETE, sample.getCampaign(), List.of(), out);
				lstSu = surveyUnitDao.getAllSurveyUnitByCamapignId(sample.getCampaign().getId());
				dataDao.deleteDataByCampaignId(sample.getCampaign().getId());
				commentDao.deleteCommentByCampaignId(sample.getCampaign().getId());
				personalizationDao.deleteByCampaignId(sample.getCampaign().getId());
				questionnaireModelDao.deleteCampaignIdForQuestionnaireModel(sample.getCampaign().getId());
				stateDataDao.deleteStateDataByCampaignId(sample.getCampaign().getId());
				surveyUnitDao.deleteMetaDataByCampaignId(sample.getCampaign().getId());
				surveyUnitDao.deleteSurveyUnitByCampaignId(sample.getCampaign().getId());
				campaignDao.delete(sample.getCampaign().getId());
				logger.log(Level.WARN, "Following survey-units deleted successfully : [{}]", String.join(",", lstSu));
				logger.log(Level.WARN, "Campaign {} deleted successfully", sample.getCampaign().getId());
			} catch (Exception e) {
				if(databaseService.isJpaDatabase()) {
					connection.rollback();
					connection.setAutoCommit(true);
				}
				throw new DataBaseException("Error during delete campaign in DB ... Rollback : " + e.getMessage());
			} finally {
				if(databaseService.isJpaDatabase()) {
					connection.setAutoCommit(true);
				}
			}
		}else {
			// Delete SU
			try {
				lstSu = sample.getSurveyUnits().stream().map(SurveyUnit::getId).collect(Collectors.toList());
				List<String> unexistingSu = surveyUnitDao.findUnexistingSurveyUnitsInList(lstSu);
				lstSu.removeAll(unexistingSu);
				if(!lstSu.isEmpty()) {
					extractionService.extractCampaign(BatchOption.EXTRACTDATA, sample.getCampaign(), lstSu, out);
					dataDao.deleteDataBySurveyUnitIds(lstSu);
					commentDao.deleteCommentBySurveyUnitIds(lstSu);
					personalizationDao.deleteBySurveyUnitIds(lstSu);
					stateDataDao.deleteStateDataBySU(lstSu);
					surveyUnitDao.deleteMetaDataByCampaignId(sample.getCampaign().getId());
					surveyUnitDao.deleteSurveyUnits(lstSu);
				}
				logger.log(Level.WARN, "Following survey-units deleted successfully : [{}]", String.join(",", lstSu));
				if(!unexistingSu.isEmpty()) {
					logger.log(Level.WARN, "Following survey-units can not be deleted because they does not exist in database : [{}]", String.join(",", unexistingSu));
					returnCode = BatchErrorCode.OK_FONCTIONAL_WARNING;
				}
			} catch (Exception e) {
				if(databaseService.isJpaDatabase()) {
					connection.rollback();
					connection.setAutoCommit(true);
				}
				throw new DataBaseException("Error during delete survey-units in DB ... Rollback : " + e.getMessage());
			} finally {
				if(databaseService.isJpaDatabase()) {
					connection.setAutoCommit(true);
				}
			}
			
		}
		return returnCode;
	}

	public BatchErrorCode deleteNomenclature(String pathToNomenclature,
		String pathToJson, BatchErrorCode returnCode) throws BatchException, SQLException, DataBaseException {
		List<Nomenclature> lstNomenclature;
		if(databaseService.isJpaDatabase()) {
			requiredNomenclatureDao = context.getBean(RequiredNomenclatureDao.class);
			connection.setAutoCommit(false);
		}
		try {
			lstNomenclature = XmlUtils.xmlToNomenclature(false, pathToNomenclature, pathToJson);
		
			for(Nomenclature nomenclature : lstNomenclature) {
				if (!nomenclatureDao.exist(nomenclature.getId())) {
					throw new BatchException(String.format("Nomenclature %s does not exist in database", nomenclature.getId()));				}
				if (nomenclatureDao.isUsed(nomenclature.getId())) {
					throw new BatchException(String.format("Nomenclature %s is still associated to a questionnaire", nomenclature.getId()));
				} 
				nomenclatureDao.delete(nomenclature);
				logger.log(Level.INFO, "Nomenclature {} succesfully deleted", nomenclature.getId());
			}
		} catch (Exception e) {
			if(databaseService.isJpaDatabase()) {
				connection.rollback();
				connection.setAutoCommit(true);
			}
			throw new DataBaseException("Error during delete nomenclatures in DB ... Rollback : " + e.getMessage());
		} finally {
			if(databaseService.isJpaDatabase()) {
				connection.setAutoCommit(true);
			}
		}
		return returnCode;
	}
}
