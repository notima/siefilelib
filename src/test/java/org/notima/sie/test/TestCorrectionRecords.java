package org.notima.sie.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.notima.sie.SIEFileType4;
import org.notima.sie.TransRec;
import org.notima.sie.TransRec.TransType;
import org.notima.sie.VerRec;

/**
 * Transactions added to (#RTRANS) and removed from (#BTRANS) a registered voucher,
 * and signatures on vouchers and transactions.
 */
public class TestCorrectionRecords {

	@Test
	public void testFullTransRec() throws Exception {
		TransRec tr = new TransRec("#TRANS 1930 {} -218 20250107 \"Bank\" 2 \"DT\"");
		assertEquals(TransType.TRANS, tr.getType());
		assertEquals(-218.0, tr.getBelopp(), 0.001);
		assertEquals("20250107", org.notima.sie.SIEFile.s_dateFormat.format(tr.getTransDatum()));
		assertEquals("Bank", tr.getTransText());
		assertEquals(2.0, tr.getKvantitet(), 0.001);
		assertEquals("DT", tr.getSign());
	}

	@Test
	public void testEmptyDateBeforeText() throws Exception {
		TransRec tr = new TransRec("#TRANS 1930 {} 100 \"\" \"Text\"");
		assertNull(tr.getTransDatum());
		assertEquals("Text", tr.getTransText());
	}

	@Test
	public void testRemovedTransRec() throws Exception {
		TransRec tr = new TransRec("#BTRANS 1799 {} 218 \"\" \"\" 0 \"Christina Hedengran\"");
		assertTrue(tr.isRemoved());
		assertFalse(tr.isAdded());
		assertEquals("1799", tr.getKontoNr());
		assertEquals(218.0, tr.getBelopp(), 0.001);
		assertEquals("Christina Hedengran", tr.getSign());
	}

	@Test
	public void testAddedTransRec() throws Exception {
		TransRec tr = new TransRec("#RTRANS 6250 {1 \"10\"} 218 \"\" \"\" 0 \"CH\"");
		assertTrue(tr.isAdded());
		assertEquals("6250", tr.getKontoNr());
		assertEquals(1, tr.getObjektLista().size());
		assertEquals("CH", tr.getSign());
	}

	@Test
	public void testEscapedQuoteInText() throws Exception {
		TransRec tr = new TransRec("#TRANS 1930 {} 100 \"\" \"A \\\"quoted\\\" word\" 0 \"DT\"");
		assertEquals("A \\\"quoted\\\" word", tr.getTransText());
		assertEquals("DT", tr.getSign());
	}

	@Test
	public void testVoucherFromFile() throws Exception {
		VerRec ver = parse(
				"#VER A 2 20250107 \"Wh ankeborg\" 20250219 \"Christina Hedengran\"",
				"{",
				"#TRANS 1930 {} -218 \"\" \"\" 0",
				"#BTRANS 1799 {} 218 \"\" \"\" 0 \"Christina Hedengran\"",
				"#RTRANS 6250 {} 218 \"\" \"\" 0 \"Christina Hedengran\"",
				"#TRANS 6250 {} 218 \"\" \"\" 0",
				"}");
		assertEquals("Wh ankeborg", ver.getVerText());
		assertEquals("Christina Hedengran", ver.getSign());
		List<TransRec> trans = ver.getTransList();
		assertEquals("the #TRANS copy after #RTRANS is the same transaction", 3, trans.size());
		assertEquals(TransType.TRANS, trans.get(0).getType());
		assertEquals(TransType.BTRANS, trans.get(1).getType());
		assertEquals(TransType.RTRANS, trans.get(2).getType());
		assertEquals("the removed transaction doesn't count", 0.0, ver.getBalance(), 0.001);
		assertEquals(218.0, ver.getTotalDebet(), 0.001);
	}

	@Test
	public void testWriteAndReadBack() throws Exception {
		VerRec ver = new VerRec();
		ver.setSerie("A");
		ver.setVerNr("2");
		ver.setVerDatum(org.notima.sie.SIEFile.s_dateFormat.parse("20250107"));
		ver.setVerText("Hyra");
		ver.setSign("DT");
		ver.addTransRec(new TransRec("1930", -218, null, "Bank"));
		TransRec removed = new TransRec("1799", 218, null, null);
		removed.setType(TransType.BTRANS);
		removed.setSign("AA");
		ver.addTransRec(removed);
		TransRec added = new TransRec("6250", 218, null, null);
		added.setType(TransType.RTRANS);
		added.setSign("AA");
		ver.addTransRec(added);

		String sie = ver.toSieString();
		assertTrue(sie, sie.startsWith("#VER \"A\" \"2\" 20250107 \"Hyra\" \"\" \"DT\"\r\n"));
		assertTrue(sie, sie.contains("    #BTRANS 1799 {} 218 \"\" \"\" 0 \"AA\"\r\n"));
		assertTrue(sie, sie.contains("    #RTRANS 6250 {} 218 \"\" \"\" 0 \"AA\"\r\n    #TRANS 6250 {} 218 \"\" \"\"\r\n"));

		VerRec back = parse(sie.split("\r\n"));
		assertEquals("DT", back.getSign());
		assertEquals(3, back.getTransList().size());
		assertTrue(back.getTransList().get(1).isRemoved());
		assertTrue(back.getTransList().get(2).isAdded());
		assertEquals("AA", back.getTransList().get(2).getSign());
		assertEquals(0.0, back.getBalance(), 0.001);
	}

	@Test
	public void testSampleFileCorrections() throws Exception {
		SIEFileType4 sieFile = new SIEFileType4("sample/AnkeborgAB2025.se");
		sieFile.readFile();
		int removed = 0, added = 0;
		for (VerRec ver : sieFile.getVerRecords()) {
			for (TransRec tr : ver.getTransList()) {
				assertTrue("every transaction has an account", tr.getKontoNr() != null);
				if (tr.isRemoved()) removed++;
				if (tr.isAdded()) added++;
			}
			assertEquals("voucher " + ver.getSerie() + ver.getVerNr() + " balances", 0.0, ver.getBalance(), 0.005);
		}
		assertTrue(removed > 0);
		assertTrue(added > 0);
	}

	/** Parses one voucher the way {@link SIEFileType4} reads it from a file. */
	private static VerRec parse(String... lines) throws Exception {
		java.io.File f = java.io.File.createTempFile("ver", ".se");
		f.deleteOnExit();
		StringBuilder s = new StringBuilder("#FLAGGA 0\r\n#SIETYP 4\r\n");
		for (String l : Arrays.asList(lines)) s.append(l).append("\r\n");
		java.nio.file.Files.write(f.toPath(), s.toString().getBytes("IBM437"));
		SIEFileType4 sieFile = new SIEFileType4(f.getPath());
		sieFile.readFile();
		assertEquals(1, sieFile.getVerRecords().size());
		return sieFile.getVerRecords().get(0);
	}
}
