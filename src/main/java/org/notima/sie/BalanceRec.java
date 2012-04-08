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
public class BalanceRec {

    private static Pattern	balancePattern = 
            Pattern.compile("#[I|U]B\\s+([-|\\d]+?)\\s+(\\d+?)\\s+([-|\\.|\\d]+)");
    
    private boolean inBalance;
    private int     yearOffset;
    private String  accountNo;
    private double  balance;

    public BalanceRec() {}
    
    public BalanceRec(String acctNo, double balance) {
    	inBalance = true;
    	yearOffset = 0;
    	accountNo = acctNo;
    	this.balance = balance;
    }
    
    public BalanceRec(String line) throws SIEParseException {
        Matcher m = balancePattern.matcher(line);
        if (m.matches()) {
            inBalance = line.charAt(1)=='I';
            yearOffset = Integer.parseInt(m.group(1));
            accountNo = m.group(2);
            balance = Double.parseDouble(m.group(3));
        } else {
            throw new SIEParseException("Raden är inte en korrekt IB/UB-rad: " + line);
        }
    }

	public String toSieString() {
		StringBuffer s = new StringBuffer();
		s.append("#");
		if (inBalance) {
			s.append("I");
		} else {
			s.append("U");
		}
		s.append("B ");
		s.append(yearOffset + " ");
		s.append(accountNo + " " + SIEFile.s_amountFormat.format(balance));
		s.append("\r\n");
		return(s.toString());
	}
    
    
    public boolean isInBalance() {
        return inBalance;
    }

    public void setInBalance(boolean inBalance) {
        this.inBalance = inBalance;
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
