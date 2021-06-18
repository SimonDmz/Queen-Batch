package fr.insee.queen.batch;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import fr.insee.queen.batch.config.ApplicationContext;
import fr.insee.queen.batch.enums.BatchErrorCode;
import fr.insee.queen.batch.enums.BatchOption;
import fr.insee.queen.batch.exception.BatchException;
import fr.insee.queen.batch.exception.ValidateException;
import fr.insee.queen.batch.service.ExtractionService;
import fr.insee.queen.batch.service.LauncherService;
import fr.insee.queen.batch.utils.PathUtils;
@TestMethodOrder(OrderAnnotation.class)
public abstract class TestEndToEnd {
	
	AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ApplicationContext.class);
	LauncherService launcherService = context.getBean(LauncherService.class);
	ExtractionService extractionService = context.getBean(ExtractionService.class);

	@BeforeAll
	public static void copyFiles() throws IOException {
		File initInDir = new File("src/test/resources/in/init");
		File testScenarioInDir = new File("src/test/resources/in/testScenarios");
		if (!testScenarioInDir.exists()) {
			testScenarioInDir.mkdir();
		}
		FileUtils.copyDirectory(initInDir, testScenarioInDir);
		List<Path> subfolder = Files.walk(new File("src/test/resources/in/testScenarios").toPath(), 1)
	            .filter(Files::isDirectory)
	            .collect(Collectors.toList());
		subfolder.remove(0);
		subfolder.parallelStream().forEach(path -> 
			new File("src/test/resources/in/testScenarios/" + path.getFileName().toString() + "/processing").mkdir());
		File outDir = new File("src/test/resources/out");
		if (!outDir.exists()) {
			outDir.mkdir();
		}
		File sampleOutDir = new File("src/test/resources/out/sample");
		if (!sampleOutDir.exists()) {
			sampleOutDir.mkdir();
		}
		File nomenclaturesOutDir = new File("src/test/resources/out/nomenclatures");
		if (!nomenclaturesOutDir.exists()) {
			nomenclaturesOutDir.mkdir();
		}
		File jsonNomenclaturesOutDir = new File("src/test/resources/out/nomenclatures/json");
		if (!jsonNomenclaturesOutDir.exists()) {
			jsonNomenclaturesOutDir.mkdir();
		}
		File extractdataOutDir = new File("src/test/resources/out/extractdata");
		if (!extractdataOutDir.exists()) {
			extractdataOutDir.mkdir();
		}
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
	
	/**
	 * Scenario 1 : xml nomenclature not valid
	 * @throws ValidateException 
	 * @throws IOException 
	 * @throws BatchException 
	 * @throws XMLStreamException 
	 */
	@Test
	@Order(1)
	public void testScenario1() throws Exception {
		String in = "src/test/resources/in/testScenarios/scenario1";
		String out = "src/test/resources/out";
		try {
			launcherService.validateTreatCleanBatch(BatchOption.LOADDATA, in , out);
		  }catch(ValidateException ve){
			  assertEquals(true, ve.getMessage().contains("Error during validation"));
			  launcherService.cleanAndReset(Model.NOMENCLATURE, in, out, BatchErrorCode.KO_FONCTIONAL_ERROR);
			  launcherService.cleanAndReset(Model.SAMPLE, in, out, BatchErrorCode.KO_FONCTIONAL_ERROR);
			  assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/nomenclatures"), "nomenclatures", "error.xml"));
			  assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/nomenclatures/json"), "cities2019", "error.xml"));
			  assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/sample"), "sample", "error.xml"));
		  }
	}

	/**
	 * Scenario 2 : xml nomenclature ok, Json is missing
	 */
	
	@Test
	@Order(2)
	public void testScenario2() throws Exception {
		String in = "src/test/resources/in/testScenarios/scenario2";
		String out = "src/test/resources/out";
		try {
			launcherService.validateTreatCleanBatch(BatchOption.LOADDATA, in, out);		   
		}catch(BatchException be){
			assertEquals(true, be.getMessage().contains("json doesn't exist"));
			launcherService.cleanAndReset(Model.NOMENCLATURE, in, out, BatchErrorCode.KO_FONCTIONAL_ERROR);
			launcherService.cleanAndReset(Model.SAMPLE, in, out, BatchErrorCode.KO_FONCTIONAL_ERROR);
			assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/nomenclatures"), "nomenclatures", "error.xml"));
			assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/nomenclatures/json"), "cities2019", "error.xml"));
			assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/sample"), "sample", "error.xml"));
		}
		
	}

	/**
	 * Scenario 3 : xml nomenclature ok, xml sample not valid
	 */
	@Test
	@Order(3)
	public void testScenario3() throws Exception {
		String in = "src/test/resources/in/testScenarios/scenario3";
		String out = "src/test/resources/out";
		try {
			launcherService.validateTreatCleanBatch(BatchOption.LOADDATA, in, out);
		  }catch(ValidateException ve){
			  assertEquals(true, ve.getMessage().contains("Error during validation"));
			  launcherService.cleanAndReset(Model.SAMPLE, in, out, BatchErrorCode.KO_FONCTIONAL_ERROR);
			  assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/nomenclatures"), "nomenclatures", "done.xml"));
			  assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/nomenclatures/json"), "cities2019", "done.xml"));
			  assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/sample"), "sample", "error.xml"));
		  }
	}

	/**
	 * Scenario 4 : xml files ok, questionnaire missing in db
	 */
	@Test
	@Order(4)
	public void testScenario4() throws Exception {
		String in = "src/test/resources/in/testScenarios/scenario4";
		String out = "src/test/resources/out";
		try {
			launcherService.validateTreatCleanBatch(BatchOption.LOADDATA, in, out);
		  }catch(ValidateException ve){
			  assertEquals(true, ve.getMessage().contains("does not exist in database"));
			  launcherService.cleanAndReset(Model.SAMPLE, in, out, BatchErrorCode.KO_FONCTIONAL_ERROR);
			  assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/nomenclatures"), "nomenclatures", "done.xml"));
			  assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/nomenclatures/json"), "cities2019", "done.xml"));
			  assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/sample"), "sample", "error.xml"));
		  }
	}

	/**
	 * Scenario 5 : xml files ok, campaign already exist
	 */
	@Test
	@Order(5)
	public void testScenario5() throws Exception {
		String in = "src/test/resources/in/testScenarios/scenario5";
		String out = "src/test/resources/out";
		assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING, launcherService.validateTreatCleanBatch(BatchOption.LOADDATA, in, out));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/nomenclatures"),
				"nomenclatures", "done.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/nomenclatures/json"),
				"cities2019", "done.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/sample"),
				"sample", "warning.xml"));
	}

	/**
	 * Scenario 6 : xml files ok, campaign ok, at least 1 survey-unit already
	 * exists
	 */
	@Test
	@Order(6)
	public void testScenario6() throws Exception {
		String in = "src/test/resources/in/testScenarios/scenario6";
		String out = "src/test/resources/out";
		assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING, launcherService.validateTreatCleanBatch(BatchOption.LOADDATA, in, out));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/nomenclatures"),
				"nomenclatures", "done.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/nomenclatures/json"),
				"cities2019", "done.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/sample"),
				"sample", "warning.xml"));
	}
	
	/**
	 * Scenario 7 : no xml nomenclature, xml sample ok, campaigns ok, survey-units ok
	 */
	@Test
	@Order(7)
	public void testScenario7() throws Exception {
		String in = "src/test/resources/in/testScenarios/scenario7";
		String out = "src/test/resources/out";
		assertEquals(BatchErrorCode.OK, launcherService.validateTreatCleanBatch(BatchOption.LOADDATA, in, out));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/sample"),
				"sample", "done.xml"));
	}
	
	/**
	 * Scenario 8 : no xml sample, xml nomenclature ok, nomenclatures ok
	 */
	@Test
	@Order(8)
	public void testScenario8() throws Exception {
		String in = "src/test/resources/in/testScenarios/scenario8";
		String out = "src/test/resources/out";
		assertEquals(BatchErrorCode.OK, launcherService.validateTreatCleanBatch(BatchOption.LOADDATA, in, out));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/nomenclatures"),
				"nomenclatures", "done.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/nomenclatures/json"),
				"cities2019", "done.xml"));
	}
	
	/**
	 * Scenario 9 : xml files ok, Nomenclatures ok, campaigns ok, survey-units ok
	 */
	@Test
	@Order(9)
	public void testScenario9() throws Exception {
		String in = "src/test/resources/in/testScenarios/scenario9";
		String out = "src/test/resources/out";
		assertEquals(BatchErrorCode.OK, launcherService.validateTreatCleanBatch(BatchOption.LOADDATA, in, out));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/nomenclatures"),
				"nomenclatures", "done.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/nomenclatures/json"),
				"cities2019", "done.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/sample"),
				"sample", "done.xml"));
	}
	
	/**
	 * Scenario 10 : xml nomenclature ok, xml sample/data not valid
	 */
	@Test
	@Order(10)
	public void testScenario10() throws Exception {
		String in = "src/test/resources/in/testScenarios/scenario10";
		String out = "src/test/resources/out";
		try {
			launcherService.validateTreatCleanBatch(BatchOption.LOADDATA, in, out);
		    fail("Should throw ValidateException");
		  }catch(ValidateException ve){
			  assertEquals(true, ve.getMessage().contains("Error during validation"));
			  launcherService.cleanAndReset(Model.SAMPLE, in, out, BatchErrorCode.KO_FONCTIONAL_ERROR);
			  assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/nomenclatures"), "nomenclatures", "done.xml"));
			  assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/nomenclatures/json"), "cities2019", "done.xml"));
			  assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/sample"), "sample", "error.xml"));
		  }
	}
	
	/**
	 * Scenario 11 : Extract data 
	 */
	@Test
	@Order(11)
	public void testScenario11() throws Exception {
		String out = "src/test/resources/out";
		assertEquals(BatchErrorCode.OK, extractionService.extract(BatchOption.EXTRACTDATA, out));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/extractdata/simpsons2020x00/differential/data"),
				"data", "xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/extractdata/simpsons2020x00/differential/paradata"),
				"paradata", "json"));
	}
	
	/**
	 * Scenario 12 : Extract data failed 
	 */
	@Test
	@Order(12)
	public void testScenario12() throws Exception {
		String out = "src/test/resources/out";
		assertEquals(BatchErrorCode.KO_TECHNICAL_ERROR, extractionService.extract(BatchOption.EXTRACTDATACOMPLETE, out+"test"));  
	}
	
	/**
	 * Scenario 13 : xml with wrong encoding
	 * @throws ValidateException 
	 * @throws IOException 
	 * @throws BatchException 
	 * @throws XMLStreamException 
	 */
	@Test
	@Order(13)
	public void testScenario13() throws Exception {
		String in = "src/test/resources/in/testScenarios/scenario13";
		String out = "src/test/resources/out";
		try {
			launcherService.validateTreatCleanBatch(BatchOption.LOADDATA, in, out);
		    fail("Should throw ValidateException");
		  }catch(ValidateException ve){
			  assertEquals(true, ve.getMessage().contains("Error during validation"));
			  launcherService.cleanAndReset(Model.NOMENCLATURE, in, out, BatchErrorCode.KO_FONCTIONAL_ERROR);
			  launcherService.cleanAndReset(Model.SAMPLE, in, out, BatchErrorCode.KO_FONCTIONAL_ERROR);
			  assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/nomenclatures"), "nomenclatures", "error.xml"));
			  assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/nomenclatures/json"), "cities2019", "error.xml"));
			  assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/sample"), "sample", "error.xml"));
		  }
	}
	
	/**
	 * Scenario 14 : xml with wrong tag
	 * @throws ValidateException 
	 * @throws IOException 
	 * @throws BatchException 
	 * @throws XMLStreamException 
	 */
	@Test
	@Order(14)
	public void testScenario14() throws Exception {
		String in = "src/test/resources/in/testScenarios/scenario14";
		String out = "src/test/resources/out";
		try {
			launcherService.validateTreatCleanBatch(BatchOption.LOADDATA, in , out);
		    fail("Should throw ValidateException");
		  }catch(ValidateException ve){
			  assertEquals(true, ve.getMessage().contains("Error during validation"));
			  launcherService.cleanAndReset(Model.SAMPLE, in, out, BatchErrorCode.KO_FONCTIONAL_ERROR);
			  assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/nomenclatures"), "nomenclatures", "done.xml"));
			  assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/nomenclatures/json"), "cities2019", "done.xml"));
			  assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/sample"), "sample", "error.xml"));
		  }
	}
	
	/**
	 * Scenario 15 : xml files ok, delete survey-units ok
	 */
	@Test
	@Order(15)
	public void testScenario15() throws Exception {
		String in = "src/test/resources/in/testScenarios/scenario15";
		String out = "src/test/resources/out";
		assertEquals(BatchErrorCode.OK, launcherService.validateTreatCleanBatch(BatchOption.DELETEDATA, in, out));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/sample"),
				"sample", "done.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/extractdata/simpsons2020x00/differential/data"),
				"data", "xml"));
	}
	
	/**
	 * Scenario 16 : xml files ok, delete survey-unit, 1 su not exists
	 */
	@Test
	@Order(16)
	public void testScenario16() throws Exception {
		String in = "src/test/resources/in/testScenarios/scenario16";
		String out = "src/test/resources/out";
		assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING, launcherService.validateTreatCleanBatch(BatchOption.DELETEDATA, in, out));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/sample"),
				"sample", "warning.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/extractdata/simpsons2020x00/differential/data"),
				"data", "xml"));
	}
	
	/**
	 * Scenario 17 : xml files ok, delete campaign ok
	 */
	@Test
	@Order(17)
	public void testScenario17() throws Exception {
		String in = "src/test/resources/in/testScenarios/scenario17";
		String out = "src/test/resources/out";
		assertEquals(BatchErrorCode.OK, launcherService.validateTreatCleanBatch(BatchOption.DELETEDATA, in, out));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/sample"),
				"sample", "done.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/extractdata/simpsons2020x00/complete/data"),
				"data", "xml"));
	}
	
	/**
	 * Scenario 18 : xml files ok, Nomenclatures ok, campaigns ok, survey-units with empty personalization
	 */
	@Test
	@Order(18)
	public void testScenario18() throws Exception {
		String in = "src/test/resources/in/testScenarios/scenario18";
		String out = "src/test/resources/out";
		assertEquals(BatchErrorCode.OK, launcherService.validateTreatCleanBatch(BatchOption.LOADDATA, in, out));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/nomenclatures"),
				"nomenclatures", "done.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/nomenclatures/json"),
				"cities2019", "done.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/sample"),
				"sample", "done.xml"));
	}
	
	/**
	 * Scenario 19 : extract data differential
	 */
	@Test
	@Order(19)
	public void testScenario19() throws Exception {
		String out = "src/test/resources/out";
		assertEquals(BatchErrorCode.OK, extractionService.extract(BatchOption.EXTRACTDATACOMPLETE, out));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/extractdata/simpsons2020x00/complete/data"),
				"data", "xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/extractdata/simpsons2020x00/complete/paradata"),
				"paradata", "json"));
	}
}
