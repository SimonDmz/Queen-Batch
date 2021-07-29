package fr.insee.queen.batch.dao.impl.jpa;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import fr.insee.queen.batch.config.ConditonJpa;
import fr.insee.queen.batch.dao.DataDao;
import fr.insee.queen.batch.object.SurveyUnit;

/**
 * Service for the Data entity that implements the interface associated
 * @author scorcaud
 *
 */
@Service
@Conditional(value= ConditonJpa.class)
public class DataDaoJpaImpl implements DataDao {

	@Autowired
	@Qualifier("jdbcTemplate")
	JdbcTemplate jdbcTemplate;
	
	/**
	 * Implements the creation of data in database
	 * @param surveyUnit
	 * @throws SQLException
	 */
	@Override
	public void createData(SurveyUnit surveyUnit) throws SQLException {
		StringBuilder qString =  new StringBuilder("INSERT INTO data (id, value, survey_unit_id) VALUES (?, ?, ?)");
		PGobject value = new PGobject();
		value.setType("json");
		value.setValue(surveyUnit.getData().getValue().toJSONString());
		jdbcTemplate.update(qString.toString(), surveyUnit.getData().getId(), value, surveyUnit.getId());
	}
	
	/**
	 * Get all data for a SurveyUnit
	 */
	@Override
	public JSONObject getDataBySurveyUnitId(String suId) throws ParseException {
		StringBuilder qString= new StringBuilder("SELECT value FROM data WHERE survey_unit_id=?");
		PGobject data =  jdbcTemplate.queryForObject(qString.toString(), new Object[]{suId}, PGobject.class);
		JSONParser parser = new JSONParser();
		return (JSONObject) parser.parse(data.getValue());
		
	}

	/**
	 * Delete all data for a list of SU
	 */
	@Override
	public void deleteDataBySurveyUnitIds(List<String> lstSu) {
		String values = lstSu.stream().map(id->"?").collect(Collectors.joining(","));
		StringBuilder qStringBuilder = new StringBuilder("DELETE FROM data AS d ")
		.append("USING survey_unit AS su ")
		.append("WHERE su.id = d.survey_unit_id ")
		.append("AND su.id IN (%s)");
		String qString = String.format(qStringBuilder.toString(), values);
		jdbcTemplate.update(qString, lstSu.toArray());
	}

	/**
	 * Delete data for a campaign
	 */
	@Override
	public void deleteDataByCampaignId(String campaignId) {
		StringBuilder qString =  new StringBuilder("DELETE FROM data AS d ")
				.append("USING ")
				.append("survey_unit AS su, ")
				.append("campaign AS c  ")
				.append("WHERE su.id = d.survey_unit_id ")
				.append("AND su.campaign_id=c.id ")
				.append("AND c.id = ?");
		jdbcTemplate.update(qString.toString(), campaignId);
	}

	/**
	 * Update the data by a SU
	 */
	@Override
	public void updateData(SurveyUnit surveyUnit) throws SQLException{
		StringBuilder qString =  new StringBuilder("UPDATE data SET value = ? WHERE survey_unit_id= ?");
		PGobject value = new PGobject();
		value.setType("json");
		value.setValue(surveyUnit.getData().getValue().toJSONString());
		jdbcTemplate.update(qString.toString(), value, surveyUnit.getId());
		
	}

}
