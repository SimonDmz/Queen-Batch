package fr.insee.queen.batch.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;

import fr.insee.queen.batch.object.Personalization;
import fr.insee.queen.batch.object.SurveyUnit;

public interface PersonalizationDao {
	/**
	 * Create a Personalization in database
	 * @param surveyUnit
	 * @throws SQLException
	 */
	void createPersonalization(SurveyUnit surveyUnit) throws SQLException;

	/**
	 * Update a personalization by a survey unit
	 * @param surveyUnit
	 * @throws SQLException
	 */
	void updatePersonalization(SurveyUnit surveyUnit) throws SQLException;
	
	/**
	 * Retrieves all the personalization for a Survey Unit
	 * @param surveyUnitId
	 * @return
	 */
	List<Personalization> findBySurveyUnitId(String surveyUnitId);
	
	/**
	 * Delete all the personalization for a campaign
	 * @param campaignId
	 */
	void deleteByCampaignId(String campaignId);
	
	/**
	 * Delete all the personalizations for a list of Survey
	 * @param lstSu
	 */
	void deleteBySurveyUnitIds(List<String> lstSu);
	
	/**
	 * Retrieve the value by the id passed in parameter
	 * @param id
	 * @return JSONObject object
	 * @throws ParseException 
	 */
	JSONArray getValueById(UUID id) throws ParseException;
}
