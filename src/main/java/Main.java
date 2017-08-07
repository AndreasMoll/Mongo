import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Main {
    
    private static String id;
    private static JSONArray newNodes = new JSONArray();
    
    //Reads a STIX .json file, stores the data in a Mongo collection and creates a new .json file with additional indexes for d3 usage
    public static void main(String [ ] args) throws UnknownHostException{
        
        
        MongoClient mongoClient = new MongoClient("localhost", 27017);        
        MongoDatabase db = mongoClient.getDatabase("myDB");
                
        //For adding a new stix file to the database        
        JSONArray stix = getJson();
        MongoCollection<Document> coll = db.getCollection(id);
        
        for(Object jsonObject : stix){
        Document stixdoc = Document.parse(jsonObject.toString());        
        coll.insertOne(stixdoc);
        }
       
        JSONArray nodes = new JSONArray();
        JSONArray links = new JSONArray();        
        
        FindIterable<Document> objectCursor = coll.find(in("type", "campaign", "malware", "attack-pattern", "course-of-action",
                "identity", "vulnerability", "tool", "indicator", "intrusion-set", "observed-data", "threat-actor", "report"));
        
        
        for(Document document : objectCursor){ //Adds object-documents to a JSONArray for nodes
            
        document.remove("_id");
        
        //used for creating additional relationships from "created_by_ref" objects
        if(document.containsKey("created_by_ref")){
            Document newRelationship = new Document("id", "relationshipcreatedby").append("type", "relationship").
                    append("source_ref", document.get("created_by_ref")).append("target_ref", document.get("id")).
                    append("created", document.get("created")).append("modified", document.get("modified")).
                    append("relationship_type", "created_by_ref");
            coll.insertOne(newRelationship);
            }
        
        nodes.add(document);
        
        }        
                
        FindIterable<Document> relationshipCursor = coll.find(eq("type", "relationship"));
        JSONParser parser = new JSONParser();       
        
        try {
                Object obj = parser.parse(nodes.toJSONString());
                newNodes = (JSONArray) obj;     
                
            } catch (ParseException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        for(Document document : relationshipCursor){ //Adds relationship-documents to a JSONArray for links
            int index;
            document.remove("_id");              
            
            Object sourceId = document.get("source_ref");
            index = getIndex(sourceId.toString());
            document.append("source", index);
                       
            Object targetId = document.get("target_ref");
            index = getIndex(targetId.toString());
            document.append("target", index);   
            
            links.add(document); 
            
        }
        JSONObject finalJson = new JSONObject(); //Create final JSON with a nodes- and a links-array
        finalJson.put("nodes", nodes);
        finalJson.put("links", links);
        try(FileWriter file = new FileWriter("D:\\test.json")){
            file.write(finalJson.toJSONString());
            file.flush();
        }
        catch(IOException e){
            e.printStackTrace();
        }        
        
    }
    
    //Returns the index of the passed node in the JSONArray
    private static int getIndex(String ref){
        JSONParser parser = new JSONParser();
        int i;
        for(i = 0; i < newNodes.size(); i++){
                String str = newNodes.get(i).toString();
                try {
                    Object obj = parser.parse(str);
                    JSONObject jsonObject = (JSONObject) obj;
                    if(ref.equals(jsonObject.get("id").toString())){
                        return i;
                    }
                    else{
                        
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        return i;
    }
    
    //Reads the specified .json file and returns the "objects" array as JSONArray
    private static JSONArray getJson(){
        
        JSONParser parser = new JSONParser();
        
        try{
            
            Object obj = parser.parse(new FileReader("D:\\Daten\\Bachelorarbeit\\poisonivy.json"));
            JSONObject jsonObject = (JSONObject) obj;
            id = (String) jsonObject.get("id");
            
            JSONArray jsonArray = (JSONArray) jsonObject.get("objects");                     
           
            return jsonArray;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
