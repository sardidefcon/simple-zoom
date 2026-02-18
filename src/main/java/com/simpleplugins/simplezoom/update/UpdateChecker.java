package com.simpleplugins.simplezoom.update;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks Modrinth API for new plugin versions and notifies console and ops.
 */
public final class UpdateChecker {

    private static final String MODRINTH_API = "https://api.modrinth.com/v2/project/simple-zoom%2B/version";
    private static final String MODRINTH_PROJECT_URL = "https://modrinth.com/plugin/simple-zoom+";
    private static final Pattern VERSION_NUMBER_PATTERN = Pattern.compile("\"version_number\"\\s*:\\s*\"([^\"]+)\"");

    private UpdateChecker() {}

    /**
     * Schedules an async check. If a newer version is found, shows a yellow message
     * in console and to all online operators, with a clickable link to Modrinth.
     */
    public static void check(JavaPlugin plugin) {
        if (!plugin.getConfig().getBoolean("check-updates", true)) {
            return;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String latest = fetchLatestVersion();
                if (latest == null) return;

                String current = plugin.getDescription().getVersion();
                if (!isNewerVersion(latest, current)) return;

                String projectUrl = MODRINTH_PROJECT_URL;
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    sendConsoleMessage(plugin, projectUrl);
                    sendMessageToOps(projectUrl);
                });
            } catch (Exception e) {
                plugin.getLogger().fine("Update check failed: " + e.getMessage());
            }
        });
    }

    private static String fetchLatestVersion() throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MODRINTH_API))
                .header("User-Agent", "SimpleZoom-UpdateChecker")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) return null;

        Matcher matcher = VERSION_NUMBER_PATTERN.matcher(response.body());
        return matcher.find() ? matcher.group(1) : null;
    }

    private static boolean isNewerVersion(String remote, String current) {
        int[] r = parseVersion(remote);
        int[] c = parseVersion(current);
        for (int i = 0; i < Math.max(r.length, c.length); i++) {
            int rn = i < r.length ? r[i] : 0;
            int cn = i < c.length ? c[i] : 0;
            if (rn > cn) return true;
            if (rn < cn) return false;
        }
        return false;
    }

    private static int[] parseVersion(String v) {
        String[] parts = v.split("\\.");
        int[] out = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                out[i] = Integer.parseInt(parts[i].replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                out[i] = 0;
            }
        }
        return out;
    }

    private static void sendConsoleMessage(JavaPlugin plugin, String url) {
        String yellow = "\u001B[33m";
        String reset = "\u001B[0m";
        plugin.getLogger().info(yellow + "> A new version of Simple Zoom is available" + reset);
        plugin.getLogger().info(yellow + "> Click here to download it: " + url + reset);
    }

    private static void sendMessageToOps(String projectUrl) {
        Component line1 = Component.text("> A new version of Simple Zoom is available").color(NamedTextColor.YELLOW);
        Component line2 = Component.text("> ")
                .color(NamedTextColor.YELLOW)
                .append(Component.text("Click here to download it")
                        .color(NamedTextColor.YELLOW)
                        .decorate(net.kyori.adventure.text.format.TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl(projectUrl)));

        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("szoom.reload") || p.isOp())
                .forEach(p -> {
                    p.sendMessage(line1);
                    p.sendMessage(line2);
                });
    }
}
