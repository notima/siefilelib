package org.notima.sie;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.notima.sie.SIEParseException.SIEParseExceptionSeverity;

public class DimRec implements Comparable<DimRec> {

	private static Pattern dimPattern = 
			Pattern.compile("#DIM\\s+(\\d+?)\\s+\"{0,1}(.*?)\"{0,1}");
	
	private int	dimId;
	private String	dimName;
	
	public static final int COSTCENTER_ID = 1;
	public static final int PROJECT_ID = 6;
	
	public static DimRec getCostCenterDimension() {
		DimRec dr = new DimRec();
		dr.setDimId(COSTCENTER_ID);
		dr.setDimName("Kostnadsstalle");
		return dr;
	}
	
	public static DimRec getProjectDimension() {
		DimRec dr = new DimRec();
		dr.setDimId(PROJECT_ID);
		dr.setDimName("Projekt");
		return dr;
	}
	
	public DimRec() {};
	
	public DimRec(String line) throws SIEParseException {
		
		Matcher m = dimPattern.matcher(line);
		if (m.find()) {
			dimId = Integer.parseInt(m.group(1));
			dimName = m.group(2);
		} else {
			throw new SIEParseException("Raden aer inte en korrekt #DIM-rad", SIEParseExceptionSeverity.NORMAL);
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

	@Override
	public int compareTo(DimRec o) {
		
		if (o.dimId < dimId) return -1;
		if (o.dimId > dimId) return 1;
		
		return 0;
	}
	
	public String toSieString() {
		return "#DIM " + dimId + " \"" + dimName + "\"";
	}
	
}
