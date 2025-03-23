// Minecraft Server Status JavaScript
const mcServerStatusModule = (function() {
    // Configuration
    const config = {
        apiUrl: 'mc-proxy.php', // Path to the PHP proxy
        refreshInterval: 30000, // Refresh data every 30 seconds
    };

    // DOM elements
    let serverStatusElement;
    let refreshTimer;

    // Initialize the module
    function init() {
        serverStatusElement = document.getElementById('minecraft-server-status');
        if (!serverStatusElement) {
            console.error('Minecraft server status element not found');
            return;
        }

        // Initial data fetch
        fetchServerData();

        // Set up periodic refresh
        refreshTimer = setInterval(fetchServerData, config.refreshInterval);
    }

    // Fetch server data from the API
    function fetchServerData() {
        // Check for mixed content issues (HTTPS site trying to load HTTP content)
        if (window.location.protocol === 'https:' && config.apiUrl.startsWith('http:')) {
            serverStatusElement.innerHTML = `
                <div class="server-error">
                    <p>Cannot load server data due to security restrictions</p>
                    <p class="error-details">Your website is using HTTPS but trying to access an HTTP API.</p>
                    <p>Options to fix this:</p>
                    <ul>
                        <li>Enable HTTPS for your Minecraft server API</li>
                        <li>Create a proxy on your web server to forward requests</li>
                        <li>Visit this page using HTTP instead of HTTPS (for testing only)</li>
                    </ul>
                </div>
            `;
            return;
        }

        serverStatusElement.innerHTML = '<p>Fetching server data...</p>';

        fetch(config.apiUrl)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                displayServerData(data);
            })
            .catch(error => {
                console.error('Error fetching server data:', error);
                serverStatusElement.innerHTML = `
                    <div class="server-error">
                        <p>Unable to connect to Minecraft server</p>
                        <p class="error-details">Error: ${error.message}</p>
                    </div>
                `;
            });
    }

    // Display server data in the HTML
    function displayServerData(data) {
        // Create server info section
        const serverInfo = `
            <div class="server-info">
                <div class="server-header">
                    <h4>${data.name || 'Minecraft Server'}</h4>
                    <span class="server-version">${data.version || ''}</span>
                </div>
                <div class="server-stats">
                    <div class="stat">
                        <span class="stat-label">Status:</span>
                        <span class="stat-value online">Online</span>
                    </div>
                    <div class="stat">
                        <span class="stat-label">Address:</span>
                        <span class="stat-value">${data.ip || 'Unknown'}:${data.port || '25565'}</span>
                    </div>
                    <div class="stat">
                        <span class="stat-label">Players:</span>
                        <span class="stat-value">${data.onlinePlayers || 0}/${data.maxPlayers || 0}</span>
                    </div>
                    <div class="stat">
                        <span class="stat-label">TPS:</span>
                        <span class="stat-value ${getTpsClass(data.tps)}">${data.tps ? data.tps.toFixed(2) : 'N/A'}</span>
                    </div>
                    <div class="stat">
                        <span class="stat-label">Memory:</span>
                        <span class="stat-value">${formatMemory(data.allocatedMemory, data.maxMemory)}</span>
                    </div>
                </div>
            </div>
        `;

        // Create player list section
        let playerList = '';
        if (data.players && data.players.length > 0) {
            playerList = `
                <div class="player-list">
                    <h4>Online Players</h4>
                    <div class="players-grid">
                        ${data.players.map(player => createPlayerCard(player)).join('')}
                    </div>
                </div>
            `;
        } else {
            playerList = `
                <div class="player-list">
                    <h4>Online Players</h4>
                    <p class="no-players">No players online</p>
                </div>
            `;
        }

        // Create plugins list section
        let pluginsList = '';
        if (data.plugins && data.plugins.length > 0) {
            pluginsList = `
                <div class="plugins-list">
                    <h4>Installed Plugins (${data.plugins.length})</h4>
                    <div class="plugins-container">
                        ${data.plugins.map(plugin => `
                            <div class="plugin-item ${plugin.enabled ? 'enabled' : 'disabled'}">
                                <span class="plugin-name">${plugin.name}</span>
                                <span class="plugin-version">v${plugin.version}</span>
                            </div>
                        `).join('')}
                    </div>
                </div>
            `;
        }
        
        // Update the DOM
        serverStatusElement.innerHTML = `
            <div class="minecraft-server-container">
                ${serverInfo}
                ${playerList}
                ${pluginsList}
                <div class="server-footer">
                    <small>Last updated: ${new Date().toLocaleTimeString()}</small>
                </div>
            </div>
        `;
    }
    
    // Helper function to create a player card
    function createPlayerCard(player) {
        return `
            <div class="player-card">
                <div class="player-avatar">
                    <img src="${player.skinUrl}" alt="${player.name}" title="${player.name}">
                </div>
                <div class="player-info">
                    <div class="player-name">${player.displayName || player.name}</div>
                    <div class="player-ping">Ping: <span class="${getPingClass(player.ping)}">${player.ping}ms</span></div>
                </div>
            </div>
        `;
    }
    
    // Helper function to format memory usage
    function formatMemory(used, max) {
        if (!used || !max) return 'N/A';
        return `${used}MB / ${max}MB`;
    }
    
    // Helper function to get TPS color class
    function getTpsClass(tps) {
        if (!tps) return '';
        if (tps >= 18) return 'good';
        if (tps >= 15) return 'warning';
        return 'critical';
    }
    
    // Helper function to get ping color class
    function getPingClass(ping) {
        if (ping < 0) return '';
        if (ping < 100) return 'good';
        if (ping < 300) return 'warning';
        return 'critical';
    }
    
    // Public API
    return {
        init: init,
        refresh: fetchServerData,
        setApiUrl: function(url) {
            config.apiUrl = url;
            fetchServerData();
        }
    };
})();

// Initialize when the DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    mcServerStatusModule.init();
});
