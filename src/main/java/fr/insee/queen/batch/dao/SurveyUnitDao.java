package fr.insee.queen.batch.dao;

import java.util.List;

import fr.insee.queen.batch.object.SurveyUnit;

/**
 * Interface for the SurveyUnit entity
 * @author scorcaud
 *
 */
public interface SurveyUnitDao {
    /**
     * Create a SurveyUnit in database
     * @param surveyUnit
     * @param idCampaign
     */
    void createSurveyUnit(SurveyUnit surveyUnit);
    
	/**
     * Check if an SurveyUnit already exist in database
     * @param xmlId
     * @return boolean
     * @throws Exception
     */
	boolean existSurveyUnit(String xmlId);

	/**
	 * Get all SU for a campaign
	 * @param campaignId
	 * @return
	 */
	List<String> getAllSurveyUnitByCamapignId(String campaignId);

	/**
	 * Get all SU with state not null for a campaign
	 * @param campaignId
	 * @return
	 */
	List<SurveyUnit> getAllSurveyUnitsWithStateByCampaignId(String campaignId);

	/**
	 * Get unexisting SurveyUnits
	 * @param lstSu
	 * @return
	 */
	List<String> findUnexistingSurveyUnitsInList(List<String> lstSu);

	List<String> findSurveyUnitsByStateByCampaignId(String campaignId, String state);
	/**
	 * Get Survey unit by his id
	 * @param id
	 * @return
	 */
	String findQuestionnaireIdBySurveyUnitId(String id);

	/**
	 * Delete a list of SU
	 * @param lstSu
	 */
	void deleteSurveyUnits(List<String> lstSu);
	
	/**
	 * Delete all SU for a campaign
	 * @param id
	 */
	void deleteSurveyUnitByCampaignId(String id);
	
	/**
	 * Update a SurveyUnit
	 * @param surveyUnit
	 */
	void updateSurveyUnit(SurveyUnit surveyUnit);
	
	/**
	 * Delete meta data by campaign id
	 * @param id
	 */
	void deleteMetaDataByCampaignId(String id);

	/**
	 * Get ids for survey unit with state "validated"
	 * @return
	 */
	List<String> findSurveyUnitsValidatedIdsByCampaignId(String campaignId);
}
