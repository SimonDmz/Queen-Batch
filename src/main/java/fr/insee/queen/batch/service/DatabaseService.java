package fr.insee.queen.batch.service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import fr.insee.queen.batch.config.ConditonJpa;
import fr.insee.queen.batch.config.ConditonMongo;
import fr.insee.queen.batch.exception.DataBaseException;

/**
 * Service for database utils
 * @author samco
 *
 */
@Service
public class DatabaseService {
	
	@Autowired
	AnnotationConfigApplicationContext context;
	
	@Autowired
	Environment env;
	
	@Autowired(required=false)
	@Qualifier("dataSource")
	DataSource dataSource;
	
	@Autowired(required=false)
	MongoTemplate mongoTemplate;
	
	@Autowired
	String getKeyParadataIdSu;
	
	@Autowired
	String getKeyParadataEvents;
	
	List<String> missingTable = new ArrayList<>();
	List<String> lstTable = List.of("survey_unit", "comment", "data", "campaign", "questionnaire_model", 
			"nomenclature", "personalization", "state_data");
	
	/**
	 * Check the database tables/collections
	 * @throws DataBaseException
	 * @throws SQLException
	 */
	public void checkDatabaseAccess() throws DataBaseException, SQLException {
		if(dataSource != null) {
			checkDatabaseAccessJpa();
		} else {
			checkDatabaseAccessMongo();
		}
	}
	
	/**
	 * Check if the tables all exists
	 * @throws DataBaseException
	 * @throws SQLException
	 */
	@Conditional(value= ConditonJpa.class)
	public void checkDatabaseAccessJpa() throws DataBaseException, SQLException {
		Connection connection = null;
		ResultSet rs = null;
		try {
			connection = dataSource.getConnection();
			DatabaseMetaData metaData = connection.getMetaData();
			for (String tableName : lstTable) {
				rs = metaData.getTables(null, null, tableName, null);
				if (!rs.next())
					missingTable.add(tableName);
			}
			if (rs != null)
				rs.close();
		} catch (SQLException e) {
			throw new DataBaseException("Error during connection to database");
		} finally {
			if (connection != null)
				connection.close();
		}
		if (!missingTable.isEmpty()) {
			throw new DataBaseException(String.format("Missing tables in database : [%s]", String.join(",", missingTable)));
		}
	}
	
	/**
	 * Check if the collections all exists
	 * @throws DataBaseException
	 * @throws SQLException
	 */
	@Conditional(value= ConditonMongo.class)
	public void checkDatabaseAccessMongo() throws DataBaseException, SQLException {
		Set<String> collectionsNames = mongoTemplate.getCollectionNames();
		for (String tableName : lstTable) {
			if (!collectionsNames.contains(tableName)) {
				missingTable.add(tableName);
			}	
		}
		if (!missingTable.isEmpty()) {
			throw new DataBaseException(String.format("Missing tables in database : [%s]", String.join(",", missingTable)));
		}
	}
	
	/**
	 * This method return true if the persistence type is JPA
	 * @return
	 */
	public boolean isJpaDatabase() {
		return "JPA".equals(env.getProperty("fr.insee.queen.application.persistenceType"));
	}
	
	/**
	 * Get the key idSu for paradata
	 * @return
	 */
	public String getKeyParadataIdSu() {
		return getKeyParadataIdSu;
	}
	
	/**
	 * Get the key events for paradata
	 * @return
	 */
	public String getKeyParadataEvents() {
		return getKeyParadataEvents;
	}
}
