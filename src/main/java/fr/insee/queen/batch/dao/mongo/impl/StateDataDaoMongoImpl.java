package fr.insee.queen.batch.dao.mongo.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import fr.insee.queen.batch.config.ConditonMongo;
import fr.insee.queen.batch.dao.StateDataDao;
import fr.insee.queen.batch.object.StateData;
import fr.insee.queen.batch.object.SurveyUnit;
import fr.insee.queen.batch.utils.PathUtils;

/**
 * Service for the StateData entity that implements the interface associated
 * @author scorcaud
 *
 */
@Service
@Conditional(value= ConditonMongo.class)
public class StateDataDaoMongoImpl implements StateDataDao{
	
	@Autowired
	MongoTemplate mongoTemplate;
	
	@Override
	public void createStateData(StateData stateData) {
		mongoTemplate.save(stateData, "state_data");
	}
	/**
	 * Delete a state for a list of Survey units
	 * @param lstSu
	 */
	@Override
	public void deleteStateDataBySU(List<String> lstSu) {
		Query query = new Query();
		query.addCriteria(Criteria.where("id").in(lstSu));
		List<SurveyUnit> suList = mongoTemplate.find(query, SurveyUnit.class, "survey_unit");
		suList.stream().forEach(su -> {
			if(su != null && su.getStateData() != null) {
				mongoTemplate.remove(su.getStateData(), "state_data");
			}
		});
	}

	/**
	 * Delete a state for all Survey units of a campaign
	 * @param campaignId
	 */
	@Override
	public void deleteStateDataByCampaignId(String campaignId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("campaign.id").is(campaignId));
		List<SurveyUnit> suList = mongoTemplate.find(query, SurveyUnit.class, "survey_unit");
		suList.stream().forEach(su -> {
			if(su != null && su.getStateData() != null) {
				mongoTemplate.remove(su.getStateData(), "state_data");
			}
		});
	}

	/**
	 * Update state of a Survey Unit
	 * @param suId
	 * @param state
	 */
	@Override
	public void updateSurveyUnitStateById(String suId, String state) {
		Query query = new Query();
		query.addCriteria(Criteria.where("surveyUnit.id").is(suId));
		query.fields().include("surveyUnit");
		query.fields().include("currentPage");
		query.fields().include("date");
		StateData su = mongoTemplate.findOne(query, StateData.class, "state_data");
		if(su != null) {
			su.setState(state);
			su.setDate(Long.valueOf(PathUtils.getTimestampForPath()));
			mongoTemplate.save(su, "state_data");
		}
	}
}
