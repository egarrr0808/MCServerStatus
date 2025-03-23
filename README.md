# MCServerStatus

MCServerStatus is a comprehensive Minecraft Paper server plugin that provides real-time server information through a REST API, allowing for seamless integration with websites and external applications. This plugin collects detailed server metrics, player information, and plugin data, making it accessible through a web interface.

## Features

MCServerStatus offers a robust set of features designed to provide comprehensive server monitoring:

- **REST API**: Exposes server information through a configurable HTTP/HTTPS endpoint
- **Real-time Data**: Collects and updates server metrics at configurable intervals
- **Comprehensive Metrics**: Tracks TPS, memory usage, player count, and more
- **Player Information**: Provides detailed player data including names, UUIDs, ping, and skin URLs
- **Plugin List**: Displays all installed plugins with version and status information
- **Web Integration**: Includes ready-to-use web components for displaying server status on websites
- **CORS Support**: Configurable cross-origin resource sharing for web integration
- **Security Options**: Optional API authentication with configurable API keys
- **HTTPS Support**: Optional secure communication with configurable SSL/TLS settings

## Project Structure

The project is organized into the following components:

```
MCServerStatus/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── mcserverstatus/
│       │           ├── MCServerStatus.java       # Main plugin class
│       │           ├── ApiServer.java            # REST API implementation
│       │           └── ServerDataCollector.java  # Data collection logic
│       └── resources/
│           ├── plugin.yml                        # Plugin metadata
│           └── config.yml                        # Plugin configuration
├── web/
│   ├── index.html                                # Example web interface
│   ├── minecraft-server-status.js                # JavaScript client
│   └── mc-proxy.php                              # PHP proxy for HTTPS sites
└── pom.xml                                       # Maven build configuration
```

## Installation

### Plugin Installation

1. Download the latest `mcserverstatus-x.x.x.jar` file from the [Releases](https://github.com/yourusername/mcserverstatus/releases) page
2. Place the JAR file in your server's `plugins` directory
3. Restart your server or run `/reload` command
4. The plugin will generate a default configuration file at `plugins/MCServerStatus/config.yml`
5. Modify the configuration as needed and run `/mcstatus reload` to apply changes

### Web Integration

To integrate the server status display on your website:

1. Copy the files from the `web` directory to your web server
2. Edit `mc-proxy.php` to point to your Minecraft server's API endpoint
3. Include the JavaScript and HTML in your website

## Configuration

### Plugin Configuration

The plugin's configuration file (`config.yml`) allows you to customize various aspects of the plugin:

```yaml
# Server port for the HTTP API
port: 8080

# Update interval in seconds
update-interval: 10

# Security settings
security:
  # Enable authentication for the API
  enable-auth: false
  # API key (only used if enable-auth is true)
  api-key: "change-this-to-your-secure-key"
  
# CORS settings for web integration
cors:
  # Allow requests from these origins (use * for any origin)
  allowed-origins: "*"

# HTTPS configuration
https:
  # Enable HTTPS
  enabled: false
  # Path to keystore file (relative to plugin data folder)
  keystore-path: "ssl/keystore.p12"
  # Keystore password
  keystore-password: "changeit"
  # Keystore type (PKCS12 or JKS)
  keystore-type: "PKCS12"
  
# Debug mode
debug: false
```

### HTTPS Configuration

To enable HTTPS for the API:

1. Set `https.enabled` to `true` in the configuration
2. Obtain an SSL certificate (Let's Encrypt recommended)
3. Convert the certificate to PKCS12 format:
   ```
   openssl pkcs12 -export -in fullchain.pem -inkey privkey.pem -out keystore.p12 -name mcserverstatus
   ```
4. Place the keystore file in the plugin's data folder (e.g., `plugins/MCServerStatus/ssl/keystore.p12`)
5. Update the keystore path, password, and type in the configuration
6. Reload the plugin with `/mcstatus reload`

## Understanding the Components

### Java Plugin (Backend)

The Java plugin consists of three main classes:

1. **MCServerStatus**: The main plugin class that initializes components and handles commands
2. **ApiServer**: Implements the REST API using the Spark framework, handling HTTP requests and responses
3. **ServerDataCollector**: Collects server metrics, player information, and plugin data

The plugin exposes a REST API endpoint at `http://your-server-ip:8080/api/status` (or with HTTPS if enabled) that returns server information in JSON format.

### PHP Proxy (Middleware)

The PHP proxy (`mc-proxy.php`) serves as middleware between the Java plugin's API and websites, solving two common issues:

1. **Mixed Content Blocking**: When a secure HTTPS website tries to load content from a non-secure HTTP API, browsers block the request. The PHP proxy, hosted on the same domain as the website, can make server-side requests to the HTTP API and relay the response.

2. **Cross-Origin Resource Sharing (CORS)**: The proxy handles CORS headers, allowing the website to request data from different domains.

To use the PHP proxy:

1. Upload `mc-proxy.php` to your web server
2. Edit the `$api_url` variable to point to your Minecraft server's API endpoint
3. Access the proxy from your JavaScript using a relative path (e.g., `mc-proxy.php`)

### JavaScript Client (Frontend)

The JavaScript client (`minecraft-server-status.js`) fetches data from the API or PHP proxy and displays it on a web page. It provides:

1. **Automatic Refreshing**: Updates the server status at configurable intervals
2. **Error Handling**: Displays user-friendly error messages when the server is unreachable
3. **Mixed Content Detection**: Warns users when HTTPS/HTTP mixed content issues occur
4. **Responsive Design**: Adapts to different screen sizes

To use the JavaScript client:

1. Include the script in your HTML:
   ```html
   <script src="minecraft-server-status.js"></script>
   ```

2. Add a container element with the ID `minecraft-server-status`:
   ```html
   <div id="minecraft-server-status"></div>
   ```

3. Initialize the module with your API URL:
   ```html
   <script>
     document.addEventListener('DOMContentLoaded', function() {
       mcServerStatusModule.setApiUrl('mc-proxy.php');
     });
   </script>
   ```

## How the Components Connect

The integration between the components works as follows:

1. **Data Collection**: The Java plugin collects server data at regular intervals (configurable in `config.yml`)
2. **API Exposure**: The plugin exposes this data through a REST API endpoint
3. **Proxy Mediation**: The PHP proxy fetches data from the API and relays it to the client
4. **Client Display**: The JavaScript client requests data from the proxy and displays it on the web page

This architecture solves several challenges:

- **Security**: The PHP proxy allows secure HTTPS websites to access data from HTTP APIs
- **Cross-Origin Access**: CORS headers in both the API and proxy enable cross-domain requests
- **Real-time Updates**: Regular polling keeps the displayed information up-to-date
- **Fallback Handling**: If the server is offline, the proxy can return basic offline status information

## Port Configuration

For the plugin to function correctly, you need to ensure the API port is accessible:

1. **Server Firewall**: Open the configured port (default: 8080) in your server's firewall:
   ```
   sudo ufw allow 8080/tcp
   ```

2. **Hosting Provider**: If using a cloud provider (AWS, Oracle Cloud, etc.), configure security groups or firewall rules to allow traffic on the API port

3. **Router Configuration**: If hosting at home, forward the API port from your router to your Minecraft server

## Building from Source

To build the plugin from source:

1. Ensure you have Java 8+ and Maven installed
2. Clone this repository:
   ```
   git clone https://github.com/yourusername/mcserverstatus.git
   ```
3. Navigate to the project directory:
   ```
   cd mcserverstatus
   ```
4. Build with Maven:
   ```
   mvn clean package
   ```
5. The compiled JAR will be in the `target` directory

## Commands and Permissions

The plugin provides the following commands:

- `/mcstatus` - Display plugin information
- `/mcstatus reload` - Reload the plugin configuration
- `/mcstatus start` - Start the API server
- `/mcstatus stop` - Stop the API server

Permissions:
- `mcserverstatus.admin` - Required to use all plugin commands

## Troubleshooting

### Mixed Content Issues

If your website uses HTTPS but the Minecraft server API uses HTTP, you'll encounter mixed content blocking. Solutions:

1. **Use the PHP Proxy**: The recommended approach is to use the included PHP proxy
2. **Enable HTTPS for the API**: Configure HTTPS in the plugin as described above
3. **Access the Website via HTTP**: For testing only, access your website using HTTP instead of HTTPS

### CORS Issues

If you see errors like "Cross-Origin Request Blocked" in your browser console:

1. Check the `cors.allowed-origins` setting in your plugin configuration
2. Ensure the PHP proxy has the correct CORS headers
3. Try setting `allowed-origins` to `"*"` for testing (not recommended for production)

### API Authentication

If using API authentication:

1. Set `security.enable-auth` to `true` in the configuration
2. Set a strong, unique API key in `security.api-key`
3. Update your PHP proxy to include the Authorization header:
   ```php
   $opts = [
       'http' => [
           'header' => 'Authorization: Bearer your-api-key'
       ]
   ];
   $context = stream_context_create($opts);
   $response = @file_get_contents($api_url, false, $context);
   ```

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgements

- [Paper](https://papermc.io/) - The high performance Minecraft server
- [Spark](http://sparkjava.com/) - The micro web framework used for the API
- [Gson](https://github.com/google/gson) - Used for JSON serialization/deserialization
- [Crafatar](https://crafatar.com/) - Used for player avatar rendering
