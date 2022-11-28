package hu.kits.rosenberg;

import static java.util.stream.Collectors.toMap;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

import javax.sql.DataSource;

import org.jdbi.v3.core.Jdbi;

import hu.kits.rosenberg.Dictionary.DictionaryEntry;

public class DictionaryJdbcRepository {

    private static final String TABLE_DICTIONARY = "DICTIONARY";
    private static final String COLUMN_ID = "ID";
    private static final String COLUMN_FILE_NAME = "FILE_NAME";
    private static final String COLUMN_CONTENT = "CONTENT";
    private static final String COLUMN_UPLOADED = "UPLOADED";
    
    private final Jdbi jdbi;
    
    public DictionaryJdbcRepository(DataSource dataSource) {
        jdbi = Jdbi.create(dataSource);
    }
    
    public Dictionaries loadDictionaries() {
        
        String sql = String.format("SELECT * FROM %s", TABLE_DICTIONARY);
        
        List<Dictionary> dictionaries = jdbi.withHandle(handle -> 
            handle.createQuery(sql)
            .map((rs, ctx) -> mapToDictionary(rs)).list());
        
        return new Dictionaries(dictionaries.stream().collect(toMap(Dictionary::id, Function.identity())));
    }
    
    private Dictionary loadDictionary(String id) {
        
        String sql = String.format("SELECT * FROM %s WHERE %s = :id", TABLE_DICTIONARY, COLUMN_ID);
        
        return jdbi.withHandle(handle -> 
            handle.createQuery(sql)
            .bind("id", id)
            .map((rs, ctx) -> mapToDictionary(rs)).one());
    }
    
    private static Dictionary mapToDictionary(ResultSet rs) throws SQLException {
        
        Blob blob = rs.getBlob(COLUMN_CONTENT);
        List<DictionaryEntry> dictionaryEntries = DictionaryParser.parseDictionaryEntries(blob.getBinaryStream());
        
        return new Dictionary(rs.getString(COLUMN_ID), 
                rs.getString(COLUMN_FILE_NAME),
                rs.getTimestamp(COLUMN_UPLOADED).toLocalDateTime(),
                dictionaryEntries);
    }
    
    public Dictionary save(String id, String fileName, byte[] content) {
        String sql = String.format("INSERT INTO %s (%s, %s, %s, %s) VALUES(?, ?, ?, ?)", TABLE_DICTIONARY, COLUMN_ID, COLUMN_FILE_NAME, COLUMN_UPLOADED, COLUMN_CONTENT);
        jdbi.withHandle(handle -> handle.createUpdate(sql)
                .bind(0, id)
                .bind(1, fileName)
                .bind(2, Clock.now())
                .bind(3, content).execute());
        
        return loadDictionary(id);
    }
    
    public Dictionary update(String id, String fileName, byte[] content) {
        delete(id);
        return save(id, fileName, content);
    }

    public void delete(String id) {
        jdbi.withHandle(handle -> handle.execute(String.format("DELETE FROM %s WHERE %s = ?", TABLE_DICTIONARY, COLUMN_ID), id));
    }
    
}
