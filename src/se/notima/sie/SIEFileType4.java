package se.notima.sie;

import java.util.*;

/**
 * Filtyp 4 är för import/export av transaktioner. Kontoplan, ingående balanser
 * och verifikat följer med.
 * 
 * @author Daniel Tamm
 * 
 */
public class SIEFileType4 extends SIEFile {

	private Vector<VerRec> m_verRecs;
	private Map<String, AccountRec> m_accountRecs;
	private Map<String, SRURec> m_sruRecs;
	private TreeMap<String, Vector<BalanceRec>> m_balanceRecs;
	private TreeMap<String, ResRec> m_resRecs;

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
	
	@Override
	public void readFile() throws Exception {
		// First read basic attributes
		super.readFile();
		m_lineNo = 0;
		m_verNo = 0;
		// Create map for storing accounts
		m_accountRecs = new TreeMap<String, AccountRec>();
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
				m_accountRecs.put(account.getAccountNo(), account);
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
