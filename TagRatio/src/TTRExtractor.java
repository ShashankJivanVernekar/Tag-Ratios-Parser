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

	
		/*
		html = html.replaceAll("(?s)<!--.*?-->", "");
		html = html.replaceAll("(?s)<script.*?>.*?</script>", "");
		html = html.replaceAll("(?s)<SCRIPT.*?>.*?</SCRIPT>", "");
		html = html.replaceAll("(?s)<style.*?>.*?</style>", "");
		
		String[] result = new String[2];
		result[0] = "";
		result[1] = "";
		try {
		
			Parser p = new Parser(html);
			NodeList nl = p.parse(null);
			NodeList list = nl.extractAllNodesThatMatch(new TagNameFilter(
					"TITLE"), true);
			if (list.size() >= 1) {
				result[0] = list.elementAt(0).toPlainTextString();
			}

			BufferedReader br = new BufferedReader(
					new StringReader(nl.asHtml()));
			int numLines = 0;
			while (br.readLine() != null) {
				numLines++;
			}
			br.close();

			if (numLines == 0) {
				return result;
			}


			// numLines must be even!
			//if (numLines % 2 != 0) {
			//	numLines++;
			//}
			
			linkTagList = new double[numLines];

			String line;
			br = new BufferedReader(new StringReader(nl.asHtml()));
			for (int i = 0; i < linkTagList.length; i++) {
				line = br.readLine();
				line = line.trim();
				if (line.equals("")) {
					continue;
				}
				linkTagList[i] = getTextToTagRatio(line);
			}
			br.close();

			derivList = computeDerivs(linkTagList);
			linkTagList = smooth(linkTagList, 2);

			result[1] = computeWekaCluster(new SimpleKMeans(), nl, k);

		} catch (MalformedURLException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		} catch (ParserException e) {
			System.out.println(e);
			return result;
		}
		return result;
	}
	
	private double[] computeDerivs(double[] list) {
		double[] newList = new double[list.length];
		for(int i=0; i<list.length-1; i++){
			if(list.length-i > 3){
				double sum = list[i+1] + list[i+2] + list[i+3];
				double avg = sum/3.0;
				double deriv = list[i] - avg;
				newList[i] = Math.abs(deriv);
			}else if(list.length-i == 3){
				double sum = list[i+1] + list[i+2];
				double avg = sum/2.0;
				double deriv = list[i] - avg;
				newList[i] = Math.abs(deriv);
			}else if(list.length-i == 2){
				double sum = list[i+1];
				double avg = sum;
				double deriv = list[i] - avg;
				newList[i] = Math.abs(deriv);
			}
			//newList[i] = list[i] - list[i+1];
		}
		return newList;
	}

	public String removeAllTags(String html) throws ParserException{
		try {
			Parser p = new Parser(html);
			NodeList nl = p.parse(null);
			return getText(nl, new StringBuffer());
		} catch (Exception e) {
			return html.replaceAll("<[^>]*>", "");
		}
	}
	
	public String getText(NodeList nl, StringBuffer sb){
		int i = 0;
		while (i < nl.size()) {
			Node next = nl.elementAt(i);
			if (next instanceof TextNode) {
				sb.append(((TextNode)next).getText()).append(" ");
			}else{
				if (next.getChildren() != null) {
					getText(next.getChildren(), sb);
				}
			}
			i++;
		}
		return sb.toString();
	}

	

	/**
	 * Compute the text to tag ratio of the line. Text is recorded as the number
	 * of characters in the String. Tag is recorded as the number of unique HTML
	 * tags that are found.
	 * 
	 * @param line
	 *            Single line of HTML text.
	 * @return The text to tag ratio of the given line. If no tags are found
	 *         then the text only is reported.
	 *
	private double getTextToTagRatio(String line) {
		int tag = 0;
		int text = 0;

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
		return (double) text / (double) tag;
	}

	/**
	 * Smoothes the array by taking the moving average of the array with a
	 * radius of f.
	 * 
	 * @param in
	 *            Array to be smoothed
	 * @param f
	 *            Radius to smooth by
	 * @return The smoothed array
	 *
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

	private String computeWekaCluster(Clusterer cl, NodeList nl,
			int numClusters) throws IOException {
		 ArrayList<Attribute> fv = new ArrayList();
		fv.add(new Attribute("textToTagRatio"));
		fv.add(new Attribute("derivList"));
		Instances inst = new Instances("TEXT_TO_TAG", fv, 10);

		ClusterEvaluation eval = new ClusterEvaluation();
		for (int i = 0; i < linkTagList.length; i++) {
			Instance ins = new DenseInstance(fv.size());
			ins.setValue(0, linkTagList[i]);
			ins.setValue(1, derivList[i]);
			inst.add(ins);
		}

		double bestClusterNum = 0;;


			try {
				if (cl instanceof EM) {
					((EM) cl).setMaxIterations(100);
					((EM) cl).setNumClusters(numClusters);
				} else if (cl instanceof SimpleKMeans) {
					((SimpleKMeans) cl).setNumClusters(numClusters);
				} else if (cl instanceof FarthestFirst) {
					((FarthestFirst) cl).setNumClusters(numClusters);
				} else if (cl instanceof AbstractClusterer) {
					
				} else if (cl instanceof Cobweb) {
					throw new Exception("Can't do Cobweb!");
					// can't do cobweb... no clustering input available
				} else {
					throw new Exception("What are you thinking?!");
					// you're on your own
				}
				cl.buildClusterer(inst);				
				eval.setClusterer(cl);
				eval.evaluateClusterer(inst);
				eval.getClusterAssignments();
				
	
				Instances cents = ((SimpleKMeans)cl).getClusterCentroids();
				bestClusterNum = 0;
				double lowest = Double.MAX_VALUE;
				
				
				for(int c=0; c<cents.numInstances(); c++){
					if(cents.instance(c).value(0) < lowest){
						lowest = cents.instance(c).value(0);
						bestClusterNum = c;
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}

		return clusterHtml(nl, eval.getClusterAssignments(), bestClusterNum);
	}

	private int getBestCluster(String results) {
		String working = results.substring(results.indexOf(" centers"), results
				.indexOf("Distortion:"));
		StringTokenizer st = new StringTokenizer(working);
		boolean start = false;
		double best = Double.MAX_VALUE;
		int bestCluster = 0;
		while(st.hasMoreTokens()){
			String tok = st.nextToken();
			if(tok.equals("Cluster")){
				start = true;
			}
			if(start){
				int clust = Integer.parseInt(st.nextToken());
				double x = Double.parseDouble(st.nextToken());
				double y = Double.parseDouble(st.nextToken());
				if(x*y < best){
					best = x*y;
					bestCluster = clust;
				}
				start = false;
			}
		}
		return bestCluster;
	}

	private String clusterHtml(NodeList nl, double[] assignments,
			double clusterNum) throws IOException {
		StringBuffer sb = new StringBuffer();
		BufferedReader br = new BufferedReader(new StringReader(nl.asHtml()));

		String line;
		try {
			for (int i = 0; (line = br.readLine()) != null; i++) {
				line = line.trim();
				if (line.equals("")) {
					continue;
				}

				if (clusterNum != assignments[i]) {
					sb.append(line).append('\n');
				}
			}
			br.close();
			return sb.toString();
		} catch (Exception e) {
			return "";
		}
	}
	*/
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
