package org.notima.sie;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DimRec {

	private static Pattern dimPattern = 
			Pattern.compile("#DIM\\s+(\\d+?)\\s+\"{0,1}(.*?)\"{0,1}");
	
	private int	dimId;
	private String	dimName;
	
	public DimRec(String line) throws SIEParseException {
		
		Matcher m = dimPattern.matcher(line);
		if (m.find()) {
			dimId = Integer.parseInt(m.group(1));
			dimName = m.group(2);
		} else {
			throw new SIEParseException("Raden aer inte en korrekt #DIM-rad");
		}
		
	}

	public int getDimId() {
		return dimId;
	}

	public void setDimId(int dimId) {
		this.dimId = dimId;
	}

	public String getDimName() {
		return dimName;
	}

	public void setDimName(String dimName) {
		this.dimName = dimName;
	}
	
	
	
}
