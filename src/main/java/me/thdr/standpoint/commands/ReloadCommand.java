package me.thdr.standpoint.commands;

import me.thdr.standpoint.Standpoint;
import me.thdr.standpoint.listeners.ChatListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

import static org.bukkit.Bukkit.getServer;


public class ReloadCommand implements StandpointCommand.SubCommand {

    private final Standpoint plugin;
    private final String name;
    private final String description;

    public ReloadCommand(Standpoint plugin) {
        this.plugin = plugin;
        this.name = "reload";
        this.description = "Reloads configuration files.";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // Reload the config
        this.plugin.reloadConfig();

        Component reloadedMessage = Component.text().append(Component.text("Standpoint's configuration", NamedTextColor.GRAY)).append(Component.text(" has been reloaded.", NamedTextColor.DARK_RED)).build();

        sender.sendMessage(reloadedMessage);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
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