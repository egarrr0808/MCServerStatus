package com.mcserverstatus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.configuration.file.FileConfiguration;
import spark.Service;

public class ApiServer {
    private final MCServerStatus plugin;
    private final int port;
    private Service http;
    private final Gson gson;
    private boolean running = false;
    private String keystorePath;
    private String keystorePassword;
    private String keystoreType;
    
    public ApiServer(MCServerStatus plugin, int port)  {
        this.plugin = plugin;
        this.port = port;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }
    
    public void start() {
        if (running) {
            return;
        }
        
        FileConfiguration config = plugin.getConfig();
        String allowedOrigins = config.getString("cors.allowed-origins", "*");
        boolean enableAuth = config.getBoolean("security.enable-auth", false);
        String apiKey = config.getString("security.api-key", "");
        boolean enableHttps = config.getBoolean("https.enabled", false) ;
        
        http = Service.ignite() ;
        http.port(port) ;
        http.threadPool(8) ;
        
        // Configure HTTPS if enabled
        if (enableHttps) {
            keystorePath = plugin.getDataFolder() + "/" + config.getString("https.keystore-path", "ssl/keystore.p12") ;
            keystorePassword = config.getString("https.keystore-password", "changeit") ;
            keystoreType = config.getString("https.keystore-type", "PKCS12") ;
            
            http.secure(keystorePath, keystorePassword, null, null) ;
            plugin.getLogger().info("HTTPS enabled with keystore: " + keystorePath);
        } else {
            plugin.getLogger().info("Running in HTTP mode");
        }
        
        // Configure CORS
        http.before((request, response)  -> {
            response.header("Access-Control-Allow-Origin", allowedOrigins);
            response.header("Access-Control-Allow-Methods", "GET");
            response.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
            response.type("application/json");
        });
        
        // OPTIONS request handling for CORS preflight
        http.options("/*", (request, response)  -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            
            return "OK";
        });
        
        // Authentication middleware if enabled
        if (enableAuth) {
            http.before("/api/*", (request, response)  -> {
                String authHeader = request.headers("Authorization");
                if (authHeader == null || !authHeader.equals("Bearer " + apiKey)) {
                    halt(401, "{\"error\":\"Unauthorized\"}");
                }
            });
        }
        
        // API endpoints
        http.get("/api/status", (request, response)  -> {
            return plugin.getDataCollector().getServerData();
        }, gson::toJson);
        
        // Health check endpoint (no auth required)
        http.get("/health", (request, response)  -> {
            return "{\"status\":\"UP\"}";
        });
        
        http.exception(Exception.class, (exception, request, response)  -> {
            plugin.getLogger().severe("API Error: " + exception.getMessage());
            exception.printStackTrace();
            response.status(500);
            response.body("{\"error\":\"Internal server error\"}");
        });
        
        running = true;
        plugin.getLogger().info("API server started on port " + port);
    }
    
    public void stop() {
        if (!running) {
            return;
        }
        
        if (http != null)  {
            http.stop() ;
            http = null;
        }
        
        running = false;
        plugin.getLogger() .info("API server stopped");
    }
    
    public boolean isRunning() {
        return running;
    }
    
    private void halt(int statusCode, String body) {
        throw new HaltException(statusCode, body);
    }
    
    // Simple exception class to halt request processing
    private static class HaltException extends RuntimeException {
        private final int statusCode;
        private final String body;
        
        public HaltException(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }
        
        public int getStatusCode() {
            return statusCode;
        }
        
        public String getBody() {
            return body;
        }
    }
}
