package com.gmail.dejayyy.autoBroadcast;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class abMain extends JavaPlugin {

    public int taskID;

    public int msgID;

    public List<String> messages = new ArrayList<String>();

    public abMain plugin;

    public void onEnable() {


        this.saveDefaultConfig();

        msgID = 0;

        this.startMe();

    }

    public void startMe() {

        long time = this.getConfig().getLong("broadcastDelay");

        messages.clear();

        time = (time * 20);

        for (String blah : this.getConfig().getStringList("Messages")) {

            messages.add(blah);

        }

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, broadcast, time, time);


    }

    public void onDisable() {

        Bukkit.getScheduler().cancelTask(taskID);

    }

    public boolean onCommand(CommandSender sender, Command cmd, String cmdL, String[] args) {

        if (cmdL.equalsIgnoreCase("ab")) {

            if (sender instanceof Player) {
                Player p = (Player) sender;

                if (!(p.hasPermission("autobroadcast.admin"))) {
                    p.sendMessage(ChatColor.AQUA + "You don't have permission to execute that command!");

                    return true;
                }
            }


            if (args.length == 0) {

                sender.sendMessage(ChatColor.DARK_AQUA + "/ab view" + ChatColor.AQUA + " View the messages currently in the configuration.");
                sender.sendMessage(ChatColor.DARK_AQUA + "/ab add [Message]" + ChatColor.AQUA + " Add a message to the broadcast list.");
                sender.sendMessage(ChatColor.DARK_AQUA + "/ab remove [#]" + ChatColor.AQUA + " Remove a message from the broadcast list. Must have the message number which is found in /ab view");
                sender.sendMessage(ChatColor.DARK_AQUA + "/ab restart " + ChatColor.AQUA + "Restart the auto broadcaster. Starts at the very first message.");
                sender.sendMessage(ChatColor.DARK_AQUA + "/ab stop " + ChatColor.AQUA + "Stop the auto broadcaster. Will not broadcast any more messages.");
                sender.sendMessage(ChatColor.DARK_AQUA + "Plugin Author: " + ChatColor.AQUA + "ImDeJay");

                return true;

            } else if (args.length == 1) {

                if (args[0].equalsIgnoreCase("view")) {

                    if (messages.isEmpty()) {

                        sender.sendMessage(ChatColor.AQUA + "No messages to display!");

                        return true;

                    }

                    int y = 0;

                    for (String blah : messages) {

                        sender.sendMessage(ChatColor.AQUA + "[" + y + "] " + ChatColor.RESET + replaceColors(blah));

                        y++;

                    }

                    return true;

                } else if (args[0].equalsIgnoreCase("restart")) {

                    this.reloadConfig();

                    Bukkit.getScheduler().cancelTask(taskID);

                    this.startMe();

                    sender.sendMessage(ChatColor.AQUA + "Auto Broadcaster has been restarted!");

                    return true;

                } else if (args[0].equalsIgnoreCase("stop")) {

                    Bukkit.getScheduler().cancelTask(taskID);

                    sender.sendMessage(ChatColor.AQUA + "Auto Broadcasting has been stopped!");

                    return true;

                } else if (args[0].equalsIgnoreCase("reload")) {

                    this.reloadConfig();

                    sender.sendMessage(ChatColor.AQUA + "Configuration successfully reloaded!");

                    sender.sendMessage(ChatColor.AQUA + "Auto Broadcaster has been restarted!");

                    Bukkit.getScheduler().cancelTask(taskID);

                    msgID = 0;

                    this.startMe();

                    return true;

                } else {

                    sender.sendMessage(ChatColor.AQUA + "Invalid Arguments!");

                    return true;
                } // view

            } else if (args.length >= 2) {

                if (args[0].equalsIgnoreCase("add")) {

                    String broadcastMSG;

                    StringBuilder builder = new StringBuilder();

                    for (int i = 1; i < args.length; i++) {

                        builder.append(args[i]).append(" ");

                    }

                    builder.setLength(builder.length() - 1);

                    broadcastMSG = builder.toString();

                    messages.add(broadcastMSG);

                    this.getConfig().set("Messages", messages);

                    sender.sendMessage(ChatColor.AQUA + "Message successfully added!");

                    this.saveConfig();

                } else if (args[0].equalsIgnoreCase("remove")) {

                    if (isInt(args[1])) {

                        int x = Integer.parseInt(args[1]);

                        if (x <= messages.size()) {

                            messages.remove(x);

                            this.getConfig().set("Messages", messages);

                            sender.sendMessage(ChatColor.AQUA + "Message removed successfully!");

                            this.saveConfig();

                            return true;

                        } else {

                            sender.sendMessage(ChatColor.AQUA + "Message doesnt exist. Check the message number then try again.");

                            return true;

                        }

                    } else {

                        sender.sendMessage(ChatColor.AQUA + "Invalid Message Number. Check the message number then try again.");

                        return true;

                    }


                } else {

                    sender.sendMessage(ChatColor.AQUA + "Invalid Arguments!");

                    return true;

                } //add

            }


        }


        return true;

    }


    private static boolean isInt(String data) {

        Pattern p = Pattern.compile("[0-9]*");
        Matcher m = p.matcher(data);

        boolean result = m.matches();

        return result;
    }


    static String replaceColors(String message) {

        return ChatColor.translateAlternateColorCodes('&', message);

    }


    public Runnable broadcast = new Runnable() { // create the Update Runnable. Register using Bukkit.getScheduler().scheduleSyncRepeatingTask(YOUR_PLUGIN, updateSign, 0L, time);

        @Override
        public void run() {

            if (msgID >= messages.size()) {

                msgID = 0;

            }

            String msg = messages.get(msgID);

            getServer().broadcastMessage(replaceColors(msg));

            msgID++;

        }

    };


}
