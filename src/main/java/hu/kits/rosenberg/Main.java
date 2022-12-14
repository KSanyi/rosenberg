package hu.kits.rosenberg;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.cj.jdbc.MysqlDataSource;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    public static void main(String[] args) throws Exception {
        
        logger.info("Starting application");
        
        int port = getPort();
        URI dbUri = getDatabaseUri();
        DataSource dataSource = createDataSource(dbUri);
        
        DictionaryService dictionaryService = createDictionaryService(dataSource);
        new HttpServer(port, dictionaryService).start();
    }
    
    private static DictionaryService createDictionaryService(DataSource dataSource) {
        DictionaryJdbcRepository dictionaryJdbcRepository = new DictionaryJdbcRepository(dataSource);
        return new DictionaryService(dictionaryJdbcRepository);
    }

    private static int getPort() {
        String port = loadMandatoryEnvVariable("PORT");

        try {
            int portNumber = Integer.parseInt(port);
            logger.info("PORT: " + port);
            return portNumber;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Illegal system environment variable PORT: " + port);
        }
    }
    
    private static URI getDatabaseUri() throws URISyntaxException {
        String databaseUrl = loadMandatoryEnvVariable("CLEARDB_DATABASE_URL");
        return new URI(databaseUrl);
    }
    
    private static DataSource createDataSource(URI dbUri) throws URISyntaxException {
        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String jdbcUrl = "jdbc:mysql://" + dbUri.getHost() + dbUri.getPath() + "?" + dbUri.getQuery(); 
        
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(jdbcUrl);
        dataSource.setUser(username);
        dataSource.setPassword(password);
        return dataSource;
    }
    
    private static String loadMandatoryEnvVariable(String name) {
        String variable = System.getenv(name);
        if (variable == null) {
            throw new IllegalArgumentException("System environment variable " + name + " is missing");
        } else {
            return variable;
        }
    }

}
