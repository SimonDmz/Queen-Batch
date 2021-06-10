package fr.insee.queen.batch.object;

import java.util.UUID;

import javax.persistence.Column;

import org.json.simple.JSONObject;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

/**
* Object Metadata
* 
* @author Claudel Benjamin
* 
*/
@Document(collection="metadata")
public class Metadata {
	/**
	 * The id of the Metadata
	 */
	private UUID id;
	
	/**
	* The value of data (jsonb format)
	*/
    @Column(columnDefinition = "jsonb")
	private JSONObject value;
	
	/**
	 * The campaign associated to the Metadata
	 */
	@DBRef
	private Campaign campaign;

	public Metadata() {
		super();
		this.id = UUID.randomUUID();
	}

	public Metadata(UUID id, JSONObject value, Campaign campaign) {
		super();
		this.id = id;
		this.value = value;
		this.campaign = campaign;
	}

	/**
	 * @return the idMetadata
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * @param idMetadata the idMetadata to set
	 */
	public void setIdMetadata(UUID id) {
		this.id = id;
	}

	/**
	 * @return the value
	 */
	public JSONObject getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(JSONObject value) {
		this.value = value;
	}

	/**
	 * @return the campaign
	 */
	public Campaign getCampaign() {
		return campaign;
	}

	/**
	 * @param campaign the campaign to set
	 */
	public void setCampaign(Campaign campaign) {
		this.campaign = campaign;
	}
}
