package org.notima.sie.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.notima.sie.SIEFile;

public class TestParseOrgnr {

	@Test
	public void testOrgNrWithDash() throws Exception {
		SIEFile sf = new SIEFile();
		assertEquals("555555-5555", sf.parseOrgNr("#ORGNR 555555-5555"));
	}

	@Test
	public void testOrgNrWithoutDash() throws Exception {
		SIEFile sf = new SIEFile();
		assertEquals("5555555555", sf.parseOrgNr("#ORGNR 5555555555"));
	}

	@Test
	public void testOrgNrQuotedWithTrailingData() throws Exception {
		SIEFile sf = new SIEFile();
		assertEquals("555555-5555", sf.parseOrgNr("#ORGNR   \"555555-5555\" 1"));
	}
}
