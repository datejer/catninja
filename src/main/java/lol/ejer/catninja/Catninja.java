package lol.ejer.catninja;

import com.google.gson.Gson;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

record CatFact(String fact, int length) {}

@SuppressWarnings("unused")
public final class Catninja extends JavaPlugin {
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final Gson GSON = new Gson();

    private static final String API_URL = "https://catfact.ninja/fact";
    private static final long FIVE_MINUTES = 20L * 60L * 5L;

    @Override
    public void onEnable() {
        startCatFactTask();
        getLogger().info("CatNinja enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("CatNinja disabled!");
    }

    private void startCatFactTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                fetchAndBroadcastCatFact();
            }
        }.runTaskTimerAsynchronously(this, 0L, 20L * 10L);
    }

    private void fetchAndBroadcastCatFact() {

        if (Bukkit.getOnlinePlayers().isEmpty()) return;

        Bukkit.getAsyncScheduler().runNow(this, task -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .GET()
                        .build();

                HttpResponse<String> response =
                        HTTP.send(request, HttpResponse.BodyHandlers.ofString());

                CatFact fact = GSON.fromJson(response.body(), CatFact.class);

                Bukkit.getScheduler().runTask(this, () -> {
                    Component message = Component.text("🐱 ", NamedTextColor.GOLD)
                            .append(Component.text(fact.fact(), NamedTextColor.GRAY));

                    Bukkit.getServer().broadcast(message);
                });

            } catch (Exception e) {
                getLogger().warning("Failed to fetch cat fact: " + e.getMessage());
            }
        });
    }
}
