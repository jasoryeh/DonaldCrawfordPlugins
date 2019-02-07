package com.gmail.dejayyy.customChat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;


public class ccMain extends JavaPlugin implements Listener {
	
	public File pluginFolder;
	public File configFile;
	public FileConfiguration playersFile;
	
	int taskID = 0;
		
	public List<String> AlreadyChanged = new ArrayList<String>();
	public HashSet<String> changingColor = new HashSet<String>();
	
	
	public void onEnable(){
		
		this.loadPlayerYML();
		
		this.getServer().getPluginManager().registerEvents(this,  this);
		
	}
	
	public void onDisable(){
		
		
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String cmdl, String[] args) {
		
		if(!(sender instanceof Player)){
			
			return false;
			
		}
		
		final Player player = (Player) sender;
		
		if(cmdl.equalsIgnoreCase("chatcolor")){
			
			if(args.length == 0){
				
					if(!(player.hasPermission("customChat.canChange"))){
						
						player.sendMessage(ChatColor.RED + "I'm sorry, but your not allowed to change your chat color!");
						
						return true;
						
					}
					
					if(canChangeColor(player) || player.hasPermission("customChat.unlmited")){
						
						changingColor.add(player.getName());
						
			            
			            taskID = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
			            
			            {
			                @Override
			                public void run() {
		
			                    changingColor.remove(player.getName());
			                    
			                    player.sendMessage(ChatColor.AQUA + "Sorry, you took to long to choose your chat color, your request was cancelled.");
			                    	                    
			                }
			                
			            }, 30 * 20);
			            
						player.sendMessage(ChatColor.AQUA + "Please choose a color from the list below. Only input the color code, anything else will be ignored.");
						
						player.sendMessage(ChatColor.BLACK + "0 " + ChatColor.DARK_BLUE + "1 " + ChatColor.DARK_GREEN + "2 " + ChatColor.DARK_AQUA + "3 " + ChatColor.DARK_RED + "4 " + 
						ChatColor.DARK_PURPLE + "5 " + ChatColor.GOLD + "6 " + ChatColor.GRAY + "7 " + ChatColor.DARK_GRAY + "8 " + ChatColor.BLUE + "9 " + 
								ChatColor.GREEN + "a " + ChatColor.AQUA + "b " + ChatColor.RED + "c " + ChatColor.LIGHT_PURPLE + "d " + ChatColor.YELLOW + "e " + ChatColor.WHITE + "f");
						
					}else{
						
						player.sendMessage(ChatColor.AQUA + "I'm sorry, but you cannot change your chat color at this time!");
						
						return true;
						
					}
					
				}else if(args.length == 2){
					
					if(args[0].equalsIgnoreCase("reset")){
						
						
						if(!(player.hasPermission("customChat.admin"))){
							
							player.sendMessage(ChatColor.RED + "You don't have permission to execute that command!");
							
							return true;
							
						}
						
						Player target = this.getServer().getPlayer(args[1]);
						
						if(target == null){
							
							player.sendMessage(ChatColor.AQUA + "Player not found!");
							
							return true;
							
						}else{
							
							AlreadyChanged.remove(target.getName());
							
							player.sendMessage(ChatColor.AQUA + target.getName() + " will now be able to change their chat color!");
							
							return true;
						}
						
					}
					
				}else{
					
					player.sendMessage(ChatColor.RED + "Invalid command usage!");
					
					return true;
					
				}
			
			}
			
			
		
		return true;
		
	}
	
	public boolean canChangeColor(Player player){
		
		return !AlreadyChanged.contains(player.getName());
		
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void playerChat(AsyncPlayerChatEvent event){
		
		Player player = event.getPlayer();
		
		if(!(changingColor.contains(player.getName()))){
			
			if(this.playersFile.isSet(player.getName())){
				
				String chatColor = this.playersFile.getString(player.getName());
				
				event.setMessage(replaceColors(chatColor + event.getMessage()));
			}
				
			
		}else{
			
			event.setCancelled(true);
			
			if(event.getMessage().length() != 1){
				
				if(event.getMessage().equalsIgnoreCase("cancel")){
					
					changingColor.remove(player.getName());
					
					this.getServer().getScheduler().cancelTask(taskID);
					
					player.sendMessage(ChatColor.AQUA + "You are no longer changing your chat color!");
					
					return;
					
				}
				
				player.sendMessage(ChatColor.AQUA + "Please choose from one of the colors above, only input the color code.");
				player.sendMessage(ChatColor.AQUA + "If you'd like to cancel this action, type 'cancel'");
				
			}else if(validColor(event.getMessage())){
				
				this.playersFile.set(player.getName(), "&" + event.getMessage());
				
				this.savePlayerYML();
				
				AlreadyChanged.add(player.getName());
				
				String chatColor = this.playersFile.getString(player.getName());
						
				player.sendMessage(replaceColors(chatColor + "Your chat color has been set!"));
				
				changingColor.remove(player.getName());
				
				this.getServer().getScheduler().cancelTask(taskID);
				
								
			}else{
				
				player.sendMessage(ChatColor.AQUA + "Please choose from one of the colors above, only input the color code.");
				player.sendMessage(ChatColor.AQUA + "If you'd like to cancel this action, type 'cancel'");
				
			}
			
		}
		
		
	}
	
	
	public boolean validColor(String colorCode){
		
		return this.getConfig().getStringList("ColorCodes").contains(colorCode);
				
	}
	
	static String replaceColors(String message) {
		
		return ChatColor.translateAlternateColorCodes('&', message);
		
	  }
	
	
	public void savePlayerYML(){
		
		try {
			playersFile.save(configFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void loadPlayerYML(){
		pluginFolder = getDataFolder();
	    configFile = new File(getDataFolder(), "players.yml");
	    
	    playersFile = new YamlConfiguration();
	    
		if(getDataFolder().exists() == false){
			
			try{
				getDataFolder().mkdir();
			}catch (Exception ex){
				//something went wrong.
			}
			
		}
	
	
		if(configFile.exists() == false){
			
			try{
				configFile.createNewFile();
			}catch (Exception ex){
				//something went wrong.
			}
		} 
		
		try{ //Load payers.yml
			playersFile.load(configFile);
		}catch (Exception ex){
			//Something went wrong
		}
	}
	
}
