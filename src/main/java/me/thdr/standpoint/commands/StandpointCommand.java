package me.thdr.standpoint.commands;

import me.thdr.standpoint.Standpoint;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.*;

public class StandpointCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final Map<String, SubCommand> subCommands;

    public StandpointCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.subCommands = new HashMap<>();

        // Register subcommands
        subCommands.put("about", new AboutCommand(plugin));
        subCommands.put("help", new HelpCommand(plugin, subCommands));
        subCommands.put("profile", new ProfileCommand((Standpoint) plugin));
        subCommands.put("enable", new EnableCommand((Standpoint) plugin));
        subCommands.put("disable", new DisableCommand(plugin));
        subCommands.put("reload", new ReloadCommand((Standpoint) plugin));
        subCommands.put("clear", new ClearCommand((Standpoint) plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Display about message by executing the about subcommand
            try {
                return subCommands.get("about").execute(sender, args);
            } catch (SQLException e) {
                e.printStackTrace();

                // Create a message about database error
                Component standpointMessage = Component.text("Standpoint", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD);
                Component errorMessage = Component.text("An error occurred somewhere in the database. Please report this to the server administrator.", NamedTextColor.GRAY);

                // Combine the two components into one message.
                Component error = Component.text().append(standpointMessage).append(Component.newline()).append(errorMessage).build();

                sender.sendMessage(error);

                return true;
            }
        } else {
            SubCommand subCommand = subCommands.get(args[0].toLowerCase());
            if (subCommand != null) {
                // Execute the subcommand
                try {
                    return subCommand.execute(sender, Arrays.copyOfRange(args, 1, args.length));
                } catch (SQLException e) {
                    e.printStackTrace();

                    // Create a message about database error
                    Component standpointMessage = Component.text("Standpoint", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD);
                    Component errorMessage = Component.text("An error occurred somewhere in the database. Please report this to the server administrator.", NamedTextColor.GRAY);

                    // Combine the two components into one message.
                    Component error = Component.text().append(standpointMessage).append(Component.newline()).append(errorMessage).build();

                    sender.sendMessage(error);

                    return true;
                }
            } else {
                // No subcommand, display an error.
                Component errorMessage = Component.text("Unknown subcommand: ", NamedTextColor.GRAY)
                        .append(Component.text(args[0].toLowerCase(), NamedTextColor.DARK_RED));
                Component errorHelp = Component.text("Try using ", NamedTextColor.GRAY)
                        .append(Component.text("/standpoint help", NamedTextColor.DARK_RED))
                        .append(Component.text(" for a list of subcommands.", NamedTextColor.GRAY));

                // Combine the two components into one message.
                Component error = Component.text().append(errorMessage).append(Component.newline()).append(errorHelp).build();

                sender.sendMessage(error);

                // Prevent usage being fired.
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>();
            for (String subCommand : this.subCommands.keySet()) {
                if (subCommand.startsWith(args[0].toLowerCase())) {
                    subCommands.add(subCommand);
                }
            }
            return subCommands;
        } else if (args.length > 1) {
            SubCommand subCommand = this.subCommands.get(args[0].toLowerCase());
            if (subCommand != null) {
                return subCommand.onTabComplete(sender, command, alias, Arrays.copyOfRange(args, 1, args.length));
            }
        }
        return Collections.emptyList();
    }
    public static interface SubCommand {
        boolean execute(CommandSender sender, String[] args) throws SQLException;

        String getName();
        String getDescription();

        List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args);
    }
}