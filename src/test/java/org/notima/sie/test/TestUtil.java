package org.notima.sie.test;

import java.util.List;

import org.junit.Test;
import org.notima.sie.SIEUtil;

public class TestUtil {

	String[] testStrings = new String[] {
		"\"1\" \"ANSV\"",
		"2 \"TOMY\" \"6\" \"106\"",
		"1 \"Testar 3.5\\\" disketter.\"",
		"1 ANSV 6 106",
		"1 ANSV \"6\" 108"
	};
	
	@Test
	public void testSplit() {
		
		List<String> result;
		for (int i=0; i<testStrings.length; i++) {
			System.out.println("Testing string: " + testStrings[i]);
			result = SIEUtil.split(testStrings[i]);
			System.out.println("Result:");
			
			for (int j=0;j<result.size(); j++) {
				System.out.println("\t" + result.get(j));
			}
		}
		
	}

}
