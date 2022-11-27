package hu.kits.rosenberg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.kits.rosenberg.Dictionary.DictionaryData;
import hu.kits.rosenberg.Dictionary.DictionaryEntry;
import hu.kits.rosenberg.SearchResult.DictionarySearchResult;

public class Dictionaries {

    private final Map<String, Dictionary> dictionaries;
    
    public Dictionaries(Map<String, Dictionary> dictionaries) {
        this.dictionaries = new HashMap<>(dictionaries);
    }
    
    public void setDictionary(String name, Dictionary dictionary) {
        dictionaries.put(name, dictionary);
    }

    public SearchResult search(String word) {
        
        if(word.length() < 3) return SearchResult.empty(word);
        
        List<DictionarySearchResult> dictionarySearchResults = new ArrayList<>();
        
        for(String dictionaryCode : dictionaries.keySet()) {
            Dictionary dictionary = dictionaries.get(dictionaryCode);
            List<DictionaryEntry> entries = dictionary.search(word);
            dictionarySearchResults.add(new DictionarySearchResult(dictionaryCode, entries));
        }
        
        return new SearchResult(word, dictionarySearchResults);
    }

    public boolean containsDictionary(String dictionaryId) {
        return dictionaries.containsKey(dictionaryId);
    }

    public List<DictionaryData> getDictionaryData() {
        return dictionaries.values().stream().map(Dictionary::toDictionaryData).toList();
    }

}
