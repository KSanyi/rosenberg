package hu.kits.rosenberg;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.rosenberg.DictionaryParser.DictionaryParseException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import io.javalin.http.staticfiles.Location;

public class HttpServer {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final Javalin app;
    
    private final int port;
    
    public HttpServer(int port) {
        
        app = Javalin.create(config -> {
            config.requestLogger.http(this::log);
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "public";
                staticFiles.location = Location.EXTERNAL;
              });
              config.http.maxRequestSize = 60000000;
              config.jsonMapper(new RosenbergJsonMapper());
        }).routes(() -> {
            path("api/upload", () -> {
                post(this::uploadDictionary);
            });
            path("api/search", () -> {
                get(this::search);
            });
        });//.exception(BadRequestException.class, this::handleException)
          //.exception(OPFRException.class, this::handleException);
        
        this.port = port;
    }
    
    private void uploadDictionary(Context context) {
        
        UploadedFile uploadedFile = context.uploadedFile("file");
        try(InputStream inputStream = uploadedFile.content()) {
            Dictionary dictionary = DictionaryParser.parseDictionary(inputStream);
            Dictionaries.setDictionary(UUID.randomUUID().toString().substring(0,5), dictionary);
        } catch(DictionaryParseException ex) {
            logger.error("Could not parse file {}: {}", uploadedFile.filename(), ex.getMessage());
        } catch(IOException ex) {
            logger.error("Error rading file {}", uploadedFile.filename(), ex);
        }
    }
    
    private void search(Context context) {
        String word = context.queryParam("word");
        logger.info("Searched for {}", word);
        SearchResult result = Dictionaries.search(word);
        //context.result(result);
        context.json(result);
        context.header("Content-Type", "text/plain; charset=utf-8");
    }
    
    public void start() {
        app.start(port);
        logger.info("OPFR server started");
    }
    
    public void stop() {
        app.stop();
        logger.info("OPFR server stopped");
    }
    
    private void log(Context ctx, @SuppressWarnings("unused") Float executionTimeMs) {
        String body = ctx.body().isBlank() ? "" : "body: " + ctx.body().replaceAll("\n", "").replaceAll("\\s+", " ");
        logger.trace("{} {} {} Status: {} from {} ({}) headers: {} agent: {}", ctx.method(), ctx.path(), body, ctx.status(), ctx.ip(), ctx.host(), ctx.headerMap(), ctx.userAgent());
        logger.info("{} {} {} Status: {}", ctx.method(), ctx.path(), body, ctx.status());
    }
    
}
