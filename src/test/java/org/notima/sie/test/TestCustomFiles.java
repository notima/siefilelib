package org.notima.sie.test;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;
import org.notima.sie.SIEFileType4;

import junit.framework.TestCase;

public class TestCustomFiles extends TestCase {

	private static final String CUSTOM_DIR = "sample/customtest";

	@Test
	public void testAllCustomFiles() throws Exception {
		File dir = new File(CUSTOM_DIR);
		if (!dir.exists() || !dir.isDirectory()) {
			return;
		}
		File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".se"));
		if (files == null || files.length == 0) {
			return;
		}
		for (File f : files) {
			try {
				SIEFileType4 sieFile = new SIEFileType4(f.getPath());
				sieFile.readFile();
			} catch (Exception e) {
				fail("Failed to read " + f.getName() + ": " + e.getMessage());
			}
		}
	}

}
