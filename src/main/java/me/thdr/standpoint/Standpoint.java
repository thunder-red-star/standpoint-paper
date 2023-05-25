package me.thdr.standpoint;

import me.thdr.standpoint.commands.StandpointCommand;
import me.thdr.standpoint.utils.Automod;
import org.bukkit.plugin.java.JavaPlugin;
import me.thdr.standpoint.listeners.ChatListener;
import me.thdr.standpoint.utils.PerspectiveRequester;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public final class Standpoint extends JavaPlugin {

    private PerspectiveRequester requester;
    private Automod automod;

    @Override
    public void onEnable() {
        // Log startup. First print a list of authors from plugin.yml and then the startup message.
        // Use the Plugin class's getLogger() method to get the logger, and the same class's getPluginMeta method to get the plugin.yml file.
        // Join the immutable list of authors with a comma and space.
        // Structure of authors msg: "Copyright (c) Year Author1, Author2, Author3. All rights reserved."
        // Fetch year using Java's Calendar class.
        // Structure of launch message: "Launching PluginName vVersion"
        getLogger().info("Copyright (c) "
                + Calendar.getInstance().get(Calendar.YEAR) + " "
                + getPluginMeta().getAuthors().stream().reduce((a, b) -> a + ", " + b).orElse("")
                + ". All rights reserved.");
        getLogger().info("Launching " +
                getPluginMeta().getName() +
                " v" + getPluginMeta().getVersion());

        // Create a new config file if it does not exist. If it was created, send a message to the console.
        if (!getDataFolder().exists()) {
            // Create the data folder.
            getDataFolder().mkdirs();
            // Check if the config file exists.
            if (!new java.io.File(getDataFolder(), "config.yml").exists()) {
                // Create the config file.
                saveDefaultConfig();
                // Log config creation. Print the config creation message.
                // Structure of config creation message: "Created config.yml"
                getLogger().info("Created config.yml");
            }
        }

        // Create a PerspectiveRequester object to be used by the chatlistener.
        this.requester = new PerspectiveRequester(
                getConfig().getString("perspective.api-key"),
                // Get the attributes from the config. Only get the key names, because those are the names of the attributes we want to request. Then turn the list into a string arraylist.
                new ArrayList<>(Objects.requireNonNull(getConfig().getConfigurationSection("perspective.attributes")).getKeys(false)),
                // Get the languages from the config. This is a list.
                (ArrayList<String>) getConfig().getStringList("perspective.languages"),
                // Get the do not store flag from the config.
                getConfig().getBoolean("perspective.do-not-store")
        );

        // Create an automod instance by reading through the config.
        this.automod = Automod.fromConfig(this, Objects.requireNonNull(getConfig().getConfigurationSection("perspective.attributes")));

        // Log config load. Print the config load message.
        // Structure of config load message: "Loaded config.yml"
        getLogger().info("Loaded config.yml");

        // Attach hook to chat listener
        getServer().getPluginManager().registerEvents(new ChatListener(this, this.requester, this.automod), this);

        // Register Standpoint command
        getCommand("standpoint").setExecutor(new StandpointCommand(this));
    }

    @Override
    public void onDisable() {
        // Log disable. Print the disable message.
        // Structure of disable message: "Disabling PluginName vVersion"
        getLogger().info("Disabling " +
                getPluginMeta().getName() +
                " v" + getPluginMeta().getVersion());

        // Say goodbye
        getLogger().info("Goodbye!");
    }
}
