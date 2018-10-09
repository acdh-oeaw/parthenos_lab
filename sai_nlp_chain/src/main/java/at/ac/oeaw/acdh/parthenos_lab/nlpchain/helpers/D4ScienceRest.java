package at.ac.oeaw.acdh.parthenos_lab.nlpchain.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * The class D4ScienceRest provides some methods to interact with a workspace or to execute an experiment
* @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
*/
public class D4ScienceRest {
    private final String HL_WEBAPP = "https://workspace-repository.d4science.org/home-library-webapp";
    private final GlobalVariables globalVariables;
    
    private static final Pattern stringPattern = Pattern.compile("<string>(http.+)</string>");
    private static final Pattern dataPattern = Pattern.compile("<d4science:Data>(http.+)</d4science:Data>");
    
    public D4ScienceRest(GlobalVariables globalVariables) {
        this.globalVariables = globalVariables;
    }
    
    /**
     * @param inPath path of the file on your local machine to upload to your d4sciece workspace
     * @param fileName name, the file will have in your workspace (without leading slash!)
     * @param description a brief description of the file
     * @return true if upload is successful, false otherwise
     * @throws Exception
     */
    public boolean uploadFile(Path inPath, String fileName, String description) throws Exception {

        String urlString = HL_WEBAPP + "/rest/Upload?" + "name=" + URLEncoder.encode(fileName, "UTF-8") + "&description=" + URLEncoder.encode(description, "UTF-8") + 
        "&parentPath=/Home/" + this.globalVariables.getUserName() + "/Workspace";
 
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestProperty("gcube-token", globalVariables.getUserToken());
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);
        con.setRequestProperty("Content-Type", "text/plain");
        con.setRequestMethod("POST");
 
        byte[] buffer = new byte[1000000];
        int bytesRead;
        // Write file to response.
        
        InputStream inStream = Files.newInputStream(inPath, StandardOpenOption.READ);
        OutputStream outStream = con.getOutputStream();
        
        while((bytesRead = inStream.read(buffer)) != -1) {
        
            outStream.write(buffer, 0, bytesRead);
        }
        
        outStream.close();
 
        BufferedReader r = new BufferedReader(new  InputStreamReader(con.getInputStream()));

        String line;
        
        return ((line = r.readLine()) != null) && line.contains(fileName);
    } 
    
    /**
     * @param pathInWorkspace path of the file to delete in the workspace (with leading slash!)
     * @return true, if the deletion is successful, false otherwise
     * @throws IOException
     */
    public boolean deleteFile(String pathInWorkspace) throws IOException {
        String urlString = HL_WEBAPP + "/rest/Delete?absPath=/Home/" + this.globalVariables.getUserName() + "/Workspace" + pathInWorkspace;
        
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestProperty("gcube-token", this.globalVariables.getUserToken());
        con.setRequestMethod("GET");
        
        BufferedReader reader = new BufferedReader(new  InputStreamReader(con.getInputStream()));
        
        String line;
        
        return  ((line = reader.readLine()) != null) && line.equals("<boolean>true</boolean>");
    }
    
    /**
     * @param pathInWorkspace path of the file in the workspace to get the public link from (with leading slash!)
     * @return the public link of the file or null, if something goes wrong
     * @throws IOException
     */
    public String getPublicLink(String pathInWorkspace) throws IOException {
        String retString = null;
        
        String urlString = HL_WEBAPP + "/rest/GetPublicLink?absPath=/Home/" + this.globalVariables.getUserName() + "/Workspace" + pathInWorkspace;
        
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestProperty("gcube-token", this.globalVariables.getUserToken());
        con.setRequestMethod("GET");
        
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String line;
        Matcher matcher;
        
        if ((line = in.readLine()) != null && (matcher  = stringPattern.matcher(line)).find()) {
            retString = matcher.group(1);
        }
        in.close();
              
        con.disconnect();
        
        return retString;
    }
    
    /**
     * @param httpRequest the http request to execute a data miner application
     * @param args dynamic array of arguments which replace the ${x} place holders in the http request (with x = 0, 1, 2, ...)
     * @return the public link to resulting file
     * @throws IOException
     */
    public String executeExperiment(String httpRequest, String... args) throws IOException {
        String returnValue = null;
        
        for(int i=0; i<args.length; i++) {
            httpRequest = httpRequest.replace("${" + i + "}", URLEncoder.encode(args[i], "UTF-8"));
        }
                
        
        URL url = new URL(httpRequest);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestProperty("gcube-token", this.globalVariables.getUserToken());
        con.setRequestMethod("GET");
        
        
        BufferedReader in = new BufferedReader(
                
        new InputStreamReader(con.getInputStream()));
        String line;
        Matcher matcher;
        
        while ((line = in.readLine()) != null) {
            if((matcher = dataPattern.matcher(line)).find()) {
                
                returnValue = matcher.group(1);
            }
            
        }
        in.close();
              
        con.disconnect();
        
        return returnValue;
    }
}
