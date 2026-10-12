package org.notima.sie;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Verifikationspost. Innehaller en eller flera transaktionsposter.
 * 
 * <pre>#VER serie vernr verdatum vertext regdatum sign</pre>
 * 
 * @author Daniel Tamm
 *
 */

public class VerRec {

	private static Pattern	verPatternPart1 = 
		Pattern.compile("#VER\\s+\"{0,1}(.*?)\"{0,1}\\s+\"{0,1}(.*?)\"{0,1}\\s+(\\d{8})(.*)");
	private static Pattern verPatternPart2 =
		Pattern.compile("\\s*\"{0,1}(.*?)\"{0,1}\\s*(?:(\\d{8})|\"\")?\\s*(?:\"(.*?)\")?");
	private String				m_serie = "";
	private String				m_verNr = "";
	private Date				m_verDatum;
	private String				m_verText;
	private Date				m_regDatum;
	private String				m_sign;
	private List<TransRec>	m_transList;
	private double				m_balance;
	private double				m_totalCredit;
	private double				m_totalDebet;
	
	public VerRec() {
		m_balance = 0.0;
		m_transList = new ArrayList<>();
	}
	
	public VerRec(String header) {
		m_balance = 0.0;
		m_transList = new ArrayList<>();
		
		Matcher m = verPatternPart1.matcher(header);
		if (m.matches()) {
			m_serie = m.group(1);
			m_verNr = m.group(2);
			try {
				m_verDatum = SIEFile.s_dateFormat.parse(m.group(3));
			} catch (java.text.ParseException pe) {
				// This shouldn't happen since the date pattern matched.
			}
			// Do a pattern match on group 4
			if (m.groupCount()>3 && m.group(4)!=null) {
				Matcher m2 = verPatternPart2.matcher(m.group(4));
				if (m2.matches()) {
					m_verText = m2.group(1);
					if (m2.groupCount()>1 && m2.group(2)!=null) {
						try {
							m_regDatum = SIEFile.s_dateFormat.parse(m2.group(2));
						} catch (java.text.ParseException pe2) {
							// This shouldn't happen since the pattern matched.
						}
					}
					m_sign = m2.group(3);
					if (m_sign != null && m_sign.isEmpty()) m_sign = null;
				}
			}
		}
	}
	
	/**
	 * Adds a transaction. A removed transaction (#BTRANS) is kept but doesn't count
	 * in the balance and totals.
	 */
	public void addTransRec(TransRec tr) {
		m_transList.add(tr);
		if (tr.isRemoved()) return;
		m_balance += tr.getBelopp();
		if (tr.getBelopp()>0) {
			m_totalDebet += tr.getBelopp();
		}
		if (tr.getBelopp()<0) {
			m_totalCredit += Math.abs(tr.getBelopp());
		}
	}
	
	/**
	 * Adds a transaction read from a file, leaving out the #TRANS copy that follows an
	 * #RTRANS record for programs not knowing #RTRANS, as it is the same transaction.
	 * 
	 * @return	True if added, false if left out as such a copy.
	 */
	public boolean addParsedTransRec(TransRec tr) {
		if (!m_transList.isEmpty() && tr.isCompatibilityCopyOf(m_transList.get(m_transList.size()-1))) {
			return false;
		}
		addTransRec(tr);
		return true;
	}
	
	public double getBalance() {
		// Some strange feature in Java that causes small values close to zero
		// Correcting by setting these small values to exactly zero.
		// if (m_balance!=0.0 && m_balance < 1.0E-10) m_balance = 0.0;
		return(m_balance);
	}
	
	public double getTotalDebet() {
		return(m_totalDebet);
	}
	
	public double getTotalCredit() {
		return(m_totalCredit);
	}
	
	/**
	 * Serie anges med en bokstav från A och framåt, alternativt
	 * med ett tal från 1 och framåt.
	 * 
	 * @return 
	 */
	public String getSerie() {
		return m_serie;
	}
	
	/**
	 * @param serie the serie to set
	 */
	public void setSerie(String serie) {
		this.m_serie = serie;
	}
	/**
	 * Verifikationsnummer. Kan vara tomt om numret skall
	 * sättas av det importerande programmet.
	 * 
	 * @return 
	 */
	public String getVerNr() {
		return m_verNr;
	}
	/**
	 * @param verNr the verNr to set
	 */
	public void setVerNr(String verNr) {
		this.m_verNr = verNr;
	}
	/**
	 * @return the verDatum
	 */
	public Date getVerDatum() {
		return m_verDatum;
	}
	/**
	 * @param verDatum the verDatum to set
	 */
	public void setVerDatum(Date verDatum) {
		this.m_verDatum = verDatum;
	}
	/**
	 * @return the verText
	 */
	public String getVerText() {
		return m_verText;
	}
	/**
	 * @param verText the verText to set
	 */
	public void setVerText(String verText) {
		m_verText = SIEFile.validateText(verText);
	}
	/**
	 * @return the regDatum
	 */
	public Date getRegDatum() {
		return m_regDatum;
	}
	/**
	 * @param regDatum the regDatum to set
	 */
	public void setRegDatum(Date regDatum) {
		this.m_regDatum = regDatum;
	}
	/**
	 * @return the transList
	 */
	public List<TransRec> getTransList() {
		return m_transList;
	}
	/**
	 * @param transList the transList to set
	 */
	public void setTransList(List<TransRec> transList) {
		this.m_transList = transList;
	}

	/**
	 * Signature of who registered the voucher.
	 * 
	 * @return	The signature, or null if not given.
	 */
	public String getSign() {
		return m_sign;
	}
	public void setSign(String sign) {
		m_sign = SIEFile.validateText(sign);
	}

	private void setDefaultVerDatum() {
		m_verDatum = Calendar.getInstance().getTime();
	}
	
	public String toSieString() {
		
		if (m_verDatum==null) {
			setDefaultVerDatum();
		}
		
		StringBuilder s = new StringBuilder();
		s.append("#VER \"" + (m_serie!=null ? m_serie : "") + "\" \"" + (m_verNr!=null ? m_verNr : "") + "\"");
		s.append(" " + SIEFile.s_dateFormat.format(m_verDatum) + " \"" + (m_verText!=null ? m_verText : "") + "\"");
		boolean sign = m_sign != null && !m_sign.isEmpty();
		if (m_regDatum!=null) {
			s.append(" " + SIEFile.s_dateFormat.format(m_regDatum));
		} else if (sign) {
			s.append(" \"\"");
		}
		if (sign) {
			s.append(" \"" + m_sign + "\"");
		}
		s.append("\r\n");
		s.append("{\r\n");
		for (TransRec tr : m_transList) {
			s.append(tr.toSieString());
			if (tr.isAdded()) {
				// Followed by the same transaction as #TRANS for programs not knowing #RTRANS
				s.append(tr.toSieString(TransRec.TransType.TRANS, false));
			}
		}
		s.append("}\r\n");
		return(s.toString());
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("VerId: " + m_verNr + "\n");
		s.append("Serie: " + m_serie + "\n");
		s.append("Ver datum: " + SIEFile.s_dateFormat.format(m_verDatum) + "\n");
		s.append("Text: " + m_verText + "\n");
		s.append("Reg datum: " + (m_regDatum!=null ? SIEFile.s_dateFormat.format(m_regDatum) : "") + "\n");
		if (m_sign!=null) s.append("Sign: " + m_sign + "\n");
		s.append("RADER:\n");
		s.append("======\n");
		for (TransRec tr : m_transList) {
			s.append(tr.toString());
		}
		s.append("====== Balans = " + getBalance() + "\n");
		return(s.toString());
	}
	
}
