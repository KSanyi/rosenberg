package hu.kits.rosenberg;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Type;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import hu.kits.rosenberg.Dictionary.DictionaryEntry;
import io.javalin.json.JsonMapper;

public class RosenbergJsonMapper implements JsonMapper {

    @Override
    public String toJsonString(Object data, Type type) {
        return toJson(data, type).toString();
    }
    
    public Object toJson(Object data, Type type) {
        
        if(type == List.class) {
            List<?> collection = (List<?>)data; 
            return new JSONArray(collection.stream().map(e -> toJson(e, e.getClass())).collect(toList()));
        } else if(type == SearchResult.class) {
            return mapToJson((SearchResult)data);
        } else if(type == DictionaryEntry.class) {
            return mapToJson((DictionaryEntry)data);
        } else {
            return data;
        }
    }
    
    private JSONObject mapToJson(SearchResult searchResult) {
        return new JSONObject()
                .put("queryString", searchResult.queryString())
                .put("entries", toJson(searchResult.matchingEntries(), List.class));
    }
    
    private static JSONObject mapToJson(DictionaryEntry dictionaryEntry) {
        return new JSONObject()
                .put("word", dictionaryEntry.word())
                .put("description", dictionaryEntry.description());
    }
    
}
