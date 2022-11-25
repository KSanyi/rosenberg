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

    public List<DictionaryEntry> search(String queryString) {
        return entries.stream().filter(e -> e.matches(queryString)).toList();
    }
    
}
