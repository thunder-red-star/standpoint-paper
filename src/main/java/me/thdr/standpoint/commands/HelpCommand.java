package me.thdr.standpoint.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;


public class HelpCommand implements StandpointCommand.SubCommand {

    private final JavaPlugin plugin;
    private final Map<String, StandpointCommand.SubCommand> subCommands;
    private final String name;
    private final String description;

    public HelpCommand(JavaPlugin plugin, Map<String, StandpointCommand.SubCommand> subCommands) {
        this.plugin = plugin;
        this.subCommands = subCommands;
        this.name = "help";
        this.description = "Display a list of commands.";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Component helpMessage = Component.text()
                .append(Component.text("=== ", NamedTextColor.GRAY))
                .append(Component.text("Standpoint", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD))
                .append(Component.text(" Help", NamedTextColor.YELLOW))
                .append(Component.text(" ===", NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text("Commands:", NamedTextColor.WHITE))
                .append(Component.newline())
                .build();

        // For each subcommand, append a line to the help message.
        for (Map.Entry<String, StandpointCommand.SubCommand> entry : subCommands.entrySet()) {
            StandpointCommand.SubCommand command = entry.getValue();
            helpMessage = helpMessage.append(Component.text("/standpoint " + command.getName(), NamedTextColor.GREEN))
                    .append(Component.text(": " + command.getDescription(), NamedTextColor.WHITE))
                    .append(Component.newline());
        }

        sender.sendMessage(helpMessage);

        return true;
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