package fr.insee.queen.batch.object;

import java.util.UUID;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.json.simple.JSONObject;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="data")
public class Data {
	/**
	 * Id of the data
	 */
	private UUID id;
	
	/**
	 * Value of the data
	 */
	private JSONObject value;
	
	/**
	 * SurveyUnit related to the data
	 */
	@DBRef
	private SurveyUnit surveyUnit;
	
	public Data(){
		
	}

	public Data(UUID id, JSONObject value, SurveyUnit surveyUnit) {
		super();
		this.id = id;
		this.value = value;
		this.surveyUnit = surveyUnit;
	}

	/**
	 * @return the id
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * @return the value
	 */
	public JSONObject getValue() {
		return value;
	}

	/**
	 * @return the surveyUnit
	 */
	public SurveyUnit getSurveyUnit() {
		return surveyUnit;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(UUID id) {
		this.id = id;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(JSONObject value) {
		this.value = value;
	}

	/**
	 * @param surveyUnit the surveyUnit to set
	 */
	public void setSurveyUnit(SurveyUnit surveyUnit) {
		this.surveyUnit = surveyUnit;
	}
}
