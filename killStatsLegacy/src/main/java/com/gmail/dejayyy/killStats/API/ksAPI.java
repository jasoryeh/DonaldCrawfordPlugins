package com.gmail.dejayyy.killStats.API;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;

import com.gmail.dejayyy.killStats.API.MySQL;
import com.gmail.dejayyy.killStats.Main;

public class ksAPI {

    private static Main plugin;
    static MySQL sql;

    public ksAPI(Main plugin) {
        ksAPI.plugin = plugin;

    }

    //API
    public static ksAPI getAPI() {

        return Main.getAPI();

    }
    //API

    public synchronized void makeConnection() {

        String dbHost = plugin.getConfig().getString("dbHost", "localhost");
        String dbPort = plugin.getConfig().getString("dbPort", "3306");
        String dbName = plugin.getConfig().getString("dbName", "minecraft");
        String dbUsername = plugin.getConfig().getString("dbUsername", "User");
        String dbPassword = plugin.getConfig().getString("dbPassword", "Pass");

        sql = new MySQL(dbHost, dbPort, dbName, dbUsername, dbPassword);

        sql.open();

    }

    public boolean seperateWorld(String worldName) {

        return plugin.getConfig().getStringList("SeperateWorlds").contains(worldName);

    }

    public int getKills(String playerName, String worldName) {

        if (plugin.useDB) {

            this.makeConnection();

            try {

                Connection con = sql.getConnection();

                PreparedStatement ps = con.prepareStatement("SELECT `kills` FROM `killstats_data` WHERE playername=?");
                ps.setString(1, playerName);

                ResultSet rs = ps.executeQuery();
                rs.next();

                int k = rs.getInt("kills");

                rs.close();

                return k;

            } catch (SQLException ex) {

                ex.printStackTrace();

            }


        }


        if (seperateWorld(worldName)) {

            if (plugin.playersFile.isSet("Players." + worldName + "." + playerName)) {

                return plugin.playersFile.getInt("Players." + worldName + "." + playerName + ".kills");

            } else {

                return 0;

            }

        } else if (plugin.playersFile.isSet("Players." + playerName)) {

            return plugin.playersFile.getInt("Players." + playerName + ".kills");

        } else {

            return 0;

        }

    }

    public boolean hasProfile(String playerName, String worldName) {

        if (seperateWorld(worldName)) {

            return plugin.playersFile.isSet("Players." + worldName + "." + playerName + ".kills");

        } else {

            return plugin.playersFile.isSet("Players." + playerName + ".kills");

        }

    }

    public void setupProfile(String playerName, String worldName) {

        if (seperateWorld(worldName)) {

            if (!(this.hasProfile(playerName, worldName))) {

                this.setKills(playerName, 0, worldName);
                this.setDeaths(playerName, 0, worldName);
                this.setStreak(playerName, 0, worldName);

                plugin.savePlayerYML();

            }

        } else if (plugin.playersFile.isSet("Players." + playerName + ".kills") == false) {

            plugin.playersFile.set("Players." + playerName + ".kills", 0);
            plugin.playersFile.set("Players." + playerName + ".deaths", 0);
            plugin.playersFile.set("Players." + playerName + ".streak", 0);

            plugin.savePlayerYML();

        }

    }

    public int getDeaths(String playerName, String worldName) {

        if (seperateWorld(worldName)) {

            if (this.hasProfile(playerName, worldName)) {

                return plugin.playersFile.getInt("Players." + worldName + "." + playerName + ".deaths");

            } else {

                return 0;

            }

        } else if (plugin.playersFile.isSet("Players." + playerName)) {

            return plugin.playersFile.getInt("Players." + playerName + ".deaths");

        } else {

            return 0;

        }

    }

    public void setStreak(String playerName, int streak, String worldName) {

        if (seperateWorld(worldName)) {

            plugin.playersFile.set("Players." + worldName + "." + playerName + ".streak", streak);

        } else {

            plugin.playersFile.set("Players." + playerName + ".streak", streak);

        }

        plugin.savePlayerYML();

    }

    public void setKills(String playerName, int kills, String worldName) {


        if (seperateWorld(worldName)) {

            plugin.playersFile.set("Players." + worldName + "." + playerName + ".kills", kills);

        } else {

            plugin.playersFile.set("Players." + playerName + ".kills", kills);

        }

        plugin.savePlayerYML();

    }

    public void setDeaths(String playerName, int deaths, String worldName) {

        if (seperateWorld(worldName)) {

            plugin.playersFile.set("Players." + worldName + "." + playerName + ".deaths", deaths);

        } else {

            plugin.playersFile.set("Players." + playerName + ".deaths", deaths);

        }


        plugin.savePlayerYML();

    }


    public int getStreak(String playerName, String worldName) {

        if (seperateWorld(worldName)) {

            if (this.hasProfile(playerName, worldName)) {

                return plugin.playersFile.getInt("Players." + worldName + "." + playerName + ".streak");

            } else {

                return 0;

            }

        } else if (plugin.playersFile.isSet("Players." + playerName)) {

            return plugin.playersFile.getInt("Players." + playerName + ".streak");

        } else {

            return 0;

        }

    }

    public double getRatio(Player player, String worldName) {

        if (this.hasProfile(player.getName(), worldName)) {

            double kills = this.getKills(player.getName(), worldName);
            double deaths = this.getDeaths(player.getName(), worldName);
            double ratio;

            if (kills == 0 && deaths == 0) {

                ratio = 0.0;

            } else if (kills > 0 && deaths == 0) {

                ratio = kills;

            } else if (deaths > 0 && kills == 0) {

                ratio = -deaths;

            } else {

                ratio = (kills / deaths);

            }

            ratio = Math.round(ratio * 100.0) / 100.0;

            return ratio;

        } else {

            return 0.0;

        }

    }


}
