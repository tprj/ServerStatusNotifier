package xyz.tprj.serverstatusnotifier;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class ServerStatusNotifier extends JavaPlugin {

    private static URL WEBHOOK_URL;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        try {
            WEBHOOK_URL = new URL(getConfig().getString("webhook", ""));
        } catch ( MalformedURLException e ) {
            e.printStackTrace();
            return;
        }
        JsonObject json = new JsonObject();
        json.add("username", new JsonPrimitive(getConfig().getString("title", "Server Status")));
        json.add("content", new JsonPrimitive(getConfig().getString("up-message", "Server starting")));
        String iconUrl = getConfig().getString("icon");
        if (iconUrl != null) {
            json.add("avatar_url", new JsonPrimitive(iconUrl));
        }
        requestWebHook(json.toString(), WEBHOOK_URL);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        JsonObject json = new JsonObject();
        json.add("username", new JsonPrimitive(getConfig().getString("title", "Server Status")));
        json.add("content", new JsonPrimitive(getConfig().getString("down-message", "Server stopping")));
        String iconUrl = getConfig().getString("icon");
        if (iconUrl != null) {
            json.add("avatar_url", new JsonPrimitive(iconUrl));
        }
        requestWebHook(json.toString(), WEBHOOK_URL);
    }

    public void requestWebHook(final String json, final URL url) {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                final HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
                con.addRequestProperty("Content-Type", "application/json; charset=utf-8");
                con.addRequestProperty("User-Agent", "ServerStatusNotifier/1.0");
                con.setDoOutput(true);
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Length", String.valueOf(json.length()));
                final OutputStream stream = con.getOutputStream();
                stream.write(json.getBytes(StandardCharsets.UTF_8));
                stream.flush();
                stream.close();
                con.disconnect();
                con.getResponseCode();
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        });
    }
}
