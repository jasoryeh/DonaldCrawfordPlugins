package com.gmail.dejayyy.killStats.ksListener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;


import com.gmail.dejayyy.killStats.PlayerStats;
import com.gmail.dejayyy.killStats.ksMain;
import com.gmail.dejayyy.killStats.API.ksAPI;
import com.gmail.dejayyy.killStats.MySQL.MySQL;
import com.gmail.dejayyy.killStats.ksLang.langHandler.Lang;

@SuppressWarnings("deprecation")
public class ksListener implements Listener {

    private static ksMain plugin;

    public static ksAPI api;

    public ksListener(ksMain plugin) {

        ksListener.plugin = plugin;

        api = new ksAPI(plugin);


    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        //changed to sqlite
        Player player = event.getPlayer();

        api.matchNameToUUID(player);

        api.putSQLDataIntoHashmap(player);

        return;


    }

    @EventHandler
    public void signCreate(SignChangeEvent event) {

        if (event.isCancelled()) {

            return;

        }

        Player player = event.getPlayer();

        if (event.getLine(0).equalsIgnoreCase("[ksrank]")) {

            if (api.worldDisabled(player.getWorld().getName())) {

                player.sendMessage(Lang.Prefix.toString() + Lang.World_Disabled.toString());

                event.setCancelled(true);

                return;
            }

            String worldName = player.getWorld().getName();

            if (!(player.hasPermission("killstats.admin.createsign"))) {

                player.sendMessage(Lang.Prefix.toString() + Lang.No_Permission.toString());

                event.getBlock().breakNaturally();

                event.setCancelled(true);

                return;

            }

            if (api.isInt(event.getLine(2)) && event.getLine(2) != null) {

                String theRank = event.getLine(2).toString();
                String theType = event.getLine(1);
                Location location = event.getBlock().getLocation();

                if (theType.equalsIgnoreCase("kills")) {

                    event.setLine(0, Lang.Sign_Top_Kills.toString().replace("%ranking%", theRank));
                    event.setLine(1, null);
                    event.setLine(2, Lang.Sign_Playername_Color.toString() + api.getRank_Name(worldName, "kills", theRank));
                    event.setLine(3, Lang.Sign_Stats_Color.toString() + api.getRank_Number(worldName, "kills", theRank));

                    api.saveSign(location, theType, theRank);

                } else if (theType.equalsIgnoreCase("deaths")) {

                    event.setLine(0, Lang.Sign_Top_Deaths.toString().replace("%ranking%", theRank));
                    event.setLine(1, null);
                    event.setLine(2, Lang.Sign_Playername_Color.toString() + api.getRank_Name(worldName, "deaths", theRank));
                    event.setLine(3, Lang.Sign_Stats_Color.toString() + api.getRank_Number(worldName, "deaths", theRank));

                    api.saveSign(location, theType, theRank);

                } else if (theType.equalsIgnoreCase("streak")) {

                    event.setLine(0, Lang.Sign_Top_Streak.toString().replace("%ranking%", theRank));
                    event.setLine(1, null);
                    event.setLine(2, Lang.Sign_Playername_Color.toString() + api.getRank_Name(worldName, "streak", theRank));
                    event.setLine(3, Lang.Sign_Stats_Color.toString() + api.getRank_Number(worldName, "streak", theRank));

                    api.saveSign(location, theType, theRank);

                } else if (theType.equalsIgnoreCase("ratio")) {

                    event.setLine(0, Lang.Sign_Top_Ratio.toString().replace("%ranking%", theRank));
                    event.setLine(1, null);
                    event.setLine(2, Lang.Sign_Playername_Color.toString() + api.getRank_Name(worldName, "ratio", theRank));
                    event.setLine(3, Lang.Sign_Stats_Color.toString() + api.getRank_Number(worldName, "ratio", theRank));

                    api.saveSign(location, theType, theRank);

                } else {

                    player.sendMessage(Lang.Prefix.toString() + Lang.Sign_Invalid_Format.toString());

                    event.getBlock().breakNaturally();

                    event.setCancelled(true);

                    return;

                }

                //TODO Save to signs.yml

            } else {

                player.sendMessage(Lang.Prefix.toString() + Lang.Sign_Invalid_Format.toString());

                event.getBlock().breakNaturally();

                event.setCancelled(true);

                return;

            }

        }

    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {

        if (event.isCancelled()) {

            return;

        }

        Block block = event.getBlock();

        if (block.getState() instanceof Sign) {

            if (api.isRankSign(block.getLocation())) {

                api.removeSignFromYML(block.getLocation());

                return;

            }

        }

        api.CheckRankSignAttatchedToBlock(block);


    }


    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        api.saveSQLDataFromHashmap(player);

    }

    @EventHandler
    public void playerDeath(PlayerDeathEvent event) {

        if (!(plugin.pluginEnabled)) {

            return;

        }

        boolean antiBoost = plugin.getConfig().getBoolean("AntiBoost.Enabled", true);

        if (api.worldDisabled(event.getEntity().getWorld().getName())) {

            return;

        }

        if (!(event.getEntity().getKiller() instanceof Player)) {

            return;

        }

        if (event.getEntity().getKiller() instanceof Player) {

            final Player victim = event.getEntity();
            final Player killer = victim.getKiller();

            int killerKills = api.getKills(killer);
            int killerStreak = api.getStreak(killer);
            int victimDeaths = api.getDeaths(victim);
            int victimStreak = api.getStreak(victim);

            if (antiBoost) {

                if (api.isBoosting(killer, victim)) {

                    String bAlert = Lang.Booster_Alert.toString();

                    if (!(bAlert.equalsIgnoreCase("disable"))) {

                        killer.sendMessage(Lang.Prefix + api.replaceVariables(bAlert, killer, victim));

                    }

                    return;

                } //they are boosting

                plugin.deadList.add(killer.getName() + ":" + victim.getName());

                long coolDown = plugin.getConfig().getLong("AntiBoost.Time_Limit", 5);

                coolDown = (coolDown * 60 * 20);

                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {

                        plugin.deadList.remove(killer.getName() + ":" + victim.getName());

                    }

                }, coolDown);

            } //anti-boost


            if (victimStreak > 0) {

                if (plugin.getConfig().getBoolean("Broadcast_Broken_Streak.Enabled", true) == true) {

                    int minStreak = plugin.getConfig().getInt("Broadcast_Broken_Streak.Minimum_Streak_To_Broadcast", 10);

                    if (api.getStreak(victim) >= minStreak) {

                        api.HandleBrokenStreak(killer, victim);

                    }


                }

            }

            killerKills++;
            victimDeaths++;
            killerStreak++;

            api.setKills(killer, killerKills);
            api.setDeaths(victim, victimDeaths);
            api.setStreak(killer, killerStreak);
            api.setStreak(victim, 0);

            api.HandleBroadcast(killer, victim);

            if (plugin.getConfig().getBoolean("Drop_Head.Enabled", true)) {

                api.DropHead(victim);

            }

            if (plugin.getConfig().getBoolean("Rewards_Enabled", false)) {

                api.HandleRewards(killer, victim);

            }

        }


    }

    @EventHandler
    public void playerChat(PlayerChatEvent event) {

        if (event.isCancelled()) {

            return;

        }

        Player player = event.getPlayer();
        String msg = event.getMessage();

        if (plugin.reset.contains(player.getName())) {

            if (msg.equalsIgnoreCase("yes") || msg.equalsIgnoreCase("y")) {

                api.setKills(player, 0);
                api.setDeaths(player, 0);
                api.setStreak(player, 0);

                player.sendMessage(Lang.Prefix.toString() + Lang.Startover_Success.toString());

                plugin.reset.remove(player.getName());

                plugin.getServer().getScheduler().cancelTask(plugin.startoverTaskID);

            } else if (msg.equalsIgnoreCase("no") || msg.equalsIgnoreCase("n")) {

                plugin.reset.remove(player.getName());

                player.sendMessage(Lang.Prefix.toString() + Lang.Startover_Cancelled.toString());

                plugin.getServer().getScheduler().cancelTask(plugin.startoverTaskID);

            } else {

                player.sendMessage(Lang.Prefix.toString() + Lang.Invalid_Answer.toString());

            }

            event.setCancelled(true);
        }

    }


}
