package org.notima.sie.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.notima.sie.ObjRec;
import org.notima.sie.SIEFileType4;
import org.notima.sie.SIEUtil;
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

	@Test
	public void testDefaultProgram() {
		SIEFileType4 f = new SIEFileType4("dummy.si");
		String sie = f.toSieString();
		assertTrue("Default #PROGRAM should contain GitHub URL",
				sie.contains(SIEUtil.SIEFileLibString));
	}

	@Test
	public void testExplicitProgramOverridesDefault() {
		SIEFileType4 f = new SIEFileType4("dummy.si");
		f.setProgram("MyApp");
		String sie = f.toSieString();
		assertTrue(sie.contains("#PROGRAM \"MyApp\""));
	}

	@Test
	public void testDefaultKptyp() {
		SIEFileType4 f = new SIEFileType4("dummy.si");
		String sie = f.toSieString();
		assertTrue("Default #KPTYP should be EUBAS97", sie.contains("#KPTYP EUBAS97"));
	}

	@Test
	public void testExplicitKptypOverridesDefault() {
		SIEFileType4 f = new SIEFileType4("dummy.si");
		f.setKpTyp("BAS2021");
		String sie = f.toSieString();
		assertTrue(sie.contains("#KPTYP BAS2021"));
	}

	@Test
	public void testNegativeAmountInFile() throws Exception {
		SIEFileType4 f = new SIEFileType4("test_neg.si");
		VerRec vr = new VerRec();
		TransRec tr = new TransRec("2440", -1234.56, null, "Neg amount");
		vr.addTransRec(tr);
		f.addVerRecord(vr);
		String sie = f.toSieString();
		assertTrue("Negative amount must use ASCII minus", sie.contains("-1234.56"));
	}
}
