package dejay;

import java.io.File;
import java.io.IOException;

import java.util.Random;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class rollDice extends JavaPlugin implements Listener {

    public static File pluginFolder;
    public static File configFile;
    public static FileConfiguration playersFile;


    public static Economy econ = null;

    public final Logger log = Logger.getLogger("Minecraft");

    @Override
    public void onEnable() {

        getServer().getPluginManager().registerEvents(this, this);

        this.loadPlayerYML();

        this.saveDefaultConfig();

    } //End onEnable

    @Override
    public void onDisable() {

        this.savePlayerYML();

    } //End ondisable


    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("You goofball, you cant use this command in console!");
            return true;
        }

        Player player = (Player) sender;

        Random obj = new Random();

        PluginDescriptionFile pdf = this.getDescription();

        int intNum;
        int intSum;
        int intPlayed = playersFile.getInt("Players." + player.getName() + ".Played");
        int intGained = playersFile.getInt("Players." + player.getName() + ".Gained");
        int intSpent = playersFile.getInt("Players." + player.getName() + ".Spent");
        int intWinningNumber = getConfig().getInt("WinningNumber");
        int intMaxNumber = this.getConfig().getInt("MaxNumber");
        String strWinnerMSG = replaceColors(this.getConfig().getString("WinnerMSG"));
        int intWinAmount = this.getConfig().getInt("WinAmount");
        String strBroadcastMSG = replaceColors(this.getConfig().getString("BroadcastMSG"));
        String strLoserMSG = replaceColors(this.getConfig().getString("LoserMSG"));
        String strNoMoney = replaceColors(this.getConfig().getString("noMoney"));
        int intCost = this.getConfig().getInt("rtdCost");

        Economy econ = VaultAdapter.getEconomy();

        if (commandLabel.equalsIgnoreCase("rollthedice") || commandLabel.equalsIgnoreCase("rtd")) {


            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {

                    if (player.hasPermission("rollthedice.reload")) {

                        this.reloadConfig();
                        this.loadPlayerYML();

                        player.sendMessage(ChatColor.DARK_AQUA + "[RTD] " + ChatColor.AQUA + "Reload Complete.");

                        return true;
                    } else {
                        player.sendMessage(ChatColor.DARK_AQUA + "[RTD] " + ChatColor.AQUA + "You dont have permission to run this command!");
                        return true;
                    }

                } else if (args[0].equalsIgnoreCase("info")) {

                    player.sendMessage(ChatColor.DARK_AQUA + "Plugin Name: " + ChatColor.AQUA + pdf.getName());
                    player.sendMessage(ChatColor.DARK_AQUA + "Version: " + ChatColor.AQUA + pdf.getVersion());
                    player.sendMessage(ChatColor.DARK_AQUA + "Author: " + ChatColor.AQUA + "ImDeJay");
                    player.sendMessage(ChatColor.DARK_AQUA + "Description: " + ChatColor.AQUA + pdf.getDescription());

                    return true;

                } else if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
                    player.sendMessage(ChatColor.DARK_AQUA + "Commands:");
                    player.sendMessage(ChatColor.DARK_AQUA + "/rtd - " + ChatColor.AQUA + "The command used to play.");
                    player.sendMessage(ChatColor.DARK_AQUA + "/rtd profile - " + ChatColor.AQUA + "Show your RTD statistics.");
                    player.sendMessage(ChatColor.DARK_AQUA + "/rtd profile [playername] - " + ChatColor.AQUA + "Show another players RTD statistics.");
                    player.sendMessage(ChatColor.DARK_AQUA + "Cost to play: " + ChatColor.AQUA + intCost);
                    player.sendMessage(ChatColor.DARK_AQUA + "Amount you win: " + ChatColor.AQUA + intWinAmount);
                    player.sendMessage(ChatColor.AQUA + "^ If odds are in your favour!");

                    return true;
                } else if (args[0].equalsIgnoreCase("profile")) {

                    player.sendMessage(ChatColor.DARK_AQUA + "=-=-= " + ChatColor.AQUA + player.getName() + ChatColor.DARK_AQUA + " =-=-=");
                    player.sendMessage(ChatColor.DARK_AQUA + "Times Played: " + ChatColor.AQUA + intPlayed);
                    player.sendMessage(ChatColor.DARK_AQUA + "Money Gained: " + ChatColor.AQUA + "$" + intGained);
                    player.sendMessage(ChatColor.DARK_AQUA + "Money Spent: " + ChatColor.AQUA + "$" + intSpent);
                    player.sendMessage(ChatColor.DARK_AQUA + "=-=-= " + ChatColor.AQUA + "RTD Stats" + ChatColor.DARK_AQUA + " =-=-=");

                    return true;
                } else {

                    player.sendMessage(ChatColor.DARK_AQUA + "[RTD] " + ChatColor.AQUA + "Invalid Command Usage!");

                    return true;
                } //End check for args

            } else if (args.length == 2) {

                if (args[0].equalsIgnoreCase("profile") == false) {

                    player.sendMessage(ChatColor.DARK_AQUA + "[RTD] " + ChatColor.AQUA + "Invalid command usage!");

                    return true;

                } else {
                    Player targetPlayer = player.getServer().getPlayer(args[1]);

                    if (player.getServer().getPlayer(args[1]) == null) {

                        player.sendMessage(ChatColor.DARK_AQUA + "[RTD] " + ChatColor.AQUA + "Player not found!");

                        return true;

                    }

                    int targetPlayed = playersFile.getInt("Players." + targetPlayer.getName() + ".Played");
                    int targetGained = playersFile.getInt("Players." + targetPlayer.getName() + ".Gained");
                    int targetSpent = playersFile.getInt("Players." + targetPlayer.getName() + ".Spent");

                    if (playersFile.isSet("Players." + targetPlayer.getName() + ".Played") == false) {

                        player.sendMessage(ChatColor.DARK_AQUA + "[RTD]" + ChatColor.AQUA + "Player not found!");
                        return true;

                    } else {

                        player.sendMessage(ChatColor.DARK_AQUA + "=-=-= " + ChatColor.AQUA + targetPlayer.getName() + ChatColor.DARK_AQUA + " =-=-=");
                        player.sendMessage(ChatColor.DARK_AQUA + "Times Played: " + ChatColor.AQUA + targetPlayed);
                        player.sendMessage(ChatColor.DARK_AQUA + "Money Gained: " + ChatColor.AQUA + "$" + targetGained);
                        player.sendMessage(ChatColor.DARK_AQUA + "Money Spent: " + ChatColor.AQUA + "$" + targetSpent);
                        player.sendMessage(ChatColor.DARK_AQUA + "=-=-= " + ChatColor.AQUA + "RTD Stats" + ChatColor.DARK_AQUA + " =-=-=");

                        return true;

                    }
                }

            } //End args length

            EconomyResponse r = econ.withdrawPlayer(player.getName(), intCost);

            if (r.transactionSuccess() == false) { //Money check

                if (strNoMoney.equalsIgnoreCase("disable")) {
                    return true;
                }

                player.sendMessage(strNoMoney);

                return true;
            } else {

                intSum = intCost + intSpent;


                playersFile.set("Players." + player.getName() + ".Spent", intSum);

                player.sendMessage(ChatColor.GRAY + "-$" + intCost);

                if (!(sender instanceof Player)) {
                    sender.sendMessage("You goofball, you cant use this command in console!");
                    return true;
                }

            }

            if (!(player.hasPermission("rollthedice.use"))) {
                player.sendMessage(ChatColor.DARK_AQUA + "[RTD]" + ChatColor.AQUA + "You don't have permission to play!");
                return true;
            }

            intSum = intPlayed + 1;

            playersFile.set("Players." + player.getName() + ".Played", intSum);

            for (int counter = 1; counter <= 1; counter++) {

                intNum = 1 + obj.nextInt(intMaxNumber);

                if (intNum == intWinningNumber) { //Check winning number

                    if (!(strWinnerMSG.equalsIgnoreCase("disable"))) {

                        player.sendMessage(strWinnerMSG);

                        econ.depositPlayer(player.getName(), intWinAmount);

                        player.sendMessage(ChatColor.GRAY + "+$" + intWinAmount);

                        intSum = intWinAmount + intGained;

                        playersFile.set("Players." + player.getName() + ".Gained", intSum);

                        this.savePlayerYML();

                    } //End check for null winnerMSG

                    if (!(strBroadcastMSG.equalsIgnoreCase("disable"))) {

                        player.getServer().broadcastMessage(strBroadcastMSG.replaceAll("<player>", player.getName()));

                    } //End check for null broadcast

                } else {

                    if (!(strLoserMSG.equalsIgnoreCase("disable"))) {
                        player.sendMessage(strLoserMSG);
                        this.savePlayerYML();
                    } //End check for null

                } //end check winning number

            } //End for loop

        }  //End check command


        return true;
    } //End onCommand


    static String replaceColors(String message) {
        return message.replaceAll("(?i)&([a-f0-9])", "§$1");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!playersFile.isSet("Players." + player.getName() + ".Played")) {

            playersFile.set("Players." + player.getName() + ".Played", 0);
            playersFile.set("Players." + player.getName() + ".Gained", 0);
            playersFile.set("Players." + player.getName() + ".Spent", 0);

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
} //End class
