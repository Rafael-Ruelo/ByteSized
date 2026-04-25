package com.example.thesiswork;

public class AppPreset {
    private String name;
    private long maxSize;
    private int quality;
    private String description;

    public AppPreset(String name, long maxSize, int quality, String description) {
        this.name = name;
        this.maxSize = maxSize;
        this.quality = quality;
        this.description = description;
    }

    public String getName() { return name; }
    public long getMaxSize() { return maxSize; }
    public int getQuality() { return quality; }
    public String getDescription() { return description; }

    public static AppPreset[] getPresets() {
        return new AppPreset[] {
                new AppPreset("WhatsApp", 16 * 1024 * 1024, 75, "16MB limit"),
                new AppPreset("Discord", 8 * 1024 * 1024, 70, "8MB limit (free)"),
                new AppPreset("Telegram", 10 * 1024 * 1024, 80, "10MB limit"),
                new AppPreset("Instagram", 8 * 1024 * 1024, 80, "8MB limit"),
                new AppPreset("Twitter/X", 5 * 1024 * 1024, 65, "5MB limit"),
                new AppPreset("Gmail", 1024 * 1024, 60, "25MB total"),
                new AppPreset("Messenger", 16 * 1024 * 1024, 75, "25MB limit")
        };
    }
}