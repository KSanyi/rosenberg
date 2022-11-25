package hu.kits.rosenberg;

import java.util.List;

public class Dictionary {

    private final List<DictionaryEntry> entries;
    
    public Dictionary(List<DictionaryEntry> entries) {
        this.entries = entries;
    }

    public record DictionaryEntry(String word, String description) {

        public boolean matches(String queryString) {
            return word.toLowerCase().contains(queryString.toLowerCase());
        }
    }

    public SearchResult search(String queryString) {

        if(queryString.length() < 3) return SearchResult.empty(queryString);
        
        List<DictionaryEntry> foundEntries = entries.stream().filter(e -> e.matches(queryString)).toList();
        return new SearchResult(queryString, foundEntries);
    }
    
}
