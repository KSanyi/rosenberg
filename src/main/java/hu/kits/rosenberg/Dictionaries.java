package hu.kits.rosenberg;

public class Dictionaries {

    public static Dictionary dictionary;
    
    public static void setDictionary(Dictionary dictionary) {
        Dictionaries.dictionary = dictionary;
    }

    public static SearchResult search(String word) {
        return dictionary.search(word);
    }
    
}
