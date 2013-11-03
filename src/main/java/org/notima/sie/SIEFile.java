package org.notima.sie;

import java.util.*;
import java.io.*;
import java.util.regex.*;
import java.text.*;

/**
 * Basklass för SIE-filer. Innehåller attribut som är gemensamma för alla
 * typer av SIE-filer.
 * 
 * @author Daniel Tamm
 *
 */
public class SIEFile {

    public static final SimpleDateFormat s_dateFormat = new SimpleDateFormat("yyyyMMdd");
    public static final DecimalFormat s_amountFormat = new DecimalFormat("#####################.##");
    public static final DecimalFormat s_qtyFormat = new DecimalFormat("#####################.####");
    
    static {
    	DecimalFormatSymbols ds = new DecimalFormatSymbols();
    	ds.setDecimalSeparator('.');
    	s_amountFormat.setDecimalFormatSymbols(ds);
    }
    
    //=======================
    // IDENTIFIKATIONSPOSTER
    // #FLAGGA - Flaggpost som anger om filen tagits emot av mottagaren
    protected int m_flagga = 0;
    // #PROGRAM - Vilket program som genererat filen
    protected String m_program;
    // #FORMAT - Vilken teckenuppsättning som använts
    protected String m_format = "PC8";
    // #GEN - När och av vem som filen genererats
    protected Date m_genDatum;
    protected String m_genSign;
    // #SIETYP - Vilken typ av SIE-formatet filen följer
    protected String m_sieTyp;
    // #PROSA - Fri kommentartext kring filens innehåll
    protected String m_prosa;
    // #FNR - Redovisningsprogrammets internkod för exporterat företag
    protected String m_fnr;
    // #ORGNR - Organisationsnummer för det företag som exporterats
    protected String m_orgNr;
    // #BKOD - Branschtillhörighet för det exporterade företaget
    protected String m_bkod;
    // #ADRESS - Adressuppgifter för det aktuella företaget
    protected String m_adress;
    // #FNAMN - Fullständigt namn för det företag som exporterats
    protected String m_fnamn;
    // #RAR - Räkenskapsår
    protected String m_rar;
    // #VALUTA - Valuta
    protected String m_valuta;
    // #TAXAR - Taxeringsår för deklarationsinformation (SRU-koder)
    protected String m_taxar;
    // #OMFATTN - Datum för periodsaldons omfattning
    protected String m_omfattn;
    // #KPTYP - Kontoplanstyp
    protected String m_kptyp;    
    
    //================================
    // Internal data
    // Pointer to file
    protected File m_sieFile;
    // Lines in file
    protected List<String> m_lines;
    // Fiscal years
    protected Map<Integer, RARRec> m_rarMap = new TreeMap<Integer, RARRec>();
    // Account map
    protected Map<String,AccountRec> m_accountMap = new TreeMap<String,AccountRec>();
    // SRU Map
	protected Map<String, SRURec> m_sruRecs;
	// Balance map
	protected Map<String, List<BalanceRec>> m_balanceRecs;
	// Result record map
	protected Map<String, ResRec> m_resRecs;
    
    /**
     * Default constructor
     */
    public SIEFile() {
    }

    /**
     * Constructor.
     * 
     * @param	The path of the file to be read
     */
    public SIEFile(String filePath) {
        m_sieFile = new File(filePath);
    }

    /**
     * Helper function to make sure the text doesn't contain any illegal characters
     * (such as linefeed etc). " are prepended with \
     * @param text
     * @return
     */
    public static String validateText(String text) {
    	if (text==null) return(null);
    	StringBuffer result = new StringBuffer();
    	char c;
    	for(int i=0; i<text.length(); i++) {
    		c = text.charAt(i);
    		if (c=='"') {
    			result.append("\\\"");
    		}
    		if (c=='\r' || c=='\n' || c=='\t') {
    			// Skip
    			continue;
    		}
    		result.append(c);
    	}
    	return(result.toString());
    }
    
    /**
     * Adds account record to the SIE-file.
     * 
     * @param rec
     */
    public void addAccountRecord(AccountRec rec) {
    	if (m_accountMap==null) {
			// Create a new accountMap
			m_accountMap = new TreeMap<String,AccountRec>();
    	}
    	m_accountMap.put(rec.getAccountNo(), rec);
    }
    
    /**
     * Sets account map from external source
     * @param accountMap
     */
    public void setAccountMap(Map<String,AccountRec> accountMap) {
    	m_accountMap = accountMap;
    }
    
    public Map<String,AccountRec> getAccountMap() {
    	if (m_accountMap==null) {
			// Create a new accountMap
			m_accountMap = new TreeMap<String,AccountRec>();
    	}
    	return(m_accountMap);
    }
    
	/**
	 * Return balance records.
	 * There can be more than one or two balance records per account
	 * UB and IB
	 * 
	 * @return
	 */
	public Map<String, List<BalanceRec>> getBalanceMap() {
		if (m_balanceRecs==null) {
			m_balanceRecs = new TreeMap<String, List<BalanceRec>>();
		}
		return(m_balanceRecs);
	}
	
	/**
	 * Return fiscal year
	 * 
	 * @param	0 for this year
	 * 			-1 for previous year
	 * @return	A RARRec if the information is available
	 */
	public RARRec getFiscalYear(int offset) {
		return m_rarMap.get(offset);
	}
	
	/**
	 * Set balance records. There can be more than one or two balance records per account
	 * UB and IB
	 * 
	 * @return
	 */
	public void setBalanceMap(Map<String, List<BalanceRec>> rec) {
		m_balanceRecs = rec;
	}

	/**
	 * Adds balance record to file
	 * @param rec
	 */
	public void addBalanceRecord(BalanceRec rec) {
		if (m_balanceRecs==null) {
			m_balanceRecs = new TreeMap<String, List<BalanceRec>>();
		}
		// First check if we already have records for this account
		List<BalanceRec> recs = m_balanceRecs.get(rec.getAccountNo());
		if (recs==null) {
			recs = new Vector<BalanceRec>();
		}
		recs.add(rec);
		m_balanceRecs.put(rec.getAccountNo(), recs);
	}

	/**
	 * Set difference.
	 * Sets the result to the difference between the existing result record
	 * and the new result record. Used when first adding the incoming result
	 * and then adding the outgoing result.
	 */
	public void diffResultRecord(ResRec rec) {
    	if (m_resRecs==null) {
			// Create a new Map
			m_resRecs = new TreeMap<String,ResRec>();
    	}
    	// First check if there's an existing record
    	ResRec existing = m_resRecs.get(rec.getAccountNo());
    	if (existing!=null) {
    		existing.setBalance(rec.getBalance()-existing.getBalance());
    	} else {
    		m_resRecs.put(rec.getAccountNo(), rec);
    	}
	}
	
    /**
     * Adds result to the SIE-file.
     * 
     * @param rec
     */
    public void addResultRecord(ResRec rec) {
    	if (m_resRecs==null) {
			// Create a new Map
			m_resRecs = new TreeMap<String,ResRec>();
    	}
    	// First check if there's an existing record
    	ResRec existing = m_resRecs.get(rec.getAccountNo());
    	if (existing!=null) {
    		existing.setBalance(rec.getBalance()+existing.getBalance());
    	} else {
    		m_resRecs.put(rec.getAccountNo(), rec);
    	}
    }
	
    /**
     * Reads the file and extracts file identification data
     */
    public void readFile() throws Exception {

        if (!m_sieFile.exists()) {
            throw new Exception("Filen " + m_sieFile.getAbsolutePath() + " finns inte.");
        }

        // TODO: Automatic code page selection depending on the FORMAT field.
        // Now it's always fixed to PC8 = IBM437 according to the specification.
        BufferedReader fr = new BufferedReader(
                new InputStreamReader(new FileInputStream(m_sieFile), "IBM437"));
        m_lines = new Vector<String>();
        String line;
        boolean lineRead;
        while ((line = fr.readLine()) != null) {
            lineRead = false;
            if (line.startsWith("#FLAGGA")) {
                parseFlagga(line);
                lineRead = true;
            }
            if (line.startsWith("#FORMAT")) {
                parseFormat(line);
                lineRead = true;
            }
            if (line.startsWith("#SIETYP")) {
                parseSieTyp(line);
                lineRead = true;
            }
            if (line.startsWith("#PROGRAM")) {
                parseProgram(line);
                lineRead = true;
            }
            if (line.startsWith("#GEN")) {
                parseGen(line);
                lineRead = true;
            }
            if (line.startsWith("#FNAMN")) {
                parseFnamn(line);
                lineRead = true;
            }
            if (line.startsWith("#ORGNR")) {
                parseOrgNr(line);
                lineRead = true;
            }
            if (line.startsWith("#KPTYP")) {
                parseKptyp(line);
                lineRead = true;
            }
            if (line.startsWith("#VALUTA")) {
            	String[] cols = line.split("\\s+");
            	if (cols.length>1);
            		m_valuta = cols[1];
            	lineRead = true;
            }
            if (line.startsWith("#RAR")) {
            	parseRarRec(line);
            	lineRead = true;
            }
            
            // If the line hasn't been recognized, add it
            if (!lineRead) {
                m_lines.add(line);
            }
        }
        fr.close();

    }
    //====================================================
    // Parse methods
    private void parseFlagga(String line) throws SIEParseException {
        Pattern flaggaPattern = Pattern.compile("#FLAGGA\\s+(\\d)");
        Matcher m = flaggaPattern.matcher(line);
        if (m.matches()) {
            m_flagga = new Integer(m.group(1));
        } else {
            throw new SIEParseException("Ogiltigt format. FLAGGA");
        }
    }

    private void parseFormat(String line) throws SIEParseException {
        Pattern flaggaPattern = Pattern.compile("#FORMAT\\s+(\\w+)");
        Matcher m = flaggaPattern.matcher(line);
        if (m.matches()) {
            m_format = m.group(1);
            if (!"PC8".equalsIgnoreCase(m_format)) {
                throw new SIEParseException("PC8 är den enda giltiga teckenuppsättningen. Denna fil har " + m_format);
            }
        } else {
            throw new SIEParseException("Ogiltigt format. FORMAT");
        }
    }

    private void parseSieTyp(String line) throws SIEParseException {
        Pattern flaggaPattern = Pattern.compile("#SIETYP\\s+(\\d)");
        Matcher m = flaggaPattern.matcher(line);
        if (m.matches()) {
            m_sieTyp = m.group(1);
        } else {
            throw new SIEParseException("Ogiltigt format. SIETYP");
        }
    }

    private void parseProgram(String line) throws SIEParseException {
        Pattern flaggaPattern = Pattern.compile("#PROGRAM\\s+(.*)");
        Matcher m = flaggaPattern.matcher(line);
        if (m.matches()) {
            m_program = m.group(1);
        } else {
            throw new SIEParseException("Ogiltigt format. PROGRAM");
        }
    }

    private void parseGen(String line) throws SIEParseException {
        Pattern flaggaPattern = Pattern.compile("#GEN\\s+(\\d{8})\\s*?(\\w*?)");
        Matcher m = flaggaPattern.matcher(line);
        if (m.matches()) {
            try {
                m_genDatum = s_dateFormat.parse(m.group(1));
            } catch (java.text.ParseException pe) {
                throw new SIEParseException("Ogiltigt datumformat. GEN");
            }
            if (m.groupCount() > 1) {
                m_genSign = m.group(2);
            }
        } else {
            throw new SIEParseException("Ogiltigt format. GEN");
        }
    }

    private void parseFnamn(String line) throws SIEParseException {
        Pattern flaggaPattern = Pattern.compile("#FNAMN\\s+(.*)");
        Matcher m = flaggaPattern.matcher(line);
        if (m.matches()) {
            m_fnamn = m.group(1);
            // Remove quotes if any
            if (m_fnamn.endsWith("\"") && m_fnamn.startsWith("\"")) {
                m_fnamn = m_fnamn.substring(1, m_fnamn.length() - 1);
            }
        } else {
            throw new SIEParseException("Ogiltigt format. FNAMN");
        }
    }

    private void parseRarRec(String line) throws SIEParseException {
    	RARRec r = new RARRec(line);
    	m_rarMap.put(r.getRarNo(), r);
    }
    
    private void parseOrgNr(String line) throws SIEParseException {
        Pattern flaggaPattern = Pattern.compile("#ORGNR\\s+(\\d{6}-{0,1}\\d{4})");
        Matcher m = flaggaPattern.matcher(line);
        if (m.matches()) {
            m_orgNr = m.group(1);
        } else {
            throw new SIEParseException("Ogiltigt format. ORGNR");
        }
    }

    private void parseKptyp(String line) throws SIEParseException {
        Pattern flaggaPattern = Pattern.compile("#KPTYP\\s+(\\w+)");
        Matcher m = flaggaPattern.matcher(line);
        if (m.matches()) {
            m_kptyp = m.group(1);
        } else {
            throw new SIEParseException("Ogiltigt format. KPTYP");
        }
    }

	public void setProgram(String program) {
		m_program = program;
	}
	
	public String getProgram() {
		return(m_program);
	}
	
	public void setOrgNr(String orgNr) {
		m_orgNr = orgNr;
	}
	
	public String getOrgNr() {
		return(m_orgNr);
		
	}
	public void setFNamn(String fnamn) {
		m_fnamn = fnamn;
	}
	public String getFNamn() {
		return(m_fnamn);
	}
    
	public void setAdress(String adress) {
		m_adress = adress;
	}
	
	public String getAdress() {
		return(m_adress);
	}
	
	
    public String toSieString() {
    	StringBuffer buf = new StringBuffer();
    	buf.append("#FLAGGA " + m_flagga + "\r\n");
    	buf.append("#FORMAT " + m_format + "\r\n"); // Only support for PC8
    	return(buf.toString());
    }
    
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("Fil: " + m_sieFile.getAbsolutePath() + "\n");
        buf.append("Inläst av mottagare: " + (m_flagga == 0 ? " Nej" : " Ja") + "\n");
        buf.append("Teckenuppsättning: " + m_format + "\n");
        buf.append("SIE typ: " + m_sieTyp + "\n");
        buf.append("Källprogram: " + m_program + "\n");
        buf.append("Fil skapad: " + (m_genDatum != null ? s_dateFormat.format(m_genDatum) : "Okänt") + "\n");
        if (m_genSign != null) {
            buf.append("Fil skapad av: " + m_genSign + "\n");
        }
        buf.append("Företagsnamn: " + m_fnamn + "\n");
        buf.append("Org nr: " + m_orgNr + "\n");
        buf.append("Kontoplanstyp: " + m_kptyp);
        return (buf.toString());
    }
}
