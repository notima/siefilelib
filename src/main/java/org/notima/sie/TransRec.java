package org.notima.sie;

import java.util.*;
import java.util.regex.*;

/**
 * Transaktionspost.
 * 
 * @author Daniel Tamm
 *
 */
public class TransRec {

	private static Pattern	transPatternPart1 = 
		Pattern.compile("\\s*#TRANS\\s+(\\w+)\\s+\\{(.*?)\\}\\s+([0-9,.,-]+)\\s*(.*)");
	private static Pattern transPatternPart2 =
		Pattern.compile("\\s*(\\d{8}){0,1}\\s*?(.*?)\\s*(\\d*)");
	
	private	String			m_kontoNr;
	private List<ObjRec>	m_objektLista;
	private double			m_belopp;
	private Date			m_transDatum;
	private String			m_transText;
	private double			m_kvantitet;	
	
	public TransRec() {
		
	}
	
	public TransRec(String acctNr, double amount, Date transDate, String desc) {
		m_kontoNr = acctNr;
		m_belopp = amount;
		m_transDatum = transDate;
		m_transText = SIEFile.validateText(desc);
	}
	
	public TransRec(String line) throws SIEParseException {
		Matcher m = transPatternPart1.matcher(line);
		String objLine, beloppStr;
		if (m.matches()) {
			m_kontoNr = m.group(1);
			objLine = m.group(2);
			beloppStr = m.group(3);
			m_belopp = new Double(beloppStr);
			if (objLine!=null && objLine.trim().length()>0) {
				m_objektLista = ObjRec.parseObjList(objLine);
			}
		}
		if (m.groupCount()>3) {
			String g4 = null;
			try {
				g4 = m.group(4);
			} catch (IllegalStateException ise) {
				g4 = null;
			}
			if (g4!=null && g4.length()>0) {
				Matcher m2 = transPatternPart2.matcher(g4);
				if (m2.matches()) {
					if (m2.group(1)!=null && m2.group(1).length()==8) {
						try {
							m_transDatum = SIEFile.s_dateFormat.parse(m2.group(1));
						} catch (java.text.ParseException pe) {
							
						}
					}
					if (m2.groupCount()>1) {
						m_transText = m2.group(2);
						// Remove quotation marks if any
						if (m_transText.startsWith("\"") && 
							m_transText.endsWith("\"")) {
							m_transText = m_transText.substring(1, m_transText.length()-1);
						}
					}
					if (m2.groupCount()>2 && m2.group(3).trim().length()>0) {
						m_kvantitet = new Double(m2.group(3));
					}
				}
			}
		}
	}
	
	public String getKontoNr() {
		return m_kontoNr;
	}
	public void setKontoNr(String kontoNr) {
		this.m_kontoNr = kontoNr;
	}
	public List<ObjRec> getObjektLista() {
		return m_objektLista;
	}
	public void setObjektLista(List<ObjRec> objektLista) {
		this.m_objektLista = objektLista;
	}
	public double getBelopp() {
		return m_belopp;
	}
	public void setBelopp(double belopp) {
		this.m_belopp = belopp;
	}
	public Date getTransDatum() {
		return m_transDatum;
	}
	public void setTransDatum(Date transDatum) {
		this.m_transDatum = transDatum;
	}
	public String getTransText() {
		return m_transText;
	}
	public void setTransText(String transText) {
		this.m_transText = SIEFile.validateText(transText);
	}
	public double getKvantitet() {
		return m_kvantitet;
	}
	public void setKvantitet(double kvantitet) {
		this.m_kvantitet = kvantitet;
	}
	
	/**
	 * Prints this record in SIE format
	 * @return
	 */
	public String toSieString() {
		// Create object list
		String objList = ""; // TODO: Make object list
		StringBuffer s = new StringBuffer();
		s.append("    #TRANS " + m_kontoNr + " {" + objList + "} " + 
				SIEFile.s_amountFormat.format(m_belopp) + " \"");
		if (m_transDatum!=null) {
				s.append(SIEFile.s_dateFormat.format(m_transDatum));
		}
		s.append("\" \"" + (m_transText!=null ? m_transText : "") + "\"");
		if (m_kvantitet!=0) {
			s.append(" " + SIEFile.s_qtyFormat.format(m_kvantitet));
		}
		s.append("\r\n");
		return(s.toString());
	}
	
	/**
	 * Prints this record in human readable format
	 */
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("\tKontonr: " + m_kontoNr);
		s.append("\tBelopp : " + m_belopp);
		s.append("\t" + (m_transText!=null ? m_transText : "") + "\n");
		return(s.toString());
	}
	
}
