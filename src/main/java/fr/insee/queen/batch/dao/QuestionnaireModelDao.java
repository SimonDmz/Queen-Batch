package fr.insee.queen.batch.dao;

import java.sql.SQLException;
import java.util.List;

import fr.insee.queen.batch.object.QuestionnaireModel;

/**
 * Interface for the QuestionnaireModel entity
 * @author scorcaud
 *
 */
public interface QuestionnaireModelDao {
	
	/**
	 * Create an QuestionnaireModel in database
	 * @param questionnaireModel
	 * @throws SQLException
	 */
	void create(QuestionnaireModel questionnaireModel, String campaignId) throws SQLException;
    
    /**
     * Get an QuestionnaireModel by id in database
     * @param id
     * @return QuestionnaireModel object
     * @throws Exception
     */
    QuestionnaireModel findById(String id);
    
    /**
     * Check if a QuestionnaireModel already exist in database
     * @param id
     * @return boolean
     * @throws Exception
     */
	boolean exist(String id);

	/**
     * Update a QuestionnaireModel in database
     * @param questionnaireModel
	 * @throws SQLException 
     * @throws Exception
     */
	void updateCampaignId(QuestionnaireModel questionnaireModel) throws SQLException;

	/**
	 * Find a Questionnaire by a campaign id
	 * @param campaignId
	 * @return
	 */
	List<QuestionnaireModel> findByCampaignId(String campaignId);

	/**
	 * Implementation to check if a QuestionnaireModel already exist in database 
	 * @param id
	 * @return boolean
	 * @throws SQLException
	 */
	boolean existForCampaign(String id, String campaignId);

	/**
	 * Delete association between campaign and questionnaire model
	 * @param questionnaireModel
	 * @throws SQLException 
	 */
	void deleteCampaignIdForQuestionnaireModel(String campaignId) throws SQLException;
	
}
