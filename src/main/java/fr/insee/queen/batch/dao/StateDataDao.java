package fr.insee.queen.batch.dao;

import java.util.List;

import fr.insee.queen.batch.object.StateData;

/**
 * Interface for the StateData entity
 * @author scorcaud
 *
 */
public interface StateDataDao {
	
	/**
	 * Create a state data
	 * @param stateData
	 */
	void createStateData(StateData stateData);

	/**
	 * Delete a state for a list of Survey units
	 * @param lstSu
	 */
	void deleteStateDataBySU(List<String> lstSu);
	
	/**
	 * Delete a state for all Survey units of a campaign
	 * @param campaignId
	 */
	void deleteStateDataByCampaignId(String campaignId);
	
	/**
	 * Update state of a Survey Unit
	 * @param suId
	 * @param state
	 */
	void updateSurveyUnitStateById(String suId, String state);
}
