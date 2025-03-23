package com.mcserverstatus;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

public class ServerDataCollector {
    private final MCServerStatus plugin;
    private JsonObject serverData;
    
    // Cache for TPS calculation
    private long lastPoll = 0;
    private double tps = 20.0;
    
    public ServerDataCollector(MCServerStatus plugin) {
        this.plugin = plugin;
        this.serverData = new JsonObject();
        updateData(); // Initial data collection
    }
    
    public void updateData() {
        // Create a new JSON object for server data
        JsonObject data = new JsonObject();
        
        // Server basic info
        data.addProperty("name", Bukkit.getServer().getName());
        data.addProperty("version", Bukkit.getServer().getVersion());
        data.addProperty("bukkitVersion", Bukkit.getServer().getBukkitVersion());
        data.addProperty("online", Bukkit.getServer().getOnlineMode());
        
        // Server address - using server.properties values instead of getInetSocketAddress
        data.addProperty("ip", Bukkit.getServer().getIp().isEmpty() ? "0.0.0.0" : Bukkit.getServer().getIp());
        data.addProperty("port", Bukkit.getServer().getPort());
        
        // Server performance
        data.addProperty("tps", calculateTPS());
        data.addProperty("maxMemory", Runtime.getRuntime().maxMemory() / 1024 / 1024);
        data.addProperty("allocatedMemory", Runtime.getRuntime().totalMemory() / 1024 / 1024);
        data.addProperty("freeMemory", Runtime.getRuntime().freeMemory() / 1024 / 1024);
        
        // Player info
        data.addProperty("maxPlayers", Bukkit.getServer().getMaxPlayers());
        data.addProperty("onlinePlayers", Bukkit.getServer().getOnlinePlayers().size());
        
        // Detailed player list
        JsonArray players = new JsonArray();
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            JsonObject playerData = new JsonObject();
            playerData.addProperty("name", player.getName());
            playerData.addProperty("uuid", player.getUniqueId().toString());
            playerData.addProperty("displayName", player.getDisplayName());
            playerData.addProperty("ping", getPing(player));
            playerData.addProperty("skinUrl", getSkinUrl(player.getUniqueId()));
            players.add(playerData);
        }
        data.add("players", players);
        
        // Plugin list
        JsonArray plugins = new JsonArray();
        for (Plugin serverPlugin : Bukkit.getServer().getPluginManager().getPlugins()) {
            JsonObject pluginData = new JsonObject();
            pluginData.addProperty("name", serverPlugin.getName());
            pluginData.addProperty("version", serverPlugin.getDescription().getVersion());
            pluginData.addProperty("enabled", serverPlugin.isEnabled());
            pluginData.addProperty("authors", String.join(", ", serverPlugin.getDescription().getAuthors()));
            plugins.add(pluginData);
        }
        data.add("plugins", plugins);
        
        // Update the server data
        this.serverData = data;
    }
    
    public JsonObject getServerData() {
        return serverData;
    }
    
    private double calculateTPS() {
        try {
            // Try to get TPS from server
            Object serverInstance = getMinecraftServerInstance();
            if (serverInstance != null) {
                Field tpsField = serverInstance.getClass().getField("recentTps");
                double[] recentTps = (double[]) tpsField.get(serverInstance);
                return Math.min(20.0, recentTps[0]); // Get the 1-minute TPS
            }
        } catch (Exception e) {
            // Fallback to manual calculation if reflection fails
            long now = System.currentTimeMillis();
            if (lastPoll == 0) {
                lastPoll = now;
                return 20.0; // Default TPS for first run
            }
            
            long timeSpent = now - lastPoll;
            if (timeSpent < 1000) {
                return tps; // Return cached TPS if less than a second has passed
            }
            
            // Calculate TPS based on server tick count
            double ticksPerSecond = 20.0 * (1000.0 / timeSpent);
            tps = Math.min(20.0, ticksPerSecond); // Cap at 20 TPS
            lastPoll = now;
        }
        
        return tps;
    }
    
    private Object getMinecraftServerInstance() {
        try {
            // Get CraftServer instance
            Object craftServer = Bukkit.getServer();
            // Get MinecraftServer instance via CraftServer
            Method getServerMethod = craftServer.getClass().getMethod("getServer");
            return getServerMethod.invoke(craftServer);
        } catch (Exception e) {
            plugin.debug("Failed to get MinecraftServer instance: " + e.getMessage());
            return null;
        }
    }
    
    private int getPing(Player player) {
        try {
            Object craftPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Field pingField = craftPlayer.getClass().getField("ping");
            return pingField.getInt(craftPlayer);
        } catch (Exception e) {
            plugin.debug("Failed to get player ping: " + e.getMessage());
            return -1;
        }
    }
    
    private String getSkinUrl(UUID uuid) {
        // Minecraft API URL for player skins
        return "https://crafatar.com/avatars/" + uuid.toString()  + "?size=64&overlay";
    }
}
