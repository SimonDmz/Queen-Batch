package fr.insee.queen.batch.object;

import org.json.simple.JSONArray;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entity Nomenclature : represent the entity table in DB
 * 
 * @author Claudel Benjamin
 * 
 */
@Document(collection="nomenclature")
public class Nomenclature {

	private String id;
	private String label;
	private JSONArray value;

	public Nomenclature(String id, String label, JSONArray value) {
		super();
		this.id = id;
		this.label = label;
		this.value = value;
	}

	public Nomenclature(String id) {
		super();
		this.id = id;
	}
	
	public Nomenclature() {
		super();
	}

	/**
	 * @return id of nomenclature
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
	 * @return label of nomenclature
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
	 * @return value of nomenclature
	 */
	public JSONArray getValue() {
		return value;
	}

	/**
	 * @param value value to set
	 */
	public void setValue(JSONArray value) {
		this.value = value;
	}

}
