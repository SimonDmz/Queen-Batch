package fr.insee.queen.batch.dao.impl.jpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import fr.insee.queen.batch.object.QuestionnaireModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import fr.insee.queen.batch.config.ConditonJpa;
import fr.insee.queen.batch.dao.SurveyUnitDao;
import fr.insee.queen.batch.object.SurveyUnit;

/**
 * Service for the SurveyUnit entity that implements the interface associated
 * @author scorcaud
 *
 */
@Service
@Conditional(value= ConditonJpa.class)
public class SurveyUnitDaoJpaImpl implements SurveyUnitDao {
	
	@Autowired
	@Qualifier("jdbcTemplate")
	JdbcTemplate jdbcTemplate;

	/**
	 * Implements the creation of a SurveyUnit in database
	 * @param surveyUnit
	 * @param idCampaign
	 * @throws SQLException
	 */
	@Override
	public void createSurveyUnit(SurveyUnit surveyUnit) {
		StringBuilder qString = new StringBuilder("INSERT INTO survey_unit (id, campaign_id, questionnaire_model_id) VALUES (?, ?, ?)");
	    jdbcTemplate.update(qString.toString(), surveyUnit.getId(), surveyUnit.getCampaign().getId(), 
	    		surveyUnit.getQuestionnaireModel().getId());
	}

	/**
	 * Implementation to check if an SurveyUnit already exist in database 
	 * @param xmlId
	 * @return boolean
	 * @throws SQLException
	 */
	@Override
	public boolean existSurveyUnit(String xmlId) {
		StringBuilder qString = new StringBuilder("SELECT COUNT(id) FROM survey_unit WHERE id LIKE ?");
		Long nbRes = jdbcTemplate.queryForObject(qString.toString(), new Object[]{xmlId}, Long.class);
		return nbRes>0;
	}

	/**
	 * Get all SU for a campaign
	 * @param campaignId
	 * @return
	 */
	@Override
	public List<String> getAllSurveyUnitByCamapignId(String campaignId) {
		StringBuilder qString =new StringBuilder( "SELECT id FROM survey_unit WHERE campaign_id=?");
		return jdbcTemplate.queryForList(qString.toString(), new Object[]{campaignId}, String.class); 
	}

	/**
	 * Get all SU for a campaign and a given state (StateData)
	 * @param campaignId, state
	 * @return
	 */
	@Override
	public List<SurveyUnit> getAllSurveyUnitsByCampaignIdByState(String state, String campaignId){
		StringBuilder qString = new StringBuilder("SELECT su.id, su.campaign_id, stateData.state FROM survey_unit AS su ")
				.append("INNER JOIN state_data AS stateData ON stateData.survey_unit_id = su.id ")
				.append("WHERE stateData.state = ? AND su.campaign_id=?");
		return jdbcTemplate.query(qString.toString(), new Object[]{campaignId}, new SurveyUnitMapper());
}

	/**
	 * Get unexisting SurveyUnits
	 * @param lstSu
	 * @return
	 */
	@Override
	public List<String> findUnexistingSurveyUnitsInList(List<String> lstSu) {
		String values = lstSu.stream().map(id->"(?)").collect(Collectors.joining(","));
		StringBuilder qStringBuilder = new StringBuilder("SELECT t.id FROM (VALUES %s) AS t(id) ")
		.append("LEFT JOIN survey_unit i ON i.id = t.id ")
		.append("WHERE i.id IS NULL");
		String qString = String.format(qStringBuilder.toString(), values);
		return jdbcTemplate.queryForList(qString, lstSu.toArray(), String.class); 
	}

	/**
	 * Delete a list of SU
	 * @param lstSu
	 */
	@Override
	public void deleteSurveyUnits(List<String> lstSu) {
		String values = lstSu.stream().map(id->"?").collect(Collectors.joining(","));
		StringBuilder qStringBuilder = new StringBuilder("DELETE FROM survey_unit AS su ")
		.append("WHERE su.id IN (%s)");
		String qString = String.format(qStringBuilder.toString(), values);
		jdbcTemplate.update(qString, lstSu.toArray());
	}

	/**
	 * Delete all SU for a campaign
	 * @param id
	 */
	@Override
	public void deleteSurveyUnitByCampaignId(String campaignId) {
		StringBuilder qString = new StringBuilder("DELETE FROM survey_unit AS su ")
		.append("USING campaign AS c ")
		.append("WHERE su.campaign_id=c.id ")
		.append("AND c.id = ?");
		jdbcTemplate.update(qString.toString(), campaignId);
	}

	/**
	 * Update a SurveyUnit
	 * @param surveyUnit
	 */
	@Override
	public void updateSurveyUnit(SurveyUnit surveyUnit) {
		StringBuilder qString = new StringBuilder("UPDATE survey_unit SET campaign_id = ?, questionnaire_model_id = ? WHERE id= ?");
		jdbcTemplate.update(qString.toString(), surveyUnit.getCampaign().getId(), 
				surveyUnit.getQuestionnaireModel().getId(), surveyUnit.getId());
	}

	/**
	 * Get Survey unit by his id
	 * @param id
	 * @return
	 */
	@Override
	public String findQuestionnaireIdBySurveyUnitId(String id) {
		StringBuilder qString = new StringBuilder("SELECT questionnaire_model_id FROM survey_unit WHERE id= ?");
		return jdbcTemplate.queryForObject(qString.toString(), new Object[]{id}, String.class);
	}

	/**
	 * Delete meta data by campaign id
	 * @param id
	 */
	@Override
	public void deleteMetaDataByCampaignId(String id) {
		StringBuilder qString = new StringBuilder("DELETE FROM metadata AS metaData ")
				.append("WHERE metaData.campaign_id =?");
		jdbcTemplate.update(qString.toString(), id);
	}

	/**
	 * Get ids for survey unit with state "validated"
	 * @return
	 */
	@Override
	public List<String> findSurveyUnitsValidatedIdsByCampaignId(String campaignId) {
		StringBuilder qString = new StringBuilder("SELECT su.id FROM survey_unit AS su ")
		.append("INNER JOIN state_data AS stateData ON stateData.survey_unit_id = su.id ")
		.append("WHERE stateData.state = 'VALIDATED' AND su.campaign_id=?");
		return jdbcTemplate.queryForList(qString.toString(), new Object[]{campaignId}, String.class);
	}

	/**
	 * Implements the mapping between the result of the query and the QuestionnaireModel entity
	 * @return QuestionnaireModelMapper
	 */
	private static final class SurveyUnitMapper implements RowMapper<SurveyUnit> {
		public SurveyUnit mapRow(ResultSet rs, int rowNum) throws SQLException         {
			SurveyUnit su = new SurveyUnit();
			su.setId(rs.getString("id"));
			su.getCampaign().setId(rs.getString("campaign_id"));
			su.getStateData().setState(rs.getString("state"));
			
			return su;
		}
	}
}

