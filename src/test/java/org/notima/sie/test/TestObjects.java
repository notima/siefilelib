package org.notima.sie.test;


import org.junit.Test;
import org.notima.sie.ObjRec;
import org.notima.sie.SIEFileType4;
import org.notima.sie.TransRec;
import org.notima.sie.VerRec;

public class TestObjects {

	SIEFileType4	sieFile = new SIEFileType4("test.si");
	
	@Test
	public void test() throws Exception {
	
		ObjRec hr = sieFile.addCostCenter("HR", "Human Resources");
		ObjRec common = sieFile.addCostCenter("GEM", "Common");
		
		ObjRec project1 = sieFile.addProject("100", "Our first project");
		ObjRec project2 = sieFile.addProject("200", "Our second project");
		
		VerRec vr = new VerRec();
		vr.setVerText("Testar ett verifikat");
		TransRec tr = new TransRec();
		tr.setKontoNr("1930");
		tr.setBelopp(100);
		tr.addObjekt(hr);
		vr.addTransRec(tr);
		
		tr = new TransRec();
		tr.setKontoNr("1610");
		tr.setBelopp(-100);
		tr.addObjekt(hr);
		vr.addTransRec(tr);
		
		sieFile.addVerRecord(vr);
		
		sieFile.writeToFile();
		
	}

}
