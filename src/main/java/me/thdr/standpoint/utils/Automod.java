package me.thdr.standpoint.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.w3c.dom.Attr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Automod {
    private final ArrayList<Attribute> attributes;
    private final JavaPlugin plugin;

    public Automod(JavaPlugin plugin, ArrayList<Attribute> attributes) {
        this.attributes = attributes;
        this.plugin = plugin;
    }

    public ArrayList<Attribute> getAttributes() {
        return this.attributes;
    }

    public Punishment getPunishment(Map<String, Double> scores) {
        // Get the maximum punishment for each attribute.
        Map<String, Punishment> punishments = new HashMap<>();

        for (Attribute attribute : this.attributes) {
            Punishment punishment = attribute.getPunishment(scores.get(attribute.getName()));
            punishments.put(attribute.getName(), punishment);
        }

        // Get the punishment with the highest threshold, and if two punishments have the same threshold, get the one with the highest weight.
        // Return the punishment for the attribute with the highest weight.
        Punishment maxPunishment = null;

        for (Attribute attribute : this.attributes) {
            Punishment punishment = punishments.get(attribute.getName());
            if (punishment != null) {
                if (maxPunishment == null) {
                    maxPunishment = punishment;
                } else {
                    if (punishment.getThreshold() > maxPunishment.getThreshold()) {
                        maxPunishment = punishment;
                    } else if (punishment.getThreshold() == maxPunishment.getThreshold()) {
                        // Since attributes and punishments are in the same order, we can just compare the index of the attribute to get the weight. We cannot do attributes.indexOf with a punishment so fetch from punishments map.
                        if (this.attributes.indexOf(attribute) > this.attributes.indexOf(this.getAttribute(scores))) {
                            maxPunishment = punishment;
                        }
                    }
                }
            }
        }

        return maxPunishment;
    }

    public Attribute getAttribute(Map<String, Double> scores) {
        // Same thing as getPunishment, but instead get the attribute with the highest weight that has a punishment.
        Map<String, Punishment> punishments = new HashMap<>();

        for (Attribute attribute : this.attributes) {
            Punishment punishment = attribute.getPunishment(scores.get(attribute.getName()));
            punishments.put(attribute.getName(), punishment);
        }

        Attribute maxAttribute = null;

        for (Attribute attribute : this.attributes) {
            if (maxAttribute == null) {
                maxAttribute = attribute;
            } else {
                if (attribute.getWeight() > maxAttribute.getWeight() && punishments.get(attribute.getName()) != null) {
                    maxAttribute = attribute;
                }
            }
        }

        return maxAttribute;
    }

    static public Automod fromConfig(JavaPlugin plugin, ConfigurationSection config) {
        // Get list of attributes.
        ArrayList<Attribute> attributes = new ArrayList<>();
        for (String attributeName : config.getKeys(false)) {
            ConfigurationSection attributeConfig = config.getConfigurationSection(attributeName);
            // Get weight.
            assert attributeConfig != null;
            int weight = attributeConfig.getInt("weight");
            // Get logger, print the weight.
            plugin.getLogger().info("Weight for attribute " + attributeName + " is " + weight + ".");
            // Get punishments.
            ArrayList<Punishment> punishments = new ArrayList<>();
            // Since its a list of different punishments, we need to iterate through them. It is not a map so don't try to get the keys!
            ArrayList<LinkedHashMap> punishmentsConfig = (ArrayList<LinkedHashMap>) attributeConfig.getList("punishments");

            for (LinkedHashMap punishmentConfigSection : punishmentsConfig) {
                // Get threshold.
                assert punishmentConfigSection != null;
                double threshold = (double) punishmentConfigSection.get("threshold");
                // Get cancel.
                boolean cancel = (boolean) punishmentConfigSection.get("cancel");
                // Get message.
                String message = (String) punishmentConfigSection.get("message");
                // Get commands as a stringlist
                ArrayList<String> commands = (ArrayList<String>) punishmentConfigSection.get("commands");
                // Create punishment.
                Punishment punishment = new Punishment(threshold, cancel, message, commands);
                punishments.add(punishment);
            }
            // Create attribute.
            Attribute attribute = new Attribute(attributeName, weight, punishments);
            attributes.add(attribute);
        }

        return new Automod(plugin, attributes);
    }
}