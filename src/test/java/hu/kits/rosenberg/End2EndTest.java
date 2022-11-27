package hu.kits.rosenberg;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;

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
    
    @BeforeEach
    private void init() throws Exception {
        port = findFreePort();
        httpServer = new HttpServer(port, createDictionaryService());
        httpServer.start();
    }
    
    @Test
    public void testWithNoDictionary() {
        
        HttpResponse<String> response = Unirest.get("http://localhost:" + port + "/api/search?word='alma'").asString();
        Assertions.assertEquals("OK", response.getStatusText());
        
        String expected = """
                {
                  "entries": [],
                  "queryString": "'alma'"
                }""";
        Assertions.assertEquals(expected, format(response.getBody()));
    }
    
    @Test
    public void test() throws IOException {
        
        String content = """
                <root>
                    <Lemma>
                        <Lemma.DicType>BM01</Lemma.DicType>
                        <Lemma.LemmaPocket>akasztesté</Lemma.LemmaPocket>
                        <info>info about akasztesté</info>
                    </Lemma>
                </root>
                """;
        
        File file = createDictionaryFile(content);
        
        HttpResponse<String> response = Unirest.post("http://localhost:" + port + "/api/upload")
                .field("file", file)
                .asString();
        Assertions.assertEquals("OK", response.getStatusText());
        
        response = Unirest.get("http://localhost:" + port + "/api/search?word=akaszt").asString();
        Assertions.assertEquals("OK", response.getStatusText());
        
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
        Assertions.assertEquals(expected, format(response.getBody()));
    }
    
    private static File createDictionaryFile(String content) throws IOException {
        Path path = Files.createTempFile("test", ".dictionary");
        Files.write(path, content.getBytes());
        return path.toFile();
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
