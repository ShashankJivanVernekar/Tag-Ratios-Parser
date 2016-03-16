import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.ToXMLContentHandler;
import org.xml.sax.ContentHandler;


public class TTRExtractor {

	private double[] linkTagList;
	private double[] derivList;
	double[] smoothed_values=new double [500000];
	public static Map<Integer, Double> ttr = new LinkedHashMap<Integer,Double>();
	public static Map<Integer, String> ttr_str = new LinkedHashMap<Integer,String>();
	public String[] extractText(String html, int k) {
		String[] str = null;
		Double res;
		String line;
		html = html.replaceAll("(?s)<!--.*?-->", "");
		html = html.replaceAll("(?s)<script.*?>.*?</script>", "");
		html = html.replaceAll("(?s)<SCRIPT.*?>.*?</SCRIPT>", "");
		html = html.replaceAll("(?s)<style.*?>.*?</style>", "");
		
		
        
		BufferedReader br = new BufferedReader(
				new StringReader(html));
		int numLines = 0;
		try
		{
			while(br.readLine()!=null)
			{
					
			int tag = 0;
			int text = 0;
			line = br.readLine();
			for (int i = 0; i >= 0 && i < line.length(); i++) {
			if (line.charAt(i) == '<') {
				tag++;
				i = line.indexOf('>', i);
				if (i == -1) {
					break;
				}
			} else if (tag == 0 && line.charAt(i) == '>') {
				text = 0;
				tag++;
			} else {
				text++;
			}
			
		}
		if (tag == 0) {
			tag = 1;
		}
		res= (double) text / (double) tag;
		ttr_str.put(numLines, line);
		smoothed_values[numLines]=res;
		numLines++;
		
		
		
		
		}
			
			smoothed_values=smooth(smoothed_values,2);
			for(int i=0; i<numLines;i++)
				ttr.put(i, smoothed_values[i]);
			ArrayList<Integer> arr= new ArrayList<Integer>();
			ttr = sortHashMapByValuesD(ttr);
			int var = 0;
			for(Entry<Integer, Double> entry:ttr.entrySet())
			{
				Integer key = entry.getKey();
				if(var >= numLines/2)
				{
				  	arr.add(key);
				}
				var++;
			}
			int j=0;
			String strFilePath="/Users/Shashank/Downloads/CSCI-599_DATA/TREC/cm_cn_co/out"+j+".txt";
			FileWriter fileWriter = new FileWriter(strFilePath);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			j++;
			
			for(int i=0;i<arr.size();i++)
			{   
				bufferedWriter.write(ttr_str.get(arr.get(i)));
				bufferedWriter.write("\n");
			}
			bufferedWriter.close();
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		
		
		
		return str;
		
		
	}
	
	public LinkedHashMap<Integer,Double> sortHashMapByValuesD(Map<Integer, Double> ttr2) {
		   List<Integer> mapKeys = new ArrayList(ttr2.keySet());
		   List<Double> mapValues = new ArrayList(ttr2.values());
		   Collections.sort(mapValues);
		   Collections.sort(mapKeys);

		   LinkedHashMap<Integer,Double> sortedMap = new LinkedHashMap<Integer,Double>();

		   Iterator<Double> valueIt = mapValues.iterator();
		   while (valueIt.hasNext()) {
		       Object val = valueIt.next();
		       Iterator<Integer> keyIt = mapKeys.iterator();

		       while (keyIt.hasNext()) {
		           Object key = keyIt.next();
		           String comp1 = ttr2.get(key).toString();
		           String comp2 = val.toString();

		           if (comp1.equals(comp2)){
		               ttr2.remove(key);
		               mapKeys.remove(key);
		               sortedMap.put((Integer) key, (Double)val);
		               break;
		           }

		       }

		   }
		   return sortedMap;
		}
	
	private double[] smooth(double in[], int f) {
		int size = in.length;
		double tmp[] = new double[size];

		for (int i = 0; i < size; i++) {
			int cnt = 0;
			int sum = 0;
			for (int j = (f * -1); j <= f; j++) {
				try {
					sum += in[i + j];
					cnt++;
				} catch (ArrayIndexOutOfBoundsException e) {
					// don't increment count
				}
			}
			tmp[i] = (double) sum / (double) cnt;
		}
		return tmp;
	}

	
			public static void main(String[] args) {

		Path path = Paths.get("/Users/Shashank/Downloads/CSCI-599_DATA/TREC/cm_cn_co/");
		File dir = new File(path.toString());
		File[] files = dir.listFiles();
		String type=null;
		TTRExtractor temp= new TTRExtractor();

		for (File f : files)
		{

			if(f.isFile())
			{
				Tika tika =  new Tika();


				try{
					type=tika.detect(f);
					ContentHandler handler = new ToXMLContentHandler();

					AutoDetectParser parser = new AutoDetectParser();
					Metadata metadata = new Metadata();
					try (InputStream stream = new FileInputStream(f)) {
						parser.parse(stream, handler, metadata);
						temp.extractText(handler.toString(),10000);
						System.out.println(handler.toString());
					}
				}
				catch (Exception e) {  
					e.printStackTrace();  

				}  


			}	

		}


	}

}
