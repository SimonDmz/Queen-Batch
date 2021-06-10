package fr.insee.queen.batch;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.testcontainers.containers.MongoDBContainer;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import fr.insee.queen.batch.config.ApplicationContext;
import fr.insee.queen.batch.service.DatasetService;

public class TestEndToEndMongo extends TestEndToEnd {
	
	private static final Logger logger = LogManager.getLogger(TestEndToEndMongo.class);

	AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ApplicationContext.class);
	
	DatasetService datasetService = context.getBean(DatasetService.class);

	/**
	 * This ClassRule create a PostgresSQL container that represents our database
	 * for the tests
	 */
	@ClassRule
	public static MongoDBContainer mongoDBContainer;
	
	@BeforeEach
	public void initData() throws Exception { 
		datasetService.createDataSet();
	}
	
	@AfterEach
	public void dropData() throws Exception {
		ConnectionString connectionString = new ConnectionString(String.format("mongodb://%s:%s/%s", mongoDBContainer.getContainerIpAddress(),
				mongoDBContainer.getFirstMappedPort(), "test"));
		MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
		.applyConnectionString(connectionString)
		.build();
		MongoClient mongo = MongoClients.create(mongoClientSettings);
		MongoDatabase database = mongo.getDatabase("test");
		database.drop();
	}
	
	/**
	 * This method initialize the test by starting the PostgreSQL container. It also
	 * set all the properties correctly from the property file.
	 * 
	 * @throws IOException
	 */
	@BeforeAll
	public static void initContainer() throws IOException {
		logger.info("Tests starts");
		mongoDBContainer = new MongoDBContainer("mongo:4.0.10");
		System.setProperty("fr.insee.queen.application.persistenceType", "MONGODB");
		mongoDBContainer.start();
		System.setProperty("fr.insee.queen.persistence.database.host", mongoDBContainer.getContainerIpAddress());
		System.setProperty("fr.insee.queen.persistence.database.port",
				Integer.toString(mongoDBContainer.getFirstMappedPort()));
		System.setProperty("fr.insee.queen.persistence.database.schema", "test");
		System.setProperty("fr.insee.queen.persistence.database.user", "queen");
		System.setProperty("fr.insee.queen.persistence.database.password", "queen");
		System.setProperty("fr.insee.queen.folder.in", "src/test/resources/in");
		System.setProperty("fr.insee.queen.folder.out", "src/test/resources/out");
		System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
	}
	
	@AfterEach
	public void cleanOutFolder() {
		purgeDirectory(new File("src/test/resources/out/nomenclatures/json"));
		purgeDirectory(new File("src/test/resources/out/nomenclatures"));
		purgeDirectory(new File("src/test/resources/out/sample"));
		purgeDirectory(new File("src/test/resources/out/extractdata"));		
		purgeDirectory(new File("src/test/resources/out/extractdata/simpsons2020x00/complete/data"));		
		purgeDirectory(new File("src/test/resources/out/extractdata/simpsons2020x00/complete/paradata"));
		purgeDirectory(new File("src/test/resources/out/extractdata/simpsons2022x00/complete/data"));		
		purgeDirectory(new File("src/test/resources/out/extractdata/simpsons2022x00/complete/paradata"));
		purgeDirectory(new File("src/test/resources/out/extractdata/simpsons2023x00/complete/data"));		
		purgeDirectory(new File("src/test/resources/out/extractdata/simpsons2023x00/complete/paradata"));
		purgeDirectory(new File("src/test/resources/out/extractdata/simpsons2024x00/complete/data"));		
		purgeDirectory(new File("src/test/resources/out/extractdata/simpsons2024x00/complete/paradata"));
		purgeDirectory(new File("src/test/resources/out/extractdata/simpsons2020x00/differential/data"));		
		purgeDirectory(new File("src/test/resources/out/extractdata/simpsons2020x00/differential/paradata"));
		purgeDirectory(new File("src/test/resources/out/extractdata/simpsons2022x00/differential/data"));		
		purgeDirectory(new File("src/test/resources/out/extractdata/simpsons2022x00/differential/paradata"));
		purgeDirectory(new File("src/test/resources/out/extractdata/simpsons2023x00/differential/data"));		
		purgeDirectory(new File("src/test/resources/out/extractdata/simpsons2023x00/differential/paradata"));
		purgeDirectory(new File("src/test/resources/out/extractdata/simpsons2024x00/differential/data"));		
		purgeDirectory(new File("src/test/resources/out/extractdata/simpsons2024x00/differential/paradata"));
	}
	
	void purgeDirectory(File dir) {
		if(dir.exists()) {
			for (File file: dir.listFiles()) {
		        if (file.exists() && file.isFile()) {
		        	file.delete();
		        }
		    }
		}
	}
	
	@AfterAll
	public static void closeContainer() {
		if(mongoDBContainer!=null && mongoDBContainer.isRunning()) {
			mongoDBContainer.close();
		}
	}

}
