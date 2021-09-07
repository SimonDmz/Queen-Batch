package fr.insee.queen.batch;

import java.io.IOException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

import fr.insee.queen.batch.config.ApplicationContext;
import fr.insee.queen.batch.service.DatasetService;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;

public class TestEndToEndJpa extends TestEndToEnd {
	
	private static final Logger logger = LogManager.getLogger(TestEndToEndJpa.class);
	
	AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ApplicationContext.class);
		
	DatasetService datasetService = context.getBean(DatasetService.class);
	
	/**
	 * This ClassRule create a PostgresSQL container that represents our database
	 * for the tests
	 */
	@SuppressWarnings("rawtypes")
	public static PostgreSQLContainer postgreSQLContainer;
	
	/**
	 * This method initialize the data for testing
	 * 
	 * @throws Exception
	 */
	@BeforeEach
	public void initData() throws Exception {
		// Datasource initialization
		PGSimpleDataSource ds = new PGSimpleDataSource();
		ds.setUrl(postgreSQLContainer.getJdbcUrl());
		ds.setUser(postgreSQLContainer.getUsername());
		ds.setPassword(postgreSQLContainer.getPassword());
		DatabaseConnection dbconn = new JdbcConnection(ds.getConnection());
		ResourceAccessor ra = new FileSystemResourceAccessor("src/test/resources/sql");
		Liquibase liquibase = new Liquibase("master.xml", ra, dbconn);
		liquibase.dropAll();
		liquibase.update(new Contexts());
		liquibase.close();
		datasetService.createDataSet();
	}
	
	/**
	 * This method initialize the test by starting the PostgreSQL container. It also
	 * set all the properties correctly from the property file.
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings({ "resource", "rawtypes" })
	@BeforeAll
	public static void initContainer() {
		logger.info("Tests starts");
		postgreSQLContainer = new PostgreSQLContainer("postgres")
				.withDatabaseName("queen").withUsername("queen").withPassword("queen");
		System.setProperty("fr.insee.queen.application.persistenceType", "JPA");
		postgreSQLContainer.start();
		System.setProperty("fr.insee.queen.persistence.database.host", postgreSQLContainer.getContainerIpAddress());
		System.setProperty("fr.insee.queen.persistence.database.port",
				Integer.toString(postgreSQLContainer.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT)));
		System.setProperty("fr.insee.queen.persistence.database.schema", postgreSQLContainer.getDatabaseName());
		System.setProperty("fr.insee.queen.persistence.database.user", postgreSQLContainer.getUsername());
		System.setProperty("fr.insee.queen.persistence.database.password", postgreSQLContainer.getPassword());
		System.setProperty("fr.insee.queen.persistence.database.driver", "org.postgresql.Driver");
		System.setProperty("fr.insee.queen.folder.in", "src/test/resources/in");
		System.setProperty("fr.insee.queen.folder.out", "src/test/resources/out");
		System.setProperty("fr.insee.queen.defaultSchema", "public");
		Configurator.setAllLevels("", Level.INFO);
	}	
	
	@AfterAll
	public static void closeContainer() {
		if(postgreSQLContainer!=null && postgreSQLContainer.isRunning()) {
			postgreSQLContainer.close();
		}
	}

}
