
import java.io.*;

// PDFBox: Apache license
import org.apache.pdfbox.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.util.PDFTextStripper;

import org.apache.pdfbox.examples.pdmodel.ReplaceString;
import org.apache.pdfbox.exceptions.COSVisitorException;

// LGPL
import com.skjegstad.utils.BloomFilter;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.regex.*;

public class SanerePdf {
	
	HashMap<String,String> postnr = new HashMap<String,String>();
	BloomFilter<String> adresser;
	
	public SanerePdf(String postNrFilnavn) {
		String line;
		String[] fields;
		BufferedReader reader;
		
		double falsePositiveProbability = 0.001;
		int expectedNumberOfElements = 10000;
		adresser = new BloomFilter<String>(falsePositiveProbability, expectedNumberOfElements);
		
		// extremely hacky example
		for (int i=1; i<2000; i++) {
			adresser.add("Gateveien " + i);
		}
		
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
						// kommet ut for et uventa filformat; la det skure
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
	
	public static String replacedTxt(String label) {
		return "<" + label + ">";
	}
	
	public boolean gyldigPostnr(String nummer, String poststed) {
		boolean ret = false;
		poststed = poststed.toUpperCase();
		if (postnr.containsKey(nummer) &&
			postnr.get(nummer).equals(poststed))
			ret = true;
		return ret;
	}
	
	public static int[] genSjekksum(String pnr) {
		int[] summa = new int[2];
		char[] siffer = pnr.toCharArray();
		int[] n = new int[siffer.length];
		// ArrayList<Integer> digits = new ArrayList<Integer>();
		
		int i = 0;
		for (Character digit : siffer) {
			n[i] = (Integer.parseInt(Character.toString(digit)));
			i++;
		}
		
		int k1 = 0;
		int k2 = 0;
		k1 = 11 - ((3 * n[0] + 7 * n[1] + 6 * n[2] + 1 * n[3] + 8 * n[4] + 9 * n[5] + 4 * n[6] + 5 * n[7] + 2 * n[8]) % 11);
		k2 = 11 - ((5 * n[0] + 4 * n[1] + 3 * n[2] + 2 * n[3] + 7 * n[4] + 6 * n[5] + 5 * n[6] + 4 * n[7] + 3 * n[8] + 2 * k1) % 11);
		// System.out.println(k1 + ", " +k2);
		
		if (k1==10) k1 = 0;
		if (k2==10) k2 = 0;
		
		summa[0] = k1;
		summa[1] = k2;
		return summa;
	}
	
	public static boolean sjekksum(String pnr) {
		char[] siffer = pnr.toCharArray();
		int[] n = new int[siffer.length];
		// ArrayList<Integer> digits = new ArrayList<Integer>();
		
		int i = 0;
		for (Character digit : siffer) {
			n[i] = (Integer.parseInt(Character.toString(digit)));
			i++;
		}
		int[] summa = genSjekksum(pnr);
		int k1 = summa[0];
		int k2 = summa[1];

		if (k1!=n[9] || k2 != n[10])
			return false; // sjekksum ikke lik seg selv
		else
			return true;
	}
	
	public static boolean gyldigFnr(int fnr) {
		// legg til en "0" foran hvis bare 10 siffer
		String nummer = Integer.toString(fnr);
		if (nummer.length() == 10)
			nummer = "0" + nummer;
		return gyldigFnr(nummer);
	}
	public static boolean gyldigFnr(String fnr) {
		if (fnr.length()!=11)
			return false;
		
		boolean ret = true;
		
		try {
			// merk, siste param i String.substring(beg,end) er ikke-inklusiv indeks
			int dag = Integer.parseInt(fnr.substring(0,2));
			int mnd = Integer.parseInt(fnr.substring(2,4));
			int aar = Integer.parseInt(fnr.substring(4,6));
			int pnr = Integer.parseInt(fnr.substring(6,9));
			int sum = Integer.parseInt(fnr.substring(9,11));
			
			if (dag>71 || dag<1 || (dag<41 && dag>31)) // 41-71: D-nummer
				ret = false;
			else if (mnd<1 || mnd>12)
				ret = false;
			else {
				sjekksum(fnr);
			}
		} catch (NumberFormatException e) {
			// System.out.println("NumberFormatException!");
			return false;
		}
		
		return ret;
	}
	
	public static String[] finnPnrKand(String dok) {
		ArrayList<String> kand = new ArrayList<String>();
		Pattern pnrp = Pattern.compile("(\\d{4}?)"); // fire siffer
		Matcher pnrm = pnrp.matcher(dok);
		
		while(pnrm.find()) {
			kand.add(pnrm.group());
		}
		String[] foo = new String[kand.size()];
		return kand.toArray(foo);
	}
	
	public String[] finnPnrSted(String dok) {
		String[] nummer = finnPnrKand(dok);
		ArrayList<String> gyldigNr = new ArrayList<String>();
		ArrayList<String> ret = new ArrayList<String>();
		for (String nr : nummer) {
			if (postnr.containsKey(nr))
				gyldigNr.add(nr);
		}
		for (String nr : gyldigNr) {
			ret.add(nr);
			ret.add(postnr.get(nr));
		}
		String[] foo = new String[ret.size()];
		return ret.toArray(foo);
	}
	
	public String sanerPostnr(String dok) {
		String[] pnrSted = finnPnrSted(dok);
		String postnr = "0000";
		// og her gjenoppfinner vi for-loopen med en iterator
		// (format på pnrSted[]: postnummer, så korresp. sted)
		int i = 0;
		int antSanert = 0;
		for (String poststed : pnrSted) {
			// System.out.print(i+ " ");
			if ((i%2)==1) {

				Pattern stedp = Pattern.compile(poststed, Pattern.CASE_INSENSITIVE);
				Matcher stedm = stedp.matcher(dok);
				// se etter poststedet før en begynner å slette ting
				if (stedm.find()) {
					stedm.reset();
					dok = stedm.replaceAll(replacedTxt("poststed"));
					// dok = dok.replace(poststed, replacedTxt("poststed"));
					dok = dok.replace(postnr, replacedTxt("postnr"));
					// merk, rekkefølgen på erstatting gjør en forskjell pga. Java-hjerneskaden
					//  kjent som immutable strings
					antSanert++;
				}
				// System.out.print("buzzyfizz");
				
			} else { // i%2==0
				postnr = poststed; // lagres for neste iterasjon
			}
			i++;
		}
		// System.out.println("Sanert: " + antSanert);
		
		return dok;
	}
	
	public static String[] finnFodsel(String dok) {
		ArrayList<String> dator = new ArrayList<String>();
		ArrayList<String> ret = new ArrayList<String>();
		Pattern datop = Pattern.compile("\\d{6}");
		Matcher datom = datop.matcher(dok);
		while(datom.find()) {
			dator.add(datom.group());
		}
		for (String dato: dator) {
			int dag = Integer.parseInt(dato.substring(0,2));
			int mnd = Integer.parseInt(dato.substring(2,4));
			int aar = Integer.parseInt(dato.substring(4,6));
			if (!((dag>71 || dag<1 || (dag<41 && dag>31)) // 41-71: D-nummer
				|| (mnd<1 || mnd>12))) {
				ret.add(dato);
			}
		}
		String[] foo = new String[ret.size()];
		return ret.toArray(foo);
	}
	
	public static ArrayList<String> finnPersnr(String dok) {
		String[] dator = finnFodsel(dok);
		Pattern fnrp = Pattern.compile("\\d{5}\\b"); 
			// \b: prøv å finn tallene på slutten
		Matcher fnrm = fnrp.matcher(dok);
		String fnr = "";
		ArrayList<String> tabu = new ArrayList<String>();
		
		while (fnrm.find()) {
			fnr = fnrm.group();
			// System.out.println("Fant " + fnr);
			for (String dato : dator) {
				if (gyldigFnr(dato+fnr)) {
					tabu.add(dato);
					tabu.add(fnr);
				}
			}
		}
		return tabu;
	}
	public static String sanerPersnr(String dok) {
		ArrayList<String> tabu = finnPersnr(dok);
		
		for (String slett : tabu) {
			dok = dok.replaceAll(slett, replacedTxt("pers.nr."));
		}
		return dok;
	}
	
	public String sanerAdresser(String dok) {
		String[] lines = dok.split("\n");
		for (String line : lines) {
			if (adresser.contains(line)) {
				dok = dok.replaceAll(line, replacedTxt("adresse"));
			}
		}
		return dok;
	}
	
	public String[] finnSensitive(String dok) {
		ArrayList<String> sens = new ArrayList<String>();
		
		ArrayList<String> tabunr = finnPersnr(dok);
		for (String ukvemsnr : tabunr) {
			sens.add(ukvemsnr);
		}
		
		String[] foo = new String[sens.size()];
		return sens.toArray(foo);
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
	
	// main() kjører diverse demo'r
	public static void main(String [] args) throws IOException, COSVisitorException {
		
		String dok = txtFromPdf(args[0]);
		
		SanerePdf sanerer = new SanerePdf("Postnummerregister_ansi.txt");
		
/*		// banale testcasez
		if (sanerer.gyldigPostnr("0301", "Oslo"))
			System.out.println("0301 Oslo (labercase)");
			
		if (SanerePdf.gyldigFnr("27061141273"))
			System.out.println("gyldigFnr: sant");
			
		String[] fods = finnFodsel(dok);
		for (String dato : fods) System.out.println(dato);
		
		String[] nr = sanerer.finnPnrSted(dok);
		for (String nummer : nr) System.out.println(nummer);
*/
		System.out.print(dok);
		System.out.println("--------------------");
		System.out.print(sanerer.sanerAdresser(
			sanerer.sanerPersnr(sanerer.sanerPostnr(dok))));
		
		System.out.println();
		
		// og så mekker vi en sanert utgave av PDF-fila
		String utfil = args[0];
		utfil = utfil.substring(0, utfil.length()-4);
		utfil = utfil + ".sanert.pdf";
		System.out.println(utfil);
		
		ReplaceString pdfreplacer = new ReplaceString();
		pdfreplacer.doIt(args[0],utfil,"fnyrrgorr","fnyrrgorr");
		// ^ kjør PDF'n gjennom kverna en gang for å myke opp teksten
		//  og få flere strenger i klartekst
		ArrayList<String> tabunr = sanerer.finnPersnr(dok);
		for (String ukvemsnr : tabunr) {
			pdfreplacer.doIt(utfil,utfil,ukvemsnr,".....");
		}
		
	}
	
	
	
}