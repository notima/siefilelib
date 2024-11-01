package org.notima.sie;

import java.util.*;
import java.io.*;
import java.util.regex.*;

import org.notima.sie.SIEParseException.SIEParseExceptionSeverity;

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
	// Dimensions
	protected Map<Integer, DimRec> m_dimRecs = new TreeMap<Integer, DimRec>();
	// Objects
	protected Map<DimRec, Map<String, ObjRec>> m_objRecs = new TreeMap<DimRec, Map<String, ObjRec>>();

	protected Map<String, ObjRec> costCenters = new TreeMap<String, ObjRec>();
	protected Map<String, ObjRec> projects = new TreeMap<String, ObjRec>();
	
	protected List<SIEParseException> parseExceptions = new ArrayList<SIEParseException>();
    
    /**
     * Default constructor
     */
    public SIEFile() {
    	init();
    }

    /**
     * Constructor.
     * 
     * @param	filePath	The path of the file to be read
     */
    public SIEFile(String filePath) {
        m_sieFile = new File(filePath);
        init();
    }

    private void init() {
    	
    	initStandardDimensions();
    	
    }
    
    private void initStandardDimensions() {

    	DimRec costCenterDimension = DimRec.getCostCenterDimension();
    	DimRec projectDimension = DimRec.getProjectDimension();
    	m_dimRecs.put(Integer.valueOf(costCenterDimension.getDimId()), costCenterDimension);
    	m_dimRecs.put(Integer.valueOf(projectDimension.getDimId()), projectDimension);
    	m_objRecs.put(costCenterDimension, costCenters);
    	m_objRecs.put(projectDimension, projects);
    	
    }
    
    /**
     * 
     * @return	Returns the actual file on the file system.
     */
    public File getActualFile() {
    	return m_sieFile;
    }
    
    /**
     * Helper function to make sure the text doesn't contain any illegal characters
     * (such as linefeed etc). " are prepended with \
     * @param text		The text to be validated
     * @return			Valid text without illegal characters.
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
     * Copy SIE-file headers from another SIE-file
     * 
     * @param source
     */
    public void copyHeadersFrom(SIEFile source) {
    	m_flagga = source.m_flagga;
    	m_format = source.m_format;
    	m_sieTyp = source.m_sieTyp;
    	m_program = source.m_program;
    	m_fnamn = source.m_fnamn;
    	m_orgNr = source.m_orgNr;
    	m_kptyp = source.m_kptyp;
    }
    
    /**
     * Checks if the SIE-file has required headers
     * 
     * @return		True if required headers are present.
     */
    public boolean hasRequiredHeaders() {
    	if (getOrgNr()==null || getOrgNr().trim().length()==0) {
    		return false;
    	}
    	if (m_sieTyp==null || m_sieTyp.trim().length()==0) {
    		return false;
    	}
    	return true;
    }
    
    /**
     * Adds cost center to file
     * 
     * @param cc
     * @param description
     * @return  The created / added cost center.
     */
    public ObjRec addCostCenter(String cc, String description) {

    	if (cc==null) return null;
    	
    	ObjRec o = costCenters.get(cc);
    	if (o==null) {
    		o = ObjRec.buildCostCenterObject(cc, description);
    		costCenters.put(cc, o);
    	} else {
    		o.setObjName(description);
    	}
    	return o;
    }
    
    /**
     *	Add project to file
     * 
     * @param project
     * @param description
     * @return 	 The created / added project
     */
    public ObjRec addProject(String project, String description) {
    	
    	if (project==null) return null;
    	
    	ObjRec o = projects.get(project);
    	if (o==null) {
    		o = ObjRec.buildProjectObject(project, description);
    		projects.put(project, o);
    	} else {
    		o.setObjName(description);
    	}
    	return o;
    	
    }
    
    /**
     * Adds rar specification
     * @param		r		A RARRecord
     */
    public void addRARRec(RARRec r) {
    	if (r==null) return;
    	if (m_rarMap==null) {
    		m_rarMap = new TreeMap<Integer, RARRec>();
    	}
    	m_rarMap.put(r.getRarNo(), r);
    }
    
    /**
     * Print RAR specification according to standard format
     * #RAR 0
     * #RAR -1 etc
     * @return	Financial year specification
     */
    public String getRARSpecification() {
    	
    	StringBuffer buf = new StringBuffer();
    	if (m_rarMap==null || m_rarMap.size()==0)
    		return null;

    	// Iterate through keys in reverse order
    	RARRec r;
    	for (Integer ii : m_rarMap.keySet()) {
    		r = m_rarMap.get(ii);
    		if (buf.length()>0) {
    			buf.insert(0, "\r\n");
    		}
    		buf.insert(0, r.toSieString());
    	}
    	
    	return buf.toString();
    	
    }
    
    public void copyMapsTo(SIEFile target) {
    	if (target==null) return;
    	copyBalanceRecsTo(target);
    	copyAccountMapTo(target);
    	copyRARRecsTo(target);
    	copyResultRecordsTo(target);
    }
    
    
    public void copyBalanceRecsTo(SIEFile target) {
    	if (target==null) return;
    	target.m_balanceRecs = new TreeMap<String,List<BalanceRec>>();
    	if (m_balanceRecs!=null) {
    		for (String s : m_balanceRecs.keySet()) {
    			target.m_balanceRecs.put(s, m_balanceRecs.get(s));
    		}
    	}
    }
    
    public void copyAccountMapTo(SIEFile target) {
    	if (target==null) return;
    	target.m_accountMap = new TreeMap<String,AccountRec>();
    	if (m_accountMap!=null) {
    		for (String s : m_accountMap.keySet()) {
    			target.m_accountMap.put(s, m_accountMap.get(s));
    		}
    	}
    }
    
    public void copyRARRecsTo(SIEFile target) {
    	if (target==null) return;
    	target.m_rarMap = new TreeMap<Integer,RARRec>();
    	if (m_rarMap!=null) {
    		for (Integer rar : m_rarMap.keySet()) {
    			target.m_rarMap.put(rar, m_rarMap.get(rar));
    		}
    	}
    }
    
    public void copyResultRecordsTo(SIEFile target) {
    	if (target==null) return;
    	target.m_resRecs = new TreeMap<String, ResRec>();
    	if (m_resRecs!=null) {
    		for (String s : m_resRecs.keySet()) {
    			target.m_resRecs.put(s, m_resRecs.get(s));
    		}
    	}
    }
    
    /**
     * Adds account record to the SIE-file.
     * 
     * @param rec		An AccountRec to be added. 
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
     * @param accountMap		The account map to be used.
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
	 * @param	offset 0 for this year
	 * 				  -1 for previous year
	 * @return	A RARRec if the information is available
	 */
	public RARRec getFiscalYear(int offset) {
		return m_rarMap.get(offset);
	}
	
	/**
	 * Set balance records. There can be more than one or two balance records per account
	 * UB and IB
	 * 
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
    	ResRec existing = m_resRecs.get(rec.getAcctString());
    	if (existing!=null) {
    		existing.setBalance(rec.getBalance()-existing.getBalance());
    	} else {
    		m_resRecs.put(rec.getAcctString(), rec);
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
    	ResRec existing = m_resRecs.get(rec.getAcctString());
    	if (existing!=null) {
    		existing.setBalance(rec.getBalance()+existing.getBalance());
    	} else {
    		m_resRecs.put(rec.getAcctString(), rec);
    	}
    }
	
    
    /**
     * Reads only the first maxLines lines of the file to determine the
     * identity of the file.
     * 
     * @param maxLines
     * @throws Exception
     */
    public void readFileHeader(long maxLines) throws Exception {
    	readFile(maxLines);
    }
    
    
    /**
     * Reads the complete file and extracts file identification data
     */    
    public void readFile() throws Exception {
    	readFile(null);
    }
    
    /**
     * Reads the file and extracts file identification data
     * 
     * @param 		maxLines to read. If null, all lines are read.
     */
    protected void readFile(Long maxLines) throws Exception {

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
        long lineCount = 0;
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
            	try {
            		parseKptyp(line);
            	} catch (SIEParseException spe) {
            		System.err.println("KPTYP unknown");
            	}
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
            if (maxLines!=null) {
            	lineCount++;
            	if (lineCount>=maxLines) {
            		break;
            	}
            }
        }
        fr.close();

    }
    //====================================================
    // Parse methods
    public void parseFlagga(String line) throws SIEParseException {
        Pattern flaggaPattern = Pattern.compile("#FLAGGA\\s+(\\d)");
        Matcher m = flaggaPattern.matcher(line);
        if (m.matches()) {
            m_flagga = new Integer(m.group(1));
        } else {
            throw new SIEParseException("Ogiltigt format. FLAGGA", SIEParseExceptionSeverity.NORMAL);
        }
    }

    public void parseFormat(String line) throws SIEParseException {
        Pattern flaggaPattern = Pattern.compile("#FORMAT\\s+(\\w+)");
        Matcher m = flaggaPattern.matcher(line);
        if (m.matches()) {
            m_format = m.group(1);
            if (!"PC8".equalsIgnoreCase(m_format)) {
                throw new SIEParseException("PC8 är den enda giltiga teckenuppsättningen. Denna fil har " + m_format, SIEParseExceptionSeverity.NORMAL);
            }
        } else {
            throw new SIEParseException("Ogiltigt format. FORMAT", SIEParseExceptionSeverity.NORMAL);
        }
    }

    public void parseSieTyp(String line) throws SIEParseException {
        Pattern flaggaPattern = Pattern.compile("#SIETYP\\s+(\\d)");
        Matcher m = flaggaPattern.matcher(line);
        if (m.matches()) {
            m_sieTyp = m.group(1);
        } else {
            throw new SIEParseException("Ogiltigt format. SIETYP", SIEParseExceptionSeverity.NORMAL);
        }
    }

    public void parseProgram(String line) throws SIEParseException {
        Pattern flaggaPattern = Pattern.compile("#PROGRAM\\s+(.*)");
        Matcher m = flaggaPattern.matcher(line);
        if (m.matches()) {
            m_program = m.group(1);
        } else {
            throw new SIEParseException("Ogiltigt format. PROGRAM", SIEParseExceptionSeverity.NORMAL);
        }
    }

    public void parseGen(String line) throws SIEParseException {
        Pattern flaggaPattern = Pattern.compile("#GEN\\s+(\\d{8})\\s*?(.*?)");
        Matcher m = flaggaPattern.matcher(line);
        if (m.matches()) {
            try {
                m_genDatum = s_dateFormat.parse(m.group(1));
            } catch (java.text.ParseException pe) {
                throw new SIEParseException("Ogiltigt datumformat. GEN", SIEParseExceptionSeverity.NORMAL);
            }
            if (m.groupCount() > 1) {
                m_genSign = m.group(2);
            }
        } else {
            throw new SIEParseException("Ogiltigt format. GEN", SIEParseExceptionSeverity.NORMAL);
        }
    }

    public void parseFnamn(String line) throws SIEParseException {
        Pattern flaggaPattern = Pattern.compile("#FNAMN\\s+(.*)");
        Matcher m = flaggaPattern.matcher(line);
        if (m.matches()) {
            m_fnamn = m.group(1);
            // Remove quotes if any
            if (m_fnamn.endsWith("\"") && m_fnamn.startsWith("\"")) {
                m_fnamn = m_fnamn.substring(1, m_fnamn.length() - 1);
            }
        } else {
            throw new SIEParseException("Ogiltigt format. FNAMN", SIEParseExceptionSeverity.NORMAL);
        }
    }

    public void parseRarRec(String line) throws SIEParseException {
    	RARRec r = new RARRec(line);
    	m_rarMap.put(r.getRarNo(), r);
    }

    /**
     * Parses org number from a line
     * 
     * @param line
     * @return	The org number.
     * @throws SIEParseException
     */
    public String parseOrgNr(String line) throws SIEParseException {
        Pattern flaggaPattern = Pattern.compile("#ORGNR\\s+(\\d{6}-{0,1}\\d{4}).*");
        Matcher m = flaggaPattern.matcher(line);
        if (m.matches()) {
            m_orgNr = m.group(1);
            return m_orgNr;
        } else {
        	// Try with quoted org nr
        	Pattern flaggaPattern2 = Pattern.compile("#ORGNR\\s+\"(\\d{6}-{0,1}\\d{4})\"\\s+.*");
        	Matcher m2 = flaggaPattern2.matcher(line);
        	if (m2.matches()) {
        		m_orgNr = m2.group(1);
        		return m_orgNr;
        	}
            throw new SIEParseException("Ogiltigt format: " + line, SIEParseExceptionSeverity.NORMAL);
        }
    }

    public void parseKptyp(String line) throws SIEParseException {
        Pattern flaggaPattern = Pattern.compile("#KPTYP\\s+(\\w+)");
        Matcher m = flaggaPattern.matcher(line);
        if (m.matches()) {
            m_kptyp = m.group(1);
        } else {
            throw new SIEParseException("Ogiltigt format. KPTYP", SIEParseExceptionSeverity.NORMAL);
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
	
	public void setKpTyp(String kpTyp) {
		m_kptyp = kpTyp;
	}
	
	public String getKpTyp() {
		return m_kptyp;
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
