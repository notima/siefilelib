/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.notima.sie;

import java.util.regex.*;

/**
 *
 * @author Daniel Tamm
 */
public class SRURec {

    private Pattern	sruPattern = 
            Pattern.compile("#SRU\\s+(\\d+?)\\s+(\\d+?)");
    
    private String  accountNo;
    private String  SRU;

    public SRURec() {
        
    }
    
    public SRURec(String line) throws SIEParseException {

        Matcher m = sruPattern.matcher(line);
        if (m.matches()) {
            accountNo = m.group(1);
            SRU = m.group(2);
        } else {
            throw new SIEParseException("Raden ar inte en korrekt #SRU-rad: " + line);
        }
        
        
    }
    
    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getSRU() {
        return SRU;
    }

    public void setSRU(String SRU) {
        this.SRU = SRU;
    }
    
}
