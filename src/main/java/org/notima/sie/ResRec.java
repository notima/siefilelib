/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.notima.sie;

import java.util.regex.*;

/**
 *
 * @author Daniel Tamm
 */
public class ResRec {

    private static Pattern	resPattern = 
            Pattern.compile("#RES\\s+([-|\\d]+?)\\s+(\\d+?)\\s+([-|\\.|\\d]+)");
    
    private int     yearOffset;
    private String  accountNo;
    private double  balance;

    public ResRec() {}
    
    public ResRec(String acctNo, double bal) {
    	accountNo = acctNo;
    	balance = bal;
    }
    
    public ResRec(String line) throws SIEParseException {
        
        Matcher m = resPattern.matcher(line);
        if (m.find()) {
            yearOffset = Integer.parseInt(m.group(1));
            accountNo = m.group(2);
            balance = Double.parseDouble(m.group(3));
        } else {
            throw new SIEParseException("Raden är inte en korrekt RES-rad: " + line);
        }
        
    }
    
    
	public String toSieString() {
		StringBuffer s = new StringBuffer();
		s.append("#RES ");
		s.append(yearOffset + " ");
		s.append(accountNo + " " + SIEFile.s_amountFormat.format(balance));
		s.append("\r\n");
		return(s.toString());
	}
    
    
    public int getYearOffset() {
        return yearOffset;
    }

    public void setYearOffset(int yearOffset) {
        this.yearOffset = yearOffset;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
    
}
