package org.notima.sie.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class CsvRecord10 {

	private static SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
	
	private double 	cashAmount;
	private String	textField;
	private String	accountKey;
	private String	department;
	private String	commentField;
	private java.util.Date	accountDate;
	private double	bankAmount;
	private double	taxAmount;
	private double	widthheldTaxAmount;
	
	
	public static CsvRecord10 parseLine(String line) {
		CsvRecord10 r = new CsvRecord10();
		String[] fields = line.split(";");
		if (fields.length<15) {
			String[] newFields = new String[15];
			for (int i=0; i<fields.length; i++) {
				newFields[i] = fields[i];
			}
			fields = newFields;
		}
		// 0 debit cash
		// 1 credit cash
		r.cashAmount = getAmount(fields[0], fields[1]);
		// 2 Note
		// 3 Text
		r.textField = fields[3];
		// 4 Account Key
		r.accountKey = fields[4];
		// 5 Department
		r.department = fields[5];
		// 6 Comment
		r.commentField = fields[6];
		// 7 Account Date
		try {
			r.accountDate = fmt.parse(fields[7]);
		} catch (ParseException pe) {
			pe.printStackTrace();
		}
		// 8 Month
		// 9 Bank debit
		// 10 Bank credit
		r.bankAmount = getAmount(fields[9], fields[10]);
		// 11 Tax debit
		// 12 Tax credit
		r.taxAmount = getAmount(fields[11], fields[12]);
		// 13 whTax debit
		// 14 whTax credit
		r.widthheldTaxAmount = getAmount(fields[13], fields[14]);
		return r;
	}
	
	private static double getAmount(String debitStr, String creditStr) {
		double debit;
		double credit;
		if (debitStr==null || debitStr.trim().length()==0) {
			debit = 0;
		} else {
			debit = Double.parseDouble(debitStr);
		}
		if (creditStr==null || creditStr.trim().length()==0) {
			credit = 0;
		} else {
			credit = Double.parseDouble(creditStr);
		}
		return(debit-credit);
	}
	
	public double getCashAmount() {
		return cashAmount;
	}
	public void setCashAmount(double cashAmount) {
		this.cashAmount = cashAmount;
	}
	public String getTextField() {
		return textField;
	}
	public void setTextField(String textField) {
		this.textField = textField;
	}
	public String getAccountKey() {
		return accountKey;
	}
	public void setAccountKey(String accountKey) {
		this.accountKey = accountKey;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getCommentField() {
		return commentField;
	}
	public void setCommentField(String commentField) {
		this.commentField = commentField;
	}
	public java.util.Date getAccountDate() {
		return accountDate;
	}
	public void setAccountDate(java.util.Date accountDate) {
		this.accountDate = accountDate;
	}
	public double getBankAmount() {
		return bankAmount;
	}
	public void setBankAmount(double bankAmount) {
		this.bankAmount = bankAmount;
	}
	public double getTaxAmount() {
		return taxAmount;
	}
	public void setTaxAmount(double taxAmount) {
		this.taxAmount = taxAmount;
	}
	public double getWidthheldTaxAmount() {
		return widthheldTaxAmount;
	}
	public void setWidthheldTaxAmount(double widthheldTaxAmount) {
		this.widthheldTaxAmount = widthheldTaxAmount;
	}
	
}
