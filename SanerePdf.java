
import java.io.*;

import org.apache.pdfbox.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.util.PDFTextStripper;

import java.util.HashMap;

public class SanerePdf {
	
	HashMap<String,String> postnr = new HashMap<String,String>();
	
	public SanerePdf(String postNrFilnavn) {
		String line;
		String[] fields;
		BufferedReader reader;
		
		try {
			reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(postNrFilnavn), "ISO8859_1"));
			
			// to kolonner i fila med postnumre
			
			while ((line = reader.readLine()) != null) {
				fields = line.split("\t");
				for (int i=0; i<=3; i+=2) {
					try {
						if (fields[i]!=null && fields[i+1]!=null) {
							postnr.put(fields[i], fields[i+1]);
							//System.out.print(fields[i]+" ");
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						// kommet ut for et merkelig format; la det skure
					}
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean gyldigFnr(String fnr) {
		return gyldigFnr(Integer.parseInt(fnr));
	}
	public static boolean gyldigFnr(int fnr) {
		return false;
	}
	
	public static String txtFromPdf(String filename) throws IOException {
		PDDocument doc = PDDocument.load(filename);

		PDFTextStripper stripper = new PDFTextStripper();

		stripper.setPageSeparator("\f"); // form feed
		//stripper.setSortByPosition(true);
		//stripper.setShouldSeparateByBeads(false);
		String text = stripper.getText(doc);
		
		doc.close();
		return text;
	}
	
	public static void main(String [] args) throws IOException {
		
		System.out.print(txtFromPdf(args[0]));
		
		SanerePdf sanerer = new SanerePdf("Postnummerregister_ansi.txt");
		
		
	}
	
	
	
	// k1 = 11 - ((3 * d1 + 7 * d2 + 6 * m1 + 1 * m2 + 8 * y1 + 9 * y2 + 4 * i1 + 5 * i2 + 2 * i3) % 11)
	// k2 = 11 - ((5 * d1 + 4 * d2 + 3 * m1 + 2 * m2 + 7 * y1 + 6 * y2 + 5 * i1 + 4 * i2 + 3 * i3 + 2 * k1) % 11)

}