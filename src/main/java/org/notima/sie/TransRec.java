package org.notima.sie;

import java.util.*;
import java.util.regex.*;

import org.notima.sie.SIEParseException.SIEParseExceptionSeverity;

/**
 * Transaktionspost: #TRANS, or #RTRANS / #BTRANS for a transaction added to or removed
 * from a registered voucher afterwards (SIE 4B §5.2).
 * 
 * <pre>#TRANS kontonr {objektlista} belopp transdat transtext kvantitet sign</pre>
 * 
 * @author Daniel Tamm
 *
 */
public class TransRec {

	/** The kind of transaction record. */
	public enum TransType {
		/** An ordinary transaction. */
		TRANS,
		/** A transaction added after the voucher was registered (supplementary transaction). */
		RTRANS,
		/** A transaction removed after the voucher was registered: kept, but no longer counts. */
		BTRANS
	}

	private static Pattern	transPatternPart1 = 
		Pattern.compile("\\s*#(TRANS|RTRANS|BTRANS)\\s+(\\w+)\\s+\\{(.*?)\\}\\s+([0-9,.,-]+)\\s*(.*)");
	private static Pattern	datePattern = Pattern.compile("\\d{8}");
	private static Pattern	numberPattern = Pattern.compile("-?[0-9]+(\\.[0-9]*)?");
	
	private TransType		m_type = TransType.TRANS;
	private	String			m_kontoNr;
	private List<ObjRec>	m_objektLista;
	private double			m_belopp;
	private Date			m_transDatum;
	private String			m_transText;
	private double			m_kvantitet;	
	private String			m_sign;
	
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
		if (!m.matches()) {
			throw new SIEParseException("Not a valid transaction record: " + line,
					SIEParseExceptionSeverity.NORMAL);
		}
		m_type = TransType.valueOf(m.group(1));
		m_kontoNr = m.group(2);
		String objLine = m.group(3);
		m_belopp = Double.parseDouble(m.group(4));
		if (objLine!=null && objLine.trim().length()>0) {
			m_objektLista = ObjRec.parseObjList(objLine);
		}
		parseOptionalFields(m.group(5));
	}

	/**
	 * Parses the optional fields after the amount: transdat transtext kvantitet sign.
	 * The date may also be left out altogether, with the text directly after the amount.
	 */
	private void parseOptionalFields(String tail) {
		List<String> fields = splitFields(tail);
		int i = 0;
		if (i < fields.size() && (fields.get(i).isEmpty() || datePattern.matcher(fields.get(i)).matches())) {
			if (!fields.get(i).isEmpty()) {
				try {
					m_transDatum = SIEFile.s_dateFormat.parse(fields.get(i));
				} catch (java.text.ParseException pe) {
					// Can't happen, the date pattern matched
				}
			}
			i++;
		}
		if (i < fields.size()) {
			m_transText = fields.get(i++);
		}
		if (i < fields.size()) {
			String qty = fields.get(i++);
			if (numberPattern.matcher(qty).matches()) {
				m_kvantitet = Double.parseDouble(qty);
			}
		}
		if (i < fields.size() && !fields.get(i).isEmpty()) {
			m_sign = fields.get(i);
		}
	}

	/**
	 * Splits the fields of a record: separated by spaces, or quoted with " where \" is a
	 * quotation mark within the field. The quotes are removed; an escaped quotation mark is
	 * kept escaped, as texts are stored escaped (see {@link SIEFile#validateText(String)}).
	 */
	static List<String> splitFields(String s) {
		List<String> fields = new ArrayList<>();
		if (s == null) return fields;
		int i = 0, n = s.length();
		while (i < n) {
			char c = s.charAt(i);
			if (Character.isWhitespace(c)) {
				i++;
			} else if (c == '"') {
				StringBuilder field = new StringBuilder();
				i++;
				while (i < n && s.charAt(i) != '"') {
					if (s.charAt(i) == '\\' && i + 1 < n && s.charAt(i + 1) == '"') {
						field.append(s.charAt(i++));
					}
					field.append(s.charAt(i++));
				}
				i++;	// closing quote
				fields.add(field.toString());
			} else {
				int start = i;
				while (i < n && !Character.isWhitespace(s.charAt(i))) i++;
				fields.add(s.substring(start, i));
			}
		}
		return fields;
	}
	
	/**
	 * @return	TRANS for an ordinary transaction, RTRANS / BTRANS for one added to /
	 * 			removed from the voucher after it was registered.
	 */
	public TransType getType() {
		return m_type;
	}
	public void setType(TransType type) {
		m_type = type != null ? type : TransType.TRANS;
	}
	
	/** True when this transaction was removed from its voucher (#BTRANS) and no longer counts. */
	public boolean isRemoved() {
		return m_type == TransType.BTRANS;
	}
	
	/** True when this transaction was added to its voucher after registration (#RTRANS). */
	public boolean isAdded() {
		return m_type == TransType.RTRANS;
	}
	
	/**
	 * Signature of who registered the transaction; for #RTRANS / #BTRANS who added
	 * or removed it.
	 * 
	 * @return	The signature, or null if not given.
	 */
	public String getSign() {
		return m_sign;
	}
	public void setSign(String sign) {
		m_sign = SIEFile.validateText(sign);
	}

	/**
	 * True when {@code other} is the #TRANS copy that follows an #RTRANS record so that
	 * programs not knowing #RTRANS still read the voucher right (the same transaction).
	 */
	public boolean isCompatibilityCopyOf(TransRec other) {
		return other != null && other.m_type == TransType.RTRANS && m_type == TransType.TRANS
				&& java.util.Objects.equals(m_kontoNr, other.m_kontoNr)
				&& m_belopp == other.m_belopp
				&& java.util.Objects.equals(m_transDatum, other.m_transDatum)
				&& java.util.Objects.equals(toDimIdList(), other.toDimIdList());
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
	
	public void addObjekt(ObjRec o) {
		if (m_objektLista==null) {
			m_objektLista = new ArrayList<>();
		}
		m_objektLista.add(o);
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
	
	private String toDimIdList() {
		if (m_objektLista==null || m_objektLista.size()==0) {
			return "";
		}
		List<String> dimIds = new ArrayList<String>();
		for (ObjRec r : m_objektLista) {
			dimIds.add(Integer.toString(r.getDimId()) + " \"" + r.getObjId() + "\"");
		}
		return String.join(",", dimIds);
	}
	
	/**
	 * Prints this record in SIE format
	 * @return
	 */
	public String toSieString() {
		return toSieString(m_type, true);
	}

	/**
	 * Prints this record in SIE format as the given record type.
	 * 
	 * @param type		The record type to print, e.g. TRANS for the copy following an #RTRANS.
	 * @param withSign	Whether to include the signature, if any.
	 */
	String toSieString(TransType type, boolean withSign) {
		// Create object list
		StringBuilder s = new StringBuilder();
		s.append("    #" + type.name() + " " + m_kontoNr + " {" + toDimIdList() + "} " + 
				SIEFile.s_amountFormat.format(m_belopp) + " \"");
		if (m_transDatum!=null) {
				s.append(SIEFile.s_dateFormat.format(m_transDatum));
		}
		s.append("\" \"" + (m_transText!=null ? m_transText : "") + "\"");
		boolean sign = withSign && m_sign != null && !m_sign.isEmpty();
		if (m_kvantitet!=0 || sign) {
			s.append(" " + SIEFile.s_qtyFormat.format(m_kvantitet));
		}
		if (sign) {
			s.append(" \"" + m_sign + "\"");
		}
		s.append("\r\n");
		return(s.toString());
	}
	
	/**
	 * Prints this record in human readable format
	 */
	public String toString() {
		StringBuilder s = new StringBuilder();
		if (m_type != TransType.TRANS) {
			s.append("\t" + m_type.name() + (m_sign != null ? " " + m_sign : ""));
		}
		s.append("\tKontonr: " + m_kontoNr);
		s.append("\tBelopp : " + m_belopp);
		s.append("\t" + (m_transText!=null ? m_transText : "") + "\n");
		return(s.toString());
	}
	
}
