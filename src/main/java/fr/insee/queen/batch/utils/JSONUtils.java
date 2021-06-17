package fr.insee.queen.batch.utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import fr.insee.queen.batch.exception.ValidateException;

/**
 * Operation on JSON Object - createJsonFile - jsonFileToJsonObject
 * 
 * @author Claudel Benjamin
 * 
 */
public class JSONUtils {
	private static final Logger logger = LogManager.getLogger(JSONUtils.class);

	private JSONUtils() {
		throw new IllegalStateException("Utility class");
	}

	
	/**
	 * Create a JSON file from a JSON Object
	 * 
	 * @param jsonObject jsonObject to put in the new json file
	 */
	public static void createJsonFile(JSONObject jsonObject) {
		// Write JSON file
		try (FileWriter file = new FileWriter("nomenclatureTest.json")) {
			file.write(jsonObject.toJSONString());
			file.flush();
		} catch (IOException e) {
			logger.log(Level.ERROR, e.getMessage(), e);
		}
	}

	/**
	 * Create a JSON array from a JSON File
	 * 
	 * @param jsonFile jsonFile to cast
	 * @return JSONArray according to JsonFile
	 * @throws ValidateException 
	 * @throws IOException 
	 */
	public static JSONArray jsonFileToJsonObject(String jsonFile) throws ValidateException, IOException {
		// Creating a JSONParser object
		JSONParser jsonParser = new JSONParser();
		FileReader fr = null;
		try {
			File newFile = new File(jsonFile); 
			if (newFile.length() == 0) {
				throw new ValidateException(String.format("%s file is empty. Impossibleto parse empty file to JSONObject", jsonFile));
			}
			
			fr = new FileReader(jsonFile);
			Object obj = jsonParser.parse(fr);
			fr.close();
			// Parsing the contents of the JSON file
			return (JSONArray) obj;
		} catch (IOException|ParseException e) {
			if(fr!=null)fr.close();
			throw new ValidateException(String.format("%s file is not well formed. Impossible to parse file to JSONObject", jsonFile));

		}
	}
}
