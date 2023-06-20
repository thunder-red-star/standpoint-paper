package me.thdr.standpoint.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;


public class AboutCommand implements StandpointCommand.SubCommand {

    private final JavaPlugin plugin;
    private final String name;
    private final String description;

    public AboutCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.name = "about";
        this.description = "Display information about the plugin.";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Component aboutTitle = Component.text().append(Component.text("Standpoint", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD)).build();
        Component aboutVersion = Component.text().append(Component.text("Plugin version ", NamedTextColor.GRAY))
                .append(Component.text(plugin.getDescription().getVersion(), NamedTextColor.DARK_RED)).build();
        Component aboutAuthor = Component.text().append(Component.text("Created by ", NamedTextColor.GRAY)).append(Component.text("ThunderRedStar", NamedTextColor.DARK_RED)).build();
        Component about = Component.text().append(aboutTitle).append(Component.newline()).append(aboutVersion).append(Component.newline()).append(aboutAuthor).build();

        sender.sendMessage(about);

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