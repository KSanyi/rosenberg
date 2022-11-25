package hu.kits.rosenberg;

import java.util.List;

import hu.kits.rosenberg.Dictionary.DictionaryEntry;

public record SearchResult(
        String queryString,
        List<DictionaryEntry> matchingEntries) {

    public static SearchResult empty(String queryString) {
        return new SearchResult(queryString, List.of());
    }

}
