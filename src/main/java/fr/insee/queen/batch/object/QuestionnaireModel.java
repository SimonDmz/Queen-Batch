package fr.insee.queen.batch.object;

import org.json.simple.JSONObject;
import org.springframework.data.mongodb.core.mapping.Document;

/**
* Object XmlQuestionnaireModel : represent the questionnaire model in XML file
* 
* @author Claudel Benjamin
* 
*/
@Document(collection="questionnaire_model")
public class QuestionnaireModel {
	/**
	* The id of questionnaire model 
	*/
	private String id;
	/**
	* The label of questionnaire model 
	*/
	private String label;
	/**
	* The JSON Questionnaire of questionnaire model 
	*/
	private JSONObject value;
	/**
	* The list of required nomenclature for questionnaire model 
	*/
	private String campaignId;
	
	/**
	 * Constructor with all fields
	 * @param id
	 * @param label
	 * @param value
	 * @param campaignId
	 */
	public QuestionnaireModel(String id, String label, JSONObject value, String campaignId) {
		super();
		this.id = id;
		this.label = label;
		this.value = value;
		this.campaignId = campaignId;
	}
	
	/**
	 * Default constructor
	 */
	public QuestionnaireModel() {
	}
	/**
	 * @return id of questionnaire model
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
	 * @return label of questionnaire model
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
	 * @return the JSON questionnaire of questionnaire model
	 */
	public JSONObject getModel() {
		return value;
	}
	/**
	 * @param xmlQuestionnaire xmlQuestionnaire to set
	 */
	public void setModel(JSONObject xmlQuestionnaire) {
		this.value = xmlQuestionnaire;
	}
	
	/**
	 * @return the list of required nomenclature for questionnaire model
	 */
	public String getCampaignId() {
		return campaignId;
	}
	/**
	 * @param xmlNomenclatures list of required nomenclature to set
	 */
	public void setCampaignId(String campaignId) {
		this.campaignId = campaignId;
	}
	
	

}
