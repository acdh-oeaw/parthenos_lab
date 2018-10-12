package at.ac.oeaw.acdh.nerlix.stanbol;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import at.ac.oeaw.acdh.nerlix.stanbol.enhancements.EntityEnhancement;
import at.ac.oeaw.acdh.nerlix.stanbol.enhancements.TextEnhancement;
import at.ac.oeaw.acdh.nerlix.stanbol.elements.Viewable;


/*
* @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
*/
public class SAIDistanbol {

    public static void main(String[] args) {
        if(args.length != 2) {
            System.out.println("call java at.ac.oeaw.acdh.nerlix.stanbol.SAIDistanbol <input file name> <confidence level>");
            System.exit(1);
        }
        
        File inFile = new File(args[0]);
        
        if(!inFile.exists()) {
            System.out.println("the input file " + inFile.getAbsolutePath() + " doesn't exist");
            System.exit(1);
        }
        
        double confidenceLevel;
        
        try{
            confidenceLevel = Double.parseDouble(args[1]);
            if(confidenceLevel < 0 || confidenceLevel > 1)
                throw new Exception();
        }
        catch(Exception ex) {
            System.out.println("confidence level not in the interval [0, 1] - using default level 0.7");
            confidenceLevel = 0.7;
            
        }
        
        
        new SAIDistanbol().process(inFile, confidenceLevel);

    }
    
    private void process(File inFile, double confidenceLevel) {
        //create html from json
        try {
            
            //System.out.println(this.getClass().getClassLoader().getResource("view.html").getFile());

            Document doc = Jsoup.parse(this.getClass().getClassLoader().getResourceAsStream("view.html"), "utf-8", "");


            //todo put the labels in properties file(even though the architecture is a little bit fuzzy for me right now)

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(inFile);


            if (jsonNode.isArray()) {
                Iterator<JsonNode> iterator = jsonNode.elements();


                ArrayList<Viewable> viewables = new ArrayList<Viewable>();
                ArrayList<EntityEnhancement> entityEnhancements = new ArrayList<EntityEnhancement>();
                ArrayList<TextEnhancement> textEnhancements = new ArrayList<TextEnhancement>();

                //create an object for each node and save them into respective lists
                while (iterator.hasNext()) {
                    JsonNode node = iterator.next();


                    //this happens only once
                    if (node.get("fulltext") != null) {


                            Element fulltextHTML = doc.getElementById("fulltext");
                            String fulltext = node.get("fulltext").asText();

                            fulltextHTML.append(fulltext);


                    } 
                    else {

                        //there are three types of nodes: viewables, entity enhancements and text enhancements.
                        //Viewables are entities to display.
                        //Entity enhancements contain confidence information
                        //Text enhancements contain context information.

                        ArrayNode typesNode = (ArrayNode) node.get("@type");
                        if (typesNode != null) {
                            if (typesNode.size() == 2 && typesNode.get(0).asText().equals("http://fise.iks-project.eu/ontology/Enhancement")) {

                                switch (typesNode.get(1).asText()) {
                                    case "http://fise.iks-project.eu/ontology/TextAnnotation":
                                        TextEnhancement textEnhancement = new TextEnhancement(node);
                                        textEnhancements.add(textEnhancement);
                                        break;
                                    case "http://fise.iks-project.eu/ontology/EntityAnnotation":
                                        EntityEnhancement entityEnhancement = new EntityEnhancement(node);
                                        //only take entity enhancements that are over the threshold,

                                        if (entityEnhancement.getConfidence() >= confidenceLevel) {
                                            entityEnhancements.add(entityEnhancement);
                                        }

                                        break;
                                    default:
                                        System.out.println("The given Stanbol output is not valid.");
                                        System.exit(1);
                                }

                            } 
                            else {
                                Viewable viewable = new Viewable(node);
                                viewables.add(viewable);
                            }

                        } 
                        else {
                            //node has no type, it means it is a viewable
                            Viewable viewable = new Viewable(node);
                            viewables.add(viewable);
                        }
                    }

                }


                ArrayList<Viewable> finalViewables = new ArrayList<>();

                //Each viewable has exactly one matching entityEnhancement.
                //Each entityEnhancement can have one or more textEnhancements.

                //entityEnhancements list is already filtered above to take confidence higher than threshold
                for (Viewable viewable : viewables) {

                    for (EntityEnhancement entityEnhancement : entityEnhancements) {

                        if (viewable.getId().equals(entityEnhancement.getReference())) {

                            viewable.setEntityEnhancement(entityEnhancement);
                            for (TextEnhancement textEnhancement : textEnhancements) {

                                if (entityEnhancement.getRelations().contains(textEnhancement.getId())) {
                                    viewable.addTextEnhancement(textEnhancement);
                                }
                            }

                            if (!finalViewables.contains(viewable)) {
                                finalViewables.add(viewable);
                            }


                        }

                    }
                }


                Element viewablesHTML = doc.getElementById("viewables");

                Element formHTML = doc.getElementById("tableBody");
                boolean firstElement = true;

                for (Viewable viewable : finalViewables) {

                    //to have a small space between elements
                    if (firstElement) {
                        firstElement = false;
                    } 
                    else {
                        viewablesHTML.append("<hr>");
                    }


                    viewablesHTML.append(viewable.getHTMLDepiction("Viewable.html"));


                    formHTML.append(viewable.getHTMLTableRowDepiction());
                    



                }
            }
            writeDocToZip(doc);
        } 
        catch (IOException e) {
            System.out.println("Can't read input json file: "+ inFile.getAbsolutePath());
            e.printStackTrace();
        }
        

    }
            
    private void writeDocToZip(Document doc) {
        
        
        
        Path outPath = Paths.get("distanbol.zip");
        
        
        //copy base.zip file (contains static elements like css and javascript) to output file »distanbol.zip«
        try {

            Files.copy(this.getClass().getClassLoader().getResourceAsStream("base.zip"), outPath, StandardCopyOption.REPLACE_EXISTING);

        
            Map<String, String> env = new HashMap<>(); 
            
            env.put("create", "true");
        
            URI uri = URI.create("jar:" + outPath.toUri());
            
            FileSystem fs = FileSystems.newFileSystem(uri, env);
            
            //adding distanbol output as index.html to the zip-file distanbol.zip
            Path nf = fs.getPath("index.html");
            
            Writer writer = Files.newBufferedWriter(nf, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
            
            writer.write(doc.html());
            
            writer.close();
            
            fs.close();
        
        }
        catch (IOException ex) {
            System.out.println("can't create zipped output file");
            ex.printStackTrace();
        }
 
    }
}
