package org.notima.sie.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.notima.sie.ObjRec;
import org.notima.sie.TransRec;
import org.notima.sie.VerRec;


public class CsvRecord10Collection {

	private List<CsvRecord10> m_records;
	
	public void add(CsvRecord10 rec) {
		if (m_records==null) {
			m_records = new Vector<CsvRecord10>();
		}
		m_records.add(rec);
	}
	
	public List<VerRec> toVerRecords() {
		
		if (m_records==null || m_records.size()==0)
			return(null);

		Vector<VerRec> verList = new Vector<VerRec>();
		
		VerRec ver;
		TransRec trec;
		List<ObjRec> objList;
		
		for (CsvRecord10 r : m_records) {
			
			ver = new VerRec();

			ver.setRegDatum(r.getAccountDate());
			ver.setVerDatum(r.getAccountDate());
			ver.setVerText(r.getTextField() + " " + r.getCommentField());
			
			trec = new TransRec();
			trec.setKontoNr(r.getAccountKey());
			trec.setTransText(r.getTextField() + " " + r.getCommentField());
			if (r.getDepartment()!=null && r.getDepartment().trim().length()>0) {
				objList = new ArrayList<ObjRec>();
				objList.add(new ObjRec(1, r.getDepartment()));
				trec.setObjektLista(objList);
			}
			trec.setBelopp(r.getCashAmount());
			// First line
			ver.addTransRec(trec);
			if (r.getTaxAmount()>0) {
				trec = new TransRec();
				trec.setKontoNr("2641"); // Ingaende moms
				trec.setBelopp(r.getTaxAmount());
				ver.addTransRec(trec);
			} else if (r.getTaxAmount()<0) {
				trec = new TransRec();
				trec.setKontoNr("2611"); // Utgaende moms
				trec.setBelopp(r.getTaxAmount());
				ver.addTransRec(trec);
			}
			if (r.getWidthheldTaxAmount()!=0) {
				trec = new TransRec();
				trec.setKontoNr("2647"); // Ingaende moms
				trec.setBelopp(-r.getWidthheldTaxAmount()*0.25);
				ver.addTransRec(trec);
				trec = new TransRec();
				trec.setKontoNr("2617"); // Utgaende moms
				trec.setBelopp(r.getWidthheldTaxAmount()*0.25);
				ver.addTransRec(trec);
			}
			// Put the rest on 1930 
			double rest = ver.getBalance();
			trec = new TransRec();
			trec.setKontoNr("1930");
			trec.setBelopp(-rest);
			ver.addTransRec(trec);
			verList.add(ver);
		}
		
		return(verList);
		
	}
	
	public VerRec toVerRec() {
		
		if (m_records==null || m_records.size()==0)
			return(null);
		
		VerRec ver = new VerRec();
		// Use date from the first record
		CsvRecord10 rec = m_records.get(0);
		ver.setRegDatum(rec.getAccountDate());
		ver.setVerDatum(rec.getAccountDate());
		ver.setVerText(rec.getTextField() + " " + rec.getCommentField());
		
		TransRec trec;
		List<ObjRec> objList;
		
		for (CsvRecord10 r : m_records) {
			trec = new TransRec();
			trec.setKontoNr(r.getAccountKey());
			trec.setTransText(r.getTextField() + " " + r.getCommentField());
			if (r.getDepartment()!=null && r.getDepartment().trim().length()>0) {
				objList = new ArrayList<ObjRec>();
				objList.add(new ObjRec(1,r.getDepartment()));
				trec.setObjektLista(objList);
			}
			trec.setBelopp(r.getCashAmount());
			// First line
			ver.addTransRec(trec);
			if (r.getTaxAmount()>0) {
				trec = new TransRec();
				trec.setKontoNr("2641"); // Ingaende moms
				trec.setBelopp(r.getTaxAmount());
				ver.addTransRec(trec);
			} else if (r.getTaxAmount()<0) {
				trec = new TransRec();
				trec.setKontoNr("2611"); // Utgaende moms
				trec.setBelopp(r.getTaxAmount());
				ver.addTransRec(trec);
			}
			if (r.getWidthheldTaxAmount()!=0) {
				trec = new TransRec();
				trec.setKontoNr("2647"); // Ingaende moms
				trec.setBelopp(-r.getWidthheldTaxAmount()*0.25);
				ver.addTransRec(trec);
				trec = new TransRec();
				trec.setKontoNr("2617"); // Utgaende moms
				trec.setBelopp(r.getWidthheldTaxAmount()*0.25);
				ver.addTransRec(trec);
			}
			// Put the rest on 1930 
			double rest = ver.getBalance();
			trec = new TransRec();
			trec.setKontoNr("1930");
			trec.setBelopp(-rest);
			ver.addTransRec(trec);
		}
		
		return(ver);
		
	}
	
}
