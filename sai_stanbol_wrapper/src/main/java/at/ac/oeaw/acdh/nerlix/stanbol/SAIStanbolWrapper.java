/**
 * 
 */
package at.ac.oeaw.acdh.nerlix.stanbol;

/**
 * @author WolfgangWalter SAUER (wowasa) <wolfgang.sauer@oeaw.ac.at>
 *
 */

import java.io.*;
import java.nio.file.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class SAIStanbolWrapper {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args){
		try {
			if(args.length < 3) {
				System.out.println("insufficient number of parameters");
				System.exit(1);
			}
			
			Path inFile = Paths.get(args[0]);
;
			String contentType = "text/plain";
			String stanbolURLString = "https://enrich.acdh.oeaw.ac.at/enhancer/chain/";
			String acceptType;
			
			
			
			switch(args[1]) {
			case "COUNTRIES":
				stanbolURLString += "geoNames_PCLI";
				break;
			case "CITIES":
				stanbolURLString += "geoNames_PPLC";
				break;
			case "LOCATIONS":
				stanbolURLString += "geoNames_SPAsubset";
				break;
			default: 
				stanbolURLString += "dbpedia-fst-linking";
			}
			
			switch(args[2]) {
			case "RDF_XML":
				acceptType = "application/rdf+xml";
				break;
			case "RDF_JSON":
				acceptType = "application/rdf+json";
				break;
			case "TURTLE":
				acceptType = "text/turtle";
				break;
			case "N_TRIPLES":
				acceptType = "text/rdf+nt";
				break;
			default:
				acceptType = "application/json";
			}
			
		

			URL stanbolURL = new URL(stanbolURLString);
			HttpURLConnection stanbolCon = (HttpURLConnection) stanbolURL.openConnection();
			stanbolCon.setRequestMethod("POST");
			stanbolCon.setRequestProperty("Accept", acceptType);
			stanbolCon.setRequestProperty("Content-Type", contentType);
			stanbolCon.setUseCaches(false);
			stanbolCon.setDoOutput(true); 
			
			byte[] buffer = new byte[1000000];
			
			int bytesRead;
			
			OutputStream stanbolOut = stanbolCon.getOutputStream();
			InputStream fileIn = Files.newInputStream(inFile, StandardOpenOption.READ);
			
			
			while((bytesRead = fileIn.read(buffer))>0) {

			    stanbolOut.write(buffer, 0, bytesRead);
			}
			
			stanbolOut.flush();
			stanbolOut.close();
			fileIn.close();
			
			InputStream stanbolIn = stanbolCon.getInputStream();
			
			Path outPath = Paths.get("stanbolOut.txt");
			
			OutputStream fileOut = Files.newOutputStream(outPath, StandardOpenOption.CREATE); 
			
			if(acceptType.equals("application/json")) {  //adding the input-text to the json output
			
    			fileIn = Files.newInputStream(inFile, StandardOpenOption.READ);
    			

    			//adding text as json
    			fileOut.write("[{\"fulltext\":\"".getBytes());
    	         while((bytesRead = fileIn.read(buffer)) > 0){
                    fileOut.write(toJSON(buffer, bytesRead));
                }
                fileOut.write("\"},\n".getBytes());
                 
                fileIn.close(); 
    	        
                //reading from stanbol while replacing the opening bracket '[' 
    	        if((bytesRead = stanbolIn.read(buffer)) > 1) {
    	            fileOut.write(buffer, 1, bytesRead -1);
    	        }
    	        
    	        
			}
			
			while((bytesRead = stanbolIn.read(buffer)) > 0){
				fileOut.write(buffer, 0, bytesRead);
			}
			
			fileOut.flush();
			fileOut.close();
			
			stanbolIn.close();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
	
	private static byte[] toJSON(byte[] text, int bytesRead) {
	    return new String(text, 0, bytesRead)
        .replace("\\", "\\\\") 
	    .replace("\b", "\\b")
	    .replace("\f", "\\f")
	    .replace("\n", "\\n")
	    .replace("\r", "\\r")
	    .replace("\t", "\\t")
	    .replace("\"", "\\\"")
	    .getBytes();
	}

}
