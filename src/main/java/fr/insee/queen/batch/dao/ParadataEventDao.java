package fr.insee.queen.batch.dao;

import java.sql.SQLException;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 * Interface for the Paradata-event entity
 * @author scorcaud
 *
 */
public interface ParadataEventDao {

	/**
	 * Create a paradata_event, only use for for testing in the creation of the dataset 
	 * @param paradata
	 * @throws SQLException 
	 */
	void createParadata(JSONObject jsonParadata) throws SQLException;
	
	/**
	 * Method used to retreive all the paradata for a SurveyUnit
	 * @throws ParseException 
	 */
	JSONObject findBySurveyUnitId(String suId) throws ParseException;
}
