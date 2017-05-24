
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
import java.io.FileReader;
import java.net.UnknownHostException;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class Main {
    
    private static String id;
    
    public static void main(String [ ] args) throws UnknownHostException{
        
        JSONArray stix = getJson();
        MongoClient mongoClient = new MongoClient("localhost", 27017);        
        MongoDatabase db = mongoClient.getDatabase("myDB");
        MongoCollection<Document> coll = db.getCollection(id);
        
        
        
        
        
        for(Object jsonObject : stix){
        Document stixdoc = Document.parse(jsonObject.toString());
        
        coll.insertOne(stixdoc);
        }
       
      
        FindIterable<Document> cursor = coll.find(eq("type", "campaign"));
        for(Document document : cursor){
           
        System.out.println(document);
        }
        
        
    }
    
    private static JSONArray getJson(){
        
        JSONParser parser = new JSONParser();
        
        try{
            
            Object obj = parser.parse(new FileReader("D:\\Daten\\Bachelorarbeit\\poisonivy.json"));
            JSONObject jsonObject = (JSONObject) obj;
            id = (String) jsonObject.get("id");
            
            JSONArray jsonArray = (JSONArray) jsonObject.get("objects");
            //System.out.println(jsonArray);                   
           
            return jsonArray;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
