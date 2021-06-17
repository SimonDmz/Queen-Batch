package fr.insee.queen.batch;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import fr.insee.queen.batch.enums.BatchErrorCode;
import fr.insee.queen.batch.exception.ArgumentException;
import fr.insee.queen.batch.utils.PathUtils;

public class UnitTests {

	public Lanceur launcher = new Lanceur();
	
	/* Run Batch */
	
	@SuppressWarnings("static-access")
	@Test
	public void noOptionDefine() throws Exception {
		String[] options= {};
		//BatchErrorCode returnCode = BatchErrorCode.OK;
		Exception exception = assertThrows(ArgumentException.class, () -> {
			BatchErrorCode returnCode = launcher.runBatch(options);
			assertEquals(BatchErrorCode.KO_TECHNICAL_ERROR, returnCode);
	    });
		String expectedMessage = "Batch type is empty, you must choose between [LOADDATA], [DELETEDATA], [EXTRACTDATA] or [EXTRACTDATACOMPLETE]";
		assertEquals(expectedMessage, exception.getMessage());
	}
	
	@SuppressWarnings("static-access")
	@Test
	public void wrongOptionDefine() throws Exception {
		String[] options= {"LOAD"}; 
		Exception exception = assertThrows(ArgumentException.class, () -> {
			BatchErrorCode returnCode = launcher.runBatch(options);
			assertEquals(BatchErrorCode.KO_TECHNICAL_ERROR, returnCode);
	    });
		String expectedMessage = "Wrong batch type, you must choose between [LOADDATA], [DELETEDATA], [EXTRACTDATA] or [EXTRACTDATACOMPLETE]";
		assertEquals(expectedMessage, exception.getMessage());
	}
	
	/* Tests for PathUtils.java */
	
	@Test
	public void directoryShouldExist() throws IOException {
		assertEquals(true, PathUtils.isDirectoryExist("src/test/resources/in"));
	}
	
	@Test
	public void directoryShouldntExist() throws IOException {
		assertEquals(false, PathUtils.isDirectoryExist("src/test/resources/test"));
	}
	
	@Test
	public void directoryShouldContainsExtension() throws IOException {
		assertEquals(true, PathUtils.isDirContainsFileExtension(Path.of("src/test/resources/in/init/scenario1/sample"), "sample.xml"));
	}
	
	@Test
	public void fileShouldExist() throws IOException {
		assertEquals(true, PathUtils.isFileExist("src/test/resources/in/init/scenario1/sample/sample.xml"));
	}
}
