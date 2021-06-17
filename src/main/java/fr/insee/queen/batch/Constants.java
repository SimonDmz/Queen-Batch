package fr.insee.queen.batch;

import java.net.URL;

public class Constants {
	
	public static final String SCHEMAS_FOLDER_PATH = "/xsd";

	public static final URL MODEL_NOMENCLATURE = Constants.class.getResource(SCHEMAS_FOLDER_PATH+"/Nomenclature.xsd");
	public static final URL MODEL_SAMPLE = Constants.class.getResource(SCHEMAS_FOLDER_PATH+"/Sample.xsd");

	public static final String JSON = ".json";

	public static final String MSG_UNKNOWN_MODEL = "Unknown Model";
	public static final String MSG_RETURN_CODE = "CODE RETOUR BATCH : {}";
	public static final String MSG_FAILED_MOVE_FILE = "Failed to move the file {}";
	public static final String MSG_FILE_MOVE_SUCCESS = "File {} renamed and moved successfully";

	private Constants() {

	}
	
}
