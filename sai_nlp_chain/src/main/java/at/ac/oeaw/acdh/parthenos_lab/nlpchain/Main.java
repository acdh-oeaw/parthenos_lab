package at.ac.oeaw.acdh.parthenos_lab.nlpchain;



import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import at.ac.oeaw.acdh.parthenos_lab.nlpchain.helpers.D4ScienceRest;
import at.ac.oeaw.acdh.parthenos_lab.nlpchain.helpers.GlobalVariables;

/*
* @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
*/
public class Main {

    public static void main(String[] args) {
        if(args.length != 2) {
            System.out.println("java -cp <name of jar> at.ac.oeaw.acdh.parthenos_lab.nlpchain.Main <text file to enrich> <minimum confidence level");
            System.exit(1);
        }
        
        new Main().process(args);
        

    }
    
    /**
     * The process method calls the dataminer applications one after the other by http request
     * @param args args[0] := name of the textfile to enhance, args[1] := minumum confidence level
     */
    private void process(String[] args) {
        
        GlobalVariables gv = new GlobalVariables();
        
        D4ScienceRest rest = new D4ScienceRest(gv);
        
        Path inPath = Paths.get(args[0]);
        try {
            
            String tmpFileName = System.currentTimeMillis() + ".txt";
            
            //load up file temporary
            rest.uploadFile(inPath, tmpFileName, "temporary file");
            
            //get public link for uploaded file
            String publicLink = rest.getPublicLink("/" + tmpFileName);
            
            //execute stanbol wrapper
            publicLink = rest.executeExperiment(
                    "http://dataminer-prototypes.d4science.org/wps/WebProcessingService?request=Execute&service=WPS&Version=1.0.0&lang=en-US&Identifier=org.gcube.dataanalysis.wps.statisticalmanager.synchserver.mappedclasses.transducerers.STANBOLWRAPPER&DataInputs=InFile=${0};EnhancementChain=${1};OutputFormat=JSON;", 
                    publicLink, 
                    args[1]);
            
            //execute distanbol (the DM app an d4science, not the webapp on ACDH server!)
            publicLink = rest.executeExperiment(
                    "http://dataminer-prototypes.d4science.org/wps/WebProcessingService?request=Execute&service=WPS&Version=1.0.0&lang=en-US&Identifier=org.gcube.dataanalysis.wps.statisticalmanager.synchserver.mappedclasses.transducerers.DISTANBOL&DataInputs=InputFile=${0};ConfidenceLevel=${1};",
                    publicLink,
                    args[2]); 

            
            //publish the result of distanbol
            publicLink = rest.executeExperiment(
                    "http://dataminer-prototypes.d4science.org/wps/WebProcessingService?request=Execute&service=WPS&Version=1.0.0&lang=en-US&Identifier=org.gcube.dataanalysis.wps.statisticalmanager.synchserver.mappedclasses.transducerers.WEB_APP_PUBLISHER&DataInputs=ZipFile=${0};",
                    publicLink);
            
            
            //create a html output file with a redirect to the web publisher output
            createHTMLOutputFile(publicLink);
            
            //delete temporary uploaded file
            rest.deleteFile("/" + tmpFileName);
        }
        catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }

    }
    
    /**
     * The method aims basically to create an html file with a redirect to the start-page provided by the web app publisher
     * @param publicLink Link to wrap into a redirect
     */
    private void createHTMLOutputFile(String publicLink) {
        Path path = Paths.get("out.html");
        
        try(BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE)){
            writer.write("<html>");
            writer.write("\t<head>");
            writer.write("\t\t<meta http-equiv=\"refresh\" content=\"0; url=" + publicLink + "\" />");
            writer.write("\t</head>");
            writer.write("\t<body>");
            writer.write("\t\t<p><a href=\"" + publicLink + "\">Redirect</a></p>");
            writer.write("\t</body>");
            writer.write("</html>");
        }
        catch (IOException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
    }
}
