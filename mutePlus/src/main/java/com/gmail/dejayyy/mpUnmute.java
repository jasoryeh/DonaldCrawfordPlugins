package com.gmail.dejayyy;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class mpUnmute implements CommandExecutor {
	public cmdExec plugin;
	

	public mpUnmute(cmdExec plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdL, String[] args) {

		Player player = (Player) sender;
		
		if(player.hasPermission("mutePlus.use")){
			
			if(cmdL.equalsIgnoreCase("unmute")){
				
				if(player.getServer().getPlayer(args[0]) == null){
					
					String p = args[0];
					
					if(plugin.playersFile.isSet("Players." + p.toLowerCase() + ".MutedBy") == false){
						
						player.sendMessage(ChatColor.DARK_AQUA + "[Mute+] " + ChatColor.AQUA + "That player is not muted!");
						
						return true;
						
					}else{
						
						plugin.playersFile.set("Players." + p.toLowerCase(), null);
						
						player.sendMessage(ChatColor.DARK_AQUA + "[Mute+] " + ChatColor.AQUA + "You have successfully unmuted " + p.toLowerCase());
						
						plugin.savePlayerYML();
						
						return true;
					
					} //End checking if muted/unmuting.
					
				} //End checking null player.
				
				Player targetPlayer = player.getServer().getPlayer(args[0]);
				
				if(args.length == 0 || args.length >= 2){
					
					player.sendMessage(ChatColor.DARK_AQUA + "[Mute+] " + ChatColor.AQUA + "Invalid arguments.");
					
					return true;
				} //End args == 0
				
				if(plugin.playersFile.isSet("Players." + targetPlayer.getName().toLowerCase() + ".MutedBy") == false){
					
					player.sendMessage(ChatColor.DARK_AQUA + "[Mute+] " + ChatColor.AQUA + "That player is not muted!");
					
					return true;
					
				}else{
					
					plugin.playersFile.set("Players." + targetPlayer.getName().toLowerCase(), null);
					
					player.sendMessage(ChatColor.DARK_AQUA + "[Mute+] " + ChatColor.AQUA + "You have successfully unmuted " + targetPlayer.getName().toLowerCase());
					targetPlayer.sendMessage(ChatColor.DARK_AQUA + "[Mute+] " + ChatColor.AQUA + "You've been unmuted!");
					
					plugin.savePlayerYML();
					
					return true;
				
				} //End checking if muted/unmuting.
				
			} //End check cmdL
		} //End perm check
		
		
		return true;
	}

}
