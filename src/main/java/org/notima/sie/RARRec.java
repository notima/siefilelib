/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.notima.sie;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.notima.sie.SIEParseException.SIEParseExceptionSeverity;

/**
 *
 * @author Daniel Tamm
 */
public class RARRec {

    private static Pattern	rarPattern = 
            Pattern.compile("#RAR\\s+(-{0,1}\\d+?)\\s+(\\d{8})\\s+(\\d{8})");
    
    private int  rarNo;
    private java.sql.Date   startDate;
    private java.sql.Date	endDate;

    public RARRec() {
        
    }
    
    public RARRec(String line) throws SIEParseException {

        Matcher m = rarPattern.matcher(line);
        if (m.find()) {
        	rarNo = Integer.parseInt(m.group(1));
        	try {
        		startDate = new java.sql.Date(SIEFile.s_dateFormat.parse(m.group(2)).getTime());
        		endDate = new java.sql.Date(SIEFile.s_dateFormat.parse(m.group(3)).getTime());
        	} catch (ParseException pe) {
        		throw new SIEParseException("Raden ar inte en korrekt #RAR-rad: " + line, SIEParseExceptionSeverity.CRITICAL);
        	}
        } else {
            throw new SIEParseException("Raden ar inte en korrekt #RAR-rad: " + line, SIEParseExceptionSeverity.CRITICAL);
        }
        
        
    }

	public int getRarNo() {
		return rarNo;
	}

	public void setRarNo(int rarNo) {
		this.rarNo = rarNo;
	}

	public java.sql.Date getStartDate() {
		return startDate;
	}

	public void setStartDate(java.sql.Date startDate) {
		this.startDate = startDate;
	}

	public java.sql.Date getEndDate() {
		return endDate;
	}

	public void setEndDate(java.sql.Date endDate) {
		this.endDate = endDate;
	}

    public String toSieString() {
    	return "#RAR " + rarNo + " " + SIEFile.s_dateFormat.format(startDate) + " " + SIEFile.s_dateFormat.format(endDate);
    }
    
}
