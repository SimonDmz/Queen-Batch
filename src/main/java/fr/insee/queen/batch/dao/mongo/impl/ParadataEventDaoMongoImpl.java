package fr.insee.queen.batch.dao.mongo.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import fr.insee.queen.batch.config.ConditonMongo;
import fr.insee.queen.batch.dao.ParadataEventDao;
import fr.insee.queen.batch.service.DatabaseService;

/**
 * Service for the Paradata-Event entity that implements the interface associated
 * @author scorcaud
 *
 */
@Service
@Conditional(value= ConditonMongo.class)
public class ParadataEventDaoMongoImpl implements ParadataEventDao{

	@Autowired
	MongoTemplate mongoTemplate;
	
	@Autowired
	DatabaseService databaseService;
	
	/**
	 * Method used to retreive all the paradata for a SurveyUnit
	 * @throws ParseException 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject findBySurveyUnitId(String suId) throws ParseException {
		JSONParser parser = new JSONParser();
		JSONArray array = new JSONArray();
		List<String> value = new ArrayList<>();
		Query query = new Query();
		query.addCriteria(Criteria.where("value." +databaseService.getKeyParadataIdSu()).is(suId));
		query.fields().include("value");
		query.fields().exclude("_id");
		value =  mongoTemplate.find(query, String.class, "paradata_event");
		for(String val : value) {
			JSONObject jsonObjectTemp = (JSONObject) parser.parse(val);
			JSONObject jsonObjectTemp2 = (JSONObject) jsonObjectTemp.get("value");
			array.add(jsonObjectTemp2.get(databaseService.getKeyParadataEvents()));
		}
		JSONObject jsobObjectClean = new JSONObject();
		jsobObjectClean.put("idSu", suId);
		jsobObjectClean.put("events", array);
		return jsobObjectClean;
	}
	
	/**
	 * This method create a paradata_event, only use for for testing in the creation of the dataset 
	 */
	@Override
	public void createParadata(JSONObject jsonParadata) throws SQLException {
		ParadataEvent paradata = new ParadataEvent(UUID.randomUUID(), jsonParadata);
		mongoTemplate.save(paradata, "paradata_event");
	}
	
	/**
	 * This class is used only to create Paradata object for testing
	 */
	static class ParadataEvent{
		UUID id;
		JSONObject value;
		public ParadataEvent(UUID id, JSONObject value) {
			this.id = id;
			this.value = value;
		}
	}
}
