package com.gmail.dejayyy.killStats;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.gmail.dejayyy.killStats.API.ksAPI;
import com.gmail.dejayyy.killStats.ksLang.langHandler.Lang;

public class ksAdmin implements CommandExecutor {

    private static ksMain plugin;

    public static ksAPI api;


    public ksAdmin(ksMain plugin) {

        ksAdmin.plugin = plugin;

        api = new ksAPI(this);

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg2, String[] args) {

        if (cmd.getName().equalsIgnoreCase("killstatsadmin")) {

            switch (args.length) {


                case 1:

                    if (args[0].equalsIgnoreCase("reload")) {

                        if (sender.hasPermission("killstats_admin.reload")) {

                            plugin.reloadConfig();

                            plugin.seperateWorlds.clear();
                            plugin.sqlWorlds.clear();

                            plugin.initialize();

                            sender.sendMessage(Lang.Prefix.toString() + Lang.Reload_Complete.toString());

                            return true;

                        } else {

                            sender.sendMessage(Lang.Prefix.toString() + Lang.No_Permission.toString());

                            return true;

                        }

                    } else if (args[0].equalsIgnoreCase("enable")) {

                        if (sender.hasPermission("killstats_admin.enable_disable")) {

                            plugin.pluginEnabled = true;

                            String msg = Lang.Plugin_Enabled.toString();

                            if (!(msg.equalsIgnoreCase("disable"))) {

                                sender.sendMessage(Lang.Prefix.toString() + msg);

                            }

                        }


                    } else if (args[0].equalsIgnoreCase("disable")) {

                        if (sender.hasPermission("killstats_admin.enable_disable")) {

                            plugin.pluginEnabled = false;

                            String msg = Lang.Plugin_Disabled.toString();

                            if (!(msg.equalsIgnoreCase("disable"))) {

                                sender.sendMessage(Lang.Prefix.toString() + msg);

                            }

                            return true;

                        }

                    } else if (args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help")) {

                        sender.sendMessage(api.adminInfo());

                        return true;

                    } else {

                        sender.sendMessage(Lang.Prefix.toString() + Lang.Invalid_Command.toString());

                        return true;

                    }


                case 2:

                    if (args[0].equalsIgnoreCase("head")) {

                        if (!(sender.hasPermission("killstats_admin.player_head"))) {

                            sender.sendMessage(Lang.Prefix.toString() + Lang.No_Permission.toString());

                            return true;

                        }

                        if (sender instanceof Player) {

                            Player player = (Player) sender;

                            player.getInventory().setItemInHand(api.playerHead(args[1].toString()));

                            return true;

                        } else {

                            sender.sendMessage(Lang.Prefix.toString() + Lang.Invalid_Console_Command.toString());

                            return true;

                        }

                    } else if (args[0].equalsIgnoreCase("reset")) {

                        if (!(sender.hasPermission("killstats_admin.reset_players"))) {

                            sender.sendMessage(Lang.Prefix.toString() + Lang.No_Permission.toString());

                            return true;

                        }

                        Player targetPlayer = plugin.getServer().getPlayer(args[1].toString());

                        if (targetPlayer == null) {

                            sender.sendMessage(Lang.Prefix.toString() + Lang.Player_Not_Found.toString());

                            return true;

                        } else {

                            api.setDeaths(targetPlayer, 0);
                            api.setKills(targetPlayer, 0);
                            api.setRatio(targetPlayer);
                            api.setStreak(targetPlayer, 0);
                            api.setScoreboardEnabled(targetPlayer, false);

                            sender.sendMessage(api.replaceVariables(Lang.Reset_Success.toString(), targetPlayer, targetPlayer));

                            return true;

                        }
                    }

                default:

                    sender.sendMessage(Lang.Prefix.toString() + Lang.Invalid_Command.toString());

                    return true;

            }

        }

        return true;

    }


}
