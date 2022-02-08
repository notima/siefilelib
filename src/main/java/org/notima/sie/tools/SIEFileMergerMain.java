package org.notima.sie.tools;

import java.io.File;

import org.notima.sie.SIEFileType4;

public class SIEFileMergerMain {

	public static SIEFileType4	targetFile;	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (args.length>1) {
			targetFile = new SIEFileType4(args[0]);

			String sourceDir = args[1];
			File sourceDirFile = new File(sourceDir);
			
			SieMerger merger = SieMerger.buildSieFile4Target(targetFile);
			
			
			try {
				merger.addDirectoryToMerge(sourceDirFile);
				merger.merge();
				
				targetFile.writeToFile();
				
			} catch (Exception e) {
				e.printStackTrace();
			}

			
			
			
		}
		

	}

}
