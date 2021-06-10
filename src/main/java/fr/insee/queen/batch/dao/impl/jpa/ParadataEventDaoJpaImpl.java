package fr.insee.queen.batch.dao.impl.jpa;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import fr.insee.queen.batch.config.ConditonJpa;
import fr.insee.queen.batch.dao.ParadataEventDao;
import fr.insee.queen.batch.service.DatabaseService;

/**
 * Service for the Paradata-Event entity that implements the interface associated
 * @author scorcaud
 *
 */
@Service
@Conditional(value= ConditonJpa.class)
public class ParadataEventDaoJpaImpl implements ParadataEventDao{

	@Autowired
	JdbcTemplate jdbcTemplate;
	
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
		StringBuilder qString = new StringBuilder("SELECT value FROM paradata_event WHERE EXISTS ( select 1 ")
				.append("from jsonb_each_text(value) as t(")
				.append(databaseService.getKeyParadataIdSu())
				.append(",v) where v ilike ?)");
		value =  jdbcTemplate.queryForList(qString.toString(), new Object[]{suId}, String.class);
		for(String val : value) {
			JSONObject jsonObjectTemp = (JSONObject) parser.parse(val);
			array.add(jsonObjectTemp.get(databaseService.getKeyParadataEvents()));
		}
		JSONObject jsobObjectClean = new JSONObject();
		jsobObjectClean.put("idSu", suId);
		jsobObjectClean.put("events", array);
		return jsobObjectClean;
	}
	
	/**
	 * This method create a paradata_event, only use for for testing in the creation of the dataset 
	 */
	public void createParadata(JSONObject paradata) throws SQLException {
		StringBuilder qString = new StringBuilder("INSERT INTO paradata_event (id, value) VALUES (?, ?)");
		PGobject value = new PGobject();
		UUID id = UUID.randomUUID();
		value.setType("json");
		value.setValue(paradata.toJSONString());
		jdbcTemplate.update(qString.toString(), id, value);
	}

}
