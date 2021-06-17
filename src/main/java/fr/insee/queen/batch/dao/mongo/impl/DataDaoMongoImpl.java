package fr.insee.queen.batch.dao.mongo.impl;

import java.sql.SQLException;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import fr.insee.queen.batch.Constants;
import fr.insee.queen.batch.config.ConditonMongo;
import fr.insee.queen.batch.dao.DataDao;
import fr.insee.queen.batch.object.Data;
import fr.insee.queen.batch.object.SurveyUnit;

/**
 * Service for the Data entity that implements the interface associated
 * @author scorcaud
 *
 */
@Service
@Conditional(value= ConditonMongo.class)
public class DataDaoMongoImpl implements DataDao {
	
	@Autowired
	MongoTemplate mongoTemplate;
	
	/**
	 * Implements the creation of data in database
	 * @param surveyUnit
	 * @throws SQLException
	 */
	@Override
	public void createData(SurveyUnit surveyUnit) {
		Data data = new Data();
		data.setId(surveyUnit.getData().getId());
		data.setValue(surveyUnit.getData().getValue());
		data.setSurveyUnit(surveyUnit);
		mongoTemplate.save(data, Constants.DATA);
	}

	/**
	 * Get the all the data for a SU
	 */
	@Override
	public JSONObject getDataBySurveyUnitId(String suId) throws ParseException {
		Query query = new Query();
		query.fields().include("value");
		return mongoTemplate.findOne(query, Data.class, Constants.DATA).getValue();
	}

	/**
	 * Delete all the data for a list of SU
	 */
	@Override
	public void deleteDataBySurveyUnitIds(List<String> lstSu) {
		Query query = new Query();
		query.addCriteria(Criteria.where("id").in(lstSu));
		List<SurveyUnit> suList = mongoTemplate.find(query, SurveyUnit.class, "survey_unit");
		suList.stream().forEach(su -> {
			if(su != null && su.getData() != null) {
				mongoTemplate.remove(su.getData(), Constants.DATA);
			}
		});
		
	}

	/**
	 * Delete the data for a Campaign id
	 */
	@Override
	public void deleteDataByCampaignId(String campaignId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("campaign.id").is(campaignId));
		List<SurveyUnit> suList = mongoTemplate.find(query, SurveyUnit.class, "survey_unit");
		suList.stream().forEach(su -> {
			if(su != null && su.getData() != null) {
				mongoTemplate.remove(su.getData(), Constants.DATA);
			}
		});
	}

	/**
	 * Update data for a SurveyUnit
	 */
	@Override
	public void updateData(SurveyUnit surveyUnit) {
		Query query = new Query();
		query.addCriteria(Criteria.where("surveyUnit.id").is(surveyUnit.getId()));
		Data dataTemp = mongoTemplate.findOne(query, Data.class, Constants.DATA);
		dataTemp.setValue(surveyUnit.getData().getValue());
		dataTemp.setSurveyUnit(surveyUnit);
		mongoTemplate.save(dataTemp, Constants.DATA);
	}

}
