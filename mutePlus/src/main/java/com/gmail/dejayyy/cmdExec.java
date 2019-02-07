package com.gmail.dejayyy;


import java.io.File;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;


	public class cmdExec extends JavaPlugin implements Listener{
		
		public cmdExec plugin;
		
		public File pluginFolder;
		public File configFile;
		public FileConfiguration playersFile;
		
		
			public void onEnable(){
				
				getCommand("mute").setExecutor(new mpMute(this));
				getCommand("unmute").setExecutor(new mpUnmute(this));
				getCommand("muteinfo").setExecutor(new mpInfo(this));
				
				
		        getServer().getPluginManager().registerEvents(this, this);
		        
		        
		        this.saveDefaultConfig();
		        
				this.loadPlayerYML();
				
			}
			
			@EventHandler
			public void onPlayerChat(AsyncPlayerChatEvent event) {
				
		        Player p = event.getPlayer();
		        
		        boolean alertPlayer = this.getConfig().getBoolean("AlertPlayer");
		        String alertMSG = this.getConfig().getString("AlertMSG");
		        
		            if(playersFile.contains("Players." + p.getName()) == true) {
		                
		            	if(alertPlayer){
		            		
		            		p.sendMessage(ChatColor.DARK_AQUA + "[Mute+] " + replaceColors(alertMSG));
		            		
		            	}
		            	
		                event.setCancelled(true);
		            }
		    }
			
			static String replaceColors(String message) {
				
			    return ChatColor.translateAlternateColorCodes('&', message);
			    
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
					
				} //plugin folder exists
			
			
				if(configFile.exists() == false){
					
					try{
						configFile.createNewFile();
					}catch (Exception ex){
						//something went wrong.
					}
				} //Configfile exist's
				
				try{ //Load payers.yml
					playersFile.load(configFile);
				}catch (Exception ex){
					//Something went wrong
				} //End load players.yml
			}
			
			public void savePlayerYML(){
				
				try {
					playersFile.save(configFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}

			
	}
