package hu.kits.rosenberg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.kits.rosenberg.Dictionary.DictionaryEntry;
import hu.kits.rosenberg.SearchResult.DictionarySearchResult;

public class Dictionaries {

    public static Map<String, Dictionary> dictionaries = new HashMap<>();
    
    public static void setDictionary(String name, Dictionary dictionary) {
        dictionaries.put(name, dictionary);
    }

    public static SearchResult search(String word) {
        
        if(word.length() < 3) return SearchResult.empty(word);
        
        List<DictionarySearchResult> dictionarySearchResults = new ArrayList<>();
        
        for(String dictionaryCode : dictionaries.keySet()) {
            Dictionary dictionary = dictionaries.get(dictionaryCode);
            List<DictionaryEntry> entries = dictionary.search(word);
            dictionarySearchResults.add(new DictionarySearchResult(dictionaryCode, entries));
        }
        
        return new SearchResult(word, dictionarySearchResults);
    }
    
}
