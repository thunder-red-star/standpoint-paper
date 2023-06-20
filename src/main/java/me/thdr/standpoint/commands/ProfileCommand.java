package me.thdr.standpoint.commands;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.thdr.standpoint.Standpoint;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class ProfileCommand implements StandpointCommand.SubCommand {

    private final Standpoint plugin;
    private final String name;
    private final String description;

    public ProfileCommand(Standpoint plugin) {
        this.plugin = plugin;
        this.name = "profile";
        this.description = "Shows basic information about a user's history with Standpoint.";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) throws SQLException {
        // This subcommand, on top of the subcommand itself, should have a username in the args.
        if (args.length < 1) {
            sender.sendMessage(Component.text("Please specify a username.", NamedTextColor.DARK_RED));
            return true;
        }

        // Get connection from the plugin.
        Connection connection = plugin.getConnection();

        // Get the username from the args.
        String username = args[0];

        // Prepare the SQL statement.
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
        statement.setString(1, username);
        statement.execute();

        // Get one result from the statement.
        ResultSet result = statement.getResultSet();

        // If there are no results, the user has never sent a message.
        if (!result.next()) {
            Component standpointMessage = Component.text().append(Component.text("Standpoint", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD)).build();
            Component errorMessage = Component.text().append(Component.text("No data found for user ", NamedTextColor.GRAY))
                    .append(Component.text(username, NamedTextColor.DARK_RED)).build();
            Component errorMessage2 = Component.text().append(Component.text("You could either ", NamedTextColor.GRAY))
                    .append(Component.text("be using an old version", NamedTextColor.DARK_RED)).append(Component.text(" of Standpoint, or ", NamedTextColor.GRAY))
                    .append(Component.text("the user has never sent a message.", NamedTextColor.DARK_RED)).build();
            Component totalErrorMessage = Component.text().append(standpointMessage).append(Component.newline()).append(errorMessage).append(Component.newline()).append(errorMessage2).build();

            sender.sendMessage(totalErrorMessage);

            return true;
        }

        // Get average (string) and messages (int) from the result.
        String average = result.getString("average");
        int messages = result.getInt("messages");

        // Parse the average string into a JsonObject.
        JsonObject averageObject = new Gson().fromJson(average, JsonObject.class);

        // Get the average values from the JsonObject.
        double averageToxicity = averageObject.get("TOXICITY").getAsDouble();
        double averageSevereToxicity = averageObject.get("SEVERE_TOXICITY").getAsDouble();
        double averageIdentityAttack = averageObject.get("IDENTITY_ATTACK").getAsDouble();
        double averageInsult = averageObject.get("INSULT").getAsDouble();
        double averageProfanity = averageObject.get("PROFANITY").getAsDouble();
        double averageThreat = averageObject.get("THREAT").getAsDouble();
        double averageSexuallyExplicit = averageObject.get("SEXUALLY_EXPLICIT").getAsDouble();

        // Create the components for the message.
        Component standpointMessage = Component.text().append(Component.text("Standpoint", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD)).build();
        Component profileMessage = Component.text().append(Component.text("Profile for ", NamedTextColor.GRAY))
                .append(Component.text(username, NamedTextColor.DARK_RED)).build();
        Component numMessagesMessage = Component.text().append(Component.text("Averages over ", NamedTextColor.GRAY))
                .append(Component.text(messages, NamedTextColor.DARK_RED)).append(Component.text((messages == 1 ? " message" : " messages"), NamedTextColor.GRAY)).build();

        TextComponent.Builder toxicityMessage = null;
        TextComponent.Builder severeToxicityMessage = null;
        TextComponent.Builder identityAttackMessage = null;
        TextComponent.Builder insultMessage = null;
        TextComponent.Builder profanityMessage = null;
        TextComponent.Builder threatMessage = null;
        TextComponent.Builder sexuallyExplicitMessage = null;

        TextComponent.Builder finalMessage = Component.text().append(standpointMessage).append(Component.newline()).append(profileMessage).append(Component.newline()).append(numMessagesMessage).append(Component.newline());

        // If the value wasn't found in averageObject, it will be null. If it's null, we don't want to display it.
        if (averageToxicity != 0) {
            toxicityMessage = Component.text().append(Component.text("Toxicity: ", NamedTextColor.GRAY))
                    .append(Component.text(averageToxicity, NamedTextColor.DARK_RED));
            finalMessage = finalMessage.append(toxicityMessage).append(Component.newline());
        }
        if (averageSevereToxicity != 0) {
            severeToxicityMessage = Component.text().append(Component.text("Severe Toxicity: ", NamedTextColor.GRAY))
                    .append(Component.text(averageSevereToxicity, NamedTextColor.DARK_RED));
            finalMessage = finalMessage.append(severeToxicityMessage).append(Component.newline());
        }
        if (averageIdentityAttack != 0) {
            identityAttackMessage = Component.text().append(Component.text("Identity Attack: ", NamedTextColor.GRAY))
                    .append(Component.text(averageIdentityAttack, NamedTextColor.DARK_RED));
            finalMessage = finalMessage.append(identityAttackMessage).append(Component.newline());
        }
        if (averageInsult != 0) {
            insultMessage = Component.text().append(Component.text("Insult: ", NamedTextColor.GRAY))
                    .append(Component.text(averageInsult, NamedTextColor.DARK_RED));
            finalMessage = finalMessage.append(insultMessage).append(Component.newline());
        }
        if (averageProfanity != 0) {
            profanityMessage = Component.text().append(Component.text("Profanity: ", NamedTextColor.GRAY))
                    .append(Component.text(averageProfanity, NamedTextColor.DARK_RED));
            finalMessage = finalMessage.append(profanityMessage).append(Component.newline());
        }
        if (averageThreat != 0) {
            threatMessage = Component.text().append(Component.text("Threat: ", NamedTextColor.GRAY))
                    .append(Component.text(averageThreat, NamedTextColor.DARK_RED));
            finalMessage = finalMessage.append(threatMessage).append(Component.newline());
        }
        if (averageSexuallyExplicit != 0) {
            sexuallyExplicitMessage = Component.text().append(Component.text("Sexually Explicit: ", NamedTextColor.GRAY))
                    .append(Component.text(averageSexuallyExplicit, NamedTextColor.DARK_RED));
            finalMessage = finalMessage.append(sexuallyExplicitMessage);
        }

        // Send the message.
        sender.sendMessage(finalMessage.build());

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) { // Only tab complete the first argument
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.startsWith(args[0]))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList(); // Return an empty list if we can't complete anything
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }
}