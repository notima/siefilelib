package org.notima.sie.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.notima.sie.tools.SIEFileValidatorMain;

public class TestSIEFileValidator {

	public final String TESTFILENAME1_WO_SUFFIX = "test";
	public final String SUFFIX = ".si";
	public final String ALTERNATION = "validated";
	
	public final String TESTFILENAME1 = TESTFILENAME1_WO_SUFFIX + SUFFIX;
	public final String ALTERNATED_FILENAME = TESTFILENAME1_WO_SUFFIX + "_" + ALTERNATION + SUFFIX;
	
	private String[] args;
	private SIEFileValidatorMain sieFileValidator;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {

		args = new String[] {
				TESTFILENAME1
		};
		
		sieFileValidator = new SIEFileValidatorMain(args);
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreateAlternateName() {
		
		String result = sieFileValidator.createAlternateName(TESTFILENAME1, ALTERNATION);
		System.out.println(result);
		assertEquals(result, ALTERNATED_FILENAME);
		
	}
	
	@Test
	public void testWithOneArgument() {
		
		
//		fail("Not yet implemented"); // TODO
	}

}
