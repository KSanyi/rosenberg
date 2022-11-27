package hu.kits.rosenberg;

import java.time.LocalDateTime;
import java.util.List;

public record Dictionary(String id, 
        String fileName, 
        LocalDateTime uploaded, 
        List<DictionaryEntry> entries) {

    public record DictionaryEntry(String word, String description) {

        public boolean matches(String queryString) {
            return word.toLowerCase().contains(queryString.toLowerCase());
        }
    }

    public List<DictionaryEntry> search(String queryString) {
        return entries.stream().filter(e -> e.matches(queryString)).toList();
    }
    
    public DictionaryData toDictionaryData() {
        return new DictionaryData(id, fileName, uploaded, entries.size());
    }
    
    public static record DictionaryData(String id, 
        String fileName, 
        LocalDateTime uploaded, int numbrOfEntries) {}
    
}
