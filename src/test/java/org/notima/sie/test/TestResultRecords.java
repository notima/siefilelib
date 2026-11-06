package org.notima.sie.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.junit.Test;
import org.notima.sie.ResRec;
import org.notima.sie.SIEFileType4;

/**
 * Result records (#RES) of this year and the previous year are kept apart.
 */
public class TestResultRecords {

	private static final String SIE =
			"#FLAGGA 0\r\n" +
			"#FORMAT PC8\r\n" +
			"#SIETYP 4\r\n" +
			"#PROGRAM \"Test\" 1.0\r\n" +
			"#GEN 20250801\r\n" +
			"#FNAMN \"Test AB\"\r\n" +
			"#ORGNR 556000-0000\r\n" +
			"#RAR 0 20240701 20250630\r\n" +
			"#RAR -1 20230701 20240630\r\n" +
			"#KONTO 3010 \"Intakter\"\r\n" +
			"#KONTO 5010 \"Lokalhyra\"\r\n" +
			"#RES 0 3010 -1000.00\r\n" +
			"#RES -1 3010 -800.00\r\n" +
			"#RES 0 5010 400.00\r\n" +
			"#RES -1 5010 300.00\r\n";

	@Test
	public void testBothYearsAreKept() throws Exception {
		SIEFileType4 sieFile = read(SIE);

		List<ResRec> thisYear = sieFile.getResultRecords(0);
		assertEquals(2, thisYear.size());
		assertEquals("3010", thisYear.get(0).getAccountNo());
		assertEquals(-1000.0, thisYear.get(0).getBalance(), 0.001);
		assertEquals(400.0, thisYear.get(1).getBalance(), 0.001);

		List<ResRec> previousYear = sieFile.getResultRecords(-1);
		assertEquals(2, previousYear.size());
		assertEquals(-800.0, previousYear.get(0).getBalance(), 0.001);
		assertEquals(300.0, previousYear.get(1).getBalance(), 0.001);
	}

	@Test
	public void testBothYearsAreWritten() throws Exception {
		String written = read(SIE).toSieString();
		assertTrue(written, written.contains("#RES 0 3010 -1000"));
		assertTrue(written, written.contains("#RES -1 3010 -800"));
		assertTrue(written, written.contains("#RES 0 5010 400"));
		assertTrue(written, written.contains("#RES -1 5010 300"));
	}

	private static SIEFileType4 read(String content) throws Exception {
		File f = File.createTempFile("res", ".se");
		f.deleteOnExit();
		Files.write(f.toPath(), content.getBytes(StandardCharsets.US_ASCII));
		SIEFileType4 sieFile = new SIEFileType4(f.getAbsolutePath());
		sieFile.readFile();
		return sieFile;
	}

}
