package me.thdr.standpoint.commands;

import me.thdr.standpoint.Standpoint;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import me.thdr.standpoint.listeners.ChatListener;

import java.util.Collections;
import java.util.List;

import static org.bukkit.Bukkit.getServer;


public class EnableCommand implements StandpointCommand.SubCommand {

    private final Standpoint plugin;
    private final String name;
    private final String description;

    public EnableCommand(Standpoint plugin) {
        this.plugin = plugin;
        this.name = "enable";
        this.description = "Enables the chat hook.";
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // Attach the chat listener
        getServer().getPluginManager().registerEvents(new ChatListener(this.plugin, this.plugin.getRequester(), this.plugin.getAutomod(), this.plugin.getConnection()), this.plugin);

        Component enabledMessage = Component.text().append(Component.text("Standpoint's chat hook", NamedTextColor.GRAY)).append(Component.text(" has been enabled.", NamedTextColor.DARK_RED)).build();
        // Make sure /standpoint enable is has a click event attached and runs the enable command
        Component disableMessage = Component.text().append(Component.text("You can disable it at any time using ", NamedTextColor.GRAY))
                .append(Component.text("/standpoint disable", NamedTextColor.DARK_RED).clickEvent(ClickEvent.runCommand("/standpoint disable"))).build();
        Component disabled = Component.text().append(enabledMessage).append(Component.newline()).append(disableMessage).build();

        sender.sendMessage(disabled);

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