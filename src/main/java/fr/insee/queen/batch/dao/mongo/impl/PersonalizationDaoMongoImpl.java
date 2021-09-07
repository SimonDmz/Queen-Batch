package fr.insee.queen.batch.dao.mongo.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import fr.insee.queen.batch.config.ConditonMongo;
import fr.insee.queen.batch.dao.PersonalizationDao;
import fr.insee.queen.batch.object.Personalization;
import fr.insee.queen.batch.object.SurveyUnit;

/**
 * Service for the Personalization entity that implements the interface associated
 * @author scorcaud
 *
 */
@Service
@Conditional(value= ConditonMongo.class)
public class PersonalizationDaoMongoImpl implements PersonalizationDao{

	@Autowired
	MongoTemplate mongoTemplate;

	/**
	 * Create a Personalization in database
	 * @param surveyUnit
	 * @throws SQLException
	 */
	@Override
	public void createPersonalization(SurveyUnit surveyUnit) {
		Personalization personalization = new Personalization();
		personalization.setId(surveyUnit.getPersonalization().getId());
		personalization.setValue(surveyUnit.getPersonalization().getValue());
		personalization.setSurveyUnit(surveyUnit);
		mongoTemplate.save(personalization, "personalization");
	}

	/**
	 * Update a personalization by a survey unit
	 * @param surveyUnit
	 * @throws SQLException
	 */
	@Override
	public void updatePersonalization(SurveyUnit surveyUnit) {
		Query query = new Query();
		query.addCriteria(Criteria.where("surveyUnit.id").is(surveyUnit.getId()));
		Personalization personalizationTemp = mongoTemplate.findOne(query, Personalization.class, "personalization");
		if(personalizationTemp != null) {
			personalizationTemp.setValue(surveyUnit.getPersonalization().getValue());
			personalizationTemp.setSurveyUnit(surveyUnit);
			mongoTemplate.save(personalizationTemp, "personalization");
		} else {
			createPersonalization(surveyUnit);
		}
	}

	/**
	 * Retrieves all the personalization for a Survey Unit
	 * @param surveyUnitId
	 * @return
	 */
	@Override
	public void deleteByCampaignId(String campaignId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("campaign.id").is(campaignId));
		List<SurveyUnit> suList = mongoTemplate.find(query, SurveyUnit.class, "survey_unit");
		suList.stream().forEach(su -> {
			if(su != null && su.getPersonalization() != null) {
				mongoTemplate.remove(su.getPersonalization(), "personalization");
			}
		});
	}

	/**
	 * Delete all the personalization for a campaign
	 * @param campaignId
	 */
	@Override
	public void deleteBySurveyUnitIds(List<String> lstSu) {
		Query query = new Query();
		query.addCriteria(Criteria.where("id").in(lstSu));
		List<SurveyUnit> suList = mongoTemplate.find(query, SurveyUnit.class, "survey_unit");
		suList.stream().forEach(su -> {
			if(su != null && su.getPersonalization() != null) {
				mongoTemplate.remove(su.getPersonalization(), "personalization");
			}
		});
	}

	/**
	 * Delete all the personalizations for a list of Survey
	 * @param lstSu
	 */
	@Override
	public List<Personalization> findBySurveyUnitId(String surveyUnitId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("surveyUnit.id").is(surveyUnitId));
		return mongoTemplate.find(query, Personalization.class, "personalization");
	}

	/**
	 * Retrieve the value by the id passed in parameter
	 * @param id
	 * @return JSONObject object
	 * @throws ParseException 
	 */
	@Override
	public JSONArray getValueById(UUID id) throws ParseException {
		Query query = new Query();
		query.fields().include("value");
		query.addCriteria(Criteria.where("id").is(id));
		JSONArray value = mongoTemplate.findOne(query, Personalization.class, "personalization").getValue();
		if(value != null) {
			return value;
		} else {
			return new JSONArray();
		}
	}
}
