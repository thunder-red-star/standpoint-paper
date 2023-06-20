package me.thdr.standpoint.listeners;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.thdr.standpoint.utils.Attribute;
import me.thdr.standpoint.utils.Automod;
import me.thdr.standpoint.utils.PerspectiveRequester;
import me.thdr.standpoint.utils.Punishment;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.bukkit.Bukkit.getServer;


public class ChatListener implements Listener {
    private final JavaPlugin plugin;
    private final PerspectiveRequester requester;
    private final Automod automod;
    private final Connection connection;

    public ChatListener(JavaPlugin plugin, PerspectiveRequester requester, Automod automod, Connection connection) {
        this.plugin = plugin;
        this.requester = requester;
        this.automod = automod;
        this.connection = connection;
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) throws IOException, SQLException {
        // Get the information about this event, like message content, player, etc.
        String message = LegacyComponentSerializer.legacySection().serialize(event.message());
        String playerName = event.getPlayer().getName();
        String playerUUID = event.getPlayer().getUniqueId().toString();

        // Convert the component to a string containing no formatting.
        JsonObject result = requester.sendRequest(message);

        // Get key "attributeScores"
        JsonObject attributeScores = result.getAsJsonObject("attributeScores");

        Set<String> attributes = attributeScores.keySet();
        Map<String, Double> scores = new HashMap<>();
        for (String attribute : attributes) {
            JsonObject attributeScore = attributeScores.getAsJsonObject(attribute);
            JsonObject summaryScore = attributeScore.getAsJsonObject("summaryScore");
            double value = summaryScore.get("value").getAsDouble();
            scores.put(attribute, value);
        }

        // If the user isn't in the users table, add them.
        PreparedStatement getterStatement = connection.prepareStatement("SELECT * FROM users WHERE uuid = ?");
        getterStatement.setString(1, playerUUID);
        ResultSet getterRes = getterStatement.executeQuery();
        if (!getterRes.next()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO users (uuid, username, average, messages) VALUES (?, ?, ?, ?)");

            statement.setString(1, playerUUID);
            statement.setString(2, playerName);
            statement.setString(3, "{}");
            statement.setInt(4, 0);

            statement.executeUpdate();
        }

        // Update the user's average. First get them
        PreparedStatement newGetter = connection.prepareStatement("SELECT * FROM users WHERE uuid = ?");
        newGetter.setString(1, playerUUID);
        // Get the result set
        ResultSet dbRes = newGetter.executeQuery();
        // Get the average
        String average = dbRes.getString("average");
        // Convert that to a JsonObject by parsing it
        JsonObject averageJson = new Gson().fromJson(average, JsonObject.class);
        // Get messages
        int messages = dbRes.getInt("messages");
        // Now create a new JsonObject where each score is multiplied by the number of messages
        JsonObject newAverage = new JsonObject();
        for (String attribute : averageJson.keySet()) {
            double score = averageJson.get(attribute).getAsDouble();
            newAverage.addProperty(attribute, score * messages);
        }
        // Add the new score to the newAverage JsonObject
        for (String attribute : scores.keySet()) {
            double score = scores.get(attribute);
            JsonElement propertyValue = newAverage.get(attribute);
            if (propertyValue == null) {
                newAverage.addProperty(attribute, score);
                continue;
            }
            newAverage.addProperty(attribute, newAverage.get(attribute).getAsDouble() + score);
        }
        // Now divide each score by the number of messages
        messages += 1;
        for (String attribute : newAverage.keySet()) {
            double score = newAverage.get(attribute).getAsDouble();
            newAverage.addProperty(attribute, score / (messages));
        }
        // Now update the user
        PreparedStatement updater = connection.prepareStatement("UPDATE users SET average = ?, messages = ? WHERE uuid = ?");
        updater.setString(1, newAverage.toString());
        updater.setInt(2, messages);
        updater.setString(3, playerUUID);
        updater.executeUpdate();

        // Use automod to determine the punishment.
        Attribute attribute = automod.getAttribute((Map<String, Double>) scores);
        Punishment punishment = automod.getPunishment((Map<String, Double>) scores);

        // Check if user has permission to bypass punishment.
        if (event.getPlayer().hasPermission(plugin.getConfig().getString("bypass-permission")) || event.getPlayer().isOp()) {
            return;
        }

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