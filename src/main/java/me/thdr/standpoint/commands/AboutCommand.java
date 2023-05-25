package me.thdr.standpoint.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;


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
        Component about = Component.text().append(Component.text("Standpoint", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD)).append(Component.text(" v1.0.0")).append(Component.newline()).append(Component.text("by ", NamedTextColor.BLUE)).append(Component.text("ThunderRedStar")).append(Component.newline()).append(Component.text("Use ", NamedTextColor.WHITE)).append(Component.text("/standpoint help")).append(Component.text(" to see commands.")).build();

        sender.sendMessage(about);

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