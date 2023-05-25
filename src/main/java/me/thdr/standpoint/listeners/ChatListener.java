package me.thdr.standpoint.listeners;

import com.google.gson.JsonObject;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.thdr.standpoint.utils.Attribute;
import me.thdr.standpoint.utils.Automod;
import me.thdr.standpoint.utils.PerspectiveRequester;
import me.thdr.standpoint.utils.Punishment;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.bukkit.Bukkit.getServer;


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
        Attribute attribute = automod.getAttribute((Map<String, Double>) scores);
        Punishment punishment = automod.getPunishment((Map<String, Double>) scores);

        // Use the plugin logger to log the arraylist of commands in the punishment, converting it to a string using join.
        if (punishment != null) {
            // If punishment.cancel is true, cancel the event.
            if (punishment.getCancel()) {
                event.setCancelled(true);
            }

            // If punishment.message is not null, send the message.
            if (punishment.getMessage() != null) {
                String messageToSend = punishment.getMessage();
                messageToSend = messageToSend.replace("%player%", playerName);
                messageToSend = messageToSend.replace("%message%", message);
                messageToSend = messageToSend.replace("%threshold%", String.valueOf(punishment.getThreshold()));
                messageToSend = messageToSend.replace("%attribute%", attribute.getName());
                messageToSend = messageToSend.replace("%weight%", String.valueOf(attribute.getWeight()));
                messageToSend = messageToSend.replace("%score%", String.valueOf(scores.get(attribute.getName())));
                messageToSend = messageToSend.replace("%allscores%", scores.toString());

                messageToSend = ChatColor.translateAlternateColorCodes('&', messageToSend);

                // Allow usage of color codes (including RGB)
                Component messageComponent = LegacyComponentSerializer.legacySection().deserialize(messageToSend);
                event.getPlayer().sendMessage(messageComponent);
            }

            // If punishment.commands is not null, execute the commands.
            if (punishment.getCommands() != null) {
                for (String command : punishment.getCommands()) {
                    // Replace %player% with the player name.
                    command = command.replace("%player%", playerName);
                    command = command.replace("%message%", message);
                    command = command.replace("%threshold%", String.valueOf(punishment.getThreshold()));
                    command = command.replace("%attribute%", attribute.getName());
                    command = command.replace("%weight%", String.valueOf(attribute.getWeight()));
                    command = command.replace("%score%", String.valueOf(scores.get(attribute.getName())));
                    command = command.replace("%allscores%", scores.toString());

                    plugin.getLogger().info("Executing command " + command + " as punishment for " + playerName);

                    String finalCommand = command;
                    getServer().getScheduler().runTask(plugin, () -> {
                        getServer().dispatchCommand(getServer().getConsoleSender(), finalCommand);
                    });
                }
            }
        }
    }
}