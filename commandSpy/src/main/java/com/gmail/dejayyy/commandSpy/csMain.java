package com.gmail.dejayyy.commandSpy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class csMain extends JavaPlugin implements Listener {

    public HashSet<String> mySet = new HashSet<String>();

    public void onEnable() {

        this.getServer().getPluginManager().registerEvents(this, this);

        this.saveDefaultConfig();

    }

    public void onDisable() {

        mySet.clear();

    }


    public boolean onCommand(CommandSender sender, Command cmd, String cmdl, String[] args) {

        if (!(sender instanceof Player)) {

            if (args.length == 1) {

                if (args[0].equalsIgnoreCase("reload")) {

                    this.reloadConfig();

                    sender.sendMessage(ChatColor.DARK_AQUA + "[commandSpy] " + ChatColor.AQUA + "Reload Comlpete.");

                    return true;

                } else if (args[0].equals("logdata")) {

                    if (this.getConfig().getBoolean("LogData")) {

                        this.getConfig().set("LogData", false);

                        this.saveConfig();

                        sender.sendMessage(ChatColor.DARK_AQUA + "[commandSpy] " + ChatColor.AQUA + "Data will no longer be logged.");

                        this.reloadConfig();

                        return true;

                    } else {

                        this.getConfig().set("LogData", true);

                        this.saveConfig();

                        sender.sendMessage(ChatColor.DARK_AQUA + "[commandSpy] " + ChatColor.AQUA + "Data will now be logged.");

                        this.reloadConfig();

                        return true;

                    }

                } else {

                    sender.sendMessage(ChatColor.DARK_AQUA + "[commandSpy] " + ChatColor.AQUA + "You goofball, you can't execute that command from console!");

                    return true;
                }

            } else {

                sender.sendMessage(ChatColor.DARK_AQUA + "[commandSpy] " + ChatColor.AQUA + "You goofball, you can't execute that command from console!");

                return true;

            }

        }

        Player player = (Player) sender;

        if (cmdl.equalsIgnoreCase("commandspy") || cmdl.equalsIgnoreCase("cspy")) {

            if (args.length == 0) {

                if (mySet.contains(player.getName())) {

                    mySet.remove(player.getName());

                    this.msgPlayer(player, "Command Spy Disabled.");

                    return true;

                } else {

                    mySet.add(player.getName());

                    this.msgPlayer(player, "Command Spy Enabled.");

                    return true;

                } //set.contains

            } else if (args.length == 1) {

                if (args[0].equalsIgnoreCase("reload")) {

                    if (player.hasPermission("commandSpy.admin")) {

                        this.reloadConfig();

                        this.msgPlayer(player, "Reload Complete.");

                        return true;

                    } else {

                        this.msgPlayer(player, "You don't have permission to execute that command!");

                        return true;

                    }


                } else if (args[0].equals("logdata")) {

                    if (player.hasPermission("commandSpy.admin")) {

                        if (this.getConfig().getBoolean("LogData")) {

                            this.getConfig().set("LogData", false);

                            this.saveConfig();

                            this.msgPlayer(player, "Data will no longer be logged.");

                            this.reloadConfig();

                            return true;

                        } else {

                            this.getConfig().set("LogData", true);

                            this.saveConfig();

                            this.msgPlayer(player, "Data will now be logged.");

                            this.reloadConfig();

                            return true;

                        }

                    } else {

                        this.msgPlayer(player, "You don't have permission to execute that command!");

                        return true;

                    }

                } else {

                    this.msgPlayer(player, "Invalid Arguments!");

                    return true;

                }
            } else {

                this.msgPlayer(player, "Invalid Arguments!");

                return true;

            }

        } //cmdL
        return true;

    }

    @EventHandler
    public void playerCMD(PlayerCommandPreprocessEvent event) {

        List<String> ignored = this.getConfig().getStringList("IgnoredCommands");

        if (event.getMessage().contains(" ")) {

            String[] blah = event.getMessage().split(" ");

            for (String a : ignored) {

                if (blah[0].equalsIgnoreCase("/" + a)) {

                    return;

                }
            }

        } else {

            for (String a : ignored) {

                if (event.getMessage().equalsIgnoreCase("/" + a)) {

                    return;

                }
            }

        }

        if (this.getConfig().getBoolean("LogData")) {

            this.logInfo(event.getPlayer().getName(), event.getMessage());

        }


        for (Player p : getServer().getOnlinePlayers()) {

            if (p.hasPermission("commandSpy.spy") && mySet.contains(p.getName())) {

                p.sendMessage(ChatColor.WHITE + event.getPlayer().getName() + ": " + event.getMessage());

            } //permission + set.contains

        } //online players

    }


    public void logInfo(String name, String message) {

        try {

            File file = new File(this.getDataFolder() + "/log.txt");

            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);

            BufferedWriter bw = new BufferedWriter(fw);


            bw.append("[" + this.currentTime() + "] " + name + ": " + message);

            bw.newLine();

            bw.close();

        } catch (IOException e) {

            e.printStackTrace();

        }


    }

    public String currentTime() {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }


    public void msgPlayer(Player player, String msg) {

        player.sendMessage(ChatColor.DARK_AQUA + "[commandSpy] " + ChatColor.AQUA + msg);

    }


}
