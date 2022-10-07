package org.notima.sie;

public class SIEParseException extends Exception {

	public enum SIEParseExceptionSeverity {
		NORMAL,
		HIGH,
		CRITICAL
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private SIEParseExceptionSeverity severity;
	
	public SIEParseException(String message, SIEParseExceptionSeverity severity) {
		super(message);
		this.severity = severity;
	}
	
	public SIEParseExceptionSeverity getSeverity() {
		return severity;
	}
	
	public void setSeverity(SIEParseExceptionSeverity severity) {
		this.severity = severity;
	}
	
	public boolean isCritical() {
		return SIEParseExceptionSeverity.CRITICAL.equals(severity);
	}
	
}
