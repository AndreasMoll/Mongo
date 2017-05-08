
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
import java.io.FileReader;
import java.net.UnknownHostException;
import org.bson.Document;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class Main {
    
    
    
    public static void main(String [ ] args) throws UnknownHostException{
        
        MongoClient mongoClient = new MongoClient("localhost", 27017);        
        MongoDatabase db = mongoClient.getDatabase("myDB");
        MongoCollection<Document> coll = db.getCollection("TestCollection");
        
        //Document doc = new Document("name", "MongoDB").append("test", "50");
        JSONObject stix = getJson();
        Document stixdoc = Document.parse(stix.toString());
        
        //coll.insertOne(stixdoc);
        
       
        //FindIterable<Document> cursor = coll.find();
        FindIterable<Document> cursor = coll.find(eq("objects.type", "malware"));
        for(Document document : cursor){
           
        System.out.println(document);
        }
        
        
    }
    
    private static JSONObject getJson(){
        
        JSONParser parser = new JSONParser();
        
        try{
            
            Object obj = parser.parse(new FileReader("D:\\Daten\\Bachelorarbeit\\poisonivy.json"));
            JSONObject jsonObject = (JSONObject) obj;
            String type = (String) jsonObject.get("type");
            //System.out.println(type);
            String str = jsonObject.toJSONString();
            //System.out.println(str);
            return jsonObject;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
