package me.thdr.standpoint.utils;

import java.util.ArrayList;

public class Punishment {
    private final double threshold;
    private final boolean cancel;
    private final String message;
    private final ArrayList<String> commands;

    public Punishment(double threshold, boolean cancel, String message, ArrayList<String> commands) {
        this.threshold = threshold;
        this.cancel = cancel;
        this.message = message;
        this.commands = commands;
    }

    public double getThreshold() {
        return this.threshold;
    }

    public boolean getCancel() {
        return this.cancel;
    }

    public String getMessage() {
        return this.message;
    }

    public ArrayList<String> getCommands() {
        return this.commands;
    }
}
