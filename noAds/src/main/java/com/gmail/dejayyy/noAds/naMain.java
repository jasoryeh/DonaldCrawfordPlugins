package com.gmail.dejayyy.noAds;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class naMain extends JavaPlugin implements Listener {

    public static File configFile;
    public static FileConfiguration playersFile;
    public List<String> matches = new ArrayList<String>();

    public List<String> whitelist = new ArrayList<String>();

    boolean whitelisted;

    public void onEnable() {

        this.saveDefaultConfig();

        for (String blah : this.getConfig().getStringList("WhiteList")) {

            whitelist.add(blah);
            whitelist.add("www." + blah.toLowerCase());
            whitelist.add("http://www." + blah.toLowerCase());
            whitelist.add("http://" + blah.toLowerCase());
            whitelist.add("http://ww." + blah.toLowerCase());
            whitelist.add("ww." + blah.toLowerCase());

        }

        this.getServer().getPluginManager().registerEvents(this, this);

    }

    public void onDisable() {


    }


    public boolean onCommand(CommandSender sender, Command cmd, String cmdl, String[] args) {

        if (cmdl.equalsIgnoreCase("noads")) {

            if (args.length == 1) {

                if (args[0].equalsIgnoreCase("reload")) {

                    if (sender instanceof Player) {

                        Player p = (Player) sender;

                        if (p.hasPermission("noAds.admin")) {

                            this.reloadConfig();

                            whitelist.clear();

                            for (String blah : this.getConfig().getStringList("WhiteList")) {

                                whitelist.add(blah);
                                whitelist.add("www." + blah.toLowerCase());
                                whitelist.add("http://www." + blah.toLowerCase());
                                whitelist.add("http://" + blah.toLowerCase());

                            }

                            p.sendMessage(ChatColor.AQUA + "[noAds] Configuration successfully reloaded!");

                            return true;

                        }

                    } else {

                        whitelist.clear();

                        for (String blah : this.getConfig().getStringList("WhiteList")) {

                            whitelist.add(blah);
                            whitelist.add("www." + blah.toLowerCase());
                            whitelist.add("http://www." + blah.toLowerCase());
                            whitelist.add("http://" + blah.toLowerCase());

                        }


                        this.reloadConfig();

                        sender.sendMessage(ChatColor.AQUA + "[noAds] Configuration successfully reloaded!");

                    }
                } else {
                    sender.sendMessage(ChatColor.AQUA + "[noAds] Invalid Arguments!");
                }
            } else {

                sender.sendMessage(ChatColor.AQUA + "[noAds] Invalid Arguments!");
            }
        }

        return true;

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerChat(AsyncPlayerChatEvent event) {

        boolean debugmode = this.getConfig().getBoolean("debugmode");

        String msg = event.getMessage();
        Player player = event.getPlayer();

        matches.clear();

        msg = msg.toLowerCase();

        if (debugmode) {

            if (safeMSG(player, msg) == false || safeMSG(player, msg.replaceAll("dot", ".")) == false) {

                player.sendMessage(ChatColor.DARK_AQUA + "[noAds Debug] " + ChatColor.AQUA + "That was advertisement!");

                event.setCancelled(true);


            } else {

                player.sendMessage(ChatColor.DARK_AQUA + "[noAds Debug] " + ChatColor.AQUA + "That was not advertisement!");

                event.setCancelled(true);

            }

            return;


        }

        if (player.hasPermission("noAds.bypass")) {

            return;

        }

        if (safeMSG(player, msg) == false || safeMSG(player, msg.replaceAll("dot", ".")) == false) {

            this.logInfo(player.getName(), msg);

            player.setBanned(true);
            player.kickPlayer("Server Advertisement!");

            event.setCancelled(true);

        }

    }

    public void logInfo(String name, String message) {

        try {

            File file = new File(this.getDataFolder() + "/log.txt");

            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);

            BufferedWriter bw = new BufferedWriter(fw);


            bw.append(this.currentTime() + " " + name + ": " + message);

            bw.newLine();

            bw.close();

        } catch (IOException e) {

            e.printStackTrace();

        }


    }


    public boolean checkForIp(Player player, String str) {

        List<String> ips = new ArrayList<String>();

        boolean debug = this.getConfig().getBoolean("debugmode");

        String ipPattern = "\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b";

        Pattern r = Pattern.compile(ipPattern);

        Matcher m = r.matcher(str);

        ips.clear();

        while (m.find()) {

            ips.add(m.group());

        }

        for (String blah : ips) {

            if (debug) {

                player.sendMessage(ChatColor.DARK_AQUA + "[noAds Debug] " + ChatColor.AQUA + "IP Found: " + blah);

            }


            if (whitelist.contains(blah)) {

                return false;

            } else {

                return true;

            }

        }


        if (m.find()) {

            return true;

        }

        return false;
    }

    public boolean checkForDomain(Player player, String str) {

        boolean debug = this.getConfig().getBoolean("debugmode");

        String domainPattern = "[a-zA-Z0-9](([a-zA-Z0-9\\-]{0,61}[A-Za-z0-9])?\\.)+(com|net|org)";
        // [a-zA-Z0-9](([a-zA-Z0-9\\-]{0,61}[A-Za-z0-9])?\\.)+[a-zA-Z0-9]{2,4}
        Pattern r = Pattern.compile(domainPattern);

        Matcher m = r.matcher(str);

        while (m.find()) {

            matches.add(m.group());

        }

        for (String blah : matches) {

            if (debug) {

                player.sendMessage(ChatColor.DARK_AQUA + "[noAds Debug] " + ChatColor.AQUA + "Link Found: " + blah);

            }


            if (whitelist.contains(blah)) {

                return false;

            } else {

                return true;


            }

        }
        return false;

    }

    public boolean safeMSG(Player player, String message) {

        boolean debug = this.getConfig().getBoolean("debugmode");

        if (!(debug)) {

            if (player.hasPermission("noAds.bypass")) {

                return true;

            }
        }

        if (checkForIp(player, message)) {

            return false;

        }

        if (checkForDomain(player, message)) {

            return false;
        }

        return true;

    }


    public String currentTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }


}
