package se.notima.sie;

public class SIEFileReader {

	public static SIEFile	m_test;	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (args.length>0) {
			m_test = new SIEFileType4(args[0]);
			try {
				m_test.readFile();
				System.out.println(m_test.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		

	}

}
