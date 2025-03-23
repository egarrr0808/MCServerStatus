<?php
// Set headers
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

// Function to get data from the Minecraft plugin API
function getMinecraftServerData() {
    // Change this URL to your Minecraft server's API endpoint
    $api_url = 'http://your-minecraft-server-ip:8080/api/status';
    
    // Try to fetch data from the plugin API
    $response = @file_get_contents($api_url);
    
    if ($response === FALSE) {
        // If API is unreachable, return basic offline status
        return json_encode([
            'online' => false,
            'name' => 'Your Minecraft Server',
            'ip' => 'your-minecraft-server-ip',
            'port' => 25565,
            'version' => 'Paper 1.20+',
            'maxPlayers' => 20,
            'onlinePlayers' => 0,
            'players' => []
        ]);
    } else {
        // If successful, just pass through the API response
        return $response;
    }
}

// Get and output the server data
echo getMinecraftServerData();
?>
