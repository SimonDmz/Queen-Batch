package fr.insee.queen.batch.dao.impl.jpa;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import fr.insee.queen.batch.config.ConditonJpa;
import fr.insee.queen.batch.dao.StateDataDao;
import fr.insee.queen.batch.object.StateData;
import fr.insee.queen.batch.utils.PathUtils;

/**
 * Service for the StateData entity that implements the interface associated
 * @author scorcaud
 *
 */
@Service
@Conditional(value= ConditonJpa.class)
public class StateDataDaoJpaImpl implements StateDataDao{

	@Autowired
	@Qualifier("jdbcTemplate")
	JdbcTemplate jdbcTemplate;
	
	@Override
	public void createStateData(StateData stateData) {
		StringBuilder qString = new StringBuilder("INSERT INTO state_data (id, current_page, date, state, survey_unit_id) ")
		.append("VALUES (?, ?, ?, ?, ?)");
		jdbcTemplate.update(qString.toString(), stateData.getId(), stateData.getCurrentPage(), stateData.getDate(), 
				 stateData.getState(), stateData.getSurveyUnit().getId());
	}
	/**
	 * Delete state by a list of SurveyUnit
	 */
	@Override
	public void deleteStateDataBySU(List<String> lstSu) {
		String values = lstSu.stream().map(id->"?").collect(Collectors.joining(","));
		StringBuilder qStringBuilder = new StringBuilder("DELETE FROM state_data AS stateData ")
		.append("WHERE stateData.survey_unit_id IN (%s)");
		String qString = String.format(qStringBuilder.toString(), values);
		jdbcTemplate.update(qString, lstSu.toArray());
	}

	/**
	 * Delete a state for all Survey units of a campaign
	 */
	@Override
	public void deleteStateDataByCampaignId(String campaignId) {
		StringBuilder qString = new StringBuilder("DELETE FROM state_data AS stateData ")
				.append("USING survey_unit AS su ")
				.append("WHERE su.id = stateData.survey_unit_id AND su.campaign_id =?");
		jdbcTemplate.update(qString.toString(), campaignId);
	}

	/**
	 * Update state of a Survey Unit
	 */
	@Override
	public void updateSurveyUnitStateById(String suId, String state) {
		StringBuilder qString = new StringBuilder("UPDATE state_data SET state = ?, date = ? WHERE survey_unit_id= ?");
		jdbcTemplate.update(qString.toString(), state, Long.valueOf(PathUtils.getTimestampForPath()), suId);
	}

}
