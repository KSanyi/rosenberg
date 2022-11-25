package hu.kits.rosenberg;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import hu.kits.rosenberg.Dictionary.DictionaryEntry;
import hu.kits.rosenberg.SearchResult.DictionarySearchResult;
import io.javalin.json.JsonMapper;

public class RosenbergJsonMapper implements JsonMapper {

    @Override
    public String toJsonString(Object data, Type type) {
        return toJson(data).toString();
    }
    
    public Object toJson(Object data) {
        
        if(data instanceof Collection) {
            List<?> collection = (List<?>)data; 
            return new JSONArray(collection.stream().map(e -> toJson(e)).collect(toList()));
        } else if(data instanceof Map) {
            Map<?, ?> map = (Map<?, ?>)data;
            Map<?, ?> jsonEntriesMap = map.entrySet().stream().collect(Collectors.toMap(
                    e -> e.getKey().toString(),
                    e -> toJson(e.getValue()),
                    (a, b) -> a, LinkedHashMap::new));
            return new JSONObject(jsonEntriesMap);
        } else if(data instanceof SearchResult) {
            return mapToJson((SearchResult)data);
        } else if(data instanceof DictionarySearchResult) {
            return mapToJson((DictionarySearchResult)data);
        } else if(data instanceof DictionaryEntry) {
            return mapToJson((DictionaryEntry)data);
        } else {
            return data;
        }
    }
    
    private JSONObject mapToJson(SearchResult searchResult) {
        return new JSONObject()
                .put("queryString", searchResult.queryString())
                .put("entries", toJson(searchResult.dictionarySearchResults()));
    }
    
    private JSONObject mapToJson(DictionarySearchResult dictionarySearchResult) {
        return new JSONObject()
                .put("dictionaryCode", dictionarySearchResult.dictionaryCode())
                .put("matchingEntries", toJson(dictionarySearchResult.matchingEntries()));
    }
    
    private static JSONObject mapToJson(DictionaryEntry dictionaryEntry) {
        return new JSONObject()
                .put("word", dictionaryEntry.word())
                .put("description", dictionaryEntry.description());
    }
    
}
