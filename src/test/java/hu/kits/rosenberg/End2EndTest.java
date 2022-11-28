package hu.kits.rosenberg;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import javax.sql.DataSource;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class End2EndTest {

    private int port;

    private HttpServer httpServer;
    
    private final String dictionary1Content = """
        <root>
            <Lemma>
                <Lemma.DicType>BM01</Lemma.DicType>
                <Lemma.LemmaPocket>akasztesté</Lemma.LemmaPocket>
                <info>info about akasztesté</info>
            </Lemma>
        </root>
        """;
    
    private String dictionary2Content = """
        <root>
            <Lemma>
                <Lemma.DicType>BM02</Lemma.DicType>
                <Lemma.LemmaPocket>akasztesté</Lemma.LemmaPocket>
                <info>other info about akasztesté</info>
            </Lemma>
        </root>
        """;
    
    @BeforeEach
    private void init() throws Exception {
        port = findFreePort();
        httpServer = new HttpServer(port, createDictionaryService());
        httpServer.start();
    }
    
    @Test
    public void upload2Dictionaries() throws IOException {
        
        Clock.setStaticTime(LocalDateTime.of(2022,9,26, 19,0));
        uploadDictionary("dir01.xml", dictionary1Content);
        
        Clock.setStaticTime(LocalDateTime.of(2022,9,27, 19,0));
        uploadDictionary("dir02.xml", dictionary2Content);
        
        String responseJon = getDictionaries();
        
        String expected = """
                [
                  {
                    "fileName": "dir01.xml",
                    "numbrOfEntries": 1,
                    "uploaded": "2022-09-26T19:00",
                    "id": "BM01"
                  },
                  {
                    "fileName": "dir02.xml",
                    "numbrOfEntries": 1,
                    "uploaded": "2022-09-27T19:00",
                    "id": "BM02"
                  }
                ]""";
        
        assertEquals(expected, responseJon);
    }
    
    @Test
    public void updateDictionary() throws IOException {
        
        Clock.setStaticTime(LocalDateTime.of(2022,9,26, 19,0));
        uploadDictionary("dir01.xml", dictionary1Content);
        
        String updatedDictionary2Content = """
                <root>
                    <Lemma>
                        <Lemma.DicType>BM01</Lemma.DicType>
                        <Lemma.LemmaPocket>akasztesté</Lemma.LemmaPocket>
                        <info>other info about akasztesté</info>
                    </Lemma>
                </root>
                """;
        Clock.setStaticTime(LocalDateTime.of(2022,9,27, 19,0));
        uploadDictionary("dir01_update.xml", updatedDictionary2Content);
        
        String responseJon = getDictionaries();
        
        String expected ="""
                [{
                  "fileName": "dir01_update.xml",
                  "numbrOfEntries": 1,
                  "uploaded": "2022-09-27T19:00",
                  "id": "BM01"
                }]""";
        
        assertEquals(expected, responseJon);
    }
    
    @Test
    public void searchWithNoDictionary() throws IOException {
        
        String resultJson = searchForWord("alma");
        
        String expected = """
                {
                  "entries": [],
                  "queryString": "alma"
                }""";
        assertEquals(expected, resultJson);
    }
    
    @Test
    public void searchWithOneResult() throws IOException {
        
        uploadDictionary("dir01.xml", dictionary1Content);
        
        String result = searchForWord("akaszt");
        
        String expected = """
                {
                  "entries": [{
                    "matchingEntries": [{
                      "description": "\\n        <info>info about akasztesté<\\/info>\\n    ",
                      "word": "akasztesté"
                    }],
                    "dictionaryCode": "BM01"
                  }],
                  "queryString": "akaszt"
                }""";
        assertEquals(expected, result);
    }
    
    private void uploadDictionary(String filName, String content) throws IOException {
        File file = createDictionaryFile(filName, content);
        
        HttpResponse<String> response = Unirest.post("http://localhost:" + port + "/api/dictionary/upload")
                .field("file", file)
                .asString();
        assertEquals("OK", response.getStatusText());
    }
    
    private static File createDictionaryFile(String filName, String content) throws IOException {
        Path dirPath = Files.createTempDirectory("dictionary_test");
        Path filPath = Files.createFile(dirPath.resolve(filName));
        Files.write(filPath, content.getBytes());
        return filPath.toFile();
    }
    
    private String searchForWord(String wordPart) throws IOException {
        HttpResponse<String> response = Unirest.get("http://localhost:" + port + "/api/search?word=" + wordPart).asString();
        Assertions.assertEquals("OK", response.getStatusText());
        
        return format(response.getBody());
    }
    
    private String getDictionaries() throws IOException {
        HttpResponse<String> response = Unirest.get("http://localhost:" + port + "/api/dictionary").asString();
        Assertions.assertEquals("OK", response.getStatusText());
        
        return format(response.getBody());
    }
    
    private static DictionaryService createDictionaryService() throws Exception {
        DataSource dataSource = InMemoryDataSourceFactory.createDataSource();
        DictionaryJdbcRepository dictionaryJdbcRepository = new DictionaryJdbcRepository(dataSource);
        return new DictionaryService(dictionaryJdbcRepository);
    }
    
    private static int findFreePort() {
        try {
            ServerSocket socket = new ServerSocket(0);
            int port = socket.getLocalPort();
            socket.close();
            return port;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private static String format(String responseJson) {
        try {
            return new JSONObject(responseJson).toString(2);
        } catch(Exception ex) {
            return new JSONArray(responseJson).toString(2);
        }
    }
}
