package at.ac.oeaw.acdh.parthenos_lab.nlpchain.helpers;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;


/*
 * Each start of a data miner application creates a file globalvariables.csv in the home directory. The file contains important information like the 
 * user name and the user's gcube-token. Both we need if we want to call a data miner application via http request. 
 * The class  GlobalVariables reads the csv-file and makes the information available by get-methods. 
* @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
*/

public class GlobalVariables {
    private final Map<String,String> globalVariables;
    
    /**
     * The standard constructor which reads glabalvariables.csv from the home directory
     */
    public GlobalVariables() {
        this(Paths.get("globalvariables.csv"));
    }
    
    
    /**
     * @param csvFilePath An explicit path to a csv-file with global variables
     */
    public GlobalVariables(Path csvFilePath) {
        this.globalVariables = new HashMap<String,String>();
        
        try(
            Reader reader = Files.newBufferedReader(csvFilePath);
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
        ){
            csvParser.forEach(record -> this.globalVariables.put(record.get(0), record.get(1)));
            
        }
        catch (IOException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }      
    }
    
    public String getUserName() {
        return this.globalVariables.get("gcube_username");
    }
    
    public String getUserToken() {
        return this.globalVariables.get("gcube_token");
    }
    
    public String getContext() {
        return this.globalVariables.get("gcube_context");
    }
}
