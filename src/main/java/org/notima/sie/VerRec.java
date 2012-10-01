package org.notima.sie;

import java.util.*;
import java.util.regex.*;

/**
 * Verifikationspost. Innehaller en eller flera transaktionsposter.
 * 
 * @author Daniel Tamm
 *
 */

public class VerRec {

	private static Pattern	verPatternPart1 = 
		Pattern.compile("#VER\\s+\"{0,1}(.*?)\"{0,1}\\s+\"{0,1}(.*?)\"{0,1}\\s+(\\d{8})(.*)");
	private static Pattern verPatternPart2 =
		Pattern.compile("\\s*\"{0,1}(.*?)\"{0,1}\\s*(\\d{8}){0,1}");
	private String				m_serie = "";
	private String				m_verNr = "";
	private Date				m_verDatum;
	private String				m_verText;
	private Date				m_regDatum;
	private Vector<TransRec>	m_transList;
	private double				m_balance;
	private double				m_totalCredit;
	private double				m_totalDebet;
	
	public VerRec() {
		m_balance = 0.0;
		m_transList = new Vector<TransRec>();
	}
	
	public VerRec(String header) {
		m_balance = 0.0;
		m_transList = new Vector<TransRec>();
		
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
				}
			}
		}
	}
	
	public void addTransRec(TransRec tr) {
		m_transList.add(tr);
		m_balance += tr.getBelopp();
		if (tr.getBelopp()>0) {
			m_totalDebet += tr.getBelopp();
		}
		if (tr.getBelopp()<0) {
			m_totalCredit += Math.abs(tr.getBelopp());
		}
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
	public Vector<TransRec> getTransList() {
		return m_transList;
	}
	/**
	 * @param transList the transList to set
	 */
	public void setTransList(Vector<TransRec> transList) {
		this.m_transList = transList;
	}

	public String toSieString() {
		StringBuffer s = new StringBuffer();
		s.append("#VER \"" + (m_serie!=null ? m_serie : "") + "\" \"" + (m_verNr!=null ? m_verNr : "") + "\"");
		s.append(" " + SIEFile.s_dateFormat.format(m_verDatum) + " \"" + (m_verText!=null ? m_verText : "") + "\"");
		if (m_regDatum!=null) {
			s.append(" " + SIEFile.s_dateFormat.format(m_regDatum));
		}
		s.append("\r\n");
		s.append("{\r\n");
		for (int i=0; i<m_transList.size(); i++) {
			s.append(m_transList.get(i).toSieString());
		}
		s.append("}\r\n");
		return(s.toString());
	}
	
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("VerId: " + m_verNr + "\n");
		s.append("Serie: " + m_serie + "\n");
		s.append("Ver datum: " + SIEFile.s_dateFormat.format(m_verDatum) + "\n");
		s.append("Text: " + m_verText + "\n");
		s.append("Reg datum: " + (m_regDatum!=null ? SIEFile.s_dateFormat.format(m_regDatum) : "") + "\n");
		s.append("RADER:\n");
		s.append("======\n");
		for (int i=0; i<m_transList.size(); i++) {
			s.append(m_transList.get(i).toString());
		}
		s.append("====== Balans = " + getBalance() + "\n");
		return(s.toString());
	}
	
}
