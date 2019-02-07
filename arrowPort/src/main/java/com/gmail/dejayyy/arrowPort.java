package com.gmail.dejayyy;

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
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class arrowPort extends JavaPlugin implements Listener{
	
	public arrowPort plugin;
	
	public final HashSet<String> set = new HashSet<String>();
	
	public List<String> arrowFired = new ArrayList<String>();
	
	public final ArrayList <String> victim = new ArrayList <String> ();
	
	public File configFile;
	public File pluginFolder;
	public FileConfiguration playersFile;
	public boolean ctPlugin;

	
	public void onEnable(){

		getServer().getPluginManager().registerEvents(this,  this);
		
		if(this.getServer().getPluginManager().getPlugin("combatTracker") != null){
			
			ctPlugin = true;

		}else{
			
			ctPlugin = false;
			
		}
		
		this.loadPlayerYML();
		this.saveDefaultConfig();
		
		
	}
	
	public void onDisable(){
		

	}
	

	
	
	@SuppressWarnings("deprecation")
	@EventHandler
    public void OnPlayerInteract(final PlayerInteractEvent event) {
    	
		final Player player = event.getPlayer();
		PlayerInventory inv = player.getInventory();
		
    	final String RefreshMsg = this.getConfig().getString("CDRefreshed");
    	
		if(!(player.hasPermission("tparrow.use"))){
			
			return;
			
		}
		
    	
		
		if(!(playersFile.isSet("Players." + player.getName() + ".IOC"))){
			
			return;
			
		}
		
		Long IOC = playersFile.getLong("Players." + player.getName() + ".IOC");
		
    	if(inv.getItemInHand().getTypeId() == IOC){
    		
        	for(String blah : this.getConfig().getStringList("IgnoredWorlds")){
        		
        		if(blah.equalsIgnoreCase(player.getWorld().getName())){
        			
        			player.sendMessage(ChatColor.DARK_AQUA + "[tpArrow] " + ChatColor.AQUA + "tpArrow is disabled in this world!");
        			
        			return;
        			
        		}
        		
        	}
    		
    		Integer IOC2 = this.getConfig().getInt("RequiredItem");
    		
    		ItemStack rItem = new ItemStack(IOC2, 1);
    		
    		
			if(this.getConfig().getBoolean("DisableInCombat")){
				
				if(ctPlugin){
					
					ctAPI api = ctAPI.getAPI();
					
					if(api.isInCombat(player)){
						
						String msg = this.getConfig().getString("DisabledMessage");
						
						if(!(msg.equalsIgnoreCase("disable"))){
							
							player.sendMessage(replaceColors(msg));
							
						}

						return;
					
					}//is in combat

				} //check for plugin
				
			}// is disabled in combat
    		
    		
    		if(!(IOC2 == 0)){
    			
    			if(!(player.getInventory().contains(IOC2)) && player.hasPermission("tparrow.admin") == false){
    				  
    				String noItem = this.getConfig().getString("NoItem");
    				
    				if(!(noItem.equalsIgnoreCase("disable"))){
    					
    					player.sendMessage(replaceColors(noItem));
    					
    				}

    				return;
    				
    			}
    			
    		}
    		
		    	if(event.getAction() == Action.RIGHT_CLICK_AIR){
		    		
		    		final String pName = player.getName();
		    		
		    			String preCD = replaceColors(this.getConfig().getString("preCDMessage"));
		    			String cdMsg = replaceColors(this.getConfig().getString("CDMessage"));
		    			Long cD = this.getConfig().getLong("CDAmount");
		    		
		    		cD = cD * 20;
		    		
		    		
		    		if(set.contains(pName)){
		    			
		    			if(!(cdMsg.equalsIgnoreCase("disable"))){
		    					player.sendMessage(cdMsg);
		    			}
		    			
		    			
			    		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
			    			
			    			@Override
			    		    public void run(){
			    				
			    		    	set.remove(pName);
			    		    	
			    		    	if(!(RefreshMsg.equalsIgnoreCase("disable"))){
			    		    		
			    		    		player.sendMessage(replaceColors(RefreshMsg));
			    		    		
			    		    	}
			    		    	

			    		    }
			    			
			    		}, cD);
	
			    		
		    			return;
		    			
		    		}else{
			    			
			    		player.getInventory().removeItem(rItem);
			    		player.updateInventory();
			    		
			    		arrowFired.add(player.getName());
			    		
			    		Arrow arrow = player.launchProjectile(Arrow.class);
			    		arrow.setShooter(player);
			    		arrow.setVelocity(player.getLocation().getDirection().multiply(1.7));
			    		
			    		Long cdTime = cD / 20;
			    		
			    		if(!(player.hasPermission("tparrow.admin"))){
			    			
			    			set.add(pName);
			    			
			    		}
			    		
			    		
			    		if(!(preCD.equalsIgnoreCase("disable")) && player.hasPermission("tparrow.admin") == false){
			    			
			    			player.sendMessage(preCD.replace("<cooldown>", cdTime.toString()));
			    			
			    		}
			    		
			    		
		    		} //End set.contains
		   
		    	} //End right click
	    	
	    } //End IOC
    		
    } //End playerinteract event.
	
	
	@EventHandler
	public void launch(ProjectileLaunchEvent event){
		

		
		if(event.getEntity().getShooter() instanceof Player){
			
			Player p = (Player) event.getEntity().getShooter();	

			if(!(p.hasPermission("tparrow.use"))){
				
				return;
				
			}
			
        	for(String blah : this.getConfig().getStringList("IgnoredWorlds")){
        		
        		if(blah.equalsIgnoreCase(p.getWorld().getName())){
        			
        			p.sendMessage(ChatColor.DARK_AQUA + "[tpArrow] " + ChatColor.AQUA + "tpArrow is disabled in this world!");
        			
        			return;
        			
        		}
        		
        	}
        	
			if(arrowFired.contains(p.getName())){
				
				arrowFired.remove(p.getName());
				
				event.getEntity().setMetadata("tpArrow", new FixedMetadataValue(this, true));
				
			}
			
		}
		
	}
	
	
	
    @EventHandler
    public void arrowEvent(ProjectileHitEvent event) {
    	
        if(event.getEntity() instanceof Arrow) {
        	
            Arrow arrow = (Arrow)event.getEntity();
            Entity shooter = arrow.getShooter();

            if(shooter instanceof Player) {
            	
	                Player player = (Player) shooter;
	                
	            	
	            	
	            	
		                if(arrow.hasMetadata("tpArrow")){
		                	
		                	for(String blah : this.getConfig().getStringList("IgnoredWorlds")){
	            		
		                		if(blah.equalsIgnoreCase(player.getWorld().getName())){
		            			
			            			player.sendMessage(ChatColor.DARK_AQUA + "[tpArrow] " + ChatColor.AQUA + "tpArrow is disabled in this world!");
			            			
			            			return;
			            		}
			            		
			            	}
		                	
		                	arrow.getLocation().setPitch(player.getLocation().getPitch());
		                	arrow.getLocation().setYaw(player.getLocation().getYaw());
		                	
		                	
		                	player.teleport(arrow.getLocation());
		                	arrow.remove();
		                    
		                }
	                                    
            }
        }
         
         
    }
    
    
	public boolean onCommand(CommandSender sender, Command cmd, String cmdL, String[] args) {
		
		if(!(sender instanceof Player)){
			
			sender.sendMessage("You goofball, you cant use that command from console!");
			
			return true;
			
		}
		
		Player player = (Player) sender;

		if(cmdL.equalsIgnoreCase("tparrow")){
				
				if(args.length == 0 || args.length >= 2){
					
					PluginDescriptionFile pdfFile = this.getDescription();
					
					player.sendMessage(ChatColor.DARK_AQUA + "=-=-=" + ChatColor.AQUA + " tpArrow Information " + ChatColor.DARK_AQUA + "=-=-=");
					player.sendMessage(ChatColor.DARK_AQUA + "Author: " + ChatColor.AQUA + "ImDeJay");
					player.sendMessage(ChatColor.DARK_AQUA + "Version: " + ChatColor.AQUA + pdfFile.getVersion());
					player.sendMessage(ChatColor.DARK_AQUA + "Description: " + ChatColor.AQUA + pdfFile.getDescription());
					player.sendMessage(ChatColor.DARK_AQUA + "Help: " + ChatColor.AQUA + "/tparrow help");
					
					
				}else if(args.length == 1){
					
					if(args[0].equalsIgnoreCase("equip")){
						
						if(player.hasPermission("tparrow.use")){
							
							playersFile.set("Players." + player.getName() + ".IOC", player.getItemInHand().getTypeId());
						
							player.sendMessage(ChatColor.DARK_AQUA + "[tpArrow] " + ChatColor.AQUA + "Successfully equipped!");
							
							this.savePlayerYML();
							
						}else{
							
							player.sendMessage(ChatColor.RED + "You don't have permission to use that command!");
							
							return true;
							
						}
						
						
					}else if(args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")){
						
						player.sendMessage(ChatColor.DARK_AQUA + "=-=-=" + ChatColor.AQUA + " tpArrow Help " + ChatColor.DARK_AQUA + "=-=-=");
						player.sendMessage(ChatColor.AQUA + "You must create a 'wand' to use tpArrow.");
						player.sendMessage(ChatColor.AQUA + "To define your 'wand', hold it in your hand and type '/tparrow equip'.");
						player.sendMessage(ChatColor.AQUA + "Now that you've defined a wand, you simply right click to use tpArrow!");
						player.sendMessage(ChatColor.AQUA + "If you want to remove tpArrow from an item, just use /tparrow disarm");
						
					}else if(args[0].equalsIgnoreCase("disarm")){
						
						if(player.hasPermission("tparrow.use")){
							
							playersFile.set("Players." + player.getName() + ".IOC", null);
						
							player.sendMessage(ChatColor.DARK_AQUA + "[tpArrow] " + ChatColor.AQUA + "tpArrow successfully disarmed!");
						
						}else{
							
							player.sendMessage(ChatColor.RED + "You don't have permisison to use that command!");
							
							return true;
							
						}

					}else{
						
						player.sendMessage(ChatColor.DARK_AQUA + "[tpArrow] " + ChatColor.AQUA + "Invalid arguments!");
						
					} //args = equip
					
				} //args length
				
			} //cmdl = tparrow
			
		return true;
		
		
	}
	
	
	public void savePlayerYML(){
		
		try {
			playersFile.save(configFile);
		} catch (IOException e) {
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
	
		
		static String replaceColors(String message)
		  {
		    return message.replaceAll("(?i)&([a-f0-9])", "§$1");
		  }
		
		
}
