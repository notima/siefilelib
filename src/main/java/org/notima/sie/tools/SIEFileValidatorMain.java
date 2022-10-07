package org.notima.sie.tools;

import org.notima.sie.SIEFileType4;

public class SIEFileValidatorMain {

	private SIEFileType4	sourceFile;
	private SIEFileType4	targetFile;	

	private SieValidator	validator;
	
	private String[]     args;
	
	public SIEFileValidatorMain(String[] args) {
		
		this.args = args;
		
	}
	
	public void runValidator() throws Exception {

		readArguments();
		
		readSourceFile();
		
		validateFile();		

		writeTargetFile();
		
		printResultToStdOut();
	}

	private void writeTargetFile() throws Exception {
		
		System.out.println("Writing to target file " + targetFile.getActualFile().getName());
		targetFile.writeToFile();
		
	}
	
	private void readSourceFile() throws Exception {
		
		System.out.println("Reading source file " + sourceFile.getActualFile().getName());
		sourceFile.readFile();
		
	}
	
	private void validateFile() throws Exception {
		
		validator = new SieValidator(sourceFile, targetFile);
		
		validator.validate();
		
	}
	
	private void printResultToStdOut() throws Exception {
		
		System.out.println(targetFile.getActualFile().getCanonicalPath() + " was written.");
		
	}
	
	
	public String createAlternateName(String fileName, String alteration) {
		if (fileName==null) return alteration;
		// Check for an ending (suffix)
		int indexOf = fileName.lastIndexOf(".");
		if (indexOf<0) {
			return fileName + "_" + alteration;
		}
		return fileName.substring(0, indexOf) + "_" + alteration + "." + fileName.substring(indexOf+1, fileName.length());
	}
	
	private void readArguments() throws Exception {

		if (args.length>0) {
			sourceFile = new SIEFileType4(args[0]);

			String targetFileName = args.length>1 ? args[1] : null;
			
			if (targetFileName==null) {
				targetFileName = createAlternateName(args[0], "validated");
			}
			
			targetFile = new SIEFileType4(targetFileName);

		} else {
			throw new Exception("At least one argument, the file to be validated is needed.");
		}
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		SIEFileValidatorMain main = new SIEFileValidatorMain(args);

		try {
			main.runValidator();
		} catch (Exception ee) {
			ee.printStackTrace();
			System.err.println(ee.getMessage());
		}

	}

}
