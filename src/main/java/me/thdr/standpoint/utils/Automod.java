package me.thdr.standpoint.utils;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Automod {
    private final ArrayList<Attribute> attributes;

    public Automod(ArrayList<Attribute> attributes) {
        this.attributes = attributes;
    }

    public ArrayList<Attribute> getAttributes() {
        return this.attributes;
    }

    public Punishment getPunishment(Map<String, Double> scores) {
        // Get the maximum punishment for each attribute.
        Map<String, Punishment> punishments = new HashMap<>();
        for (Attribute attribute : attributes) {
            Punishment punishment = attribute.getPunishment(scores.get(attribute.getName()));
            if (punishment != null) {
                punishments.put(attribute.getName(), punishment);
            }
        }

        // Get the maximum punishment, using the weights of each attribute to decide what punishment to give.
        Punishment maxPunishment = null;
        for (String attribute : punishments.keySet()) {
            Punishment punishment = punishments.get(attribute);
            if (maxPunishment == null) {
                maxPunishment = punishment;
            } else {
                if (punishment.getThreshold() > maxPunishment.getThreshold()) {
                    maxPunishment = punishment;
                }
            }
        }

        return maxPunishment;
    }

    static public Automod fromConfig(ConfigurationSection config) {
        // Get list of attributes.
        ArrayList<Attribute> attributes = new ArrayList<>();
        for (String attributeName : config.getKeys(false)) {
            ConfigurationSection attributeConfig = config.getConfigurationSection(attributeName);
            // Get weight.
            assert attributeConfig != null;
            int weight = attributeConfig.getInt("weight");
            // Get punishments.
            ArrayList<Punishment> punishments = new ArrayList<>();
            // Since its a list of different punishments, we need to iterate through them. It is not a map so don't try to get the keys!
            ConfigurationSection punishmentsConfig = attributeConfig.getConfigurationSection("punishments");
            // Each punishment is an entry, which contains threshold, cancel, message, and commands.
            assert punishmentsConfig != null;
            for (String punishmentName : punishmentsConfig.getKeys(false)) {
                ConfigurationSection punishmentConfig = punishmentsConfig.getConfigurationSection(punishmentName);
                // Get threshold.
                assert punishmentConfig != null;
                double threshold = (double) punishmentConfig.getDouble("threshold");
                // Get cancel.
                boolean cancel = punishmentConfig.getBoolean("cancel");
                // Get message.
                String message = punishmentConfig.getString("message");
                // Get commands.
                ArrayList<String> commands = new ArrayList<>();
                ConfigurationSection commandsConfig = punishmentConfig.getConfigurationSection("commands");
                assert commandsConfig != null;
                for (String commandName : commandsConfig.getKeys(false)) {
                    commands.add(commandsConfig.getString(commandName));
                }
                // Create punishment.
                Punishment punishment = new Punishment(threshold, cancel, message, commands);
                punishments.add(punishment);
            }
            // Create attribute.
            Attribute attribute = new Attribute(attributeName, weight, punishments);
            attributes.add(attribute);
        }

        return new Automod(attributes);
    }
}
