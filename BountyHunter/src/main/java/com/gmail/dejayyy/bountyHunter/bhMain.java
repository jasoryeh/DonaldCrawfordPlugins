package com.gmail.dejayyy.bountyHunter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class bhMain extends JavaPlugin implements Listener {

    public static Economy econ = null;
    public File pluginFolder;
    public File configFile;
    public FileConfiguration playersFile;
    public List<String> myTop5 = new ArrayList<String>();

    public void onEnable() {

        this.getServer().getPluginManager().registerEvents(this, this);
        this.loadPlayerYML();
        this.saveDefaultConfig();

        this.playersFile.set("Players.blah", null);


    }

    public void onDisable() {


    }

    public boolean onCommand(CommandSender sender, Command cmd, String cmdL, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("You goofball, you cant run that command from console!");
        }

        Economy econ = VaultAdapter.getEconomy();
        Player player = (Player) sender;

        if (cmdL.equalsIgnoreCase("bounty") || cmdL.equalsIgnoreCase("bh")) {

            if (args.length == 0 || args.length > 3) {

                this.pluginHelp(player);

                return true;

            } //invalid args

            if (args.length == 1) {

                if (args[0].equalsIgnoreCase("list")) {

                    if (this.playersFile.getConfigurationSection("Players").getKeys(false).isEmpty()) {

                        this.msgPlayer(player, "No players to display.");

                        return true;

                    }

                    player.sendMessage(ChatColor.AQUA + "=-= Bounty List =-=");

                    this.bountyList(player, 1);

                    return true;

                } else if (args[0].equalsIgnoreCase("top")) {

                    if (this.playersFile.getConfigurationSection("Players").getKeys(false).isEmpty()) {

                        this.msgPlayer(player, "No players to display.");

                        return true;

                    }

                    player.sendMessage(ChatColor.AQUA + "=-= Most Wanted =-=");

                    this.getTop(player);

                } else {

                    this.pluginHelp(player);

                    return true;

                } //Bounty list

            } //args.length == 1

            if (args.length == 2) {

                if (args[0].equalsIgnoreCase("list")) {

                    if (isInt(args[1])) {

                        if (this.playersFile.getConfigurationSection("Players").getKeys(false).isEmpty()) {

                            this.msgPlayer(player, "No players to display.");

                            return true;

                        }

                        int page = Integer.parseInt(args[1]);

                        player.sendMessage(ChatColor.AQUA + "=-= Bounty List =-=");

                        this.bountyList(player, page);

                    } else {

                        this.pluginHelp(player);

                    }


                    return true;

                } else if (args[0].equalsIgnoreCase("remove")) {

                    if (!(player.hasPermission("bountyHunter.Admin"))) {

                        player.sendMessage(ChatColor.DARK_AQUA + "[Bounty Hunter] " + ChatColor.AQUA + "You dont have permission to execute that command!");

                        return true;

                    }

                    String tPlayer = args[1].toString();

                    if (this.playersFile.getString("Players." + tPlayer) != null) {

                        this.playersFile.set("Players." + tPlayer, null);

                    } else {

                        this.msgPlayer(player, "Player not found!");

                    }

                } else {

                    this.pluginHelp(player);

                    return true;

                } //Bounty list

            } else if (args.length == 3) {

                if (args[0].equalsIgnoreCase("add")) {

                    Player targetPlayer = this.getServer().getPlayer(args[1]);

                    if (targetPlayer != null) {

                        if (isInt(args[2])) {

                            long intCost = Long.parseLong(args[2]);

                            EconomyResponse er = econ.withdrawPlayer(player.getName(), intCost);

                            if (er.transactionSuccess()) {

                                if (this.playersFile.contains("Players." + targetPlayer.getName())) {

                                    long intAmount = this.playersFile.getLong("Players." + targetPlayer.getName());
                                    long intTotal = (intCost + intAmount);

                                    this.playersFile.set("Players." + targetPlayer.getName(), intTotal);

                                    this.savePlayerYML();

                                    this.msgPlayer(player, "$" + intCost + " was added to the reward for the death of " + targetPlayer.getName());

                                    return true;

                                } else {

                                    this.playersFile.set("Players." + targetPlayer.getName(), intCost);

                                    this.savePlayerYML();

                                    this.msgPlayer(player, targetPlayer.getName() + " was successfully added to the bounty list!");

                                    return true;

                                }

                            } else {

                                this.msgPlayer(player, "You don't have enough money to place that bounty!");

                            } //transaction success

                        } else {

                            this.msgPlayer(player, "You must input a valid money value. Example: 500");

                            return true;

                        }

                    } else {

                        this.msgPlayer(player, "Player not found!");

                        return true;

                    } //targetplayer online

                } //add

            } //args.length == 3

        } //cmdl.equalsignorecase

        return true;
    }

    private static boolean isInt(String data) {

        Pattern p = Pattern.compile("[0-9]*");
        Matcher m = p.matcher(data);
        boolean result = m.matches();

        return result;
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

        }


        if (configFile.exists() == false) {

            try {
                configFile.createNewFile();
            } catch (Exception ex) {
                //something went wrong.
            }
        }

        try { //Load payers.yml
            playersFile.load(configFile);
        } catch (Exception ex) {
            //Something went wrong
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        Economy econ = VaultAdapter.getEconomy();

        Player player = event.getEntity();
        Player killer = player.getKiller();

        if (player instanceof Player && killer instanceof Player) {

            if (this.playersFile.contains("Players." + player.getName())) {

                double lngMoney = this.playersFile.getDouble("Players." + player.getName());
                long blah = (long) lngMoney;

                econ.depositPlayer(killer.getName(), lngMoney);

                this.msgPlayer(killer, "Congratulations! There was a reward for the death of " + player.getName() + ". You've been rewarded $" + blah);

                this.playersFile.set("Players." + player.getName(), null);

                this.savePlayerYML();

                return;

            }  //playersfile.contains

        } //instanceof Player

    } // onDeath

    public void msgPlayer(Player player, String message) {

        player.sendMessage(ChatColor.DARK_AQUA + "[BountyHunter] " + ChatColor.AQUA + message);

    }


    public void getTop(Player player) {

        Map<String, Integer> scoreMap = new HashMap<String, Integer>();
        List<String> finalScore = new ArrayList<String>();

        ConfigurationSection score = this.playersFile.getConfigurationSection("Players");

        for (String playerName : score.getKeys(false)) {

            int money = score.getInt(playerName);

            scoreMap.put(playerName, money);


        }

        for (int i = 0; i < 5; i++) {

            String topName = "";
            int topScore = 0;

            for (String playerName : scoreMap.keySet()) {

                int myScore = scoreMap.get(playerName);

                if (myScore > topScore) {

                    topName = playerName;
                    topScore = myScore;

                }

            }

            if (!topName.equals("")) {

                scoreMap.remove(topName);

                int moola = score.getInt(topName);

                String finalString = ChatColor.DARK_AQUA + topName + ChatColor.DARK_AQUA + ": " + ChatColor.AQUA + "$" + moola;

                finalScore.add(finalString);

            } else

                break;

        }

        myTop5 = finalScore;

        for (String blah : myTop5) {
            player.sendMessage(blah);
        }

    }


    public void pluginHelp(Player player) {

        player.sendMessage(ChatColor.DARK_AQUA + "/bounty add [PlayerName] [Reward] " + ChatColor.AQUA + "Add a player to the bounty list with a cash reward");
        player.sendMessage(ChatColor.DARK_AQUA + "/bounty list [Page] " + ChatColor.AQUA + "View the players on the bounty list.");
        player.sendMessage(ChatColor.DARK_AQUA + "/bounty top " + ChatColor.AQUA + "View the player on the bounty list with the highest reward.");

        if (player.hasPermission("bountyHunter.admin")) {

            player.sendMessage(ChatColor.DARK_AQUA + "/bounty remove [PlayerName] " + ChatColor.AQUA + "Remove a player from the bounty list.");

        }
    }


    static String replaceColors(String message) {
        return message.replaceAll("(?i)&([a-f0-9])", "�$1");
    }

    public void bountyList(Player player, int page) {

        ConfigurationSection users = this.playersFile.getConfigurationSection("Players");

        String[] players = users.getKeys(false).toArray(new String[0]);

        int start = (page - 1) * 5;
        int end = (page * 5 - 1);
        int x;

        double blah = players.length;
        double pages = (blah / 5);
        double a = Math.ceil(pages);
        long lngPage = (long) a;


        if (page > lngPage) {

            this.msgPlayer(player, "Invalid page number!");

            return;

        }

        for (int i = start; i <= end; i++) {

            try {

                x = users.getInt(players[i]);
                player.sendMessage(ChatColor.DARK_AQUA + players[i] + ": " + ChatColor.AQUA + "$" + x);

            } catch (Exception ex) {

                //oops

            } //end try

        }

        if (lngPage > page) {

            player.sendMessage(ChatColor.DARK_AQUA + "Type " + ChatColor.AQUA + "/bh list " + (page + 1) + ChatColor.DARK_AQUA + " to view more.");

        }
    }

}
