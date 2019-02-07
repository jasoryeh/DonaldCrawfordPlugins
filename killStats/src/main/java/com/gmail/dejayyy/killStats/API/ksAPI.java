package com.gmail.dejayyy.killStats.API;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import com.gmail.dejayyy.killStats.PlayerStats;
import com.gmail.dejayyy.killStats.ksAdmin;
import com.gmail.dejayyy.killStats.ksMain;
import com.gmail.dejayyy.killStats.MySQL.MySQL;
import com.gmail.dejayyy.killStats.MySQL.SQLite;
import com.gmail.dejayyy.killStats.ksLang.langHandler;
import com.gmail.dejayyy.killStats.ksLang.langHandler.Lang;

public class ksAPI {

	private static ksMain plugin;
	
	public File worldContainer;
	public File dataFolder;
	
	public Logger log;
	
	public boolean aSyncCheckConnection;
	
	public HashMap<String, Integer> killsMap = new HashMap<String, Integer>();
	public HashMap<String, Integer> deathsMap = new HashMap<String, Integer>();
	public HashMap<String, Integer> streakMap = new HashMap<String, Integer>();
	public HashMap<String, Double> ratioMap = new HashMap<String, Double>();
	
	private static final BlockFace[] faces =  new BlockFace[]{BlockFace.UP, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
	
	public ksAPI(ksMain plugin){
		
		ksAPI.plugin = plugin;
		
		dataFolder = plugin.getDataFolder();
		worldContainer = plugin.getServer().getWorldContainer();
		
		log = Logger.getLogger("Minecraft");
		
				
	}

	public ksAPI(langHandler langHandler) {

	}



	public ksAPI(ksAdmin ksAdmin) {

	}

	
	public ksAPI(MySQL mySQL) {
		
		
	}

	public boolean MakeSQLConnection(boolean runAsync){
		
		final String theHost = plugin.getConfig().getString("MySQL.Host");
		final String thePort = plugin.getConfig().getString("MySQL.Port");
		final String dbName = plugin.getConfig().getString("MySQL.dbName");
		final String userName = plugin.getConfig().getString("MySQL.Username");
		final String passWord = plugin.getConfig().getString("MySQL.Password");
		
		if(runAsync){

			aSyncCheckConnection = false;
			
			    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new BukkitRunnable(){

			        @Override
			        public void run() {
			    
			    		if(plugin.sqlEnabled){
			    			
				    		MySQL sql = new MySQL(plugin, theHost, thePort, dbName, userName, passWord);
				    		
				    		plugin.sqlConnection = sql.openConnection();
				    		
				    		aSyncCheckConnection = sql.checkConnection();
				    		
			    		}else{
			    			
			    			SQLite sql = new SQLite(plugin, "player_data.db");
			    			
			    			plugin.sqlConnection = sql.openConnection();
			    			
			    			aSyncCheckConnection = sql.checkConnection();
			    			
			    			
			    		}
			    		

			        }
			        
			    });
			    
			    plugin.getServer().getLogger().log(Level.SEVERE, Boolean.toString(this.aSyncCheckConnection));
			    
			    return this.aSyncCheckConnection;

		}else{
				
			if(plugin.sqlEnabled){
				
				MySQL sql = new MySQL(plugin, theHost, thePort, dbName, userName, passWord);
		    		
		    	plugin.sqlConnection = sql.openConnection();
	
	    		return sql.checkConnection();
	    		
			}else{	
				
				SQLite sql = new SQLite(plugin, "player_data.db");
					
				plugin.sqlConnection = sql.openConnection();
					
				return sql.checkConnection();
				
			}

		}
		
	}
	
	public void removeSignFromYML(Location l){
		
		File f = new File(plugin.getDataFolder() + File.separator + "signs.yml");
		
		YamlConfiguration s = YamlConfiguration.loadConfiguration(f);
		
		String worldName = l.getWorld().getName().replaceAll("_", "-");
		String x = Integer.toString(l.getBlockX());
		String y = Integer.toString(l.getBlockY());
		String z = Integer.toString(l.getBlockZ());
		String loc = worldName + "_" + x + "_" + y + "_" + z;
		
		s.set("Signs." + loc, null);
		
		try {
			s.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	public boolean isRankSign(Location l){
		
		File f = new File(plugin.getDataFolder() + File.separator + "signs.yml");
		
		YamlConfiguration s = YamlConfiguration.loadConfiguration(f);
		
		String worldName = l.getWorld().getName().replaceAll("_", "-");
		String x = Integer.toString(l.getBlockX());
		String y = Integer.toString(l.getBlockY());
		String z = Integer.toString(l.getBlockZ());
		String loc = worldName + "_" + x + "_" + y + "_" + z;
		
		return s.contains("Signs." + loc);
		

	}
	
	public void saveSign(Location l, String type, String theRank){
	
			File s = new File(plugin.getDataFolder() + File.separator + "signs.yml");
			
			if(!(s.exists())){
				
				try {
					s.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}

			YamlConfiguration signs = YamlConfiguration.loadConfiguration(s);
					
			String worldName = l.getWorld().getName().replaceAll("_", "-");
			String x = Integer.toString(l.getBlockX());
			String y = Integer.toString(l.getBlockY());
			String z = Integer.toString(l.getBlockZ());
			String theKey = worldName + "_" + x + "_" + y + "_" + z;
			
			signs.set("Signs." + theKey + ".Type", type);
			signs.set("Signs." + theKey + ".Rank", theRank);
			
			try {
				signs.save(s);
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	
	public void setRatio(Player player){

		double kills = this.getKills(player);
		double deaths = this.getDeaths(player);
		double ratio;
		
		if(kills == 0 && deaths == 0){
			
			ratio = 0.0;
			
		}else if(kills > 0 && deaths == 0){
			
			ratio = kills;
			
		}else if(deaths > 0 && kills == 0){
			
			ratio = -deaths;
			
		}else{
			
			ratio = (kills / deaths);
			
		}
		
		ratio = Math.round(ratio*100.0)/100.0;
			
			String worldName = player.getWorld().getName();
			
			if(seperateWorld(worldName)){
				
				plugin.sqlInfo.get(worldName + ":" + player.getName()).setRatio(ratio);
			
			}else{
				
				plugin.sqlInfo.get(player.getName()).setRatio(ratio);
				
			}

			this.saveSQLDataFromHashmap(player);
			
			return;

		
	}
	
	public double getRatio(Player player){
			
			String worldName = player.getWorld().getName();
			
			if(seperateWorld(worldName)){
				
				if(plugin.sqlInfo.containsKey(worldName + ":" + player.getName())){
					
					return plugin.sqlInfo.get(worldName + ":" + player.getName()).getRatio();
					
				}else{
					
					return 0.0;
					
				}

			}else{
				
				if(plugin.sqlInfo.containsKey(player.getName())){
					
					return plugin.sqlInfo.get(player.getName()).getRatio();
					
				}else{

					return 0.0;
					
				}
				
				
			}

	
	}
	
	public int getKills(Player player){

			String worldName = player.getWorld().getName();
			
			if(seperateWorld(worldName)){
				
				if(plugin.sqlInfo.containsKey(worldName + ":" + player.getName())){
					
					return plugin.sqlInfo.get(worldName + ":" + player.getName()).getKills();
					
				}else{

					return 0;
					
				}

			}else{
				
				if(plugin.sqlInfo.containsKey(player.getName())){
					
					return plugin.sqlInfo.get(player.getName()).getKills();
					
				}else{

					return 0;
					
				}
				
				
			}
		
	}
	


	public void setKills(Player player, int kills){
			
			String worldName = player.getWorld().getName();
			
			if(seperateWorld(worldName)){
				
				plugin.sqlInfo.get(worldName + ":" + player.getName()).setKills(kills);
			
			}else{
				
				plugin.sqlInfo.get(player.getName()).setKills(kills);
				
			}

			this.setRatio(player);
			
			return;
		
	}
	
	public int getDeaths(Player player){
			
			String worldName = player.getWorld().getName();
			
			if(seperateWorld(worldName)){
				
				if(plugin.sqlInfo.containsKey(worldName + ":" + player.getName())){
					
					return plugin.sqlInfo.get(worldName + ":" + player.getName()).getDeaths();
					
				}else{
					
					return 0;
					
				}

			}else{
				
				if(plugin.sqlInfo.containsKey(player.getName())){
					
					return plugin.sqlInfo.get(player.getName()).getDeaths();
					
				}else{

					return 0;
					
				}
				
				
			}		
		
		
	}

	public void setDeaths(Player player, int deaths){

			
			String worldName = player.getWorld().getName();
			
			if(seperateWorld(worldName)){
				
				plugin.sqlInfo.get(worldName + ":" + player.getName()).setDeaths(deaths);
			
			}else{
				
				plugin.sqlInfo.get(player.getName()).setDeaths(deaths);
				
			}
			
			this.setRatio(player);
			
			return;

		
	}
	
	public int getStreak(Player player){
			
			String worldName = player.getWorld().getName();
			
			if(seperateWorld(worldName)){
				
				if(plugin.sqlInfo.containsKey(worldName + ":" + player.getName())){
					
					return plugin.sqlInfo.get(worldName + ":" + player.getName()).getStreak();
				}else{
					
					return 0;
					
				}

			}else{
				
				if(plugin.sqlInfo.containsKey(player.getName())){
					
					return plugin.sqlInfo.get(player.getName()).getStreak();
					
				}else{
					
					return 0;
					
				}
				
				
			}

		
	}

	public void setStreak(Player player, int streak){
			
			String worldName = player.getWorld().getName();
			
			if(seperateWorld(worldName)){
				
				plugin.sqlInfo.get(worldName + ":" + player.getName()).setStreak(streak);
			
			}else{
				
				plugin.sqlInfo.get(player.getName()).setStreak(streak);
				
			}
			
			this.setRatio(player);
			
			return;
		
	}
	
	public void checkWorldFolder(String worldName){
		
		File wFolder = new File(plugin.getDataFolder() + File.separator + "player_data" + File.separator + worldName);
		
		if(!(wFolder.exists())){
			
			try{
				
				wFolder.mkdir();
			
			}catch(Exception ex){
				
				ex.printStackTrace();
												
			}
		}
		
		
	}	
	
	
	public boolean seperateWorld(String worldName){
		
		List<String> seperateWorlds = new ArrayList<String>();
		
		seperateWorlds = plugin.getConfig().getStringList("Seperate_Worlds");

		return seperateWorlds.contains(worldName);
				
	}
	
	public boolean worldDisabled(String worldName){
		
		List<String> disabledWorlds = new ArrayList<String>();
		
		disabledWorlds = plugin.getConfig().getStringList("Disabled_Worlds");
		
		return disabledWorlds.contains(worldName);
		
	}
	
	public boolean isBoosting(Player killer, Player victim){
		  
		  String k = killer.getName();
		  String v = victim.getName();
		  
		  int MaxKilling = plugin.getConfig().getInt("AntiBoost.Max_Killing", 3);
		  
		  int x = 0;
		  
		  for(String blah : plugin.deadList){
			  
			  if(blah.equalsIgnoreCase(k + ":" + v)){
				  
				  x++;
				  
			  }
			  
		  }
		  
		  if(x == MaxKilling){
			  
			  return true;
			  
		  }else{
			  
			  return false;
			  
		  }
		  
	  }
	
	public String replaceVariables(String msg, Player k, Player v){
		
		String victim = v.getName();
		String killer = k.getName();
		
		int victimKills = this.getKills(v);
		int killerKills = this.getKills(k);
		int victimDeaths = this.getDeaths(v);
		int killerDeaths = this.getDeaths(k);
		int victimStreak = this.getStreak(v);
		int killerStreak = this.getStreak(k);
		double killerRatio = this.getRatio(k);
		double victimRatio = this.getRatio(v);
		
		if(victim == null){
			victim = "---";
		}
		
		if(killer == null){
			killer = "---";
		}
		
		msg = msg.replaceAll("%killer.name%", killer);
		msg = msg.replaceAll("%victim.name%", victim);
		msg = msg.replaceAll("%victim.kills%", Integer.toString(victimKills));
		msg = msg.replaceAll("%victim.deaths%", Integer.toString(victimDeaths));
		msg = msg.replaceAll("%killer.kills%", Integer.toString(killerKills));
		msg = msg.replaceAll("%killer.deaths%", Integer.toString(killerDeaths));
		msg = msg.replaceAll("%victim.streak%", Integer.toString(victimStreak));
		msg = msg.replaceAll("%killer.streak%", Integer.toString(killerStreak));
		msg = msg.replaceAll("%victim.ratio%", Double.toString(victimRatio));
		msg = msg.replaceAll("%killer.ratio%", Double.toString(killerRatio));
		
		return ChatColor.translateAlternateColorCodes('&', msg);
		
	}

	public void DropHead(Player player){
		
		Random r = new Random();
		
		double percentage = plugin.getConfig().getDouble("Drop_Head.Percentage", 100) / 100;
		
		if(r.nextDouble() <= percentage){
			
			ItemStack i = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
		      
		     SkullMeta meta = (SkullMeta)i.getItemMeta();
		      
		     meta.setOwner(player.getName());
		      
		     i.setItemMeta(meta);

		     player.getWorld().dropItem(player.getLocation(), i);
		     
		}
		
	}
	
	
	public void HandleBrokenStreak(Player killer, Player victim){
		
		int min = plugin.getConfig().getInt("Minimum_Streak_To_Broadcast", 10);
		
		int victimStreak = this.getStreak(victim);
		
		if(!(victimStreak >= min)){
			
			return;
			
		}

		String msg = this.replaceVariables(Lang.Broken_Streak_Message.toString(), killer, victim);
		
		if(!(msg.equalsIgnoreCase("disable"))){
			
			if(seperateWorld(killer.getWorld().getName())){
				
				for(Player p : plugin.getServer().getOnlinePlayers()){
					
					if(p.getWorld().getName().equalsIgnoreCase(killer.getWorld().getName())){
						
						p.sendMessage(Lang.Prefix + msg);
						
					}
				}
				
			}else{
				
				for(Player p : plugin.getServer().getOnlinePlayers()){
					
					String worldName = p.getWorld().getName();
					
					if(!(worldDisabled(worldName)) && !(seperateWorld(worldName))){
					
						p.sendMessage(msg);
						
					}
				}
				
			}
						
		}
		
	}
	
	public String currentTime() {
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
		Calendar cal = Calendar.getInstance();
		return dateFormat.format(cal.getTime());
	}
	

	public void giveRewards(Player killer, Player victim, List<String> theList){
		
		for(String a : theList){
			
			if(a.startsWith("command")){ 
				
				String[] data = a.split(";");
				
					String cmd = this.replaceVariables(data[1].toString(), killer, victim);
					
					if(data[1].startsWith("/")){
						
						cmd = cmd.replaceFirst("/", "");
						
						if(killer.isOp()){
							
							killer.performCommand(cmd);
							
						}else{
							
							killer.setOp(true);
							
							killer.performCommand(cmd);
							
							killer.setOp(false);
							
						}
						
					}else{
						
						if(killer.isOp()){
							
							killer.performCommand(cmd);
							
						}else{
							
							killer.setOp(true);
							
							killer.performCommand(cmd);
							
							killer.setOp(false);
							
						}
						
					}
				
				
			}else{
				
				String[] sp = a.split(" ");
				
				if(sp.length > 2){
				
					ItemStack reward;
					
					if(sp[0].contains(":")){

						reward = new ItemStack(Integer.parseInt(sp[0].split(":")[0]), Integer.parseInt(sp[1]), (short) Integer.parseInt(sp[0].split(":")[1]));
					
					}else{
						
						reward = new ItemStack(Integer.parseInt(sp[0]), Integer.parseInt(sp[1]));
						
					}
					
					for(int i = 1 ; i < sp.length ; i++){
						
						if(sp[i].contains(":")){
							
							String[] data = sp[i].split(":");
							
							if(data[0].equalsIgnoreCase("protection") || data[0].equalsIgnoreCase("prot")){
								
								reward.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, Integer.parseInt(data[1]));
																	
							}else if(data[0].equalsIgnoreCase("fireprotection") || data[0].equalsIgnoreCase("fireprot")){
								
								reward.addEnchantment(Enchantment.PROTECTION_FIRE, Integer.parseInt(data[1]));
							
							}else if(data[0].equalsIgnoreCase("featherfalling")){
								
								reward.addEnchantment(Enchantment.PROTECTION_FALL, Integer.parseInt(data[1]));
							
							}else if(data[0].equalsIgnoreCase("blastprotection") || data[0].equalsIgnoreCase("blastprot")){
								
								reward.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, Integer.parseInt(data[1]));
							
							}else if(data[0].equalsIgnoreCase("projectionprotection") || data[0].equalsIgnoreCase("projectileprot")){
								
								reward.addEnchantment(Enchantment.PROTECTION_PROJECTILE, Integer.parseInt(data[1]));
							
							}else if(data[0].equalsIgnoreCase("aquainfinity")){
								
								reward.addEnchantment(Enchantment.OXYGEN, Integer.parseInt(data[1]));
							
							}else if(data[0].equalsIgnoreCase("thorns")){
								
								reward.addEnchantment(Enchantment.THORNS, Integer.parseInt(data[1]));
								
							}else if(data[0].equalsIgnoreCase("sharpness")){
								
								reward.addEnchantment(Enchantment.DAMAGE_ALL, Integer.parseInt(data[1]));
								
							}else if(data[0].equalsIgnoreCase("smite")){
								
								reward.addEnchantment(Enchantment.DAMAGE_UNDEAD, Integer.parseInt(data[1]));
							
							}else if(data[0].equalsIgnoreCase("baneofarthropods") || data[0].equalsIgnoreCase("boa")){
								
								reward.addEnchantment(Enchantment.DAMAGE_ARTHROPODS, Integer.parseInt(data[1]));
							
							}else if(data[0].equalsIgnoreCase("knockback")){
								
								reward.addEnchantment(Enchantment.KNOCKBACK, Integer.parseInt(data[1]));
								
							}else if(data[0].equalsIgnoreCase("looting")){
								
								reward.addEnchantment(Enchantment.LOOT_BONUS_MOBS, Integer.parseInt(data[1]));
								
							}else if(data[0].equalsIgnoreCase("fireaspect")){
								
								reward.addEnchantment(Enchantment.FIRE_ASPECT, Integer.parseInt(data[1]));
								
							}else if(data[0].equalsIgnoreCase("efficiency")){
								
								reward.addEnchantment(Enchantment.DIG_SPEED, Integer.parseInt(data[1]));
								
							}else if(data[0].equalsIgnoreCase("silktouch")){
								
								reward.addEnchantment(Enchantment.SILK_TOUCH, Integer.parseInt(data[1]));
							
							}else if(data[0].equalsIgnoreCase("unbreaking")){
								
								reward.addEnchantment(Enchantment.DURABILITY, Integer.parseInt(data[1]));
								
							}else if(data[0].equalsIgnoreCase("fortune")){
								
								reward.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, Integer.parseInt(data[1]));
								
							}else if(data[0].equalsIgnoreCase("power")){
								
								reward.addEnchantment(Enchantment.ARROW_DAMAGE, Integer.parseInt(data[1]));
								
							}else if(data[0].equalsIgnoreCase("punch")){
								
								reward.addEnchantment(Enchantment.ARROW_KNOCKBACK, Integer.parseInt(data[1]));
								
							}else if(data[0].equalsIgnoreCase("infinity")){
								
								reward.addEnchantment(Enchantment.ARROW_INFINITE, Integer.parseInt(data[1]));
								
							}else if(data[0].equalsIgnoreCase("luckofthesea")){
								
								reward.addEnchantment(Enchantment.LUCK, Integer.parseInt(data[1]));
								
							}else if(data[0].equalsIgnoreCase("lure")){
								
								reward.addEnchantment(Enchantment.LURE, Integer.parseInt(data[1]));
								
							}else if(data[0].equalsIgnoreCase("name")){
								
								ItemMeta im = reward.getItemMeta();
								im.setDisplayName(this.replaceVariables(data[1], killer, victim).replaceAll("_", " "));
								
								reward.setItemMeta(im);

							}else if(data[0].equalsIgnoreCase("lore")){
								

								List<String> loreList = new ArrayList<String>();
								
								if(data[1].contains(";")){
									
									String[] blah = data[1].split(";");
									
									for(String s : blah){
										
										loreList.add(this.replaceVariables(s, killer, victim).replaceAll("_", " "));
										
									}
									
								}else{
									
									loreList.add(this.replaceVariables(data[1], killer, victim).replaceAll("_", " "));
									
								}
								
								ItemMeta im = reward.getItemMeta();
								
								im.setLore(loreList);
								
								reward.setItemMeta(im);
								
								
							}
						}
					}
					
					if(killer.getInventory().firstEmpty() == -1){
						
						String msg = Lang.Full_Inventory_Message.toString();
						
						killer.getWorld().dropItem(killer.getLocation(), reward);
						
						if(!(msg.equalsIgnoreCase("disable"))){
							
							killer.sendMessage(Lang.Prefix + this.replaceVariables(Lang.Full_Inventory_Message.toString(), killer, victim));
							
						}
					
					}else{
						
						killer.getInventory().addItem(reward);
						killer.updateInventory();
					
					}
					
				}else{

					ItemStack reward;
					
					if(sp[0].contains(":")){

						reward = new ItemStack(Integer.parseInt(sp[0].split(":")[0]), Integer.parseInt(sp[1]), (short) Integer.parseInt(sp[0].split(":")[1]));
					
					}else{
						
						reward = new ItemStack(Integer.parseInt(sp[0]), Integer.parseInt(sp[1]));
						
					}

					if(killer.getInventory().firstEmpty() == -1){
						
						String msg = Lang.Full_Inventory_Message.toString();
						
						killer.getWorld().dropItem(killer.getLocation(), reward);
						
						if(!(msg.equalsIgnoreCase("disable"))){
							
							killer.sendMessage(Lang.Prefix + this.replaceVariables(Lang.Full_Inventory_Message.toString(), killer, victim));
							
						}
					
					}else{
						
						killer.getInventory().addItem(reward);
						killer.updateInventory();
					
					}
					
				}
				
			} //command
			
		} 
		
	}
	
	//TODO: rewards need to be tested.
	@SuppressWarnings("deprecation")
	public void HandleRewards(Player killer, Player victim){

	try{
		
		File file = new File(this.dataFolder + File.separator + "rewards.yml");
		
		YamlConfiguration rConfig = YamlConfiguration.loadConfiguration(file);
		
		String theStreak = Integer.toString(this.getStreak(killer));

		if(rConfig.getStringList("Rewards." + theStreak) != null || rConfig.getStringList("Rewards.all") != null){ 
	
			List<String> theList = new ArrayList<String>();
	
			if(rConfig.getStringList("Rewards.all") != null){
				
				theList = rConfig.getStringList("Rewards.all");
				
				this.giveRewards(killer, victim, theList);
				
			}
			
			if(rConfig.getStringList("Rewards." + theStreak) != null){
				
				theList = rConfig.getStringList("Rewards." + theStreak);
				
				this.giveRewards(killer, victim, theList);
				
			}

			
		}
		
	}catch(Exception ex){
		
		ex.printStackTrace();
		
	}
		
		
	}
	
	public void HandleBroadcast(Player killer, Player victim){
		
		ConfigurationSection streaks = plugin.getConfig().getConfigurationSection("Streaks_To_Broadcast"); 

		try{
			for(String streak : streaks.getKeys(false)) {
				
				int myStreak = Integer.parseInt(streak);
				int killStreak = this.getStreak(killer);
				
				if(myStreak == killStreak){
					
					String blah = Integer.toString(myStreak);
					
					String msg = replaceVariables(streaks.getString(blah), killer, victim);

					if(seperateWorld(killer.getWorld().getName())){
						
						for(Player p : plugin.getServer().getOnlinePlayers()){
							
							if(p.getWorld().getName().equalsIgnoreCase(killer.getWorld().getName())){
								
								p.sendMessage(msg);
								
							}
						}
						
					}else{
						
						for(Player p : plugin.getServer().getOnlinePlayers()){
							
							String worldName = p.getWorld().getName();
							
							if(!(worldDisabled(worldName)) && !seperateWorld(worldName)){
							
								p.sendMessage(msg);
								
							}
						}
						
					}
									
				}
		
			}
		}catch(Exception e){
			
			
			
		}
		
		
	}
	
	
	public String playerInfo(Player targetPlayer){
		//TODO: implement top streak.
		int kills = this.getKills(targetPlayer);
		int deaths = this.getDeaths(targetPlayer);
		int streak = this.getStreak(targetPlayer);
		double ratio = this.getRatio(targetPlayer);
		
		String blah = Lang.Stat_Display_Seperator.toString() + "\n";
		blah = blah + Lang.Stat_Display_Kills.toString() + kills + "\n";
		blah = blah + Lang.Stat_Display_Deaths.toString() + deaths + "\n";
		blah = blah + Lang.Stat_Display_KillStreak.toString() + streak +"\n";
		blah = blah + Lang.Stat_Display_Ratio.toString() + ratio + "\n";
		blah = blah + Lang.Stat_Display_Seperator.toString();
		
		return this.replaceVariables(blah, targetPlayer, targetPlayer);
		
	}
	
	public void updateFiles(){
		
		File folder = new File(plugin.getDataFolder() + File.separator + "old_data_files");
		
		if(!(folder.exists())){
			
			folder.mkdir();
			
		}
		
    	String path = dataFolder + File.separator + "old_data_files" + File.separator + "players.yml";
   	 
 		File oldFile = new File(dataFolder + File.separator + "players.yml");
 		File newFile = new File(path);
 		
		boolean success = oldFile.renameTo(newFile);
		  
		plugin.pluginEnabled = true;
		
		if(!success){

			newFile.delete();
			
			oldFile.renameTo(newFile);
			
			plugin.pluginEnabled = true;

		}
		
	}
	
	public boolean isWorldFolder(String worldName){
		
		File worldFolder = new File(worldContainer, worldName);
		
		return worldFolder.exists();
		
	}
    
    public Scoreboard scoreboardInfo(Player targetPlayer){
    	
    	//TODO: implement top streak
		int lngKills = this.getKills(targetPlayer);
		int lngDeaths = this.getDeaths(targetPlayer);
		int lngKS = this.getStreak(targetPlayer);

		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getNewScoreboard();
		Objective objective = board.registerNewObjective("ks", "dummy");

		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(this.replaceVariables(Lang.Scoreboard_Title.toString(), targetPlayer, targetPlayer));
		
		
		Score kills = objective.getScore(Lang.Scoreboard_Kills.toString());
		Score deaths = objective.getScore(Lang.Scoreboard_Deaths.toString());
		Score streak = objective.getScore(Lang.Scoreboard_KillStreak.toString());

		streak.setScore(lngKS);
		kills.setScore(lngKills);
		deaths.setScore(lngDeaths);

		return board;
    	
    }
    
    public Scoreboard scoreboardRanking(int killsRank, int deathsRank, int streakRank, int ratioRank){

		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getNewScoreboard();
		Objective objective = board.registerNewObjective("ks", "dummy");

		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(Lang.Rank_Scoreboard_Header.toString());

		Score kills = objective.getScore(Lang.Scoreboard_Kills.toString());
		Score deaths = objective.getScore(Lang.Scoreboard_Deaths.toString());
		Score streak = objective.getScore(Lang.Scoreboard_KillStreak.toString());
		Score ratio = objective.getScore(Lang.Scoreboard_Ratio.toString());
		
		kills.setScore(killsRank);
		deaths.setScore(deathsRank);
		streak.setScore(streakRank);
		ratio.setScore(ratioRank);

		return board;
    	
    }

    public boolean scoreboardEnabled(Player player){
    		
    		String worldName = player.getWorld().getName();
    		
    		if(plugin.allowScoreboard == false){
    			
    			return false;
    			
    		}
    		
    		if(seperateWorld(worldName)){
    			
    			if(plugin.sqlInfo.containsKey(worldName + ":" + player.getName())){
    				
    				return plugin.sqlInfo.get(worldName + ":" + player.getName()).getScoreboardEnabled();
    				
    			}else{
    				
    				return false;
    				
    			}

    		}else{
    			
    			if(plugin.sqlInfo.containsKey(player.getName())){
    				
    				return plugin.sqlInfo.get(player.getName()).getScoreboardEnabled();
    				
    			}else{
    				
    				return false;
    				
    			}
    			
    			
    		}

    }
    
    public void setScoreboardEnabled(Player player, boolean yesno){

    		
			String worldName = player.getWorld().getName();
			
			if(seperateWorld(worldName)){
				
				plugin.sqlInfo.get(worldName + ":" + player.getName()).setScoreboardEnabled(yesno);
			
			}else{
				
				plugin.sqlInfo.get(player.getName()).setScoreboardEnabled(yesno);
				
			}
			
			return;

    	    	
    }
    
    public int getRatioRank(Player player){
    	
    	String worldName = player.getWorld().getName();
    	
    	if(seperateWorld(worldName)){
    		
    		List<String> sortedList = new ArrayList<String>();
    		
    		for(String blah : plugin.topRatio){
    			
    			String[] sp = blah.split(":");
    			
    			if(sp.length == 3){
    				
    				if(sp[0].equalsIgnoreCase(worldName)){
    					
    					sortedList.add(blah);
    					
    				}
    			}
    			
    		}
    		
    		try{
    			
    			double playerRatio = this.ratioMap.get(worldName + ":" + player.getName());
        		
        		return sortedList.indexOf(worldName + ":" + player.getName() + ":" + playerRatio) + 1;
        		
    		}catch(Exception ex){
    			
    			return 0;
    			
    		}
    		
    	}else{
    		
    		List<String> mainList = new ArrayList<String>();
    		
    		for(String blah : plugin.topRatio){
    			
    			String[] sp = blah.split(":");
    			
    			if(sp.length == 2){
    				
    				mainList.add(blah);
    				
    			}
    			
    		}
    		
        	try{
        		
        		double playerRatio = this.ratioMap.get(player.getName());
            	
            	return mainList.indexOf(player.getName() + ":" + playerRatio) + 1;
            	
        	}catch(Exception ex){
        		
        		return 0;
        		
        	}
    		
    	}
	
    }
    
    public int getStreakRank(Player player){
    	
    	String worldName = player.getWorld().getName();
    	
    	if(seperateWorld(worldName)){
    		
    		List<String> sortedList = new ArrayList<String>();
    		
    		for(String blah : plugin.topStreak){
    			
    			String[] sp = blah.split(":");
    			
    			if(sp.length == 3){
    				
    				if(sp[0].equalsIgnoreCase(worldName)){
    					
    					sortedList.add(blah);
    					
    				}
    			}
    			
    		}
    		
    		try{
    			
    			int playerStreak = this.streakMap.get(worldName + ":" + player.getName());
        		
        		return sortedList.indexOf(worldName + ":" + player.getName() + ":" + playerStreak) + 1;
        	
    		}catch(Exception ex){
    			
    			return 0;
    			
    		}
    		
    	}else{
    		
    		List<String> mainList = new ArrayList<String>();
    		
    		for(String blah : plugin.topStreak){
    			
    			String[] sp = blah.split(":");
    			
    			if(sp.length == 2){
    				
    				mainList.add(blah);
    				
    			}
    			
    		}
    		
    		
        	try{
        		
        		int playerStreak = this.streakMap.get(player.getName());
            	
            	return mainList.indexOf(player.getName() + ":" + playerStreak) + 1;
            	
        	}catch(Exception ex){
        		
        		return 0;
        		
        	}
    		
    	}
	
    }
    
    public int getDeathsRank(Player player){
    	
    	String worldName = player.getWorld().getName();
    	
    	if(seperateWorld(worldName)){
    		
    		List<String> sortedList = new ArrayList<String>();
    		
    		for(String blah : plugin.topDeaths){
    			
    			String[] sp = blah.split(":");
    			
    			if(sp.length == 3){
    				
    				if(sp[0].equalsIgnoreCase(worldName)){
    					
    					sortedList.add(blah);
    					
    				}
    			}
    			
    		}
    		
    		try{
    			
    			int playerDeaths = this.deathsMap.get(worldName + ":" + player.getName());
        		
        		return sortedList.indexOf(worldName + ":" + player.getName() + ":" + playerDeaths) + 1;
        		
    		}catch(Exception ex){
    			
    			return 0;
    			
    		}
    		
    	}else{
    		
    		List<String> mainList = new ArrayList<String>();
    		
    		for(String blah : plugin.topDeaths){
    			
    			String[] sp = blah.split(":");
    			
    			if(sp.length == 2){
    				
    				mainList.add(blah);
    				
    			}
    			
    		}
    		
    		
        	try{
        		
        		int playerDeaths = this.deathsMap.get(player.getName());
            	
            	return mainList.indexOf(player.getName() + ":" + playerDeaths) + 1;
            	
        	}catch(Exception ex){
        		
        		return 0;
        		
        	}
    		
    	}
	
    }
    
    public String getRank_Number(String worldName, String theType, String theRank){

    	int rank = Integer.parseInt(theRank);
    	
    	List<String> theList = new ArrayList<String>();
    	
    	if(theType.equalsIgnoreCase("kills")){
    		
    		theList = plugin.topKills;
    		
    	}else if(theType.equalsIgnoreCase("deaths")){
    		
    		theList = plugin.topDeaths;
    		
    	}else if(theType.equalsIgnoreCase("streak")){
    		
    		theList = plugin.topStreak;
    		
    	}else if(theType.equalsIgnoreCase("ratio")){
    		
    		theList = plugin.topRatio;
    		
    	}else{
    		
    		theList = plugin.topKills;
    	}
    	
    	if(seperateWorld(worldName)){
    		
    		List<String> sortedList = new ArrayList<String>();
    		
    		for(String blah : theList){
    			
    			String[] sp = blah.split(":");
    			
    			if(sp.length == 3){
    				
    				if(sp[0].equalsIgnoreCase(worldName)){
    					
    					sortedList.add(blah);
    					
    				}
    			}
    			
    		}
    		
    		try{

    			String blah = sortedList.get(rank - 1);
    			blah = blah.split(":")[2];
    			
    			return blah;
    			
    		}catch(Exception ex){
    			
    			return "---";
    			
    		}
    		
    	}else{

    		List<String> mainList = new ArrayList<String>();
    		
    		for(String blah : theList){
    			
    			String[] sp = blah.split(":");
    			
    			if(sp.length == 2){
    				
    				mainList.add(blah);
    				
    			}
    			
    		}
    		
    		try{
    			
            	String blah = mainList.get(rank - 1);
            	blah = blah.split(":")[1];
            	return blah;
            	
    			
    		}catch(Exception ex){
    			
    			return "---";
    			
    		}
    		
    			
    	}
	
    }
    
    public String getRank_Name(String worldName, String theType, String theRank){

    	int rank = Integer.parseInt(theRank);
    	
    	List<String> theList = new ArrayList<String>();
    	
    	if(theType.equalsIgnoreCase("kills")){
    		
    		theList = plugin.topKills;
    		
    	}else if(theType.equalsIgnoreCase("deaths")){
    		
    		theList = plugin.topDeaths;
    		
    	}else if(theType.equalsIgnoreCase("streak")){
    		
    		theList = plugin.topStreak;
    		
    	}else if(theType.equalsIgnoreCase("ratio")){
    		
    		theList = plugin.topRatio;
    		
    	}else{
    		
    		theList = plugin.topKills;
    	}
    	
    	if(seperateWorld(worldName)){
    		
    		List<String> sortedList = new ArrayList<String>();
    		
    		for(String blah : theList){
    			
    			String[] sp = blah.split(":");
    			
    			if(sp.length == 3){
    				
    				if(sp[0].equalsIgnoreCase(worldName)){
    					
    					sortedList.add(blah);
    					
    				}
    			}
    			
    		}
    		
    		try{

    			String blah = sortedList.get(rank - 1);
    			blah = blah.split(":")[1];
    			
    			return blah;
    			
    		}catch(Exception ex){
    			
    			return "---";
    			
    		}
    		
    	}else{

    		List<String> mainList = new ArrayList<String>();
    		
    		for(String blah : theList){
    			
    			String[] sp = blah.split(":");
    			
    			if(sp.length == 2){
    				
    				mainList.add(blah);
    				
    			}
    			
    		}
    		
    		try{
    			
            	String blah = mainList.get(rank - 1);
            	blah = blah.split(":")[0];
            	return blah;
            	
    			
    		}catch(Exception ex){
    			
    			return "---";
    			
    		}
    		
    			
    	}
	
    }
    
    
    public int getKillsRank(Player player){

    	String worldName = player.getWorld().getName();
    	 
    	if(seperateWorld(worldName)){
    		
    		List<String> sortedList = new ArrayList<String>();
    		
    		for(String blah : plugin.topKills){
    			
    			String[] sp = blah.split(":");
    			
    			if(sp.length == 3){
    				
    				if(sp[0].equalsIgnoreCase(worldName)){
    					
    					sortedList.add(blah);
    					
    				}
    			}
    			
    		}
    		
    		try{
    			
        		int playerKills = this.killsMap.get(worldName + ":" + player.getName());
        		
        		return sortedList.indexOf(worldName + ":" + player.getName() + ":" + playerKills) + 1;
    		
    		}catch(Exception ex){
    			
    			return 0;
    			
    		}
    		
    	}else{
    		
    		List<String> mainList = new ArrayList<String>();
    		
    		for(String blah : plugin.topKills){
    			
    			String[] sp = blah.split(":");
    			
    			if(sp.length == 2){
    				
    				mainList.add(blah);
    				
    			}
    			
    		}
    		
    		try{
    			
            	int playerKills = this.killsMap.get(player.getName());
            	
            	return mainList.indexOf(player.getName() + ":" + playerKills) + 1;
            	
    			
    		}catch(Exception ex){
    			
    			return 0;
    			
    		}
    		
    	}
	
    }
    
    public String pluginInfo(){
    	
    	String blah;
    	
    	blah = ChatColor.AQUA + "/ks " + Lang.Help_Personal.toString() + "\n";
    	blah = blah + ChatColor.AQUA + "/ks [playername] " + Lang.Help_Other.toString() + "\n";
    	blah = blah + ChatColor.AQUA + "/ks scoreboard " + Lang.Help_Scoreboard.toString() + "\n";
    	blah = blah + ChatColor.AQUA + "/ks startover " + Lang.Help_Startover.toString() + "\n";
    	blah = blah + ChatColor.AQUA + "/ks rank " + Lang.Help_Rank.toString() + "\n";
    	blah = blah + ChatColor.AQUA + "/ks top[kills|deaths|streak|ratio] [pagenumber]" + Lang.Help_Top.toString() + "\n";
    	blah = blah + Lang.Help_Admin.toString() + "\n";
    	
    	return blah;
    	
    }
    
    public String adminInfo(){
    	
    	String blah;
    	
    	blah = ChatColor.AQUA + "/ksa reset [playername] " + Lang.Help_Reset.toString() + "\n";
    	blah = blah + ChatColor.AQUA + "/ks head [playername] " + Lang.Help_Head.toString() + "\n";
    	blah = blah + ChatColor.AQUA + "/ks reload " + Lang.Help_Reload.toString() + "\n";
    	blah = blah + ChatColor.AQUA + "/ks enable " + Lang.Help_Enable.toString() + "\n";
    	blah = blah + ChatColor.AQUA + "/ks disable " + Lang.Help_Disable.toString() + "\n";
    	
    	return blah;
    	
    }
    
    
    public ItemStack playerHead(String targetPlayer){
    	
		Player tPlayer = plugin.getServer().getPlayer(targetPlayer.toString());

		if (tPlayer == null) {

			ItemStack i = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);

			SkullMeta meta = (SkullMeta) i.getItemMeta();

			meta.setOwner(targetPlayer.toString());

			i.setItemMeta(meta);
			
			return i;

		} else {

			ItemStack i = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);

			SkullMeta meta = (SkullMeta) i.getItemMeta();

			meta.setOwner(tPlayer.getName());

			i.setItemMeta(meta);
			
			return i;

		}
		
    }
    
    public void populateTopLists(){

    	Map<String, Integer> kMap;
    	Map<String, Integer> dMap;
    	Map<String, Integer> sMap;
    	Map<String, Double> rMap;
    	
    	plugin.topKills.clear();
    	plugin.topDeaths.clear();
    	plugin.topStreak.clear();
    	plugin.topRatio.clear();
    		
    	kMap = this.sortByValue(killsMap);
    	dMap = this.sortByValue(deathsMap);
    	sMap = this.sortByValue(streakMap);
    	rMap = this.sortByDouble(ratioMap);
        
        for(int i=0 ; i<kMap.size(); i++){
        	 
        	plugin.topKills.add(kMap.keySet().toArray()[i].toString() + ":" + kMap.values().toArray()[i].toString());

        }
        
        for(int i=0 ; i<dMap.size(); i++){
       	 
        	plugin.topDeaths.add(dMap.keySet().toArray()[i].toString() + ":" + dMap.values().toArray()[i].toString());
        	
        }
        
        for(int i=0 ; i<sMap.size(); i++){
       	 
        	plugin.topStreak.add(sMap.keySet().toArray()[i].toString() + ":" + sMap.values().toArray()[i].toString());
        	
        }
        
        for(int i=0 ; i<rMap.size(); i++){
       	 
        	plugin.topRatio.add(rMap.keySet().toArray()[i].toString() + ":" + rMap.values().toArray()[i].toString());

        }
        
        plugin.lastUpdate = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis());
        
        plugin.getServer().getScheduler().runTask(plugin, updateLBSigns);

    }
    
   
    
    public String replaceStatsVariables(Integer position, String playerName, String playerData){
    	
    	String originalFormat = Lang.TS_Chat_Format.toString();
    	
    	String newFormat = null;
    	
    	newFormat = originalFormat.replaceAll("%stats.playername%", playerName);
    	newFormat = newFormat.replaceAll("%stats.value%", playerData);
    	newFormat = newFormat.replaceAll("%stats.position%", Integer.toString(position));    	
    	
    	return newFormat;
    	    	
    }

    
    public void checkDataFiles(){
    	
    	final double configVersion = plugin.getConfig().getDouble("configVersion");
    	String cv = plugin.getDescription().getVersion();
    	final double currentVersion = Double.valueOf(cv);
    	final List<String> l = new ArrayList<String>();
    	
    	
    	plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){ 
    		
    		 @Override
 	        public void run() {

    			 l.add("****** KillStats ******");
             	
         		if(configVersion != currentVersion){
         			
         			File folder = new File(dataFolder + File.separator + "old_data_files");
         			
         			if(!(folder.exists())){
         				
         				folder.mkdir();
         				
         			}
         			
         			
         			l.add("Updating config.yml");

         			
         			File oldConfig = new File(dataFolder + File.separator + "config.yml");
         			File newName = new File(dataFolder + File.separator + "old_data_files" + File.separator + "config.yml");
         			
         			try{
         				
         				boolean success = oldConfig.renameTo(newName);
             			
             			if(success){
             				
             				plugin.saveDefaultConfig();
             				
             			}else{
             				
             				newName.delete();
             				
             				oldConfig.renameTo(newName);
             				
             				plugin.saveDefaultConfig();

             			}
         				
         			}catch(Exception ex){
         				
         				ex.printStackTrace();
         				         				
         			}
         			
         			File playersFile = new File(dataFolder + File.separator + "players.yml");
         			
         			if(playersFile.exists()){
         				
         				l.add("Moving players.yml to 'old_data_files'");

         				updateFiles();
         				
         				plugin.pluginEnabled = false;

         			}
         						
         		}else{
         			
         			File playersFile = new File(dataFolder + File.separator + "players.yml");
         			
         			if(playersFile.exists()){
         				
         				l.add("Moving players.yml to 'old_data_files'");
         				
         				updateFiles();
         				
         				plugin.pluginEnabled = false;
         				
         			}else{
         				
         				l.add("All files up to date. Enjoy!");
         				
         			}
         			
         			
         		}
 	        	
         		l.add("***********************");
         		
         		for(String a : l){
         			
         			log.info(a);
         			
         		}
         		
 	        }
    		 
    		 
 	        
 	    });
    	
    }
    	
    
    public boolean tableHasData(String tableName){
    
    	try{
    		
    		PreparedStatement ps = plugin.sqlConnection.prepareStatement("SELECT * FROM `" + tableName + "`");
    		
    		ResultSet rs = ps.executeQuery();
    		boolean toReturn = rs.next();
    		
    		ps.close();
    		rs.close();
    		
    		return toReturn;

    	}catch(Exception ex){

    		ex.printStackTrace();
    		
    		return false;
    	}
    	
    }

    public void populateSQLKillsMap(){

    	int theLimit = plugin.getConfig().getInt("Top_Stats.Limit", 1000);
    	
		  Iterator<String> wrlds = plugin.sqlWorlds.iterator();
		  
		  while(wrlds.hasNext()){
			  
			  String blah = wrlds.next();
    		
    		if(tableHasData("killstats_data_" + blah)){
    			
        		try{
        			
            		PreparedStatement ps = plugin.sqlConnection.prepareStatement("SELECT * FROM `killstats_data_" + blah + "` ORDER BY `kills` DESC LIMIT " + theLimit);
                	
            		ResultSet rs = ps.executeQuery();

                		while(rs.next()){
                			
                			String playerName = rs.getString("playerName");
                			int kills = rs.getInt("kills");

                			killsMap.put(blah + ":" + playerName, kills);
                			
                		}

                		ps.close();
                		rs.close();
                		
                		
        		}catch(Exception ex){
        			
        			ex.printStackTrace();
        			
        			return;
        			
        		}
    			
        		
    		}
    		
    	}
    	
    	
    	if(tableHasData("killstats_data")){
    		
    		try{
    			
        		PreparedStatement ps = plugin.sqlConnection.prepareStatement("SELECT * FROM `killstats_data` ORDER BY `kills` DESC LIMIT " + theLimit);
            	
        		ResultSet rs = ps.executeQuery();

            		while(rs.next()){
            			
            			String playerName = rs.getString("playerName");
            			int kills = rs.getInt("kills");
            			
            			killsMap.put(playerName, kills);
            			
            		}

            		
            		ps.close();
            		rs.close();
            		
    		}catch(Exception ex){
    			
    			ex.printStackTrace();
    			
    			return;
    			
    		}
    	}
    	
    }
    
    public void broadcast(String theMSG){
    	
    	plugin.getServer().broadcastMessage(theMSG);
    	
    }
    
    public void populateSQLDeathsMap(){
 	
    	int theLimit = plugin.getConfig().getInt("Top_Stats.Limit", 1000);
    	
		  Iterator<String> wrlds = plugin.sqlWorlds.iterator();
		  
		  while(wrlds.hasNext()){
			  
		String blah = wrlds.next();
    		
    		if(tableHasData("killstats_data_" + blah)){
    			
        		try{
        			
            		PreparedStatement ps = plugin.sqlConnection.prepareStatement("SELECT * FROM `killstats_data_" + blah + "` ORDER BY `deaths` DESC LIMIT " + theLimit);
                	
            		ResultSet rs = ps.executeQuery();

            			while(rs.next()){
                			
                			String playerName = rs.getString("playerName");
                			int deaths = rs.getInt("deaths");

                			deathsMap.put(blah + ":" + playerName, deaths);
                			
                		}
            		
                		ps.close();
                		rs.close();
                		
        		}catch(Exception ex){

        		ex.printStackTrace();
        			
        			return;
        			
        		}
    			
    		}
    		
    	}
    	
    	if(tableHasData("killstats_data")){
    		
    		try{
    			
        		PreparedStatement ps = plugin.sqlConnection.prepareStatement("SELECT * FROM `killstats_data` ORDER BY `deaths` DESC LIMIT " + theLimit);
            	
        		ResultSet rs = ps.executeQuery();

        			while(rs.next()){
        				
            			String playerName = rs.getString("playerName");
            			int deaths = rs.getInt("deaths");
            			
            			deathsMap.put(playerName, deaths);
            			
            		}

            		ps.close();
            		rs.close();
        		
    		}catch(Exception ex){
    			
    			ex.printStackTrace();
    			
    			return;
    			
    		}
    	}
    	
    }
    
    public void populateSQLStreakMap(){
  	
    	int theLimit = plugin.getConfig().getInt("Top_Stats.Limit", 1000);
    	
		  Iterator<String> wrlds = plugin.sqlWorlds.iterator();
		  
		  while(wrlds.hasNext()){
			  
			  String blah = wrlds.next();
    		
    		if(tableHasData("killstats_data_" + blah)){
    			
        		try{
        			
            		PreparedStatement ps = plugin.sqlConnection.prepareStatement("SELECT * FROM `killstats_data_" + blah + "` ORDER BY `streak` DESC LIMIT " + theLimit);
                	
            		ResultSet rs = ps.executeQuery();

            			while(rs.next()){
            				
                			String playerName = rs.getString("playerName");
                			int streak = rs.getInt("streak");
                			
                			streakMap.put(blah + ":" + playerName, streak);
                			
                		}
            		
            		
                		ps.close();
                		rs.close();
                		
        		}catch(Exception ex){
        			
        			ex.printStackTrace();
        			
        			return;
        			
        		}
    			
    		}
    		
    	}
    	
    	if(tableHasData("killstats_data")){
    		
    		try{
    			
        		PreparedStatement ps = plugin.sqlConnection.prepareStatement("SELECT * FROM `killstats_data` ORDER BY `streak` DESC LIMIT " + theLimit);
            	
        		ResultSet rs = ps.executeQuery();
        		
        			while(rs.next()){
        				
            			String playerName = rs.getString("playerName");
            			int streak = rs.getInt("streak");
            			
            			streakMap.put(playerName, streak);
            			
            		}

        			ps.close();
            		rs.close();
        		
    		}catch(Exception ex){
    			
    			ex.printStackTrace();
    			
    			return;
    			
    		}
    	}
    	
    }
    
    public void populateSQLRatioMap(){
    	
    	int theLimit = plugin.getConfig().getInt("Top_Stats.Limit", 1000);
    	
		  Iterator<String> wrlds = plugin.sqlWorlds.iterator();
		  
		  while(wrlds.hasNext()){
			  
			  String blah = wrlds.next();
    		
    		if(tableHasData("killstats_data_" + blah)){
    			
        		try{
        			
            		PreparedStatement ps = plugin.sqlConnection.prepareStatement("SELECT * FROM `killstats_data_" + blah + "` ORDER BY `ratio` DESC LIMIT " + theLimit);
                	
            		ResultSet rs = ps.executeQuery();
            		
            			while(rs.next()){
            				
                			String playerName = rs.getString("playerName");
                			double ratio = rs.getDouble("ratio");
                			
                			ratioMap.put(blah + ":" + playerName, ratio);
                			
                		}

                		ps.close();
                		rs.close();
                		
        		}catch(Exception ex){
        			
        			ex.printStackTrace();
        			
        			return;
        			
        		}
    			
    		}
    		
    	}
    	
    	if(tableHasData("killstats_data")){
    		
    		try{
    			
        		PreparedStatement ps = plugin.sqlConnection.prepareStatement("SELECT * FROM `killstats_data` ORDER BY `ratio` DESC LIMIT " + theLimit);
            	
        		ResultSet rs = ps.executeQuery();

        			while(rs.next()){
            			
            			String playerName = rs.getString("playerName");
            			double ratio = rs.getDouble("ratio");
            			
            			ratioMap.put(playerName, ratio);
            			
            		}

            		ps.close();
            		rs.close();
            		
    		}catch(Exception ex){
    			
    			ex.printStackTrace();
    			
    			return;
    			
    		}
    	}
    	
    }
    
    public void populateLeaderBoards(){

    	for(Player p : plugin.getServer().getOnlinePlayers()){
    		
    		this.saveSQLDataFromHashmap(p);
    		
    	}

    	plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new BukkitRunnable() {

       	 @Override
            public void run() {

            	killsMap.clear();
            	deathsMap.clear();
            	streakMap.clear();
            	ratioMap.clear();
            	
               	if(plugin.sqlConnection == null){

               		MakeSQLConnection(false);
               		
               	}
               	
            		populateSQLDeathsMap();
            		populateSQLStreakMap();
            		populateSQLRatioMap();
            		populateSQLKillsMap();
            		
            		populateTopLists();
            		
                
            }
           
        }, 10, plugin.statsUpdateTime);
        
    	
    }
    
    
    public Runnable updateLBSigns = new BukkitRunnable(){
    
    	@Override
    	public void run(){

    		File f = new File(plugin.getDataFolder() + File.separator + "signs.yml");
    		YamlConfiguration s = YamlConfiguration.loadConfiguration(f);
    		
    		ConfigurationSection signSection = s.getConfigurationSection("Signs");
    		
    		if(!(f.exists())){
    			
    			try {
					f.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
    			
    		}
    		
    		if(signSection == null || !f.exists()){

    			return;
    			
    		}
    		
    		for(String loc : signSection.getKeys(false)){
    			
    			String[] sp = loc.split("_");
    			//Need to change to handle the worl differently, maybe put world into [] or something else.
    			String worldName = sp[0].replaceAll("-", "_");
    			World w = plugin.getServer().getWorld(worldName);
    			
    			int x = Integer.parseInt(sp[1]);
    			int y = Integer.parseInt(sp[2]);
    			int z = Integer.parseInt(sp[3]);
    			Location l = new Location(w, x, y, z);
    			
    			Block b = plugin.getServer().getWorld(worldName).getBlockAt(l);

    			if(b.getState() instanceof Sign){
    				
    				String theType = signSection.getString(loc + ".Type");
    				String theRank = signSection.getString(loc + ".Rank");
    				
    				Sign sign = (Sign) b.getState();
    				
    				if(isRankSign(l)){

    					if(theType.equalsIgnoreCase("kills")){

    						sign.setLine(0, Lang.Sign_Top_Kills.toString().replace("%ranking%", theRank));
    						sign.setLine(1, null);
    						sign.setLine(2, Lang.Sign_Playername_Color.toString() + getRank_Name(worldName, "kills", theRank));
    						sign.setLine(3, Lang.Sign_Stats_Color.toString() + getRank_Number(worldName, "kills", theRank));
    						
    						sign.update();
    						
    					}else if(theType.equalsIgnoreCase("deaths")){
    						
    						sign.setLine(0, Lang.Sign_Top_Deaths.toString().replace("%ranking%", theRank));
    						sign.setLine(1, null);
    						sign.setLine(2, Lang.Sign_Playername_Color.toString() + getRank_Name(worldName, "deaths", theRank));
    						sign.setLine(3, Lang.Sign_Stats_Color.toString() + getRank_Number(worldName, "deaths", theRank));
    						
    						sign.update();
    						
    					}else if(theType.equalsIgnoreCase("streak")){
    						
    						sign.setLine(0, Lang.Sign_Top_Streak.toString().replace("%ranking%", theRank));
    						sign.setLine(1, null);
    						sign.setLine(2, Lang.Sign_Playername_Color.toString() + getRank_Name(worldName, "streak", theRank));
    						sign.setLine(3, Lang.Sign_Stats_Color.toString() + getRank_Number(worldName, "streak", theRank));

    						sign.update();
    						
    					}else if(theType.equalsIgnoreCase("ratio")){
    						
    						sign.setLine(0, Lang.Sign_Top_Ratio.toString().replace("%ranking%", theRank));
    						sign.setLine(1, null);
    						sign.setLine(2, Lang.Sign_Playername_Color.toString() + getRank_Name(worldName, "ratio", theRank));
    						sign.setLine(3, Lang.Sign_Stats_Color.toString() + getRank_Number(worldName, "ratio", theRank));
    						
    						sign.update();
    						
    					}else{
    						
    						b.breakNaturally();
    						
    					}
    					
    				}else{
    					
    					removeSignFromYML(l);

    					return;
    					
    				}
    				
    			}else{
    				
    				removeSignFromYML(l);
    				
    			}
    			
    		}
    		
    		
    	}
    	
    };
    
    public boolean CheckRankSignAttatchedToBlock(Block block){

    	Material type = block.getType();
    	
    	for(BlockFace face: faces){
    		
    		type = block.getRelative(face).getType();
    		
    		if(type == Material.SIGN_POST){
    			
    			Sign sign = (Sign) block.getRelative(face).getState();

    			if(isRankSign(sign.getLocation())){
    				
    				removeSignFromYML(sign.getLocation());
    				
    			}
    			
    		}
    		
    	}
		
    	return false;
    	
    }
    
    public Runnable checkSQLWorlds = new Runnable(){ 

        @Override
        public void run() {

        	if(plugin.sqlConnection == null){
        		
        		MakeSQLConnection(false);
        		        		
        	}      

			  try{
				  
					Statement createMainTable = plugin.sqlConnection.createStatement();
					createMainTable.executeUpdate("CREATE TABLE IF NOT EXISTS `killstats_data` (`playerID` VARCHAR(32), `playerName` VARCHAR(16), `kills` int(11), `deaths` int(11), `streak` int(11), `ratio` DECIMAL(11,2), `ScoreboardEnabled` tinyint(1));");

					createMainTable.close();
					
				  for(String blah : plugin.seperateWorlds){
					  
					 if(!(plugin.sqlWorlds.contains("killstats_data_" + blah)) && isWorldFolder(blah)){
						 
						 try{

							 Statement createWorldTables = plugin.sqlConnection.createStatement();
							 createWorldTables.executeUpdate("CREATE TABLE IF NOT EXISTS `killstats_data_" + blah + "` (`playerID` VARCHAR(32), `playerName` VARCHAR(16), `kills` int(11), `deaths` int(11), `streak` int(11), `ratio` DECIMAL(11,2), `ScoreboardEnabled` tinyint(1));");
							 
							 plugin.sqlWorlds.add(blah);	
							 
							 createWorldTables.close();
							 
						 }catch(Exception ex){
							 
							 ex.printStackTrace();
							 
						 }
						 
					 }
					 
				  }
				  
			  }catch(Exception ex){

				  ex.printStackTrace();

			  }
			  
				if(plugin.getServer().getOnlinePlayers().size() > 0){
					
					for(Player p : plugin.getServer().getOnlinePlayers()){
						
						putSQLDataIntoHashmap(p);
						
					}
				}
        	
        }
       
    };
    
    public void saveSQLDataFromHashmap(Player player){
    	
    	final String playerName = player.getName();
    	final String playerID = player.getUniqueId().toString().replace("-", "");
    	
    	plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			
			  public void run() {

				if(plugin.sqlConnection == null){
					
	        		MakeSQLConnection(false);

				}

				Iterator<String> wrlds = plugin.sqlWorlds.iterator();
				
				while(wrlds.hasNext()){

					String blah = wrlds.next();
					
					int kills = sqlKills(playerName, blah);
					int deaths = sqlDeaths(playerName, blah);
					int streak = sqlStreak(playerName, blah);
					double ratio = sqlRatio(playerName, blah);
					boolean sbEnabled = sqlScoreboardEnabled(playerName, blah);
					
					
					if(playerDataExistsSQL(playerID, "killstats_data_" + blah)){
							
							try{
								
	
								PreparedStatement ps = plugin.sqlConnection.prepareStatement("UPDATE `killstats_data_" + blah + "` SET `kills`=?, `deaths`=?, `streak`=?, `ratio`=?, `ScoreboardEnabled`=?, `playerName`=? WHERE playerID=?;");
								ps.setInt(1, kills);
								ps.setInt(2, deaths);
								ps.setInt(3, streak);
								ps.setDouble(4, ratio);
								ps.setBoolean(5, sbEnabled);
								ps.setString(6, playerName);
								ps.setString(7, playerID);
								
								ps.executeUpdate();
								
								ps.close();
								
							}catch(Exception ex){
								
								ex.printStackTrace();
								
							}
						 
					}else{
						
						try{
							
							
							PreparedStatement ps = plugin.sqlConnection.prepareStatement("INSERT INTO `killstats_data_" + blah + "` (`playerName`, `kills`, `deaths`, `streak`, `ratio`, `playerID`, `ScoreboardEnabled`) VALUES (?,?,?,?,?,?,?);");
							ps.setString(1, playerName);
							ps.setInt(2, kills);
							ps.setInt(3, deaths);
							ps.setInt(4, streak);
							ps.setDouble(5, ratio);
							ps.setString(6, playerID);
							ps.setBoolean(7, sbEnabled);
							
							ps.executeUpdate();
							
							ps.close();
							
						}catch(Exception ex){
							
							ex.printStackTrace();
							
						}
						
					}
					
				}
				
				if(playerDataExistsSQL(playerID, "killstats_data")){

					int kills = sqlKills(playerName, null);
					int deaths = sqlDeaths(playerName, null);
					int streak = sqlStreak(playerName, null);
					double ratio = sqlRatio(playerName, null);
					boolean sbEnabled = sqlScoreboardEnabled(playerName, null);
					
					try{

						PreparedStatement ps = plugin.sqlConnection.prepareStatement("UPDATE `killstats_data` SET `kills`=?, `deaths`=?, `streak`=?, `ratio`=?, `playerName`=?, `ScoreboardEnabled`=? WHERE playerID=?;");
						ps.setInt(1, kills);
						ps.setInt(2, deaths);
						ps.setInt(3, streak);
						ps.setDouble(4, ratio);
						ps.setString(5, playerName);
						ps.setBoolean(6, sbEnabled);
						ps.setString(7, playerID);
						
						ps.executeUpdate();
						
						ps.close();
						
					}catch(Exception ex){
						
						ex.printStackTrace();
						
					}
					
					
				}else{
					
					int kills = sqlKills(playerName, null);
					int deaths = sqlDeaths(playerName, null);
					int streak = sqlStreak(playerName, null);
					double ratio = sqlRatio(playerName, null);
					boolean sbEnabled = sqlScoreboardEnabled(playerName, null);
					
					try{

						PreparedStatement ps = plugin.sqlConnection.prepareStatement("INSERT INTO `killstats_data` (`playerName`, `kills`, `deaths`, `streak`, `ratio`, `playerID`, `ScoreboardEnabled`) VALUES (?,?,?,?,?,?,?);");
						ps.setString(1, playerName);
						ps.setInt(2, kills);
						ps.setInt(3, deaths);
						ps.setInt(4, streak);
						ps.setDouble(5, ratio);
						ps.setString(6, playerID);
						ps.setBoolean(7, sbEnabled);
						
						ps.executeUpdate();
						
						ps.close();
						
					}catch(Exception ex){
						
						ex.printStackTrace();
						
					}
				}
    	
			  }
			  
		});
		
    }
    
    public boolean playerDataExistsSQL(String playerID, final String tableName){
  
			try{
					  
				PreparedStatement ps = plugin.sqlConnection.prepareStatement("SELECT * FROM `" + tableName + "` WHERE playerID=?");
				ps.setString(1, playerID);
				
				ResultSet rs = ps.executeQuery();
				
				boolean toReturn = rs.next();
				
				ps.close();
				rs.close();
				
				return toReturn;
				
			}catch(Exception ex){
					  
				ex.printStackTrace();
				
				return false;
					  
			}
		
    }
    
    public void matchNameToUUID(Player player){

    	final String playerName = player.getName();
    	
    	final String playerID = player.getUniqueId().toString().replace("-", "");
    	
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new BukkitRunnable() {
			
			  public void run() {
				  
				  if(plugin.sqlConnection == null){
					  
					  MakeSQLConnection(false);
					  
				  }
				  
				  try{
					  
					  PreparedStatement ps = plugin.sqlConnection.prepareStatement("SELECT playerName FROM `killstats_data` WHERE playerID=?");
					  ps.setString(1, playerID);
					  
					  ResultSet rs = ps.executeQuery();
					  
					  if(rs.next()){

						  String oldName = rs.getString("playerName");
						  
						  if(oldName != playerName){
	
							  PreparedStatement statement1 = plugin.sqlConnection.prepareStatement("UPDATE `killstats_data` SET `playerName`=? WHERE playerID=?;");
							  statement1.setString(1, playerName);
							  statement1.setString(2, playerID);
							  
							  statement1.close();
							  
							  
							  
						  }
					  
					  }
					  
					  ps.close();
					  rs.close();
					  
					  
				  }catch(Exception ex){
					  
					  ex.printStackTrace();
					  
				  }
				  
				  try{
					  
					  Iterator<String> wrlds = plugin.sqlWorlds.iterator();
					  
					  while(wrlds.hasNext()){
						  
						  String blah = wrlds.next();
						  
						  PreparedStatement ps = plugin.sqlConnection.prepareStatement("SELECT playerName FROM `killstats_data_" + blah + "` WHERE playerID=?");
						  ps.setString(1, playerID);
						  
						  ResultSet rs = ps.executeQuery();
						  
						  
						  if(rs.next()){

							  String oldName = rs.getString("playerName");
							  
							  if(oldName != playerName){
	
								  PreparedStatement statement1 = plugin.sqlConnection.prepareStatement("UPDATE `killstats_data_" + blah + "` SET `playerName`=? WHERE playerID=?;");
								  statement1.setString(1, playerName);
								  statement1.setString(2, playerID);
								  
								  statement1.close();
								  
							  }
							  
						  }
						  
						  ps.close();
						  rs.close();
						  
					  }
					  
				  }catch(Exception ex){
					  
					  ex.printStackTrace();
					  
				  }
				  
			  }
			  
		});
    	
    	
    }
    
    public void putSQLDataIntoHashmap(Player player){
    	
		final String playerName = player.getName();
		final String playerID = player.getUniqueId().toString().replace("-", "");
		
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new BukkitRunnable() {
			
			  public void run() {

				  if(plugin.sqlConnection == null){
					  
					  MakeSQLConnection(false);
					  
				  }
						
					  try{
						  
						  Iterator<String> wrlds = plugin.sqlWorlds.iterator();
						  
						  while(wrlds.hasNext()){
							  
							  String blah = wrlds.next();
							  
							  PreparedStatement ps = plugin.sqlConnection.prepareStatement("SELECT * FROM `killstats_data_" + blah + "` WHERE playerID=?");
							  ps.setString(1, playerID);
							  
							  ResultSet rs = ps.executeQuery();
							  
							  if(!(rs.next())){
								  
								  plugin.sqlInfo.put(blah + ":" + playerName, new PlayerStats(playerName, 0, 0, 0, 0, false));

							  }else{

									  int kills = rs.getInt("kills");
									  int deaths = rs.getInt("deaths");
									  int streak = rs.getInt("streak");
									  double ratio = rs.getDouble("ratio");
									  boolean sbEnabled = rs.getBoolean("ScoreboardEnabled");
									  
									  plugin.sqlInfo.put(blah + ":" + playerName, new PlayerStats(playerName, kills, deaths, streak, ratio, sbEnabled));

							  }

							  ps.close();
							  rs.close();
							  
						  }
						  
						  PreparedStatement ps = plugin.sqlConnection.prepareStatement("SELECT * FROM `killstats_data` WHERE playerID=?");
						  ps.setString(1, playerID);
						  
						  ResultSet rs = ps.executeQuery();
						  
						  if(!(rs.next())){
							  
							  plugin.sqlInfo.put(playerName, new PlayerStats(playerName, 0, 0, 0, 0, false));

						  }else{ 
							  
							  int kills = rs.getInt("kills");
							  int deaths = rs.getInt("deaths");
							  int streak = rs.getInt("streak");
							  double ratio = rs.getDouble("ratio");
							  boolean sbEnabled = rs.getBoolean("ScoreboardEnabled");

							  plugin.sqlInfo.put(playerName, new PlayerStats(playerName, kills, deaths, streak, ratio, sbEnabled));
						  
						  }

						  ps.close();
						  rs.close();
						  
					  }catch(Exception ex){
						  
						  ex.printStackTrace();
						  
					  }

			  }
			  
			});
    	
    }
    
    
	public boolean isInt(String data){
		 
		Pattern p = Pattern.compile("[0-9]*");
		Matcher m = p.matcher(data);
		boolean result = m.matches();
		 
		return result;
		
		}
    
	public long getMaxPage(double listSize){

		double pages = (listSize / 10);
		double a = Math.ceil(pages);
		long maxPage = (long) a;
		
		return maxPage;

	}
	
	public boolean noPlayerToDisplay(List<String> theList, Player player){
		
		List<String> mainList = new ArrayList<String>();
		
		List<String> seperatedList = new ArrayList<String>();
		
		List<String> seperateWorldList = new ArrayList<String>();
				
		
		for(String s : theList){

			String sp[] = s.split(":");
			
			if(sp.length == 3){
				
				seperatedList.add(s);
				
			}else if(sp.length == 2){
				
				mainList.add(s);

			}
			
		}
		
		String worldName = player.getWorld().getName();
		
		if(seperateWorld(worldName)){
			
			for(String a : seperatedList){
				
				String b[] = a.split(":");
				
				if(b[0].equalsIgnoreCase(worldName)){
					
					seperateWorldList.add(a);
					
				}
				
			}
			
			theList = seperateWorldList;
			
			
		}else{
			
			theList = mainList;
			
		}
		
		return theList.isEmpty();
		
	}
	
	public void showTopPlayers(List<String> theList, Player player, Integer thePage){
		
		List<String> mainList = new ArrayList<String>();
		
		List<String> seperatedList = new ArrayList<String>();
		
		List<String> seperateWorldList = new ArrayList<String>();
				
		
		for(String s : theList){
			
			String sp[] = s.split(":");
			
			if(sp.length == 3){
				
				seperatedList.add(s);
				
			}else if(sp.length == 2){
				
				mainList.add(s);

			}
			
		}
		
		if(seperateWorld(player.getWorld().getName())){
			
			for(String a : seperatedList){
				
				String[] b = a.split(":");
				
				if(b[0].equalsIgnoreCase(player.getWorld().getName())){
					
					seperateWorldList.add(a);
					
				}
			}
			
			theList = seperateWorldList;
			
			
		}else{
			
			theList = mainList;
			
		}

		if(thePage > this.getMaxPage(theList.size())){
			
			thePage = (int) this.getMaxPage(theList.size());
			
		}
		
		int fromIndex = (Math.max(thePage, 1) - 1) * 10;
				
		
		for(String blah : theList.subList(Math.min(fromIndex, theList.size()), Math.min(fromIndex + 10, theList.size()))){
			
			String[] playerData = blah.split(":");
			
			int curPosition = theList.indexOf(blah) + 1;
			
			if(seperateWorld(player.getWorld().getName())){
				
				player.sendMessage(this.replaceStatsVariables(curPosition, playerData[1], playerData[2]));
				
			}else{
				
				player.sendMessage(this.replaceStatsVariables(curPosition, playerData[0], playerData[1]));
				
			}
			
			
		}
		
		long maxPage = this.getMaxPage(theList.size());
		
		player.sendMessage(Lang.TS_Chat_Page.toString().replaceAll("%stats.currentpage%", Long.toString(thePage)).replaceAll("%stats.maxpage%", Long.toString(maxPage)));
		
		long lUpdate = (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis()) - plugin.lastUpdate);

		player.sendMessage(Lang.TS_Chat_Last_Update.toString().replaceAll("%stats.lastupdate%", Long.toString(lUpdate)));
		
	}

	public void sbTopPlayers(List<String> theList, final Player player, Integer thePage){

		List<String> mainList = new ArrayList<String>();
		
		List<String> seperatedList = new ArrayList<String>();
		
		List<String> seperateWorldList = new ArrayList<String>();
				
		
		for(String s : theList){
			
			String sp[] = s.split(":");
			
			if(sp.length == 3){
				
				seperatedList.add(s);
				
			}else if(sp.length == 2){
				
				mainList.add(s);

			}
			
		}
		
		if(seperateWorld(player.getWorld().getName())){
			
			for(String a : seperatedList){
				
				String[] b = a.split(":");
				
				if(b[0].equalsIgnoreCase(player.getWorld().getName())){
					
					seperateWorldList.add(a);
					
				}
			}
			
			theList = seperateWorldList;
			
			
		}else{
			
			theList = mainList;
			
		}

		if(thePage > this.getMaxPage(theList.size())){
			
			thePage = (int) this.getMaxPage(theList.size());
			
		}
		
		if(player.getScoreboard().getObjective("ks") != null){
			
			player.sendMessage(Lang.Prefix.toString() + Lang.Scoreboard_Already_Open.toString());
			
			return;
		}
		
		long maxPage = this.getMaxPage(theList.size());
		
		int fromIndex = (Math.max(thePage, 1) - 1) * 10;
		
		if(theList.size() == 0){
			
			player.sendMessage(Lang.Prefix.toString() + Lang.TS_No_Players.toString());
			
			return;
			
		}
	
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getNewScoreboard();
		
		Team team = board.registerNewTeam("killStats");
		team.setDisplayName("killStats Top");
		
		Objective objective = board.registerNewObjective("ks", "dummy");

		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(Lang.TS_Scoreboard_Page.toString().replaceAll("%stats.currentpage%", Integer.toString(thePage)).replaceAll("%stats.maxpage%", Long.toString(maxPage)));

		for(String blah : theList.subList(Math.min(fromIndex, theList.size()), Math.min(fromIndex + 10, theList.size()))){
		
			String[] playerData = blah.split(":");

			OfflinePlayer p;
			
			if(seperateWorld(player.getWorld().getName())){

				p = plugin.getServer().getOfflinePlayer(ChatColor.AQUA + playerData[1]);
				
			}else{
				
				p = plugin.getServer().getOfflinePlayer(ChatColor.AQUA + playerData[0]);
				
			}
			
			if(p.getName().length() > 16){
				
				if(seperateWorld(player.getWorld().getName())){
					
					p = plugin.getServer().getOfflinePlayer(ChatColor.AQUA + playerData[0].substring(0,14));
					
				}else{
					
					p = plugin.getServer().getOfflinePlayer(ChatColor.AQUA + playerData[0].substring(0,14));
				}
				
			}

			team.addPlayer(p);

			Score score = objective.getScore(p);
			
			if(seperateWorld(player.getWorld().getName())){
				
				score.setScore(Integer.parseInt(playerData[2]));
				
			}else{
				
				score.setScore(Integer.parseInt(playerData[1]));
				
			}
			
			
		}
		
		final Scoreboard oldSB = player.getScoreboard();

		player.setScoreboard(board);
		
		if(this.showScoreboardTip()){
			
			player.sendMessage(Lang.Prefix.toString() + Lang.Scoreboard_Toggle.toString());
			
		}
		
		int displayTime = plugin.getConfig().getInt("Scoreboard_Display_Time");
		
		displayTime = (displayTime * 20);
		
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {

				player.setScoreboard(oldSB);

			}

		}, displayTime);

		long lUpdate = (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis()) - plugin.lastUpdate);

		player.sendMessage(Lang.TS_Chat_Last_Update.toString().replaceAll("%stats.lastupdate%", Long.toString(lUpdate)));
		
	}
	
	public boolean showScoreboardTip(){
		
		boolean val = new Random().nextInt(9)==0;
		
		return val;
		
	}
	
    public Map<String, Double> sortByDouble(Map<String, Double> map) {
        List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(map.entrySet());
 
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
 
            public int compare(Map.Entry<String, Double> m1, Map.Entry<String, Double> m2) {
                return (m2.getValue()).compareTo(m1.getValue());
            }
        });
 
        Map<String, Double> result = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
	
    public Map<String, Integer> sortByValue(Map<String, Integer> map) {
        List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(map.entrySet());
 
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
 
            public int compare(Map.Entry<String, Integer> m1, Map.Entry<String, Integer> m2) {
                return (m2.getValue()).compareTo(m1.getValue());
            }
        });
 
        Map<String, Integer> result = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
	
    
    
    //sqlInfo hashmap getters.
    public int sqlKills(String playerName, String worldName){

		if(worldName != null){
				
			if(plugin.sqlInfo.containsKey(worldName + ":" + playerName)){
					
				return plugin.sqlInfo.get(worldName + ":" + playerName).getKills();
					
			}else{

				return 0;
					
			}

		}else{
				
			if(plugin.sqlInfo.containsKey(playerName)){
					
				return plugin.sqlInfo.get(playerName).getKills();
					
			}else{
					
				return 0;
					
			}
				
				
		}
		
	}
    
    public int sqlDeaths(String playerName, String worldName){

		if(worldName != null){
				
			if(plugin.sqlInfo.containsKey(worldName + ":" + playerName)){
					
				return plugin.sqlInfo.get(worldName + ":" + playerName).getDeaths();
					
			}else{

				return 0;
					
			}

		}else{
				
			if(plugin.sqlInfo.containsKey(playerName)){
					
				return plugin.sqlInfo.get(playerName).getDeaths();
					
			}else{
					
				return 0;
					
			}
				
				
		}
		
	}
    
    public double sqlRatio(String playerName, String worldName){

		if(worldName != null){
				
			if(plugin.sqlInfo.containsKey(worldName + ":" + playerName)){
					
				return plugin.sqlInfo.get(worldName + ":" + playerName).getRatio();
					
			}else{

				return 0;
					
			}

		}else{
				
			if(plugin.sqlInfo.containsKey(playerName)){
					
				return plugin.sqlInfo.get(playerName).getRatio();
					
			}else{
					
				return 0;
					
			}
				
				
		}
		
	}
    
    public int sqlStreak(String playerName, String worldName){

		if(worldName != null){
				
			if(plugin.sqlInfo.containsKey(worldName + ":" + playerName)){
					
				return plugin.sqlInfo.get(worldName + ":" + playerName).getStreak();
					
			}else{

				return 0;
					
			}

		}else{
				
			if(plugin.sqlInfo.containsKey(playerName)){
					
				return plugin.sqlInfo.get(playerName).getStreak();
					
			}else{
					
				return 0;
					
			}
				
				
		}
		
	}
    
    public boolean sqlScoreboardEnabled(String playerName, String worldName){

		if(worldName != null){
				
			if(plugin.sqlInfo.containsKey(worldName + ":" + playerName)){
					
				return plugin.sqlInfo.get(worldName + ":" + playerName).getScoreboardEnabled();
					
			}else{

				return false;
					
			}

		}else{
				
			if(plugin.sqlInfo.containsKey(playerName)){
					
				return plugin.sqlInfo.get(playerName).getScoreboardEnabled();
					
			}else{
					
				return false;
					
			}
				
				
		}
		
	}
}
