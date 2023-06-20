package me.thdr.standpoint.commands;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.thdr.standpoint.listeners.ChatListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;

import static org.bukkit.Bukkit.getServer;


public class DisableCommand implements StandpointCommand.SubCommand {

    private final JavaPlugin plugin;
    private final String name;
    private final String description;

    public DisableCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.name = "disable";
        this.description = "Disables the chat hook in case the plugin is causing issues with the chat event.";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        HandlerList.unregisterAll(plugin);

        Component disabledMessage = Component.text().append(Component.text("Standpoint's chat hook", NamedTextColor.GRAY)).append(Component.text(" has been disabled.", NamedTextColor.DARK_RED)).build();
        // Make sure /standpoint enable is has a click event attached and runs the enable command
        Component enableMessage = Component.text().append(Component.text("You can re-enable it at any time using ", NamedTextColor.GRAY))
                .append(Component.text("/standpoint enable", NamedTextColor.DARK_RED).clickEvent(ClickEvent.runCommand("/standpoint enable"))).build();
        Component disabled = Component.text().append(disabledMessage).append(Component.newline()).append(enableMessage).build();

        sender.sendMessage(disabled);

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