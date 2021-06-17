package fr.insee.queen.batch.object;

import java.util.UUID;

import org.json.simple.JSONObject;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="comment") 
public class Comment {
	
	/**
	 * The id of the comment
	 */
	private UUID id;
	
	/**
	 * Value of the comment
	 */
	private JSONObject value;
	
	/**
	 * SurveyUnit associated to the comment
	 */
	@DBRef
	private SurveyUnit surveyUnit;
	
	/**
	 * All args constructor
	 * @param id
	 * @param value
	 * @param surveyUnit
	 */
	public Comment(UUID id, JSONObject value, SurveyUnit surveyUnit) {
		super();
		this.id = id;
		this.value = value;
		this.surveyUnit = surveyUnit;
	}
	
	/**
	 * COnstructor with id only
	 * @param randomUUID
	 */
	public Comment(UUID randomUUID) {
		// TODO Auto-generated constructor stub
		this.id = randomUUID;
	}

	/**
	 * Default constructor
	 */
	public Comment() {
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
