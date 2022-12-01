package hu.kits.rosenberg;

import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;

import hu.kits.rosenberg.Dictionary.DictionaryEntry;

public record SearchResult(
        String queryString,
        List<DictionarySearchResult> dictionarySearchResults) {

    public static SearchResult empty(String queryString) {
        return new SearchResult(queryString, List.of());
    }
    
    public record DictionarySearchResult(String dictionaryCode, List<DictionaryEntry> matchingEntries) {
    }
    
    public Map<String, Integer> numberOfMatches() {
        
        return dictionarySearchResults.stream()
                .collect(toMap(d -> d.dictionaryCode, d -> d.matchingEntries.size()));
    }

}
