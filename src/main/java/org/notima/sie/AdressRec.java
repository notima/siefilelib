package org.notima.sie;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.notima.sie.SIEParseException.SIEParseExceptionSeverity;

/**
 * The address of the company of the file: {@code #ADRESS kontakt utdelningsadr postadr tel}, ie
 * {@code #ADRESS "Sven Svensson" "Box 21" "211 20 MALMÖ" "040-12345"}.
 */
public class AdressRec {

	/** A Swedish postal address line: postal code (with or without space) and city. */
	private static final Pattern POSTAL = Pattern.compile("^(\\d{3}\\s?\\d{2})\\s+(.+)$");

	private String	contact;
	private String	street;
	private String	postalAddress;
	private String	phone;

	public AdressRec() {}

	public AdressRec(String contact, String street, String postalAddress, String phone) {
		this.contact = contact;
		this.street = street;
		this.postalAddress = postalAddress;
		this.phone = phone;
	}

	public AdressRec(String line) throws SIEParseException {
		if (line == null || !line.trim().startsWith("#ADRESS")) {
			throw new SIEParseException("Raden ar inte en korrekt #ADRESS-rad: " + line, SIEParseExceptionSeverity.NORMAL);
		}
		List<String> cols = SIEUtil.split(line);
		contact = col(cols, 1);
		street = col(cols, 2);
		postalAddress = col(cols, 3);
		phone = col(cols, 4);
	}

	private static String col(List<String> cols, int i) {
		if (cols.size() <= i) return null;
		String s = cols.get(i) != null ? cols.get(i).trim() : null;
		return s == null || s.isEmpty() ? null : s;
	}

	/** @return	The contact person (kontakt); null if none. */
	public String getContact() { return contact; }
	public void setContact(String contact) { this.contact = contact; }

	/** @return	The street or box address (utdelningsadr); null if none. */
	public String getStreet() { return street; }
	public void setStreet(String street) { this.street = street; }

	/** @return	The postal code and city on one line (postadr), ie "211 20 MALMÖ"; null if none. */
	public String getPostalAddress() { return postalAddress; }
	public void setPostalAddress(String postalAddress) { this.postalAddress = postalAddress; }

	/** @return	The phone number (tel); null if none. */
	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }

	/** @return	The postal code of {@link #getPostalAddress()}, ie "211 20"; null if it has none. */
	public String getPostalCode() {
		if (postalAddress == null) return null;
		Matcher m = POSTAL.matcher(postalAddress.trim());
		return m.matches() ? m.group(1) : null;
	}

	/** @return	The city of {@link #getPostalAddress()}, or all of it if it has no postal code. */
	public String getCity() {
		if (postalAddress == null) return null;
		Matcher m = POSTAL.matcher(postalAddress.trim());
		return m.matches() ? m.group(2).trim() : postalAddress.trim();
	}

	/** @return	Whether there is nothing in the record. */
	public boolean isEmpty() {
		return contact == null && street == null && postalAddress == null && phone == null;
	}

	public String toSieString() {
		return "#ADRESS " + quote(contact) + " " + quote(street) + " " + quote(postalAddress) + " " + quote(phone) + "\r\n";
	}

	private static String quote(String s) {
		return "\"" + (s != null ? s.replace("\"", "\\\"") : "") + "\"";
	}

}
