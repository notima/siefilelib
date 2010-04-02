/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.notima.sie;

import java.util.regex.*;

/**
 * Konto och SRU kod
 * 
 * @author Daniel Tamm
 */
public class AccountRec {

    private Pattern	accountPattern = 
            Pattern.compile("#KONTO\\s+(\\d+?)\\s+\"{0,1}(.*?)\"{0,1}");
    
    private String  accountNo;
    private String  accountName;

    public AccountRec(String line) throws SIEParseException {

        Matcher m = accountPattern.matcher(line);
        if (m.matches()) {
            accountNo = m.group(1);
            accountName = m.group(2);
        } else {
            throw new SIEParseException("Raden aer inte en korrekt #KONTO-rad: " + line);
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
    
}
