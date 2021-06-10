package fr.insee.queen.batch.dao;

import java.sql.SQLException;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import fr.insee.queen.batch.object.SurveyUnit;

/**
 * Interface for the Data entity
 * @author scorcaud
 *
 */
public interface DataDao {
	
	/**
	 * Create a Data in database
	 * @param surveyUnit
	 * @throws SQLException
	 */
    void createData(SurveyUnit surveyUnit) throws SQLException;
    
    /**
     * Update data for a SU
     * @param surveyUnit
     */
    void updateData(SurveyUnit surveyUnit) throws SQLException;
    
    /**
     * Get the data by a SurveyUnit id
     * @param suId
     * @return
     * @throws ParseException
     */
	JSONObject getDataBySurveyUnitId(String suId) throws ParseException;
	
	/**
	 * Delete data by a list of SU
	 * @param lstSu
	 */
	void deleteDataBySurveyUnitIds(List<String> lstSu);

	/**
	 * Delete data by a campaign Id
	 * @param campaignId
	 */
	void deleteDataByCampaignId(String campaignId);
}
