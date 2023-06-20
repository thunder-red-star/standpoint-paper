package me.thdr.standpoint.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
        Component helpTitle = Component.text().append(Component.text("Standpoint", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD)).build();
        Component helpVersion = Component.text().append(Component.text("Plugin version ", NamedTextColor.GRAY))
                .append(Component.text(plugin.getDescription().getVersion(), NamedTextColor.DARK_RED)).build();

        ArrayList<Component> helpMessages = new ArrayList<>();
        // For each subcommand, append a line to the help message.
        for (Map.Entry<String, StandpointCommand.SubCommand> entry : subCommands.entrySet()) {
            String command = entry.getKey();
            StandpointCommand.SubCommand subCommand = entry.getValue();
            Component commandComponent = Component.text("- ", NamedTextColor.GRAY)
                    .append(Component.text("/standpoint " + command, NamedTextColor.DARK_RED)
                            .hoverEvent(HoverEvent.showText(Component.text("Click to suggest command")))
                            .clickEvent(ClickEvent.suggestCommand("/" + command)))
                    .append(Component.text(" " + subCommand.getDescription()));
            helpMessages.add(commandComponent);
        }

        Component helpMessage = Component.text().append(helpTitle).append(Component.newline()).append(helpVersion).append(Component.newline()).append(Component.join(Component.newline(), helpMessages)).build();

        sender.sendMessage(helpMessage);

        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return description;
    }
}