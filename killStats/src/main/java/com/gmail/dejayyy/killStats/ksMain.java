package com.gmail.dejayyy.killStats;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;

import com.gmail.dejayyy.killStats.API.ksAPI;
import com.gmail.dejayyy.killStats.MySQL.MySQL;
import com.gmail.dejayyy.killStats.ksLang.langHandler.Lang;
import com.gmail.dejayyy.killStats.ksListener.ksListener;

public class ksMain extends JavaPlugin {
	//TODO add [level] and algo to config.
	public static YamlConfiguration LANG;
	public static File LANG_FILE;
	
	public List<String> deadList = new ArrayList<String>();
	public HashSet<String> reset = new HashSet<String>();
	
	public long lastUpdate;
	public long statsUpdateTime;
	public static ksAPI api;
	
	public int startoverTaskID;
	public BukkitTask checkSQLWorldsTask;
	
	public ksMain plugin;	
	
	public HashMap<String, PlayerStats> sqlInfo = new HashMap<String, PlayerStats>();
	
	public List<String> seperateWorlds = new ArrayList<String>();
	public List<String> topKills = new ArrayList<String>();
	public List<String> topDeaths = new ArrayList<String>();
	public List<String> topStreak = new ArrayList<String>();
	public List<String> topRatio = new ArrayList<String>();
	public List<String> sqlWorlds = new ArrayList<String>();
	
	public Logger log;
	
	public boolean pluginEnabled = true;
	public boolean sqlEnabled = false;
	public boolean allowScoreboard;
	
	
	public String currentVersion;
	
	public Double version;
	
	public Connection sqlConnection = null;

	@Override
	public void onEnable(){
		//TODO: metrics
		
		loadLang();
		
		saveDefaultConfig();

		api = new ksAPI(this);
		
		getServer().getPluginManager().registerEvents(new ksListener(this), this);
		
		saveResource("README.txt", true);
		saveResource("rewards.yml", false);

		log = Logger.getLogger("Minecraft");
				
		this.getCommand("killstatsadmin").setExecutor(new ksAdmin(this));
		
		api.checkDataFiles();
		
		statsUpdateTime = this.getConfig().getLong("Top_Stats.Update", 20);
		
		statsUpdateTime = (statsUpdateTime * 1200);
		
		sqlEnabled = this.getConfig().getBoolean("MySQL.Enabled", false);
		
		seperateWorlds = this.getConfig().getStringList("Seperate_Worlds");

		api.MakeSQLConnection(true);
			
		checkSQLWorldsTask = this.getServer().getScheduler().runTaskAsynchronously(this, api.checkSQLWorlds);
		
		api.populateLeaderBoards();
		
		allowScoreboard = this.getConfig().getBoolean("Allow_Scoreboard", true);
		
        
	}
	
	public void initialize(){
		
		loadLang();
		
		saveDefaultConfig();

		saveResource("README.txt", true);
		saveResource("rewards.yml", false);

		log = Logger.getLogger("Minecraft");
				
		this.getCommand("killstatsadmin").setExecutor(new ksAdmin(this));
		
		api.checkDataFiles();
		
		statsUpdateTime = this.getConfig().getLong("Top_Stats.Update", 20);
		
		statsUpdateTime = (statsUpdateTime * 1200);
		
		sqlEnabled = this.getConfig().getBoolean("MySQL.Enabled", false);
		
		seperateWorlds = this.getConfig().getStringList("Seperate_Worlds");

		api.MakeSQLConnection(true);
			
		checkSQLWorldsTask = this.getServer().getScheduler().runTaskAsynchronously(this, api.checkSQLWorlds);
		
		allowScoreboard = this.getConfig().getBoolean("Allow_Scoreboard", true);
		
		api.populateLeaderBoards();
		
		
	}
	
	public void onDisable(){

		this.getServer().getScheduler().cancelTasks(this);
		
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		
		if(cmd.getName().equalsIgnoreCase("killstats")){
			
			if(!(pluginEnabled)){

				sender.sendMessage(Lang.Prefix.toString() + Lang.Plugin_Disabled.toString());

			}
			
			if(sender instanceof Player){
				
				Player player = (Player) sender;
				
				String worldName = player.getWorld().getName();
				
				if(api.worldDisabled(worldName)){
					
					String msg = Lang.World_Disabled.toString();

					player.sendMessage(Lang.Prefix + api.replaceVariables(msg, player, player));
					
					return true;

				}
				
			}else{
				
				sender.sendMessage(Lang.Prefix.toString() + Lang.Invalid_Console_Command.toString());
				
				return true;
				
			}
			
			switch(args.length){

				case 0:
						
						final Player tPlayer = (Player) sender;
						
						if(api.scoreboardEnabled(tPlayer)){
								
								int displayTime = this.getConfig().getInt("Scoreboard_Display_Time", 6);
								
								displayTime = displayTime * 20;
								
								final Scoreboard oldSB = tPlayer.getScoreboard();
								
								if(tPlayer.getScoreboard().getObjective("ks") != null){
									
									tPlayer.sendMessage(Lang.Prefix.toString() + Lang.Scoreboard_Already_Open.toString());
									
									return true;
									
								}
								
								tPlayer.setScoreboard(api.scoreboardInfo(tPlayer));
								
								this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
									
									@Override
									public void run() {

										tPlayer.setScoreboard(oldSB);

									}

								}, displayTime);
								
								return true;
								

							}else{
								
								tPlayer.sendMessage(api.playerInfo(tPlayer));
							
								return true;
							
							}
										
				case 1:
					
					if(args[0].equalsIgnoreCase("startover")){
						
						if(!(sender.hasPermission("killstats.player.startover"))){
							
							sender.sendMessage(Lang.Prefix.toString() + Lang.No_Permission.toString());
							
							return true;
							
						}
								
							final Player player = (Player) sender;
							
							player.sendMessage(Lang.Prefix.toString() + Lang.About_To_Startover.toString());

							reset.add(player.getName());

							int cD = this.getConfig().getInt("Startover_Cooldown", 15) * 20;

							startoverTaskID = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
								
										@Override
										public void run() {

											reset.remove(player.getName());
											
											player.sendMessage(Lang.Prefix.toString() + Lang.Startover_Time_Expired.toString());

										}

									}, cD);
	
							return true;
							
					}else if(args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help")){
						
						sender.sendMessage(api.pluginInfo());
						
						return true;
						
					}else if(args[0].equalsIgnoreCase("topkills")){
							
							Player player = (Player) sender;
							
							if(api.noPlayerToDisplay(topKills, player)){
								
								player.sendMessage(Lang.Prefix.toString() + Lang.TS_No_Players.toString());
								
								return true;
								
							}
							
							
							if(api.scoreboardEnabled(player)){
								
								api.sbTopPlayers(topKills, player, 1);
								
								return true;
								
								
							}
							
							sender.sendMessage(Lang.TS_Chat_Header_Kills.toString());
							
							api.showTopPlayers(topKills, player, 1);
							
							if(api.showScoreboardTip()){
								
								sender.sendMessage(Lang.Prefix.toString() + Lang.Scoreboard_Toggle.toString());
								
							}
							
							return true;


					}else if(args[0].equalsIgnoreCase("topdeaths")){
							
							Player player = (Player) sender;
							
							if(api.noPlayerToDisplay(topDeaths, player)){
								
								player.sendMessage(Lang.Prefix.toString() + Lang.TS_No_Players.toString());
								
								return true;
								
							}
							
							if(api.scoreboardEnabled(player)){
								
								api.sbTopPlayers(topDeaths, player, 1);
								
								return true;
								
								
							}
							
							sender.sendMessage(Lang.TS_Chat_Header_Deaths.toString());
							
							api.showTopPlayers(topDeaths, player, 1);
							
							if(api.showScoreboardTip()){
								
								sender.sendMessage(Lang.Prefix.toString() + Lang.Scoreboard_Toggle.toString());
								
							}
							
							return true;
						
					}else if(args[0].equalsIgnoreCase("topstreak")){
							
							Player player = (Player) sender;
							
							if(api.noPlayerToDisplay(topStreak, player)){
								
								player.sendMessage(Lang.Prefix.toString() + Lang.TS_No_Players.toString());
								
								return true;
								
							}
							
							if(api.scoreboardEnabled(player)){
								
								api.sbTopPlayers(topStreak, player, 1);
								
								return true;
								
								
							}
							
							sender.sendMessage(Lang.TS_Chat_Header_Streak.toString());
							
							api.showTopPlayers(topStreak, player, 1);
							
							if(api.showScoreboardTip()){
								
								sender.sendMessage(Lang.Prefix.toString() + Lang.Scoreboard_Toggle.toString());
								
							}
							
							return true;
						
					}else if(args[0].equalsIgnoreCase("topratio")){
							
							Player player = (Player) sender;

							if(api.noPlayerToDisplay(topRatio, player)){
								
								player.sendMessage(Lang.Prefix.toString() + Lang.TS_No_Players.toString());
								
								return true;
								
							}
							
							sender.sendMessage(Lang.TS_Chat_Header_Ratio.toString());
							
							api.showTopPlayers(topRatio, player, 1);
							
							if(api.showScoreboardTip()){
								
								sender.sendMessage(Lang.Prefix.toString() + Lang.Scoreboard_Toggle.toString());
								
							}
							
							return true;
						
					}else if(args[0].equalsIgnoreCase("scoreboard")){

							Player player = (Player) sender;
							
							if(!(this.allowScoreboard)){
								
								player.sendMessage(Lang.Prefix.toString() + Lang.Scoreboard_Not_Allowed.toString());
								
								return true;
								
							}

							if(api.scoreboardEnabled(player)){
								
								api.setScoreboardEnabled(player, false);
								
								String msg = api.replaceVariables(Lang.Scoreboard_Disabled.toString(), player, player);

								player.sendMessage(Lang.Prefix.toString() + msg);

								return true;
								
							}else{
								
								api.setScoreboardEnabled(player, true);
								
								String msg = api.replaceVariables(Lang.Scoreboard_Enabled.toString(), player, player);

								player.sendMessage(Lang.Prefix.toString() + msg);
									
								return true;
								
							}
						
					}else if(args[0].equalsIgnoreCase("rank")){
						
						final Player player = (Player) sender;
						
						int killsRank = api.getKillsRank(player);
						int deathsRank = api.getDeathsRank(player);
						int streakRank = api.getStreakRank(player);
						int ratioRank = api.getRatioRank(player);
						
						if(api.scoreboardEnabled(player)){
							
							if(player.getScoreboard().getObjective("ks") != null){
								
								player.sendMessage(Lang.Prefix.toString() + Lang.Scoreboard_Already_Open.toString());
								
								return true;
								
							}else{
								
								final Scoreboard oldSB = player.getScoreboard();
								
								player.setScoreboard(api.scoreboardRanking(killsRank, deathsRank, streakRank, ratioRank));
								
								int displayTime = this.getConfig().getInt("Scoreboard_Display_Time", 6);
								
								displayTime = displayTime * 20;
								
								this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
									@Override
									public void run() {

										player.setScoreboard(oldSB);

									}

								}, displayTime);
								
								return true;
								
							}
							
						}
							
						player.sendMessage(Lang.Rank_Chat_Header.toString());
						player.sendMessage(Lang.Rank_Kills.toString().replaceAll("%ranking%", Integer.toString(killsRank)));
						player.sendMessage(Lang.Rank_Deaths.toString().replaceAll("%ranking%", Integer.toString(deathsRank)));
						player.sendMessage(Lang.Rank_Streak.toString().replaceAll("%ranking%", Integer.toString(streakRank)));
						player.sendMessage(Lang.Rank_Ratio.toString().replaceAll("%ranking%", Integer.toString(ratioRank)));

						if(api.showScoreboardTip()){
							
							sender.sendMessage(Lang.Prefix.toString() + Lang.Scoreboard_Toggle.toString());

						}
						
						return true;
						
						
					}else{
						
						Player targetPlayer = this.getServer().getPlayer(args[0].toString());

						if(targetPlayer == null){
							
							sender.sendMessage(Lang.Prefix.toString() + Lang.Invalid_Command.toString());
							
							return true;
							
						}else{
								
								final Player player = (Player) sender;
								
								if(api.scoreboardEnabled(player)){
									
									int displayTime = this.getConfig().getInt("Scoreboard_Display_Time", 6);
									
									displayTime = displayTime * 20;
									
									final Scoreboard oldSB = player.getScoreboard();
									
									if(player.getScoreboard().getObjective("ks") != null){
										
										player.sendMessage(Lang.Prefix.toString() + Lang.Scoreboard_Already_Open.toString());
										
										return true;
										
									}
									
									player.setScoreboard(api.scoreboardInfo(targetPlayer));
									
									if(api.showScoreboardTip()){
										
										sender.sendMessage(Lang.Prefix.toString() + Lang.Scoreboard_Toggle.toString());
										
									}
									
									this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
										@Override
										public void run() {

											player.setScoreboard(oldSB);

										}

									}, displayTime);
									
									
								}else{
									
									player.sendMessage(api.playerInfo(targetPlayer));
									
									if(api.showScoreboardTip()){
										
										sender.sendMessage(Lang.Prefix.toString() + Lang.Scoreboard_Toggle.toString());
										
									}
									
									return true;
									
								}

						}
						
					}

				case 2:
					
					if(args[0].equalsIgnoreCase("topkills")){
						
						if(!(api.isInt(args[1]))){
							
							sender.sendMessage(Lang.Prefix.toString() + Lang.Invalid_Command.toString());
							
							return true;
							
						}
						
						if(topKills.size() == 0){
							
							sender.sendMessage(Lang.Prefix.toString() + Lang.TS_No_Players.toString());
							
							return true;
							
						}
						
						int pageNumber = Integer.parseInt(args[1].toString());
							
							Player player = (Player) sender;
							
							if(api.scoreboardEnabled(player)){
								
								api.sbTopPlayers(topKills, player, pageNumber);
								
								return true;
								
								
							}
							
							sender.sendMessage(Lang.TS_Chat_Header_Kills.toString());
							
							api.showTopPlayers(topKills, player, pageNumber);
							
							if(api.showScoreboardTip()){
								
								sender.sendMessage(Lang.Prefix.toString() + Lang.Scoreboard_Toggle.toString());
								
							}
							
							return true;

					}else if(args[0].equalsIgnoreCase("topdeaths")){

						if(!(api.isInt(args[1]))){
							
							sender.sendMessage(Lang.Prefix.toString() + Lang.Invalid_Command.toString());
							
							return true;
							
						}
						
						if(topDeaths.size() == 0){
							
							sender.sendMessage(Lang.Prefix.toString() + Lang.TS_No_Players.toString());
							
							return true;
							
						}
						
						
						int pageNumber = Integer.parseInt(args[1].toString());
							
							Player player = (Player) sender;
							
							if(api.scoreboardEnabled(player)){
								
								api.sbTopPlayers(topDeaths, player, pageNumber);
								
								return true;
								
								
							}
							
							
							sender.sendMessage(Lang.TS_Chat_Header_Deaths.toString());
							
							api.showTopPlayers(topDeaths, player, pageNumber);
							
							if(api.showScoreboardTip()){
								
								sender.sendMessage(Lang.Prefix.toString() + Lang.Scoreboard_Toggle.toString());
								
							}
							
							return true;
						
					}else if(args[0].equalsIgnoreCase("topstreak")){

						if(!(api.isInt(args[1]))){
							
							sender.sendMessage(Lang.Prefix.toString() + Lang.Invalid_Command.toString());
							
							return true;
							
						}
						
						if(topStreak.size() == 0){
							
							sender.sendMessage(Lang.Prefix.toString() + Lang.TS_No_Players.toString());
							
							return true;
							
						}
						
						int pageNumber = Integer.parseInt(args[1].toString());
							
							Player player = (Player) sender;
							
							if(api.scoreboardEnabled(player)){
								
								api.sbTopPlayers(topStreak, player, pageNumber);
								
								return true;
								
								
							}
							
							sender.sendMessage(Lang.TS_Chat_Header_Streak.toString());
							
							api.showTopPlayers(topStreak, player, pageNumber);
							
							if(api.showScoreboardTip()){
								
								sender.sendMessage(Lang.Prefix.toString() + Lang.Scoreboard_Toggle.toString());
								
							}
							
							return true;

						
					}else if(args[0].equalsIgnoreCase("topratio")){

						if(!(api.isInt(args[1]))){
							
							sender.sendMessage(Lang.Prefix.toString() + Lang.Invalid_Command.toString());
							
							return true;
							
						}

						int pageNumber = Integer.parseInt(args[1].toString());
							
							Player player = (Player) sender;
							
							if(topRatio.size() == 0){
								
								sender.sendMessage(Lang.Prefix.toString() + Lang.TS_No_Players.toString());
								
								return true;
								
							}
							
							sender.sendMessage(Lang.TS_Chat_Header_Ratio.toString());
							
							api.showTopPlayers(topRatio, player, pageNumber);
							
							if(api.showScoreboardTip()){
								
								sender.sendMessage(Lang.Prefix.toString() + Lang.Scoreboard_Toggle.toString());
								
							}
							
							return true;
					}
					
					
				default:
					
					sender.sendMessage(Lang.Prefix.toString() + Lang.Invalid_Command.toString());
					
					return true;
					
			}
			
				
		}
		
		return true;
		
	}
	
	
	
	
	
	
	
	
	
	
	
	 /**
	   * Load the lang.yml file.
	   * @return The lang.yml config.
	   */
	  private void loadLang() {
			
	        File lang = new File(getDataFolder() + File.separator + "Messages", "messages.yml");
	        File folder = new File(getDataFolder() + File.separator + "Messages");
	        
	        OutputStream out = null;
	        InputStream defLangStream = this.getResource("messages.yml");
	        
	        if (!lang.exists()) {
	        	
	            try {
	            	
	                getDataFolder().mkdir();
	                folder.mkdir();
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

	            	e.printStackTrace();
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
	                conf.set(item.getPath(), item.getDefault().toString());
	            }
	        }
	        
	        Lang.setFile(conf);
	        ksMain.LANG = conf;
	        ksMain.LANG_FILE = lang;
	        
	        try {
	        	
	            conf.save(getLangFile());
	            
	        } catch (IOException e) {
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
	
	  /* 

      String path = this.getDataFolder() + File.separator + "player_data";
      File folder = new File(path);
      File[] listOfFiles = folder.listFiles();
     
      
      for(File p : listOfFiles){
      	
      	if(p.getName().endsWith(".yml"){
      		
      		YamlConfiguration a = YamlConfiguration.loadConfiguration(api.pFile(p.getName(), "world_nether"));

      		sender.sendMessage(p.getName().replace(".yml", null") + " - " + a.getInt("kills"));
      	
      	}
      	
      } */

}
