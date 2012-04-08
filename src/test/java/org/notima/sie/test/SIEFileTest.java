package org.notima.sie.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.notima.sie.SIEFile;
import org.notima.sie.SIEFileType4;


public class SIEFileTest {

	@Test
	public void testReadFile() {

		SIEFile	m_test;			
		
		m_test = new SIEFileType4("C:\\SIE\\LÖN.SI");
		try {
			m_test.readFile();
			System.out.println(m_test.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

}
