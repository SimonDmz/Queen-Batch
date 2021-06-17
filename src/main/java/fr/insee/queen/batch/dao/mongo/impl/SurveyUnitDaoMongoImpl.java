package fr.insee.queen.batch.dao.mongo.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import fr.insee.queen.batch.config.ConditonMongo;
import fr.insee.queen.batch.dao.CampaignDao;
import fr.insee.queen.batch.dao.SurveyUnitDao;
import fr.insee.queen.batch.object.Metadata;
import fr.insee.queen.batch.object.StateData;
import fr.insee.queen.batch.object.SurveyUnit;

/**
 * Service for the SurveyUnit entity that implements the interface associated
 * @author scorcaud
 *
 */
@Service
@Conditional(value= ConditonMongo.class)
public class SurveyUnitDaoMongoImpl implements SurveyUnitDao {
	
	@Autowired
	MongoTemplate mongoTemplate;
	
	@Autowired
	CampaignDao campaignDao;

	/**
	 * Implements the creation of a SurveyUnit in database
	 * @param surveyUnit
	 * @param idCampaign
	 * @throws SQLException
	 */
	@Override
	public void createSurveyUnit(SurveyUnit surveyUnit) {
	    mongoTemplate.save(surveyUnit, "survey_unit");
	}

	/**
	 * Implementation to check if an SurveyUnit already exist in database 
	 * @param xmlId
	 * @return boolean
	 * @throws SQLException
	 */
	@Override
	public boolean existSurveyUnit(String xmlId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(xmlId));
		return mongoTemplate.findOne(query, SurveyUnit.class, "survey_unit") != null;
	}

	/**
	 * Get all SU for a campaign
	 * @param campaignId
	 * @return
	 */
	@Override
	public List<String> getAllSurveyUnitByCamapignId(String campaignId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("campaign.id").is(campaignId));
		List<SurveyUnit> surveyUnits = mongoTemplate.find(query, SurveyUnit.class, "survey_unit");
		List<String> surveyIds = new ArrayList<>();
		if(!surveyUnits.isEmpty()) {
			for(SurveyUnit su : surveyUnits) {
				surveyIds.add(su.getId());
			}
		}
		return surveyIds;
	}

	/**
	 * Get unexisting SurveyUnits
	 * @param lstSu
	 * @return
	 */
	@Override
	public List<String> findUnexistingSurveyUnitsInList(List<String> lstSu) {
		List<String> unexistingSu = new ArrayList<>();
		lstSu.stream().forEach(su -> {
			Query query = new Query();
			query.addCriteria(Criteria.where("id").is(su));
			SurveyUnit suTemp = mongoTemplate.findOne(query, SurveyUnit.class, "survey_unit");
			if(suTemp == null) {
				unexistingSu.add(su);
			}
		});
		return unexistingSu;
	}

	/**
	 * Delete a list of SU
	 * @param lstSu
	 */
	@Override
	public void deleteSurveyUnits(List<String> lstSu) {
		Query query = new Query();
		query.addCriteria(Criteria.where("id").in(lstSu));
		List<SurveyUnit> suList = mongoTemplate.find(query, SurveyUnit.class, "survey_unit");
		suList.stream().forEach(su -> {
			if(su != null) {
				mongoTemplate.remove(su, "survey_unit");
			}
		});
	}

	/**
	 * Delete all SU for a campaign
	 * @param id
	 */
	@Override
	public void deleteSurveyUnitByCampaignId(String campaignId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("campaign.id").is(campaignId));
		List<SurveyUnit> suList = mongoTemplate.find(query, SurveyUnit.class, "survey_unit");
		suList.stream().forEach(su -> {
			if(su != null) {
				mongoTemplate.remove(su, "survey_unit");
			}
		});
	}

	/**
	 * Update a SurveyUnit
	 * @param surveyUnit
	 */
	@Override
	public void updateSurveyUnit(SurveyUnit surveyUnit) {
		Query query = new Query();
		query.addCriteria(Criteria.where("id").is(surveyUnit.getId()));
		SurveyUnit su = mongoTemplate.findOne(query, SurveyUnit.class, "survey_unit");
		su.setCampaign(surveyUnit.getCampaign());
		su.setComment(surveyUnit.getComment());
		su.setData(surveyUnit.getData());
		su.setPersonalization(surveyUnit.getPersonalization());
		su.setStateData(surveyUnit.getStateData());
		mongoTemplate.save(su, "survey_unit");
	}

	/**
	 * Get Survey unit by his id
	 * @param id
	 * @return
	 */
	@Override
	public String findQuestionnaireIdBySurveyUnitId(String id) {
		Query query = new Query();
		query.fields().include("questionnaireModel.id");
		query.addCriteria(Criteria.where("id").is(id));
		return mongoTemplate.findOne(query, SurveyUnit.class, "survey_unit").getQuestionnaireModel().getId();
	}

	/**
	 * Delete meta data by campaign id
	 * @param id
	 */
	@Override
	public void deleteMetaDataByCampaignId(String id) {
		Query query = new Query();
		query.addCriteria(Criteria.where("campaign.id").is(id));
		List<Metadata> mdList = mongoTemplate.find(query, Metadata.class, "metadata");
		mdList.stream().forEach(md -> {
			if(md != null) {
				mongoTemplate.remove(md, "metadata");
			}
		});
	}

	/**
	 * Get ids for survey unit with state "validated"
	 * @return
	 */
	@Override
	public List<String> findSurveyUnitsValidatedIdsByCampaignId(String campaignId) {
		List<String> surveysIdsAll = getAllSurveyUnitByCamapignId(campaignId);
		List<String> surveysToRemove = new ArrayList<>();
		if(!surveysIdsAll.isEmpty()) {
			surveysIdsAll.stream().forEach(su -> {
				Query query = new Query();
				query.addCriteria(Criteria.where("surveyUnit.id").is(su));
				query.addCriteria(Criteria.where("state").is("VALIDATED"));
				StateData stateTemp = mongoTemplate.findOne(query, StateData.class, "state_data");
				if(stateTemp == null) {
					surveysToRemove.add(su);
				}
			});
			if(!surveysToRemove.isEmpty()) {
				surveysIdsAll.removeAll(surveysToRemove);
			}
		}
		return surveysIdsAll;
	}
}

