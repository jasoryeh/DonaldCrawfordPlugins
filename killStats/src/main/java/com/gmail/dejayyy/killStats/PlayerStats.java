package com.gmail.dejayyy.killStats;

public class PlayerStats {
    private String player;
    private int kills;
    private int deaths;
    private int streak;
    private double ratio;
    boolean sbEnabled;
    

    // Instantiate
    public PlayerStats(String player, int kills, int deaths, int streak, double ratio, boolean scoreBoardEnabled) {
            this.player = player;
            this.kills = kills;
            this.deaths = deaths;
            this.streak = streak;
            this.ratio = ratio;
            this.sbEnabled = scoreBoardEnabled;
    }
    

    // Getters
    public String getPlayer() { return player; }
    public int getKills() { return kills; }
    public int getDeaths() { return deaths; }
    public int getStreak() { return streak; }
    public double getRatio() { return ratio; }
    public boolean getScoreboardEnabled() { return sbEnabled; }
    

    // Setters
    public void setPlayer(String player) { this.player = player; }
    public void setKills(int kills) { this.kills = kills; }
    public void setDeaths(int deaths) { this.deaths = deaths; }
    public void setStreak(int streak) { this.streak = streak; }
    
    public void setRatio(double ratio) { this.ratio = ratio; }
    public void setScoreboardEnabled(boolean yesno) { this.sbEnabled = yesno; }
    

    // Convenience
    public void kill() {
            kills++;
            streak++;
    }
    
    public void death() {
            deaths++;
            streak = 0;
    }
}
