package org.notima.sie.test;

import org.junit.Test;
import org.notima.sie.SIEFile;

public class TestParseOrgnr {

	public static String[] orgNrAlts = new String[] {
		"#ORGNR 555555-5555",
		"#ORGNR 5555555555",
		"#ORGNR   \"555555-5555\" 1"
	};
	
	@Test
	public void TestParseOrgNr() {

		SIEFile sf = new SIEFile();
		String orgNr;
		for (String o : orgNrAlts) {
			
			try {
				orgNr = sf.parseOrgNr(o);
				System.out.println(orgNr);
			} catch (Exception ee) {
				System.out.println(ee.getMessage());
			}
			
		}
		
		
	}
	
}
