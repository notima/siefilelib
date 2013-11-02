package org.notima.sie;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

/**
 * Filtyp 4 Är för import/export av transaktioner. Kontoplan, ingående balanser
 * och verifikat följer med.
 * 
 * @author Daniel Tamm
 * 
 */
public class SIEFileType4 extends SIEFile {

	private Vector<VerRec> m_verRecs;

	private int m_lineNo;
	private int m_verNo;
	private String m_line;

	public SIEFileType4(String filename) {
		super(filename);
	}
	
	/**
	 * @return Return the transaction records in the file
	 */
	public Vector<VerRec> getVerRecords() {
		return(m_verRecs);
	}
	
	public void addVerList(List<VerRec> list) {
		if (m_verRecs==null) {
			m_verRecs = new Vector<VerRec>();
		}
		m_verRecs.addAll(list);
	}
	
	public void addVerRecord(VerRec r) {
		if (m_verRecs==null) {
			m_verRecs = new Vector<VerRec>();
		}
		m_verRecs.add(r);
	}
	
	@Override
	public void readFile() throws Exception {
		// First read basic attributes
		super.readFile();
		m_lineNo = 0;
		m_verNo = 0;
		AccountRec account;
		// Create map for storing SRU-codes
		m_sruRecs = new TreeMap<String, SRURec>();
		SRURec sru;
		// Create map for storing balance records
		m_balanceRecs = new TreeMap<String, Vector<BalanceRec>>();
		BalanceRec balanceRec;
		Vector<BalanceRec> brecV;
		// Create map for storing balance for result accounts
		ResRec resRec;
		m_resRecs = new TreeMap<String, ResRec>();

		// Create vector for verification records
		m_verRecs = new Vector<VerRec>();
		// Iterate through the file's lines to parse verfication records
		while (m_lineNo < m_lines.size()) {
			m_line = m_lines.get(m_lineNo);
			if (m_line.startsWith("#KONTO")) {
				account = new AccountRec(m_line);
				m_accountMap.put(account.getAccountNo(), account);
			}
			if (m_line.startsWith("#SRU")) {
				sru = new SRURec(m_line);
				m_sruRecs.put(sru.getAccountNo(), sru);
			}
			if (m_line.startsWith("#IB") || m_line.startsWith("#UB")) {
				balanceRec = new BalanceRec(m_line);
				// Check if we have a vector for this account
				brecV = m_balanceRecs.get(balanceRec.getAccountNo());
				if (brecV == null) {
					brecV = new Vector<BalanceRec>();
				}
				brecV.add(balanceRec);
				m_balanceRecs.put(balanceRec.getAccountNo(), brecV);
			}
			if (m_line.startsWith("#RES")) {
				resRec = new ResRec(m_line);
				m_resRecs.put(resRec.getAccountNo(), resRec);
			}
			if (m_line.startsWith("#VER")) {
				m_verRecs.add(parseVer());
			}

			m_lineNo++; // Read next line
		}

	}

	private VerRec parseVer() throws SIEParseException {
		VerRec rec = new VerRec(m_line);
		// Get next line
		m_line = m_lines.get(++m_lineNo);
		// Make sure it is a left curly bracket
		if (!m_line.trim().startsWith("{")) {
			throw new SIEParseException(
					"Verification record not in curly brackets");
		}
		// Get next line
		m_line = m_lines.get(++m_lineNo);
		TransRec tr;
		while (!m_line.trim().startsWith("}")) {
			tr = new TransRec(m_line);
			// Add transaction to verification
			rec.addTransRec(tr);
			// Next line
			m_line = m_lines.get(++m_lineNo);
		}
		return (rec);
	}

	public void writeToFile() throws Exception {
		
		// Open account map file (if any)
		BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(m_sieFile), "IBM437"));
		writer.append(toSieString());
		writer.close();
		
	}
	
	public String toSieString() {
		String superString = super.toSieString();
		StringBuffer s = new StringBuffer();
		s.append(superString);
		// Add specifics
		s.append("#SIETYP 4\r\n");
		s.append("#PROGRAM \"" + (m_program!=null ? m_program : "") + "\"\r\n");
		if (m_genDatum==null) {
			m_genDatum = Calendar.getInstance().getTime();
		}
		s.append("#GEN " + SIEFile.s_dateFormat.format(m_genDatum) + "\r\n\r\n");
		s.append("#FNAMN \"" + (m_fnamn!=null ? m_fnamn : "") + "\"\r\n");
		s.append("#ORGNR " + (m_orgNr!=null ? m_orgNr : "") + "\r\n");
		s.append("#KPTYP " + (m_kptyp!=null ? m_kptyp : "") + "\r\n");
		s.append("\r\n");
		// Add specifications of accounts
		if (m_accountMap!=null) {
			Collection<AccountRec> accounts = m_accountMap.values();
			for (AccountRec a : accounts) {
				s.append(a.toSieString());
			}
			s.append("\r\n");
		}
		
		// Add balance records
		if (m_balanceRecs!=null) {
			Collection<Vector<BalanceRec>> balanceRecs = m_balanceRecs.values();
			for (Vector<BalanceRec> bv : balanceRecs) {
				for (BalanceRec b : bv) {
					s.append(b.toSieString());
				}
			}
			s.append("\r\n");
		}
		
		// Add result records
		if (m_resRecs!=null) {
			Collection<ResRec> resRecs = m_resRecs.values();
			for (ResRec r : resRecs) {
				s.append(r.toSieString());
			}
			s.append("\r\n");
		}
		
		// Add verifications
		if (m_verRecs != null) {
			for (int i = 0; i < m_verRecs.size(); i++) {
				s.append(m_verRecs.get(i).toSieString());
			}
		}
		return (s.toString());
	}
	
	public String toString() {
		String superString = super.toString();
		StringBuffer s = new StringBuffer();
		s.append(superString);
		// Add verifications
		if (m_verRecs != null) {
			s.append("\n");
			for (int i = 0; i < m_verRecs.size(); i++) {
				s.append(m_verRecs.get(i).toString());
			}
		}
		return (s.toString());
	}

}
