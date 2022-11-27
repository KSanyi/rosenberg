package hu.kits.rosenberg;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.rosenberg.Dictionary.DictionaryData;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;

public class HttpServer {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final Javalin app;
    
    private final int port;
    
    private final DictionaryService dictionaryService;
    
    public HttpServer(int port, DictionaryService dictionaryService) {
        
        this.dictionaryService = dictionaryService;
        
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
            path("api/dictionary", () -> {
                get(this::listDictionaries);
            });
            path("api/dictionary/upload", () -> {
                post(this::uploadDictionary);
            });
            path("api/dictionary/{id}", () -> {
                delete(this::deleteDictionary);
            });
            path("api/search", () -> {
                get(this::search);
            });
        });//.exception(BadRequestException.class, this::handleException)
          //.exception(OPFRException.class, this::handleException);
        
        this.port = port;
    }
    
    private void listDictionaries(Context context) {
        List<DictionaryData> dictionaryDataList = dictionaryService.getDictionaries();
        context.json(dictionaryDataList);
        context.header("Content-Type", "text/json; charset=utf-8");
    }
    
    private void uploadDictionary(Context context) {
        dictionaryService.uploadDictionary(context.uploadedFile("file"));
    }
    
    private void deleteDictionary(Context context) {
        dictionaryService.deleteDictionary(context.pathParam("id"));
    }
    
    private void search(Context context) {
        String word = context.queryParam("word");
        logger.info("Searched for {}", word);
        SearchResult result = dictionaryService.search(word);
        context.json(result);
        context.header("Content-Type", "text/json; charset=utf-8");
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
