package org.notima.sie.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.notima.sie.SIEFileType4;
import org.notima.sie.VerRec;

import junit.framework.TestCase;

public class TestSampleFile extends TestCase {

	private static final String SAMPLE_FILE = "sample/AnkeborgAB2025.se";

	@Test
	public void testReadFile() throws Exception {
		SIEFileType4 sieFile = new SIEFileType4(SAMPLE_FILE);
		sieFile.readFile();

		assertEquals("556000-0001", sieFile.getOrgNr());
		assertEquals("Ankeborg AB", sieFile.getFNamn());

		assertNotNull(sieFile.getAccountMap());
		assertFalse("Account map should not be empty", sieFile.getAccountMap().isEmpty());

		List<VerRec> verRecs = sieFile.getVerRecords();
		assertNotNull(verRecs);
		assertFalse("Verification records should not be empty", verRecs.isEmpty());
	}

	@Test
	public void testBalanceRecords() throws Exception {
		SIEFileType4 sieFile = new SIEFileType4(SAMPLE_FILE);
		sieFile.readFile();

		assertNotNull(sieFile.getBalanceMap());
		assertFalse("Balance map should not be empty", sieFile.getBalanceMap().isEmpty());
	}

}
