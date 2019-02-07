package com.gmail.dejayyy.customMotd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class cmMain extends JavaPlugin implements Listener {

    int intNum;

    public File pluginFolder;
    public File configFile;
    public FileConfiguration playersFile;
    List<String> messages = new ArrayList<String>();

    public void onEnable() {

        this.getServer().getPluginManager().registerEvents(this, this);

        this.loadPlayerYML();

        this.saveDefaultConfig();

        messages = this.getConfig().getStringList("Messages");


    }

    public boolean onCommand(CommandSender sender, Command cmd, String cmdl, String[] args) {

        if (sender instanceof Player) {

            Player player = (Player) sender;

            if (cmdl.equalsIgnoreCase("custommotd") || cmdl.equalsIgnoreCase("cmotd")) {

                if (player.hasPermission("customMotd.admin")) {

                    if (args.length == 1) {

                        if (args[0].equalsIgnoreCase("list")) {

                            int x = 0;

                            for (String m : messages) {

                                player.sendMessage(ChatColor.DARK_AQUA + "[" + x + "] " + ChatColor.AQUA + m);

                                x++;

                            }

                        } else if (args[0].equalsIgnoreCase("reload")) {

                            this.reloadConfig();

                            this.msgPlayer(player, "Reload Complete.");

                            return true;

                        } else {

                            this.msgPlayer(player, "Invalid Arguments!");

                            return true;

                        } //list

                    } else if (args.length >= 2) {

                        if (args[0].equalsIgnoreCase("remove")) {

                            if (isInt(args[1])) {

                                int y = Integer.parseInt(args[1]);

                                if (y <= messages.size()) {

                                    messages.remove(y);

                                    this.getConfig().set("Messages", messages);

                                    this.msgPlayer(player, "Message successfully removed.");

                                    this.saveConfig();

                                    return true;

                                } else {

                                    this.msgPlayer(player, "Message ID doesnt exist, check the ID then try again.");
                                    this.msgPlayer(player, "To view messages and their ID, type /cmotd list");

                                    return true;

                                }//msgs.size

                            } //isInt

                        } else if (args[0].equalsIgnoreCase("add")) {

                            String motdMSG;

                            StringBuilder builder = new StringBuilder();

                            for (int i = 1; i < args.length; i++) {

                                builder.append(args[i]).append(" ");

                            }

                            builder.setLength(builder.length() - 1);

                            motdMSG = builder.toString();

                            messages.add(motdMSG);

                            this.getConfig().set("Messages", messages);

                            this.msgPlayer(player, "Message added successfully!");

                            this.saveConfig();

                        } else {

                            this.msgPlayer(player, "Invalid Arguments!");

                            return true;

                        } //remove

                    } else {

                        player.sendMessage(ChatColor.AQUA + "/cmotd list " + ChatColor.DARK_AQUA + "List all messages and their ID's.");
                        player.sendMessage(ChatColor.AQUA + "/cmotd remove [X] " + ChatColor.DARK_AQUA + "Remove a selected message from the list. X = Message ID");
                        player.sendMessage(ChatColor.AQUA + "/cmotd add [Message] " + ChatColor.DARK_AQUA + "Add a message to the MOTD message list.");

                    } //args.length

                } else {

                    this.msgPlayer(player, "You don't have permission to execute that command!");

                } // permission

            } //cmdl

        } //instanceof player

        return true;

    }

    private static boolean isInt(String data) {

        Pattern p = Pattern.compile("[0-9]*");
        Matcher m = p.matcher(data);

        boolean result = m.matches();

        return result;
    }

    public void msgPlayer(Player player, String msg) {

        player.sendMessage(ChatColor.DARK_AQUA + "[Custom Motd] " + ChatColor.AQUA + msg);

    }

    public static String getIP(Player player) {

        return player.getAddress().toString().substring(1).split(":")[0];

    }


    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        String add = getIP(event.getPlayer());

        add = add.replaceAll("\\.", "-");

        this.playersFile.set(add, event.getPlayer().getName());

        this.savePlayerYML();
    }

    static String replaceColors(String message) {

        return message.replaceAll("(?i)&([a-f0-9])", "§$1");

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void serverPing(ServerListPingEvent event) {

        String add = event.getAddress().toString();

        add = add.replaceAll("\\.", "-").substring(1);

        if (this.playersFile.isSet(add)) {

            Random obj = new Random();

            for (int counter = 1; counter <= 1; counter++) {

                intNum = 1 + obj.nextInt(messages.size());

                event.setMotd(replaceColors(messages.get(intNum - 1).replaceAll("<player>", this.playersFile.getString(add))));

            }

        }

    }

    public void savePlayerYML() {

        try {
            playersFile.save(configFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void loadPlayerYML() {

        pluginFolder = getDataFolder();
        configFile = new File(getDataFolder(), "players.yml");

        playersFile = new YamlConfiguration();

        if (getDataFolder().exists() == false) {

            try {
                getDataFolder().mkdir();
            } catch (Exception ex) {
                //something went wrong.
            }

        } //plugin folder exists


        if (configFile.exists() == false) {

            try {
                configFile.createNewFile();
            } catch (Exception ex) {
                //something went wrong.
            }
        } //Configfile exist's

        try { //Load payers.yml
            playersFile.load(configFile);
        } catch (Exception ex) {
            //Something went wrong
        } //End load players.yml
    }

}
