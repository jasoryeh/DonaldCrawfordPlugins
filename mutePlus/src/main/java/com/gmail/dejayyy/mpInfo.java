package com.gmail.dejayyy;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class mpInfo implements CommandExecutor {

    public cmdExec plugin;

    public mpInfo(cmdExec plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cmdL, String[] args) {

        if (!(sender instanceof Player)) {

            sender.sendMessage("You goofball, you cant run that command from console!");

            return true;

        } //end check consoel.

        if (cmdL.equalsIgnoreCase("muteinfo")) {
            Player player = (Player) sender;

            if (player.hasPermission("mutePlus.use") == false) {

                player.sendMessage(ChatColor.DARK_AQUA + "[Mute+] " + ChatColor.AQUA + "You dont have permission to run this command!");

                return true;

            } //End perm check

            if (args.length > 1 || args.length < 1) {

                player.sendMessage(ChatColor.DARK_AQUA + "[Mute+] " + ChatColor.AQUA + "Invalid command usage!");

                return true;

            } //end args.length


            Player targetPlayer = player.getServer().getPlayer(args[0]);

            if (!(targetPlayer == null)) {

                if (plugin.playersFile.isSet("Players." + targetPlayer.getName().toLowerCase() + ".MutedBy")) {

                    String strMutedBy = plugin.playersFile.getString("Players." + targetPlayer.getName().toLowerCase() + ".MutedBy");
                    String strReason = plugin.playersFile.getString("Players." + targetPlayer.getName().toLowerCase() + ".Reason");

                    player.sendMessage(ChatColor.DARK_AQUA + "=-=-= " + ChatColor.AQUA + "Mute+" + ChatColor.DARK_AQUA + " =-=-=");
                    player.sendMessage(ChatColor.DARK_AQUA + "Player Name: " + ChatColor.AQUA + targetPlayer.getName().toLowerCase());
                    player.sendMessage(ChatColor.DARK_AQUA + "Muted By: " + ChatColor.AQUA + strMutedBy);
                    player.sendMessage(ChatColor.DARK_AQUA + "Reason: " + ChatColor.AQUA + strReason);
                    player.sendMessage(ChatColor.DARK_AQUA + "=-=-= " + ChatColor.AQUA + "Information" + ChatColor.DARK_AQUA + " =-=-=");

                    return true;

                } else {

                    player.sendMessage(ChatColor.DARK_AQUA + "[Mute+] " + ChatColor.AQUA + "That player is not muted!");

                    return true;

                }


            } else {

                String p = args[0];

                if (plugin.playersFile.isSet("Players." + p.toLowerCase() + ".MutedBy")) {

                    String strMutedBy = plugin.playersFile.getString("Players." + p.toLowerCase() + ".MutedBy");
                    String strReason = plugin.playersFile.getString("Players." + p.toLowerCase() + ".Reason");

                    player.sendMessage(ChatColor.DARK_AQUA + "=-=-= " + ChatColor.AQUA + "Mute+" + ChatColor.DARK_AQUA + " =-=-=");
                    player.sendMessage(ChatColor.DARK_AQUA + "Player Name: " + ChatColor.AQUA + p.toLowerCase());
                    player.sendMessage(ChatColor.DARK_AQUA + "Muted By: " + ChatColor.AQUA + strMutedBy);
                    player.sendMessage(ChatColor.DARK_AQUA + "Reason: " + ChatColor.AQUA + strReason);
                    player.sendMessage(ChatColor.DARK_AQUA + "=-=-= " + ChatColor.AQUA + "Information" + ChatColor.DARK_AQUA + " =-=-=");

                    return true;

                } else {

                    player.sendMessage(ChatColor.DARK_AQUA + "[Mute+] " + ChatColor.AQUA + "That player is not muted!");

                    return true;

                }
            }


        } //End muteinfo args

        return true;
    }

}
