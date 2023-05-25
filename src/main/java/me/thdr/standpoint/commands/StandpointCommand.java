package me.thdr.standpoint.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class StandpointCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final Map<String, SubCommand> subCommands;

    public StandpointCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.subCommands = new HashMap<>();

        // Register subcommands
        subCommands.put("about", new AboutCommand(plugin));
        subCommands.put("help", new HelpCommand(plugin, subCommands));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Display about message.
        } else {
            SubCommand subCommand = subCommands.get(args[0].toLowerCase());
            if (subCommand != null) {
                // Execute the subcommand
                return subCommand.execute(sender, Arrays.copyOfRange(args, 1, args.length));
            } else {
                sender.sendMessage("Unknown subcommand. Usage: /mycommand help|about");
                return true;
            }
        }

        return false;
    }

    public static interface SubCommand {
        boolean execute(CommandSender sender, String[] args);

        String getName();
        String getDescription();
    }
}