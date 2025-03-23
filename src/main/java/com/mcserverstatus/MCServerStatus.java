package com.mcserverstatus;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.logging.Logger;

public class MCServerStatus extends JavaPlugin {
    private static MCServerStatus instance;
    private Logger logger;
    private FileConfiguration config;
    private ServerDataCollector dataCollector;
    private ApiServer apiServer;
    private BukkitTask updateTask;
    private int updateInterval;
    private int port;
    private boolean debug;

    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        
        // Save default config if it doesn't exist
        saveDefaultConfig();
        config = getConfig();
        
        // Load configuration
        port = config.getInt("port", 8080);
        updateInterval = config.getInt("update-interval", 10);
        debug = config.getBoolean("debug", false);
        
        // Initialize components
        dataCollector = new ServerDataCollector(this);
        apiServer = new ApiServer(this, port);
        
        // Start API server
        try {
            apiServer.start();
            logger.info("API server started on port " + port);
        } catch (Exception e) {
            logger.severe("Failed to start API server: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Schedule regular data updates
        startUpdateTask();
        
        logger.info("MCServerStatus plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Stop the update task
        if (updateTask != null) {
            updateTask.cancel();
        }
        
        // Stop the API server
        if (apiServer != null) {
            apiServer.stop();
        }
        
        logger.info("MCServerStatus plugin has been disabled!");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("mcstatus")) {
            if (args.length == 0) {
                sender.sendMessage("§a[MCServerStatus] §fRunning version " + getDescription().getVersion());
                sender.sendMessage("§a[MCServerStatus] §fAPI server running on port " + port);
                return true;
            }
            
            if (args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                config = getConfig();
                updateInterval = config.getInt("update-interval", 10);
                
                // Restart the update task with new interval
                if (updateTask != null) {
                    updateTask.cancel();
                }
                startUpdateTask();
                
                sender.sendMessage("§a[MCServerStatus] §fConfiguration reloaded!");
                return true;
            }
            
            if (args[0].equalsIgnoreCase("stop")) {
                if (apiServer != null) {
                    apiServer.stop();
                    sender.sendMessage("§a[MCServerStatus] §fAPI server stopped!");
                }
                return true;
            }
            
            if (args[0].equalsIgnoreCase("start")) {
                if (apiServer != null) {
                    try {
                        apiServer.start();
                        sender.sendMessage("§a[MCServerStatus] §fAPI server started on port " + port);
                    } catch (Exception e) {
                        sender.sendMessage("§c[MCServerStatus] §fFailed to start API server: " + e.getMessage());
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    private void startUpdateTask() {
        updateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            dataCollector.updateData();
            if (debug) {
                logger.info("Server data updated");
            }
        }, 20L, updateInterval * 20L); // Convert seconds to ticks (20 ticks = 1 second)
    }
    
    public static MCServerStatus getInstance() {
        return instance;
    }
    
    public ServerDataCollector getDataCollector() {
        return dataCollector;
    }
    
    public boolean isDebugEnabled() {
        return debug;
    }
    
    public void debug(String message) {
        if (debug) {
            logger.info("[Debug] " + message);
        }
    }
}
