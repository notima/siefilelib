package org.notima.sie;

import java.util.ArrayList;
import java.util.List;

public class SIEUtil {

	public static String SIEFileLibString = "https://github.com/notima/siefilelib";
	
	/**
	 * Splits a line into columns.
	 * The format of s is assumed to be the SIE-format where white space is column
	 * separator and " is used to enclose columns containing white space.
	 * If " is in a field it's proceeded by \
	 * 
	 * @param s
	 * @return	A list of string representing the columns. Enclosing quotes are removed.
	 */
	public static List<String> split(String s) {

		s = s.trim();
		List<Integer> splitPos = new ArrayList<Integer>();
		char c;
		boolean inQuote = false;
		boolean inSplit = false;
		for (int i=0;i<s.length(); i++) {
			c = s.charAt(i);
			if (c=='"' && (i==0 || s.charAt(i-1)!='\\')) {
				// Toggle quote
				inQuote = !inQuote;
				if (inSplit)
					inSplit = false;
				continue;
			}
			if (inSplit && !(c==' ' || c=='\t')) {
				inSplit = false;
				inQuote = false;
				continue;
			}
			if (!inQuote && (c==' ' || c=='\t') && !inSplit) {
				splitPos.add(i);
				inSplit = true;
			}
		}
		
		// When we're here, we have the split positions
		List<String> result = new ArrayList<String>();
		for (int i=0; i<splitPos.size(); i++) {
			if (i==0) {
				result.add(removeQuotes(s.substring(0, splitPos.get(i))));
			}
			if (i>0)
				result.add(removeQuotes(s.substring(splitPos.get(i-1)+1, splitPos.get(i))));
			
			if (i==splitPos.size()-1) {
				result.add(removeQuotes(s.substring(splitPos.get(i))));
			}
			
		}
		
		return result;
		
	}
	
	public static String removeQuotes(String s) {
		if (s==null) return null;
		s = s.trim();
		if (s.startsWith("\""))
			s = s.substring(1);
		if (s.endsWith("\"") && !s.endsWith("\\\"")) {
			s = s.substring(0, s.length()-1);
		}
		// Replace any \" occurances
		s = s.replace("\\\"", "\"");
		return s;
	}
	
	
}
