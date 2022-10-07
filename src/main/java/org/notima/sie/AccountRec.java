/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.notima.sie;

import java.util.regex.*;

import org.notima.sie.SIEParseException.SIEParseExceptionSeverity;

/**
 * Konto och SRU kod
 * 
 * @author Daniel Tamm
 */
public class AccountRec {

    private static Pattern	accountPattern = 
            Pattern.compile("#KONTO\\s+(\\d+?)\\s+\"{0,1}(.*?)\"{0,1}");
    
    private String  accountNo;
    private String  accountName;

    public AccountRec(String accountNo, String accountName) {
    	this.accountNo = accountNo;
    	this.accountName = accountName;
    }
    
    public AccountRec(String line) throws SIEParseException {

        Matcher m = accountPattern.matcher(line);
        if (m.matches()) {
            accountNo = m.group(1);
            accountName = m.group(2);
        } else {
            throw new SIEParseException("Raden aer inte en korrekt #KONTO-rad: " + line, SIEParseExceptionSeverity.NORMAL);
        }
        
    }
    
    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
    
    public String toSieString() {
    	return("#KONTO " + accountNo + " \"" + accountName + "\"\r\n");
    }
    
}
