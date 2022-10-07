package org.notima.sie.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.notima.sie.SIEFileType4;
import org.notima.sie.TransRec;
import org.notima.sie.VerRec;

public class SieValidator {

	private SIEFileType4	targetFile;
	private SIEFileType4	sourceFile;

	private List<VerRec>	sourceVouchers;
	private List<VerRec>	targetVouchers = new ArrayList<VerRec>();

	public SieValidator(SIEFileType4 sourceFile, SIEFileType4 targetFile) {
		this.sourceFile = sourceFile;
		this.targetFile = targetFile;
	}
	
	public void validate() {
		
		validateHeaders();
		sourceFile.copyMapsTo(targetFile);
		validateVouchers();
		
	}
	
	private void validateVouchers() {

		sourceVouchers = sourceFile.getVerRecords();
		
		for (VerRec rec : sourceVouchers) {
			targetVouchers.add(validateVoucher(rec));
		}
		
		targetFile.addVerList(targetVouchers);
		
	}

	private VerRec validateVoucher(VerRec rec) {

		truncateDescription(rec);
		
		return rec;
	}
	
	
	private void validateHeaders() {
		
		if (!targetFile.hasRequiredHeaders()) {
			if (sourceFile.hasRequiredHeaders()) {
				targetFile.copyHeadersFrom(sourceFile);
			}
		}
	}
	
	private void truncateDescription(VerRec rec) {
		
		if (rec.getVerText()!=null && rec.getVerText().length()>100) {
			// System.out.println("Trucating text on voucher " + rec.getVerNr() + " : " + rec.getVerText());
			rec.setVerText(rec.getVerText().substring(0, 99));
		}

		validateTransList(rec.getTransList());
		
	}

	private void validateTransList(Vector<TransRec> transactions) {
		if (transactions==null) return;
		
		for (TransRec tr : transactions) {
			validateTransRec(tr);
		}
	}
	
	
	private void validateTransRec(TransRec tr) {
		
		if (tr==null) return;
		
		if (tr.getTransText()!=null && tr.getTransText().length()>100) {
			// System.out.println("Trucating transaction text: " + tr.getTransText());
			tr.setTransText(tr.getTransText().substring(0,99));
		}
		
	}
	
	
}
