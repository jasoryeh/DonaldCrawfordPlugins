package com.gmail.dejayyy;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class mpMute implements CommandExecutor {
	
	public cmdExec plugin;

	public mpMute(cmdExec plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdL, String[] args) {
		
		if(!(sender instanceof Player)){
			
			if(cmdL.equalsIgnoreCase("mute")){
				
					if(args.length == 0){
						
						sender.sendMessage(ChatColor.DARK_AQUA + "[Mute+] " + ChatColor.AQUA + "Invalid usage. You must choose a player to mute.");
						
						return true;
						
					}

					if(Bukkit.getServer().getPlayer(args[0]) == null){
						
						sender.sendMessage(ChatColor.DARK_AQUA + "[Mute+] " + ChatColor.AQUA + "Player not found!");
						
						return true;
						
					}
					
					if(args.length == 1){
						
						Player targetPlayer = Bukkit.getServer().getPlayer(args[0]);
						
									
							plugin.playersFile.set("Players." + targetPlayer.getName().toLowerCase() + ".MutedBy", "Console");
							plugin.playersFile.set("Players." + targetPlayer.getName().toLowerCase() + ".Reason", "No reason given.");
							
							targetPlayer.sendMessage(ChatColor.DARK_AQUA + "[Mute+] " + ChatColor.AQUA + "You've been muted!");
							
							sender.sendMessage(ChatColor.DARK_AQUA + "[Mute+] " + ChatColor.AQUA + "You have successfully muted " + targetPlayer.getName().toLowerCase());
							
							plugin.savePlayerYML();
							
							return true;
						
					}else if(args.length >= 2){
						
						String muteMSG;
						
						StringBuilder builder = new StringBuilder();
						
						for(int i = 1; i < args.length; i++) {
							
						    builder.append(args[i]).append(" ");
						    
						}
						
						builder.setLength(builder.length() - 1);
						muteMSG = builder.toString();
						
						Player targetPlayer = Bukkit.getServer().getPlayer(args[0]);
						
						plugin.playersFile.set("Players." + targetPlayer.getName().toLowerCase() + ".MutedBy", "Console");
						plugin.playersFile.set("Players." + targetPlayer.getName().toLowerCase() + ".Reason", muteMSG);
						
						sender.sendMessage(ChatColor.DARK_AQUA + "[Mute+] " + ChatColor.AQUA + "You have successfully muted " + targetPlayer.getName().toLowerCase());
						
						plugin.savePlayerYML();
						
						return true;
					
					}else{
						
						sender.sendMessage(ChatColor.DARK_AQUA + "[Mute+] " + ChatColor.AQUA + "Invalid Arguments!");
						
						return true;
						
					}
				
			} //End check cmdL
			
			return true;
			
		}
		
		
		Player player = (Player) sender;
		
		if(cmdL.equalsIgnoreCase("mute")){
			
			if(player.hasPermission("mutePlus.use")){
			
				if(args.length == 0){
					
					player.sendMessage(ChatColor.DARK_AQUA + "[Mute+] " + ChatColor.AQUA + "Invalid usage. You must choose a player to mute.");
					
					return true;
					
				}

				if(player.getServer().getPlayer(args[0]) == null){
					
					player.sendMessage(ChatColor.DARK_AQUA + "[Mute+] " + ChatColor.AQUA + "Player not found!");
					
					return true;
					
				}
				
				if(args.length == 1){
					
					Player targetPlayer = player.getServer().getPlayer(args[0]);
					
								
						plugin.playersFile.set("Players." + targetPlayer.getName().toLowerCase() + ".MutedBy", player.getName());
						plugin.playersFile.set("Players." + targetPlayer.getName().toLowerCase() + ".Reason", "No reason given.");
						
						targetPlayer.sendMessage(ChatColor.DARK_AQUA + "[Mute+] " + ChatColor.AQUA + "You've been muted!");
						
						player.sendMessage(ChatColor.DARK_AQUA + "[Mute+] " + ChatColor.AQUA + "You have successfully muted " + targetPlayer.getName().toLowerCase());
						
						plugin.savePlayerYML();
						
						return true;
					
				}else if(args.length >= 2){
					
					String muteMSG;
					
					StringBuilder builder = new StringBuilder();
					
					for(int i = 1; i < args.length; i++) {
						
					    builder.append(args[i]).append(" ");
					    
					}
					
					builder.setLength(builder.length() - 1);
					muteMSG = builder.toString();
					
					Player targetPlayer = player.getServer().getPlayer(args[0]);
					
					plugin.playersFile.set("Players." + targetPlayer.getName().toLowerCase() + ".MutedBy", player.getName());
					plugin.playersFile.set("Players." + targetPlayer.getName().toLowerCase() + ".Reason", muteMSG);
					
					player.sendMessage(ChatColor.DARK_AQUA + "[Mute+] " + ChatColor.AQUA + "You have successfully muted " + targetPlayer.getName().toLowerCase());
					
					plugin.savePlayerYML();
					
					return true;
				
				}else{
					
					player.sendMessage(ChatColor.DARK_AQUA + "[Mute+] " + ChatColor.AQUA + "Invalid Arguments!");
					
					return true;
					
				}
			
			}else{
				
				player.sendMessage(ChatColor.DARK_AQUA + "[Mute+] " + ChatColor.AQUA + "You dont have permission to run this command!");
				
			} //End check perms
			
		} //End check cmdL
		
		return true;
	}


	

}
