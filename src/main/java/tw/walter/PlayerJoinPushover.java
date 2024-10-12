package tw.walter;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.json.JSONObject;

public class PlayerJoinPushover extends JavaPlugin implements Listener {

    private String userKey;
    private String apiToken;
    private Boolean loginNotification;
    private Boolean logoutNotification;
    private String loginTitle;
    private String loginMessage;
    private String logoutTitle;
    private String logoutMessage;

    @Override
    public void onEnable() {

        // Save the default config if it doesn't exist
        saveDefaultConfig();

        // Load the config
        loadConfig();

        // Register events
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("PlayerJoinPushover enabled!");

    }

    @Override
    public void onDisable() {
        getLogger().info("PlayerJoinPushover disabled!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (loginNotification) {
            String playerName = event.getPlayer().getName();
            String serverName = getServerName();
            String loginTitlePrepared = loginTitle.replace("{player}", playerName).replace("{server}", serverName);
            String loginMessagePrepared = loginMessage.replace("{player}", playerName).replace("{server}", serverName);
            sendPushoverNotification(loginTitlePrepared, loginMessagePrepared);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (logoutNotification) {
            String playerName = event.getPlayer().getName();
            String serverName = getServerName();
            String logoutTitlePrepared = logoutTitle.replace("{player}", playerName).replace("{server}", serverName);
            String logoutMessagePrepared = logoutMessage.replace("{player}", playerName).replace("{server}", serverName);
            sendPushoverNotification(logoutTitlePrepared, logoutMessagePrepared);
        }
    }

    private void loadConfig() {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream("plugins/PlayerJoinPushover/config.yml"), StandardCharsets.UTF_8)) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(reader);
            userKey = config.getString("pushover.user_key");
            apiToken = config.getString("pushover.api_token");
            loginNotification = config.getBoolean("pushover.login_notification");
            logoutNotification = config.getBoolean("pushover.logout_notification");
            loginTitle = config.getString("pushover.login_title");
            loginMessage = config.getString("pushover.login_message");
            logoutTitle = config.getString("pushover.logout_title");
            logoutMessage = config.getString("pushover.logout_message");
        } catch (IOException e) {
            getLogger().severe("Failed to load configuration: " + e.getMessage());
        }
    }

    private void sendPushoverNotification(String title, String message) {
        String url = "https://api.pushover.net/1/messages.json";

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);

            JSONObject json = new JSONObject();
            json.put("token", apiToken);
            json.put("user", userKey);
            json.put("message", message);
            json.put("title", title);
            json.put("html", 1);

            StringEntity entity = new StringEntity(json.toString(), StandardCharsets.UTF_8);
            post.setEntity(entity);
            post.setHeader("Content-type", "application/json; charset=UTF-8");
            client.execute(post);
        } catch (Exception e) {
            getLogger().severe("Failed to send Pushover notification: " + e.getMessage());
        }
    }

    private String getServerName() {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("server.properties")) {
            properties.load(input);
            return properties.getProperty("motd", "Unknown Server");
        } catch (IOException e) {
            getLogger().severe("Failed to load server properties: " + e.getMessage());
            return "Unknown Server";
        }
    }

}
