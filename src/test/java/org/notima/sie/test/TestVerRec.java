package org.notima.sie.test;

import junit.framework.*;
import org.notima.sie.VerRec;

import java.util.*;

import org.notima.sie.*;

public class TestVerRec extends TestCase {

	public void testVerRecString() {

		try {
			VerRec verRec = new VerRec("#VER \"\" \"\" 20080725 \"L\u00d6nek\u00d6rning nr: 12\" 20080731");
			assertEquals("", verRec.getSerie());
			assertEquals("", verRec.getVerNr());
			assertEquals("L\u00d6nek\u00d6rning nr: 12", verRec.getVerText());
			
			Calendar verDatum = new GregorianCalendar();
			verDatum.setTime(verRec.getVerDatum());
			assertEquals(2008, verDatum.get(Calendar.YEAR));
			assertEquals(6, verDatum.get(Calendar.MONTH));
			assertEquals(25, verDatum.get(Calendar.DAY_OF_MONTH));
			
			Calendar regDatum = new GregorianCalendar();
			regDatum.setTime(verRec.getRegDatum());
			assertEquals(2008, regDatum.get(Calendar.YEAR));
			assertEquals(6, regDatum.get(Calendar.MONTH));
			assertEquals(31, regDatum.get(Calendar.DAY_OF_MONTH));
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
	}

}
