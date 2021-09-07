package fr.insee.queen.batch.object;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

/**
* Object XmlCampaign : represent the campaign in XML file
* 
* @author Claudel Benjamin
* 
*/
@Document(collection="campaign")
public class Campaign {
	/**
	* The id of campaign 
	*/
	private String id;
	/**
	* The label of campaign 
	*/
	private String label;
	
	/**
	* The list of survey unit associated to campaign
	*/
	@DBRef
	private List<QuestionnaireModel> questionnaireModels;
	
	/**
	 * 
	 * @param id
	 * @param label
	 * @param questionnaireModels
	 */
	public Campaign(String id, String label) {
		super();
		this.id = id;
		this.label = label;
	}
	
	public Campaign() {
		super();
		this.id = null;
		this.label = null;
		this.questionnaireModels = null;
	}

	/**
	 * @return id of campaign
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return label of campaign
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * @param label label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	/**
	 * @return list of survey unit associated to campaign
	 */
	public List<QuestionnaireModel> getQuestionnaireModels() {
		return questionnaireModels;
	}
	/**
	 * @param surveyUnits list of survey unit to set
	 */
	public void setQuestionnaireModels(List<QuestionnaireModel> questionnaireModels) {
		this.questionnaireModels = questionnaireModels;
	}
	
}
