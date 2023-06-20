package me.thdr.standpoint;

import me.thdr.standpoint.commands.StandpointCommand;
import me.thdr.standpoint.utils.Automod;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import me.thdr.standpoint.listeners.ChatListener;
import me.thdr.standpoint.utils.PerspectiveRequester;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

// SQLite lib
import org.sqlite.SQLiteDataSource;

public final class Standpoint extends JavaPlugin {

    private PerspectiveRequester requester;
    private Automod automod;
    private Connection connection;

    @Override
    public void onEnable() {
        // Log startup. First print a list of authors from plugin.yml and then the startup message.
        // Use the Plugin class's getLogger() method to get the logger, and the same class's getPluginMeta method to get the plugin.yml file.
        // Join the immutable list of authors with a comma and space.
        // Structure of authors msg: "Copyright (c) Year Author1, Author2, Author3. All rights reserved."
        // Fetch year using Java's Calendar class.
        // Structure of launch message: "Launching PluginName vVersion"
        getLogger().info("Copyright (c) " + Calendar.getInstance().get(Calendar.YEAR) + " " + getPluginMeta().getAuthors().stream().reduce((a, b) -> a + ", " + b).orElse("") + ". All rights reserved.");
        getLogger().info("Launching " + getPluginMeta().getName() + " v" + getPluginMeta().getVersion());

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
        this.requester = new PerspectiveRequester(getConfig().getString("perspective.api-key"),
                // Get the attributes from the config. Only get the key names, because those are the names of the attributes we want to request. Then turn the list into a string arraylist.
                new ArrayList<>(Objects.requireNonNull(getConfig().getConfigurationSection("perspective.attributes")).getKeys(false)),
                // Get the languages from the config. This is a list.
                (ArrayList<String>) getConfig().getStringList("perspective.languages"),
                // Get the do not store flag from the config.
                getConfig().getBoolean("perspective.do-not-store"));

        // Create an automod instance by reading through the config.
        this.automod = Automod.fromConfig(this, Objects.requireNonNull(getConfig().getConfigurationSection("perspective.attributes")));

        // Log config load. Print the config load message.
        // Structure of config load message: "Loaded config.yml"
        getLogger().info("Loaded config.yml");

        // Log database connection. Print the database connection message.
        // Structure of database connection message: "Connecting to database..."
        getLogger().info("Connecting to database...");

        // Create a SQLiteDataSource object.
        SQLiteDataSource dataSource = new SQLiteDataSource();
        // Set the database file to the file "database.db" in the plugin's data folder.
        dataSource.setUrl("jdbc:sqlite:" + getDataFolder().getAbsolutePath() + "/standpoint.db");

        // Try to connect to the database.
        try {
            Connection connection = dataSource.getConnection();
            // Log database connection. Print the database connection message.
            // Structure of database connection message: "Connected to database."
            getLogger().info("Connected to database.");

            // Create a statement object.
            Statement statement = connection.createStatement();

            // Create a table for messages. We are just caching the messages so we can avoid sending them to the API again.
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS messages (message TEXT PRIMARY KEY, results TEXT);");

            // Create a table for users. We want to store the user's uuid, username, average score over all categories provided with a json string (see config), and the number of messages sent.
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS users (uuid TEXT PRIMARY KEY, username TEXT, average TEXT, messages INTEGER);");

            // Log database table creation.
            getLogger().info("Created table users");
            getLogger().info("Created table messages");

            // Attach the database connection to the requester.
            this.requester.setConnection(connection);

            this.connection = connection;

            // Log database connection. Print the database connection message.
            // Structure of database connection message: "Connected to database."
            getLogger().info("Connected to database.");

            // Register ChatListener
            getServer().getPluginManager().registerEvents(new ChatListener(this, this.requester, this.automod, connection), this);

            // Register Standpoint command
            getCommand("standpoint").setExecutor(new StandpointCommand(this));
        } catch (SQLException e) {
            // Log database connection error. Print the database connection error message.
            // Structure of database connection error message: "Failed to connect to database."
            getLogger().severe("Failed to connect to database. Aborting!");
            // Log database connection error. Print the database connection error message.
            // Structure of database connection error message: "Error: ErrorMessage"
            getLogger().severe("Error: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        // Log disable. Print the disable message.
        // Structure of disable message: "Disabling PluginName vVersion"
        getLogger().info("Disabling " + getPluginMeta().getName() + " v" + getPluginMeta().getVersion());

        // Say goodbye
        getLogger().info("Goodbye!");
    }

    public Connection getConnection() {
        return this.connection;
    }

    public PerspectiveRequester getRequester() {
        return this.requester;
    }

    public Automod getAutomod() {
        return this.automod;
    }

    public void reloadConfig() {
        super.reloadConfig();
        this.requester = new PerspectiveRequester(getConfig().getString("perspective.api-key"),
                new ArrayList<>(Objects.requireNonNull(getConfig().getConfigurationSection("perspective.attributes")).getKeys(false)),
                (ArrayList<String>) getConfig().getStringList("perspective.languages"),
                getConfig().getBoolean("perspective.do-not-store"));
        this.automod = Automod.fromConfig(this, Objects.requireNonNull(getConfig().getConfigurationSection("perspective.attributes")));

        // Log config load. Print the config load message.
        // Structure of config load message: "Loaded config.yml"
        getLogger().info("Loaded config.yml");

        // Unattach the old event listener
        HandlerList.unregisterAll(this);

        // Register ChatListener
        getServer().getPluginManager().registerEvents(new ChatListener(this, this.requester, this.automod, this.connection), this);
    }
}
