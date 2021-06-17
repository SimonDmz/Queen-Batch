package fr.insee.queen.batch.dao.impl.jpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import fr.insee.queen.batch.config.ConditonJpa;
import fr.insee.queen.batch.dao.PersonalizationDao;
import fr.insee.queen.batch.object.Personalization;
import fr.insee.queen.batch.object.SurveyUnit;

/**
 * Service for the Personalization entity that implements the interface associated
 * @author scorcaud
 *
 */
@Service
@Conditional(value= ConditonJpa.class)
public class PersonalizationDaoJpaImpl implements PersonalizationDao{

	@Autowired
	JdbcTemplate jdbcTemplate;
	
	/**
	 * Create a personalization for a SurveyUnit
	 */
	@Override
	public void createPersonalization(SurveyUnit surveyUnit) throws SQLException {
		StringBuilder qString = new StringBuilder("INSERT INTO personalization (id, value, survey_unit_id) VALUES (?, ?, ?)");
		PGobject value = new PGobject();
		value.setType("json");
		value.setValue(surveyUnit.getPersonalization().getValue().toJSONString());
		jdbcTemplate.update(qString.toString(), surveyUnit.getPersonalization().getId(), value, surveyUnit.getId());
	}

	/**
	 * Update personalization for a SurveyUnit
	 */
	@Override
	public void updatePersonalization(SurveyUnit surveyUnit) throws SQLException {
		List<Personalization> personalizationTemp = findBySurveyUnitId(surveyUnit.getId());
		if(personalizationTemp != null && !personalizationTemp.isEmpty()) {
			StringBuilder qString = new StringBuilder("UPDATE personalization SET value = ? WHERE survey_unit_id= ?");
			personalizationTemp.forEach(pers -> {
				PGobject value = new PGobject();
				value.setType("json");
				try {
					value.setValue(surveyUnit.getPersonalization().getValue().toJSONString());
				} catch (SQLException e) {
					e.printStackTrace();
				}
				jdbcTemplate.update(qString.toString(), value, surveyUnit.getId());
			});
		} else {
			createPersonalization(surveyUnit);
		}
		
		
	}

	/**
	 * Delete personalization for a campaign
	 */
	@Override
	public void deleteByCampaignId(String campaignId) {
		StringBuilder qString = new StringBuilder("DELETE FROM personalization AS pers ")
			.append("USING ")
			.append("survey_unit AS su, ")
			.append("campaign AS c  ")
			.append("WHERE su.id = pers.survey_unit_id ")
			.append("AND su.campaign_id=c.id ")
			.append("AND c.id = ?");
		jdbcTemplate.update(qString.toString(), campaignId);
	}

	/**
	 * Delete all the personalizations for a list of SurveyUnit
	 */
	@Override
	public void deleteBySurveyUnitIds(List<String> lstSu) {
		String values = lstSu.stream().map(id->"?").collect(Collectors.joining(","));
		StringBuilder qStringBuilder = new StringBuilder("DELETE FROM personalization AS pers ")
		.append("USING survey_unit AS su ")
		.append("WHERE su.id = pers.survey_unit_id ")
		.append("AND su.id IN (%s)");
		String qString = String.format(qStringBuilder.toString(), values);
		jdbcTemplate.update(qString, lstSu.toArray());
	}

	/**
	 * Get the personalization for a SurveyUnit id
	 */
	@Override
	public List<Personalization> findBySurveyUnitId(String surveyUnitId) {
		StringBuilder qString = new StringBuilder("SELECT * FROM personalization WHERE survey_unit_id= ?");
		return jdbcTemplate.query(qString.toString(), new Object[]{surveyUnitId}, new PersonalizationModelMapper());
	}
	
	/**
	 * Retrieve the QuestionnaireModel by the id passed in parameter
	 * @param id
	 * @return QuestionnaireModel object
	 * @throws ParseException 
	 */
	@Override
	public JSONArray getValueById(UUID id) throws ParseException {
		StringBuilder qString = new StringBuilder("SELECT value FROM personalization WHERE id=?");
		PGobject value =  jdbcTemplate.queryForObject(qString.toString(), new Object[]{id}, PGobject.class);
		if(value != null && (value.getValue().contains("name") || value.getValue().contains("value"))) {
			JSONParser parser = new JSONParser();
			return (JSONArray) parser.parse(value.getValue());
		} else {
			return new JSONArray();
		}
		
	}
	
	/**
	 * Implements the mapping between the result of the query and the QuestionnaireModel entity
	 * @return QuestionnaireModelMapper
	 */
	private static final class PersonalizationModelMapper implements RowMapper<Personalization> {
        public Personalization mapRow(ResultSet rs, int rowNum) throws SQLException         {
        	Personalization personalization = new Personalization();
        	personalization.setId(rs.getObject("id", UUID.class));
            return personalization;
        }
    }
}
