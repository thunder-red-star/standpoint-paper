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
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class ClearCommand implements StandpointCommand.SubCommand {

    private final Standpoint plugin;
    private final String name;
    private final String description;

    public ClearCommand(Standpoint plugin) {
        this.plugin = plugin;
        this.name = "clear";
        this.description = "Clears a user's history with Standpoint.";
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

        // Execute profile clear.
        PreparedStatement clearStatement = connection.prepareStatement("DELETE FROM users WHERE username = ?");
        clearStatement.setString(1, username);
        clearStatement.execute();

        // Now notify the user that the profile has been cleared.
        Component standpointMessage = Component.text().append(Component.text("Standpoint", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD)).build();
        Component clearedMessage = Component.text().append(Component.text("Cleared ", NamedTextColor.GRAY))
                .append(Component.text(username, NamedTextColor.DARK_RED)).append(Component.text("'s profile.", NamedTextColor.GRAY)).build();
        Component informationMessage = Component.text().append(Component.text("This action is irreversible, they will appear to now be a new user.", NamedTextColor.GRAY)).build();
        Component totalClearedMessage = Component.text().append(standpointMessage).append(Component.newline()).append(clearedMessage).append(Component.newline()).append(informationMessage).build();

        sender.sendMessage(totalClearedMessage);

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