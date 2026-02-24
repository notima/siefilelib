package org.notima.sie.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.notima.sie.AccountRec;
import org.notima.sie.BalanceRec;
import org.notima.sie.ResRec;
import org.notima.sie.TransRec;
import org.notima.sie.VerRec;

public class TestRecordParsing {

	// AccountRec

	@Test
	public void testAccountRecParsing() throws Exception {
		AccountRec rec = new AccountRec("#KONTO 1930 \"Kassa\"");
		assertEquals("1930", rec.getAccountNo());
		assertEquals("Kassa", rec.getAccountName());
	}

	@Test
	public void testAccountRecToSieString() throws Exception {
		AccountRec rec = new AccountRec("#KONTO 1930 \"Kassa\"");
		assertEquals("#KONTO 1930 \"Kassa\"\r\n", rec.toSieString());
	}

	@Test
	public void testAccountRecConstructor() {
		AccountRec rec = new AccountRec("1610", "Kundfordringar");
		assertEquals("1610", rec.getAccountNo());
		assertEquals("Kundfordringar", rec.getAccountName());
	}

	// BalanceRec

	@Test
	public void testBalanceRecIncoming() throws Exception {
		BalanceRec rec = new BalanceRec("#IB 0 1930 1500.00");
		assertTrue(rec.isInBalance());
		assertEquals(0, rec.getYearOffset());
		assertEquals("1930", rec.getAccountNo());
		assertEquals(1500.0, rec.getBalance(), 0.001);
	}

	@Test
	public void testBalanceRecOutgoing() throws Exception {
		BalanceRec rec = new BalanceRec("#UB 0 1930 2500.00");
		assertFalse(rec.isInBalance());
		assertEquals("1930", rec.getAccountNo());
		assertEquals(2500.0, rec.getBalance(), 0.001);
	}

	@Test
	public void testBalanceRecNegativeAmount() throws Exception {
		BalanceRec rec = new BalanceRec("#IB -1 2440 -5000.00");
		assertTrue(rec.isInBalance());
		assertEquals(-1, rec.getYearOffset());
		assertEquals("2440", rec.getAccountNo());
		assertEquals(-5000.0, rec.getBalance(), 0.001);
	}

	@Test
	public void testBalanceRecRoundTrip() throws Exception {
		BalanceRec original = new BalanceRec("#IB 0 1930 1500");
		BalanceRec reparsed = new BalanceRec(original.toSieString().trim());
		assertEquals(original.getAccountNo(), reparsed.getAccountNo());
		assertEquals(original.getBalance(), reparsed.getBalance(), 0.001);
		assertEquals(original.isInBalance(), reparsed.isInBalance());
		assertEquals(original.getYearOffset(), reparsed.getYearOffset());
	}

	// ResRec

	@Test
	public void testResRecParsing() throws Exception {
		ResRec rec = new ResRec("#RES 0 3000 -50000.00");
		assertEquals(0, rec.getYearOffset());
		assertEquals("3000", rec.getAccountNo());
		assertEquals(-50000.0, rec.getBalance(), 0.001);
	}

	@Test
	public void testResRecAcctString() throws Exception {
		ResRec rec = new ResRec("#RES -1 3000 -50000.00");
		assertEquals("3000_1", rec.getAcctString());
	}

	@Test
	public void testResRecConstructor() {
		ResRec rec = new ResRec("3000", -50000.0);
		assertEquals("3000", rec.getAccountNo());
		assertEquals(-50000.0, rec.getBalance(), 0.001);
	}

	// TransRec

	@Test
	public void testTransRecParsing() throws Exception {
		// Format without explicit date field: text is picked up as-is
		TransRec tr = new TransRec("#TRANS 1930 {} 1000.00 \"Test\"");
		assertEquals("1930", tr.getKontoNr());
		assertEquals(1000.0, tr.getBelopp(), 0.001);
		assertEquals("Test", tr.getTransText());
	}

	@Test
	public void testTransRecNegativeAmount() throws Exception {
		TransRec tr = new TransRec("#TRANS 2440 {} -1000.00");
		assertEquals("2440", tr.getKontoNr());
		assertEquals(-1000.0, tr.getBelopp(), 0.001);
	}

	@Test
	public void testTransRecConstructor() {
		TransRec tr = new TransRec("1930", 500.0, null, "Payment");
		assertEquals("1930", tr.getKontoNr());
		assertEquals(500.0, tr.getBelopp(), 0.001);
		assertEquals("Payment", tr.getTransText());
	}

	// Decimal amounts

	@Test
	public void testBalanceRecDecimalAmount() throws Exception {
		BalanceRec rec = new BalanceRec("#IB 0 1930 1500.50");
		assertEquals(1500.50, rec.getBalance(), 0.001);
	}

	@Test
	public void testBalanceRecNegativeDecimalAmount() throws Exception {
		BalanceRec rec = new BalanceRec("#IB 0 2440 -2750.25");
		assertEquals(-2750.25, rec.getBalance(), 0.001);
	}

	@Test
	public void testTransRecDecimalAmount() throws Exception {
		TransRec tr = new TransRec("#TRANS 1930 {} 1250.75");
		assertEquals(1250.75, tr.getBelopp(), 0.001);
	}

	@Test
	public void testTransRecNegativeDecimalAmount() throws Exception {
		TransRec tr = new TransRec("#TRANS 2440 {} -999.99");
		assertEquals(-999.99, tr.getBelopp(), 0.001);
	}

	@Test
	public void testAmountFormatDecimalRoundTrip() throws Exception {
		// Verify that the amount formatter preserves decimal values correctly
		BalanceRec original = new BalanceRec("#IB 0 1930 1234.56");
		BalanceRec reparsed = new BalanceRec(original.toSieString().trim());
		assertEquals(original.getBalance(), reparsed.getBalance(), 0.001);
	}

	@Test
	public void testNegativeAmountFormatRoundTrip() throws Exception {
		// Verify that negative amounts survive a format/parse round-trip
		BalanceRec original = new BalanceRec("#IB 0 2440 -5678.90");
		BalanceRec reparsed = new BalanceRec(original.toSieString().trim());
		assertEquals(original.getBalance(), reparsed.getBalance(), 0.001);
		assertTrue(original.toSieString().contains("-5678.9"));
	}

	// VerRec balance tracking

	@Test
	public void testVerRecBalance() {
		VerRec ver = new VerRec();
		TransRec debit = new TransRec("1930", 1000.0, null, "Debit");
		TransRec credit = new TransRec("2440", -1000.0, null, "Credit");
		ver.addTransRec(debit);
		ver.addTransRec(credit);
		assertEquals(0.0, ver.getBalance(), 0.001);
		assertEquals(1000.0, ver.getTotalDebet(), 0.001);
		assertEquals(1000.0, ver.getTotalCredit(), 0.001);
	}

	@Test
	public void testVerRecTextValidation() {
		VerRec ver = new VerRec();
		ver.setVerText("Normal text");
		assertEquals("Normal text", ver.getVerText());
	}

	@Test
	public void testVerRecTextStripsNewlines() {
		VerRec ver = new VerRec();
		ver.setVerText("Line1\nLine2\r\nLine3");
		assertEquals("Line1Line2Line3", ver.getVerText());
	}
}
