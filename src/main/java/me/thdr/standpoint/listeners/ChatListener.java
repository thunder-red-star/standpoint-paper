package me.thdr.standpoint.listeners;

import com.google.gson.JsonObject;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.thdr.standpoint.utils.Automod;
import me.thdr.standpoint.utils.PerspectiveRequester;
import me.thdr.standpoint.utils.Punishment;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class ChatListener implements Listener {
    private final JavaPlugin plugin;
    private final PerspectiveRequester requester;
    private final Automod automod;

    public ChatListener(JavaPlugin plugin, PerspectiveRequester requester, Automod automod) {
        this.plugin = plugin;
        this.requester = requester;
        this.automod = automod;
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) throws IOException {
        // Get the information about this event, like message content, player, etc.
        String message = LegacyComponentSerializer.legacySection().serialize(event.message());
        String playerName = event.getPlayer().getName();

        // Convert the component to a string containing no formatting.
        JsonObject result = requester.sendRequest(message);

        // Get key "attributeScores"
        JsonObject attributeScores = result.getAsJsonObject("attributeScores");

        /*
        "PROFANITY": {
			"spanScores": [{
				"begin": 0,
				"end": 4,
				"score": {
					"value": 0.014669105,
					"type": "PROBABILITY"
				}
			}],
			"summaryScore": {
				"value": 0.014669105,
				"type": "PROBABILITY"
			}
		},
		This is a collection of attributes, with the key being the attribute name and various values. We will build a Map<String, double> object.
         */
        Set<String> attributes = attributeScores.keySet();
        Map<String, Double> scores = new HashMap<>();
        for (String attribute : attributes) {
            JsonObject attributeScore = attributeScores.getAsJsonObject(attribute);
            JsonObject summaryScore = attributeScore.getAsJsonObject("summaryScore");
            double value = summaryScore.get("value").getAsDouble();
            scores.put(attribute, value);
        }

        // Use automod to determine the punishment.
        Punishment punishment = automod.getPunishment((Map<String, Double>) scores);

        // Use the plugin logger to log the arraylist of commands in the punishment, converting it to a string using join.
        plugin.getLogger().info(String.join(", ", punishment.getCommands()));
    }
}