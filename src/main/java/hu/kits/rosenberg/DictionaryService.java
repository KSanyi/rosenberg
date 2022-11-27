package hu.kits.rosenberg;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.rosenberg.Dictionary.DictionaryData;
import hu.kits.rosenberg.DictionaryParser.DictionaryParseException;
import io.javalin.http.UploadedFile;

public class DictionaryService {
    
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final DictionaryJdbcRepository dictionaryRepository;
    
    private final Dictionaries dictionaries;

    public DictionaryService(DictionaryJdbcRepository dictionaryRepository) {
        this.dictionaryRepository = dictionaryRepository;
        dictionaries = dictionaryRepository.loadDictionaries();
    }
    
    public List<DictionaryData> getDictionaries() {
        logger.info("Listing dictionaries");
        return dictionaries.getDictionaryData();
    }
    
    public void deleteDictionary(String id) {
        dictionaryRepository.delete(id);
        logger.info("Dictionary deleted: {}", id);
    }

    public void uploadDictionary(UploadedFile uploadedFile) {
        
        logger.info("Uploading {}", uploadedFile.filename());
        
        try(InputStream inputStream = uploadedFile.content()) {
            
            byte[] data = inputStream.readAllBytes();
            
            String dictionaryId = DictionaryParser.parseDictionaryId(new ByteArrayInputStream(data));
            
            logger.info("Dictionary id: {}", dictionaryId);
            
            String fileName = uploadedFile.filename();
            
            Dictionary dictionary;
            if(dictionaries.containsDictionary(dictionaryId)) {
                logger.info("Updating dictionary");
                dictionary = dictionaryRepository.update(dictionaryId, fileName, data);
            } else {
                logger.info("Saving new dictionary");
                dictionary = dictionaryRepository.save(dictionaryId, fileName, data);
            }
            dictionaries.setDictionary(dictionaryId, dictionary);
            logger.info("Dictionary is set");
        } catch(DictionaryParseException ex) {
            logger.error("Could not parse file {}: {}", uploadedFile.filename(), ex.getMessage());
            throw ex;
        } catch(IOException ex) {
            logger.error("Error reading file {}", uploadedFile.filename(), ex);
            throw new RuntimeException("Error reading file + " + uploadedFile.filename(), ex);
        }
    }

    public SearchResult search(String word) {
        return dictionaries.search(word);
    }
    
}
