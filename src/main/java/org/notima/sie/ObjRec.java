package org.notima.sie;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.notima.sie.SIEParseException.SIEParseExceptionSeverity;

public class ObjRec {

	private static Pattern objPattern = 
			Pattern.compile("#OBJEKT\\s+(\\d+?)\\s+\"{0,1}(.*?)\"{0,1}\\s+\"{0,1}(.*?)\"{0,1}");
	
	private int dimId;
	private String objId;
	private String	objName;
	
	public ObjRec(int dimId, String objId) {
		this.dimId = dimId;
		this.objId = objId;
	}
	
	public static ObjRec buildCostCenterObject(String cc, String description) {
		ObjRec o = new ObjRec(DimRec.COSTCENTER_ID, cc);
		o.setObjName(description);
		return o;
	}
	
	public static ObjRec buildProjectObject(String cc, String description) {
		ObjRec o = new ObjRec(DimRec.PROJECT_ID, cc);
		o.setObjName(description);
		return o;
	}
	
	
	public ObjRec(String line) throws SIEParseException {
		
		Matcher m = objPattern.matcher(line);
		if (m.find()) {
			dimId = Integer.parseInt(m.group(1));
			objId = m.group(2);
			objName = m.group(3);
		} else {
			throw new SIEParseException("Raden aer inte en korrekt #OBJEKT-rad", SIEParseExceptionSeverity.NORMAL);
		}
		
	}

	public int getDimId() {
		return dimId;
	}

	public void setDimId(int dimId) {
		this.dimId = dimId;
	}

	public String getObjId() {
		return objId;
	}

	public void setObjId(String objId) {
		this.objId = objId;
	}

	public String getObjName() {
		return objName!=null ? objName : "";
	}

	public void setObjName(String objName) {
		this.objName = objName;
	}

	/**
	 * Makes a record of object records from an object string
	 * 
	 * @param str	Object string from SIE-file
	 * @return		A list of object records
	 * @throws SIEParseException	A parse exception if the str doesn't comply to the format.
	 */
	public static List<ObjRec> parseObjList(String str) throws SIEParseException {
		List<ObjRec> result = new ArrayList<ObjRec>();
		if (str==null || str.trim().length()==0) return result;
		
		List<String> objStr = SIEUtil.split(str);
		if (objStr.size()%2!=0) {
			throw new SIEParseException(str + " aer inte en korrekt objektlista.", SIEParseExceptionSeverity.NORMAL);
		}
		ObjRec r;
		int dId;
		for (int i=0; i<objStr.size(); i+=2) {
			try {
				dId = Integer.parseInt(objStr.get(i));
			} catch (NumberFormatException ne) {
				throw new SIEParseException("ObjString " + str + " invalid", SIEParseExceptionSeverity.NORMAL);
			}
			r = new ObjRec(dId, objStr.get(i+1));
			result.add(r);
		}
		
		return result;
	}
	
	
	
	public String toSieString() {
		
		return "#OBJEKT " + dimId + " \"" + objId + "\" \"" + getObjName() + "\"";
		
	}
}
