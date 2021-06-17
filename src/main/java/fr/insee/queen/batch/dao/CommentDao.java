package fr.insee.queen.batch.dao;

import java.sql.SQLException;
import java.util.List;

import fr.insee.queen.batch.object.SurveyUnit;

/**
 * Interface for the Comment entity
 * @author scorcaud
 *
 */
public interface CommentDao {
	
	/**
	 * Create a Comment in database
	 * @param surveyUnit
	 * @throws SQLException
	 */
	void createComment(SurveyUnit surveyUnit) throws SQLException;

	/**
	 * Delete all comments for a list of SU
	 * @param lstSu
	 */
	void deleteCommentBySurveyUnitIds(List<String> lstSu);

	/**
	 * Delete a comment by a campaign id
	 * @param campaignId
	 */
	void deleteCommentByCampaignId(String campaignId);

	/**
	 * Update the comment by a SU
	 */
	void updateComment(SurveyUnit surveyUnit) throws SQLException;
}
