package me.thdr.standpoint.utils;

import java.util.ArrayList;

public class Attribute {
    private final ArrayList<Punishment> punishments;
    private final int weight;
    private final String name;

    public Attribute(String name, int weight, ArrayList<Punishment> punishments) {
        this.weight = weight;
        this.punishments = punishments;
        this.name = name;
    }

    public ArrayList<Punishment> getPunishments() {
        return this.punishments;
    }

    public int getWeight() {
        return this.weight;
    }

    public Punishment getPunishment(double score) {
        Punishment punishment = null;
        double highestThreshold = 0;
        for (Punishment p : punishments) {
            // Println the score and threshold
            if (score >= p.getThreshold()) {
                if (p.getThreshold() > highestThreshold) {
                    punishment = p;
                    highestThreshold = p.getThreshold();
                }
            }
        }
        return punishment;
    }

    public String getName() {
        return this.name;
    }
}
