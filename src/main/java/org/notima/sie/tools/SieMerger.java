package org.notima.sie.tools;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.notima.sie.SIEFile;
import org.notima.sie.SIEFileType4;

/**
 * Class that merges a SIE4 file with another.
 * 
 * @author Daniel Tamm
 *
 */
public class SieMerger {

	private SIEFileType4	targetFile;
	private List<SIEFileType4>	sourceFiles = new ArrayList<SIEFileType4>();
	
	public static SieMerger buildSieFile4Target(SIEFileType4 target) {
		SieMerger merger = new SieMerger();
		merger.targetFile = target;
		return merger;
	}

	public void addSourceToMerge(SIEFileType4 sourceFile) {
		sourceFiles.add(sourceFile);
	}

	public void addDirectoryToMerge(File dir) throws Exception {
		
		if (dir==null || !dir.isDirectory()) {
			return;
		}
		
		String[] filesToMerge = dir.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				if (name.toLowerCase().endsWith("si")
						|| name.toLowerCase().endsWith("se"))
					return true;
				return false;
			}});

		
		if (filesToMerge!=null && filesToMerge.length>0) {
			
			// Sort
			Arrays.sort(filesToMerge);
			
			SIEFileType4 sieFile;
			for (String fileName : filesToMerge) {
				sieFile = new SIEFileType4(dir.getAbsolutePath() + File.separator + fileName);
				sieFile.readFile();
				addSourceToMerge(sieFile);
			}
			
		}
		
	}
	
	
	public void merge() {

		copyHeaders();
		copyVouchers();
		
	}

	private void copyVouchers() {

		for (SIEFileType4 source : sourceFiles) {
			targetFile.addVerList(source.getVerRecords());
		}
		
	}
	
	
	private void copyHeaders() {
		
		if (!targetFile.hasRequiredHeaders()) {
			for (SIEFile source : sourceFiles) {
				if (source.hasRequiredHeaders()) {
					targetFile.copyHeadersFrom(source);
					break;
				}
			}
		}
	}
	
}
