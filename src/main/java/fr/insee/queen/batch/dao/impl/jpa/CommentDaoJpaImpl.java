package fr.insee.queen.batch.dao.impl.jpa;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import fr.insee.queen.batch.config.ConditonJpa;
import fr.insee.queen.batch.dao.CommentDao;
import fr.insee.queen.batch.object.SurveyUnit;

/**
 * Service for the Comment entity that implements the interface associated
 * @author scorcaud
 *
 */
@Service
@Conditional(value= ConditonJpa.class)
public class CommentDaoJpaImpl implements CommentDao {

	@Autowired
	JdbcTemplate jdbcTemplate;

	/**
	 * Implements the creation of a comment in database
	 * @param surveyUnit
	 * @throws SQLException
	 */
	@Override
	public void createComment(SurveyUnit surveyUnit) throws SQLException {
		StringBuilder qString = new StringBuilder("INSERT INTO comment (id, value, survey_unit_id) VALUES (?, ?, ?)");
		PGobject value = new PGobject();
		value.setType("json");
		value.setValue(surveyUnit.getData().getValue().toJSONString());
		jdbcTemplate.update(qString.toString(), surveyUnit.getComment().getId(), value, surveyUnit.getId());
	}

	/**
	 * Delete all comments for a list of SU
	 */
	@Override
	public void deleteCommentBySurveyUnitIds(List<String> lstSu) {
		String values = lstSu.stream().map(id->"?").collect(Collectors.joining(","));
		StringBuilder qStringBuilder = new StringBuilder("DELETE FROM comment AS com ")
		.append("USING survey_unit AS su ")
		.append("WHERE su.id = com.survey_unit_id ")
		.append( "AND su.id IN (%s)");
		String qString = String.format(qStringBuilder.toString(), values);
		jdbcTemplate.update(qString, lstSu.toArray());
	}

	/**
	 * Delete all comment for a campaign
	 */
	@Override
	public void deleteCommentByCampaignId(String campaignId) {
		StringBuilder qString = new StringBuilder("DELETE FROM comment AS com ")
				.append("USING ")
				.append("survey_unit AS su, ")
				.append("campaign AS c ")
				.append("WHERE su.id = com.survey_unit_id ")
				.append("AND su.campaign_id=c.id ")
				.append("AND c.id = ?");
		jdbcTemplate.update(qString.toString(), campaignId);
	}

	/**
	 * Update comment for a SurveyUnit
	 */
	@Override
	public void updateComment(SurveyUnit surveyUnit) throws SQLException {
		StringBuilder qString = new StringBuilder("UPDATE comment SET value = ? WHERE survey_unit_id= ?");
		PGobject value = new PGobject();
		value.setType("json");
		value.setValue(surveyUnit.getData().getValue().toJSONString());
		jdbcTemplate.update(qString.toString(), value, surveyUnit.getId());
	}
}
