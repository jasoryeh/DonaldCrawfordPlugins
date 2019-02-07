package com.gmail.dejayyy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.dejayyy.langHandler.Lang;
import com.gmail.dejayyy.mcStats;

public class Main extends JavaPlugin implements Listener{
	//TODO disable players in fly mode if they are in fly mode upon attack
	public static YamlConfiguration LANG;
	public static File LANG_FILE;
	public static Logger log;
	
	public final HashSet<String> playerKicked = new HashSet<String>();
	int attackID;
	int vicID;
	boolean pluginEnabled;
	public final HashMap<String, Integer> combat = new HashMap<String, Integer>();
	
	public List<String> disabledWorlds = new ArrayList<String>();
	
	public ArrayList <String> combatList = new ArrayList <String> ();
	
	//API
	private static ctAPI API = null;
	//API
	
	public Main plugin;
	
	public boolean isInCombat(Player player){
		
		return combat.containsKey(player.getName());
		
	}
	
	public void startupStats(){
		
		try {
		      mcStats metrics = new mcStats(this);
		      metrics.start();
		    } catch (IOException e) {
		      // Failed to submit the stats :-(
		    }
		
	}
	
	public void onEnable(){
		
		//API
		API = new ctAPI(this);
		//API
		
		//mcstats
		this.startupStats();		
		//mcstats
		
		this.loadLang();
		
		log = this.getServer().getLogger();
		
		this.getServer().getPluginManager().registerEvents(this,  this);
		
		saveDefaultConfig();
		
		updateConfig();
		
		pluginEnabled = true;
		
		for(String x : this.getConfig().getStringList("IgnoredWorlds")){
			
			disabledWorlds.add(x.toLowerCase());
			
		}
		
		
	}
	
	public void updateConfig(){
			
			System.out.println(this.getConfig().getString("combatMSG"));
		
	}
	
	
	public void onDisable(){
		
		
	}

	public boolean onCommand(CommandSender sender, Command cmd, String cmdL, String[] args) {
		
		if(!(sender instanceof Player)){
			
			if(cmdL.equalsIgnoreCase("combatTracker") || cmdL.equalsIgnoreCase( "ct")){
				
				if(args.length == 0){
					
					sender.sendMessage(ChatColor.DARK_AQUA + Lang.isPluginEnabled.toString().replace("%a", Boolean.toString(pluginEnabled)));
					sender.sendMessage(ChatColor.DARK_AQUA + Lang.PlayersInCombat.toString().replace("%a", Integer.toString(combat.size())));
					
				}else if(args.length == 1 ){
					
					if(args[0].equalsIgnoreCase("reload")){
						
						disabledWorlds.clear();
						
						this.reloadConfig();
						
						this.loadLang();
						
						for(String x : this.getConfig().getStringList("IgnoredWorlds")){
							
							disabledWorlds.add(x.toLowerCase());
							
						}
						
						sender.sendMessage(ChatColor.DARK_AQUA + "[combatTracker] " + ChatColor.AQUA + Lang.ReloadComplete.toString());
						
					}else if(args[0].equalsIgnoreCase("disable")){
						
						combat.clear();
						
						this.getServer().getScheduler().cancelTasks(this);
						
						sender.sendMessage(ChatColor.DARK_AQUA + "[combatTracker] " + ChatColor.AQUA + Lang.DisabledAndCleared.toString());
						
						this.getServer().broadcastMessage(ChatColor.DARK_AQUA + "[combatTracker] " + ChatColor.AQUA + Lang.PluginDisabled.toString());
						
						pluginEnabled = false;
						
						return true;
						
					}else if(args[0].equalsIgnoreCase("enable")){
						
						pluginEnabled = true;
						
						sender.sendMessage(ChatColor.DARK_AQUA + "[combatTracker] " + ChatColor.AQUA + Lang.PluginEnabled.toString());
						
						return true;
						
					}else{
						
						sender.sendMessage(ChatColor.DARK_AQUA + "[combatTracker] " + ChatColor.AQUA + Lang.CannotRunFromConsole.toString());
						
						return true;
						
					}
					
				}else{
					
					sender.sendMessage(ChatColor.DARK_AQUA + "[combatTracker] " + ChatColor.AQUA + Lang.CannotRunFromConsole.toString());
					
					return true;
					
				}
				
			} //console reload
			
			return true;
			
		} //check console
		
		Player player = (Player) sender;
		
		if(cmdL.equalsIgnoreCase("combatTracker") || cmdL.equalsIgnoreCase("ct")){
			
			if(args.length == 0){
				
				this.pluginInfo(player);
				
			}else if(args.length == 1){
				
				if(args[0].equalsIgnoreCase("reload")){

					if(player.hasPermission("combatTracker.admin")){
						
						disabledWorlds.clear();
						
						this.reloadConfig();
				
						for(String x : this.getConfig().getStringList("IgnoredWorlds")){
							
							disabledWorlds.add(x.toLowerCase());
							
						}

						player.sendMessage(ChatColor.DARK_AQUA + "[combatTracker] " + ChatColor.AQUA + Lang.ReloadComplete.toString());
						
					}else{
						
						player.sendMessage(ChatColor.DARK_AQUA + "[combatTracker] " + ChatColor.AQUA + Lang.NoPermission.toString());
						
					} // check perm
					
				}else if(args[0].equalsIgnoreCase("disable")){
					
					if(player.hasPermission("combatTracker.admin")){

						combat.clear();
						
						this.getServer().getScheduler().cancelTasks(this);
						
						player.sendMessage(ChatColor.DARK_AQUA + "[combatTracker] " + ChatColor.AQUA + Lang.DisabledAndCleared.toString());
						
						this.getServer().broadcastMessage(ChatColor.DARK_AQUA + "[combatTracker] " + ChatColor.AQUA + Lang.PluginDisabled.toString());
						
						pluginEnabled = false;
						
						return true;
						
					}else{
						
						player.sendMessage(ChatColor.DARK_AQUA + "[combatTracker] " + ChatColor.AQUA + Lang.NoPermission.toString());
						
					}
					
					
				}else if(args[0].equalsIgnoreCase("enable")){
					
					if(player.hasPermission("combatTracker.admin")){
						
						pluginEnabled = true;
						
						this.getServer().broadcastMessage(ChatColor.DARK_AQUA + "[combatTracker] " + ChatColor.AQUA + Lang.PluginEnabled.toString());
						
						player.sendMessage(ChatColor.DARK_AQUA + "[combatTracker] " + ChatColor.AQUA + Lang.PluginEnabled.toString());
						
					}else{
						
						player.sendMessage(ChatColor.AQUA + "[combatTracker] " + ChatColor.AQUA + Lang.NoPermission.toString());
						
					}
					
				}else if(args[0].equalsIgnoreCase("check")){
					
					if(isInCombat(player)){
						
						player.sendMessage(ChatColor.DARK_AQUA + "[combatTracker] " + ChatColor.AQUA + Lang.InCombat.toString());
					
					}else{
						
						player.sendMessage(ChatColor.DARK_AQUA + "[combatTracker] " + ChatColor.AQUA + Lang.NotInCombat.toString());
						
					}
					
				}else{

					this.pluginHelp(player);
					
					return true;
					
				} //args == reload
				
			}else if(args.length == 2){
				
				if(args[0].equalsIgnoreCase("remove")){
					
					if(player.hasPermission("combatTracker.mod")){
						Player p = this.getServer().getPlayer(args[1]);
						
						if(p != null){
							
							this.getServer().getScheduler().cancelTask(combat.get(p.getName()));
							
							combat.remove(p.getName());
							
							String noCombat = replaceColors(getConfig().getString("noCombatMSG"));
							
							p.sendMessage(noCombat);
							
							player.sendMessage(ChatColor.DARK_AQUA + "[combatTracker] " + ChatColor.AQUA + Lang.TakenOutOfCombat.toString().replace("%p", p.getName()));
						
							return true;
							
						}else{
							
							player.sendMessage(ChatColor.DARK_AQUA + "[combatTracker] " + ChatColor.AQUA + Lang.PlayerNotFound.toString());
							
							return true;
							
						}
						
					}else{
						
						player.sendMessage(ChatColor.DARK_AQUA + "[combatTracker] " + ChatColor.AQUA + Lang.NoPermission.toString());
						
					}
										
				}
				
								
			}else{
				
				this.pluginHelp(player);
				
				return true;
				
			}
			
			
		}
		
		return true;
		
		
	}
	
	public void pluginInfo(Player player){
		
		player.sendMessage(ChatColor.DARK_AQUA + Lang.PluginAuthor.toString() + ChatColor.AQUA + "ImDeJay");
		player.sendMessage(ChatColor.DARK_AQUA + Lang.isPluginEnabled.toString().replace("%a", Boolean.toString(pluginEnabled)));
		player.sendMessage(ChatColor.DARK_AQUA + Lang.PlayersInCombat.toString().replace("%a", Integer.toString(combat.size())));
		player.sendMessage(ChatColor.DARK_AQUA + Lang.CombatTime.toString().replace("%a", Long.toString(this.getConfig().getLong("combatTime"))));
		
	}
	
	public void pluginHelp(Player player){
		
		player.sendMessage(ChatColor.DARK_AQUA + "/ct check " + ChatColor.AQUA + Lang.cmdHelp_Check);
		
		if(player.hasPermission("combatTracker.admin")){
			
			player.sendMessage(ChatColor.DARK_AQUA + "/ct reload " + ChatColor.AQUA + Lang.cmdHelp_Reload);
			player.sendMessage(ChatColor.DARK_AQUA + "/ct disable " + ChatColor.AQUA + Lang.cmdHelp_Disable);
			player.sendMessage(ChatColor.DARK_AQUA + "/ct enable " + ChatColor.AQUA + Lang.cmdHelp_Enable);
			
		}
		
		if(player.hasPermission("combatTracker.mod") || player.hasPermission("combatTracker.admin")){
			
			player.sendMessage(ChatColor.DARK_AQUA + "/ct remove [PlayerName] " + ChatColor.AQUA + Lang.cmdHelp_TakeOutOfCombat);
		
		}
		
		
	}
	@EventHandler(priority=EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageByEntityEvent e) {
		
		if(worldDisabled(e.getEntity().getWorld())){
			
			return;
			
		}
		
		if(e.isCancelled() || e.getDamage() == 0 || pluginEnabled == false){
			
			return;
			
		}
		
		Long coolDown = this.getConfig().getLong("combatTime");
		
		if(coolDown == 0){
			
			return;
			
		}
		
		
		
		if(e.getDamager() instanceof Arrow){
			
			Arrow arrow = (Arrow) e.getDamager();
			
			if(arrow.getShooter() instanceof Player){
				
				if(e.getEntity() instanceof Player){
					
					Player a = (Player) arrow.getShooter();
					
					Player v = (Player) e.getEntity();
					
					this.putInCombat(a, v);
					
				}
					
			}
				
		}
		
        if(e.getEntity() instanceof Player && e.getDamager() instanceof Player) {

    			final Player victim = (Player) e.getEntity();
    			final Player attacker = (Player) e.getDamager();
    		
                if(attacker.hasPermission("combatTracker.bypass") || victim.hasPermission("combatTracker.bypass") || attacker.isOp() || victim.isOp()){
                	
                	return;
                	
                }
 
                
                this.putInCombat(attacker, victim);
                
        } //instance of player
        
    } //onentitydamage
	
	public void putInCombat(final Player attacker, final Player victim){

		String inCombat = replaceColors(this.getConfig().getString("combatMSG"));
		Long coolDown = this.getConfig().getLong("combatTime");
		
		if(coolDown == 0){
			
			return;
			
		}

            if(attacker.hasPermission("combatTracker.bypass") || victim.hasPermission("combatTracker.bypass") || attacker.isOp() || victim.isOp()){
            	
            	return;
            	
            }
            // If list does not already contain the attacker, add him/her to the list
            	
            if(!(isInCombat(attacker))){
            	
            	if(!(inCombat.equalsIgnoreCase("disable"))){
            		attacker.sendMessage(inCombat);
            	}
            	
            }else{
            	
            	this.getServer().getScheduler().cancelTask(combat.get(attacker.getName()));
            	
            }

            if(!(isInCombat(victim))){
            	
            	if(!(inCombat.equalsIgnoreCase("disable"))){
            		victim.sendMessage(inCombat);
            	}
            	
            }else{
            	
            	this.getServer().getScheduler().cancelTask(combat.get(victim.getName()));
            }

            coolDown = coolDown * 20;
            
            
            // Remove both from the lists after 'x' amount of time
            attackID = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
            {
                @Override
                public void run() {

                	combat.remove(attacker.getName());
                    
            		String noCombat = replaceColors(getConfig().getString("noCombatMSG"));
                    
                    if(!(isInCombat(attacker))){
                    		
                    	if(!(noCombat.equalsIgnoreCase("disable"))){
                    			
                    		attacker.sendMessage(noCombat);
                    		
                    	}
                    }
                    
                    }
                
            }, coolDown);
            
            vicID = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
            {
                @Override
                public void run() {
                	
                    combat.remove(victim.getName());
                    
            		String noCombat = replaceColors(getConfig().getString("noCombatMSG"));
                    
                    if(!(isInCombat(victim))){
                    		
                    	if(!(noCombat.equalsIgnoreCase("disable"))){
                    			
                    		victim.sendMessage(noCombat);
                    		
                    	}
                    }
                    	
                    
                    }
                
            }, coolDown);
        
            combat.put(attacker.getName(), attackID);
            combat.put(victim.getName(), vicID);
	}
	
	
	@EventHandler
	public void onPlayerChat(PlayerCommandPreprocessEvent event) {
		
		if(pluginEnabled == false){
			
			return;
			
		}
		
		String msg = replaceColors(this.getConfig().getString("cmdDisabled"));
		
		List<String> cmdsDisabled = this.getConfig().getStringList("cmdsDisabled");
		
		Player player = event.getPlayer();
		
		Boolean allDisabled = this.getConfig().getBoolean("disableAll");
		
		if(isInCombat(player)){
			if(allDisabled){
				
				event.setCancelled(true);
				
				if(!(msg.equalsIgnoreCase("disabled"))){
					player.sendMessage(msg);
				}
				
				
				return;
			}
			
			for(String cmdsd : cmdsDisabled){
				
				if(event.getMessage().toLowerCase().startsWith("/" + cmdsd.toLowerCase())){
					
					if(!(msg.equalsIgnoreCase("disabled"))){
						player.sendMessage(msg);
					}
					
					event.setCancelled(true);
					
					
				} //end message check
				
			} // end for loop
		}
		
	} //Playerchat
	
	static String replaceColors(String message) {
		
		return ChatColor.translateAlternateColorCodes('&', message);
		
	  }
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void playerDeath(PlayerDeathEvent event){
		
		if(worldDisabled(event.getEntity().getWorld())){
			
			return;
			
		}
		
		if(pluginEnabled == false){
			
			return;
			
		}
		
		
		Player player = event.getEntity().getPlayer();
		
		if(isInCombat(player)){
			
			String noCombat = replaceColors(getConfig().getString("noCombatMSG"));
			
        	if(!(noCombat.equalsIgnoreCase("disable"))){
    			
        		player.sendMessage(noCombat);
        		
        	}
			
        	
        	
			this.getServer().getScheduler().cancelTask(combat.get(player.getName()));
			
			combat.remove(player.getName());
		}

	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void rageQuit(PlayerQuitEvent event){
		
		if(worldDisabled(event.getPlayer().getWorld())){
			
			return;
			
		}
		
		if(pluginEnabled == false){
			
			return;
			
		}
		
		Player player = event.getPlayer();
		
		
		if(isInCombat(player)){
			
			String logMSG = replaceColors(this.getConfig().getString("broadcastMSG"));
			
			if(!(logMSG.equalsIgnoreCase("disable"))){
				
				player.getServer().broadcastMessage(logMSG.replaceAll("<player>", player.getName()));
				
			}
			
			
			Boolean killOnLogout = this.getConfig().getBoolean("killOnLogout");

				if(killOnLogout == true){
					
					player.setHealth(0);
					
					player.setMetadata("logger", new FixedMetadataValue(this, true));
					
				}
			
			combat.remove(player.getName());
			
		}
	}

	
	//API
	public static ctAPI getAPI() {
		return API;
	}
	//API
	
	public boolean worldDisabled(World world){
		
		return disabledWorlds.contains(world.getName().toLowerCase());
		
	}
	
	  /**
	   * Load the lang.yml file.
	   * @return The lang.yml config.
	   */
	  private void loadLang() {
	        File lang = new File(getDataFolder(), "lang.yml");
	        OutputStream out = null;
	        InputStream defLangStream = this.getResource("lang.yml");
	        if (!lang.exists()) {
	            try {
	                getDataFolder().mkdir();
	                lang.createNewFile();
	                if (defLangStream != null) {
	                    out = new FileOutputStream(lang);
	                    int read = 0;
	                    byte[] bytes = new byte[1024];

	                    while ((read = defLangStream.read(bytes)) != -1) {
	                        out.write(bytes, 0, read);
	                    }
	                    YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defLangStream);
	                    Lang.setFile(defConfig);
	                    return;
	                }
	            } catch (IOException e) {
	                e.printStackTrace(); // So they notice
	                log.severe("[combatTracker] Couldn't create language file.");
	                log.severe("[combatTracker] This is a fatal error. Now disabling");
	                this.setEnabled(false); // Without it loaded, we can't send them messages
	            } finally {
	                if (defLangStream != null) {
	                    try {
	                        defLangStream.close();
	                    } catch (IOException e) {
	                        e.printStackTrace();
	                    }
	                }
	                if (out != null) {
	                    try {
	                        out.close();
	                    } catch (IOException e) {
	                        e.printStackTrace();
	                    }

	                }
	            }
	        }
	        YamlConfiguration conf = YamlConfiguration.loadConfiguration(lang);
	        for (Lang item : Lang.values()) {
	            if (conf.getString(item.getPath()) == null) {
	                conf.set(item.getPath(), item.getDefault());
	            }
	        }
	        Lang.setFile(conf);
	        Main.LANG = conf;
	        Main.LANG_FILE = lang;
	        try {
	            conf.save(getLangFile());
	        } catch (IOException e) {
	            log.log(Level.WARNING, "combatTracker: Failed to save lang.yml.");
	            log.log(Level.WARNING, "combatTracker: Report this stack trace to ImDeJay");
	            e.printStackTrace();
	        }
	    }

	  
	  /**
	  * Gets the lang.yml config.
	  * @return The lang.yml config.
	  */
	  public YamlConfiguration getLang() {
	      return LANG;
	  }
	   
	  /**
	  * Get the lang.yml file.
	  * @return The lang.yml file.
	  */
	  public File getLangFile() {
	      return LANG_FILE;
	  }
	  
	
}
