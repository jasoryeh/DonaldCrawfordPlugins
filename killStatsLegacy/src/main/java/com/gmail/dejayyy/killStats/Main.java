package com.gmail.dejayyy.killStats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import com.gmail.dejayyy.killStats.API.ksAPI;
import com.gmail.dejayyy.killStats.API.MySQL;

import com.gmail.dejayyy.killStats.langHandler.Lang;

@SuppressWarnings("deprecation")
public class Main extends JavaPlugin implements Listener {
	// TODO: add /ks reset all
	// TODO: re-do permissions, for better handling of admin commands/abuse
	// TODO: option to leave scoreboard on
	// TODO: option to change how long anti boost last's

	public List<String> myTop5 = new ArrayList<String>();
	public File configFile;
	public FileConfiguration playersFile;
	public File cFile;
	public FileConfiguration langFile;
	public static Logger log;
	public static YamlConfiguration LANG;
	public static File LANG_FILE;
	public List<String> deadList = new ArrayList<String>();
	public HashSet<String> reset = new HashSet<String>();
	boolean logActions;
	public int taskID;
	public Main plugin;
	public boolean useDB;
	public static ksAPI API;

	public List<String> disabledWorlds = new ArrayList<String>();

	Connection connection = null;

	static MySQL sql;

	public void onEnable() {

		this.loadLang();

		log = getServer().getLogger();

		getServer().getPluginManager().registerEvents(this, this);

		API = new ksAPI(this);

		this.loadPlayerYML();

		this.saveDefaultConfig();

		this.saveResource("OriginalConfig.txt", true);

		logActions = this.getConfig().getBoolean("LogActions");

		for (String x : this.getConfig().getStringList("IgnoredWorlds")) {

			disabledWorlds.add(x.toLowerCase());

		}

		this.startMetrics();

	}

	public void startMetrics() {

		try {
			mcStats metrics = new mcStats(this);
			metrics.start();

		} catch (IOException e) {

			// Failed to submit the stats :-(

		}

	}

	public void onDisable() {

		this.savePlayerYML();

	}

	@EventHandler
	public void playerLogin(PlayerJoinEvent event) {

		Player player = event.getPlayer();

		if (player.hasPermission("killstats.admin")) {

			if (this.getConfig().getDouble("configversion") < 2.6) {

				player.sendMessage(ChatColor.AQUA
						+ Lang.ConfigOutOfDate.toString());
				player.sendMessage(ChatColor.AQUA
						+ Lang.ViewOriginalConfig.toString());

			}
		}

	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event) throws SQLException {

		if (this.getConfig().getBoolean("OnlyPvPDeaths")) {

			if (!(event.getEntity().getKiller() instanceof Player)) {

				return;

			}
		}

		if (worldDisabled(event.getEntity().getWorld())) {

			return;

		}

		boolean antiBoost = this.getConfig().getBoolean("AntiBoostEnabled");

		if (event.getEntity().getKiller() instanceof Player) {

			final Player player = event.getEntity();
			final Player killer = player.getKiller();

			final String pWorld = player.getWorld().getName();
			final String kWorld = killer.getWorld().getName();

			if (!(API.hasProfile(player.getName(), player.getWorld().getName()))) {

				API.setupProfile(player.getName(), player.getWorld().getName());

			}

			if (!(API.hasProfile(killer.getName(), killer.getWorld().getName()))) {

				API.setupProfile(killer.getName(), killer.getWorld().getName());

			}

			boolean dropHeads = this.getConfig().getBoolean("DropHead");

			int playerDeath = API.getDeaths(player.getName(), pWorld);
			int killerKills = API.getKills(killer.getName(), kWorld);
			int playerStreak = API.getStreak(player.getName(), pWorld);
			int killerStreak = API.getStreak(killer.getName(), kWorld);

			if (antiBoost) {

				if (isBoosting(killer, player)) {

					String bAlert = this.getConfig().getString("BoosterAlert");

					bAlert = bAlert.replaceAll("<player>", player.getName());

					if (!(bAlert.equalsIgnoreCase("disable"))) {

						killer.sendMessage(replaceColors(bAlert));

					}

					return;

				}

				deadList.add(killer.getName() + ":" + player.getName());

				long coolDown = this.getConfig().getLong("TimeLimit");

				coolDown = (coolDown * 60 * 20);

				this.getServer().getScheduler()
						.scheduleSyncDelayedTask(this, new Runnable() {
							@Override
							public void run() {

								deadList.remove(killer.getName() + ":"
										+ player.getName());

							}

						}, coolDown);

			}

			if (dropHeads == true) {

				this.DropHead(player);
			}

			if (playerStreak > 0) {

				if (this.getConfig().getBoolean("BroadcastBrokenStreak") == true) {

					this.HandleBrokenStreak(player, killer, playerStreak);

				}

				API.setStreak(player.getName(), 0, pWorld);

			} // playerstreak > 0

			killerStreak++;
			playerDeath++;
			killerKills++;

			API.setStreak(killer.getName(), killerStreak, kWorld);
			API.setDeaths(player.getName(), playerDeath, pWorld);
			API.setKills(killer.getName(), killerKills, kWorld);

			if (this.getConfig().getBoolean("RewardsEnabled") == true) {

				this.HandleRewards(killer, player, killerStreak);

			}

			if (this.getConfig().getBoolean("broadcastStreak") == true) {

				this.HandleBroadcast(player, killer, killerStreak);

			}

			this.savePlayerYML();

		} else {

			Player player = event.getEntity();

			if (!(API.hasProfile(player.getName(), player.getWorld().getName()))) {

				API.setupProfile(player.getName(), player.getWorld().getName());

			}

			final String pWorld = player.getWorld().getName();

			int playerDeath = API.getDeaths(player.getName(), pWorld);

			playerDeath++;

			API.setDeaths(player.getName(), playerDeath, pWorld);
			API.setStreak(player.getName(), 0, pWorld);

			return;

		}

	}

	public boolean onCommand(CommandSender sender, Command cmd, String cmdL,
			String[] args) {

		if (!(sender instanceof Player)) {

			sender.sendMessage(Lang.CannotRunFromConsole.toString());

			return true;

		}

		final Player player = (Player) sender;

		if (cmdL.equalsIgnoreCase("killStats") || cmdL.equalsIgnoreCase("ks")) {

			if (this.worldDisabled(player.getWorld())) {

				player.sendMessage(ChatColor.DARK_AQUA + "[killStats] "
						+ ChatColor.AQUA + Lang.NoStatsToDisplay.toString());

				return true;

			}

			if (args.length > 2) {

				this.pluginHelp(player);

				return true;

			}

			if (args.length == 0) {

				this.getPlayerInfo(player, player);

				return true;
			}

			if (args.length == 1) {

				if (args[0].equalsIgnoreCase("?")) {

					this.pluginHelp(player);

					player.sendMessage(ChatColor.DARK_AQUA + "Plugin Author: "
							+ ChatColor.AQUA + "ImDeJay");

					return true;

				} else if (args[0].equalsIgnoreCase("top")) {

					// player.sendMessage(ChatColor.DARK_AQUA + "[Rank]" +
					// ChatColor.AQUA + " [Playername]" + ChatColor.DARK_AQUA +
					// " - " + ChatColor.AQUA + "[Kills : Deaths]");
					String dfault = this.getConfig()
							.getString("DefaultTopType");

					if (dfault.equalsIgnoreCase("kills")
							|| dfault.equalsIgnoreCase("deaths")
							|| dfault.equalsIgnoreCase("streak")
							|| dfault.equalsIgnoreCase("ratio")) {

						this.getRank(player, dfault);

						return true;

					} else {

						this.getRank(player, "kills");

						return true;

					}

				} else if (args[0].equalsIgnoreCase("startover")) {

					player.sendMessage(ChatColor.DARK_AQUA + "[killStats] "
							+ ChatColor.AQUA + Lang.AboutToReset.toString());
					player.sendMessage(ChatColor.DARK_AQUA + "[killStats] "
							+ ChatColor.AQUA + Lang.AreYouSure.toString());
					player.sendMessage(ChatColor.DARK_AQUA + "[killStats] "
							+ ChatColor.AQUA + Lang.AnswerWithYesNo.toString());

					reset.add(player.getName());

					int cD = 15 * 20;

					taskID = this.getServer().getScheduler()
							.scheduleSyncDelayedTask(this, new Runnable() {
								@Override
								public void run() {

									reset.remove(player.getName());
									player.sendMessage(ChatColor.DARK_AQUA
											+ "[killStats] " + ChatColor.AQUA
											+ Lang.RequestTimedOut.toString());

								}

							}, cD);

					return true;

				} else if (args[0].equalsIgnoreCase("reload")) {

					if (player.hasPermission("killStats.admin")) {

						this.loadPlayerYML();

						this.loadLang();

						disabledWorlds.clear();

						this.reloadConfig();

						for (String x : this.getConfig().getStringList(
								"IgnoredWorlds")) {

							disabledWorlds.add(x.toLowerCase());

						}

						player.sendMessage(ChatColor.DARK_AQUA + "[killStats] "
								+ ChatColor.AQUA
								+ Lang.ReloadComplte.toString());

						if (this.logActions) {

							this.logInfo(player.getName(),
									"Reloaded Configuration.");

						}

						return true;

					} else {

						player.sendMessage(ChatColor.DARK_AQUA + "[killStats] "
								+ ChatColor.AQUA + Lang.NoPermission.toString());

						return true;

					}

				}

				Player tPlayer = player.getServer().getPlayer(args[0]);

				if (!(tPlayer == null)) {

					this.getPlayerInfo(player, tPlayer);

					return true;

				} else {

					player.sendMessage(ChatColor.DARK_AQUA + "[killStats] "
							+ ChatColor.AQUA + Lang.PlayerNotFound.toString());

					return true;

				} // args = profile

			} else if (args.length == 2) {

				if (args[0].equalsIgnoreCase("head")) {

					if (!(player.hasPermission("killstats.admin"))) {

						player.sendMessage(ChatColor.DARK_AQUA + "[killStats] "
								+ ChatColor.AQUA + Lang.NoPermission.toString());

						return true;

					}

					Player tPlayer = this.getServer().getPlayer(args[1]);

					if (tPlayer == null) {

						ItemStack i = new ItemStack(Material.SKULL_ITEM, 1,
								(short) 3);

						SkullMeta meta = (SkullMeta) i.getItemMeta();

						meta.setOwner(args[1].toString());

						i.setItemMeta(meta);

						player.getInventory().setItemInHand(i);

					} else {

						ItemStack i = new ItemStack(Material.SKULL_ITEM, 1,
								(short) 3);

						SkullMeta meta = (SkullMeta) i.getItemMeta();

						meta.setOwner(tPlayer.getName());

						i.setItemMeta(meta);

						player.getInventory().setItemInHand(i);

					}

				} else if (args[0].equalsIgnoreCase("prune")) {

					if (args[1].equalsIgnoreCase("kills")) {

						this.prunePlayers(player, "kills");

						return true;

					} else if (args[1].equalsIgnoreCase("deaths")) {

						this.prunePlayers(player, "deaths");

						return true;

					} else if (args[1].equalsIgnoreCase("inactive")) {

						this.prunePlayers(player, "inactive");

						return true;

					} // args == kills/deaths

				} else if (args[0].equalsIgnoreCase("top")) {

					String type = args[1].toString();

					if (type.equalsIgnoreCase("ratio")) {

						this.getRank(player, "ratio");

					} else if (type.equalsIgnoreCase("kills")) {

						this.getRank(player, "kills");

					} else if (type.equalsIgnoreCase("deaths")) {

						this.getRank(player, "deaths");

					} else if (type.equalsIgnoreCase("streak")) {

						this.getRank(player, "streak");

					}

				} else if (args[0].equalsIgnoreCase("reset")) {

					Player targetPlayer = (Player) this.getServer().getPlayer(
							args[1]);

					if (player.hasPermission("killStats.admin")) {

						if (targetPlayer == null) {

							OfflinePlayer offlinePlayer = this.getServer()
									.getOfflinePlayer(args[0]);

							API.setKills(offlinePlayer.getName(), 0, player
									.getWorld().getName());
							API.setDeaths(offlinePlayer.getName(), 0, player
									.getWorld().getName());
							API.setStreak(offlinePlayer.getName(), 0, player
									.getWorld().getName());

							player.sendMessage(ChatColor.DARK_AQUA
									+ "[killStats] " + ChatColor.AQUA
									+ Lang.ResetSuccess.toString());

							return true;

						}

						API.setKills(targetPlayer.getName(), 0, targetPlayer
								.getWorld().getName());
						API.setDeaths(targetPlayer.getName(), 0, targetPlayer
								.getWorld().getName());
						API.setStreak(targetPlayer.getName(), 0, targetPlayer
								.getWorld().getName());

						this.savePlayerYML();

						player.sendMessage(ChatColor.DARK_AQUA + "[killStats] "
								+ ChatColor.AQUA
								+ Lang.PersonalResetSuccess.toString());

						if (this.logActions) {

							this.logInfo(player.getName(), "Reset "
									+ targetPlayer.getName() + "'s profile.");

						}

						return true;

					} else {

						player.sendMessage(ChatColor.DARK_AQUA + "[killStats] "
								+ ChatColor.AQUA + Lang.NoPermission.toString());

					}

				} else {

					this.pluginHelp(player);

					return true;

				} // args == profile

			} // args.length == 2

		} // cmdl ==

		return true;

	} // onCommand

	@EventHandler
	@SuppressWarnings("deprecation")
	public void playerChat(PlayerChatEvent event) {

		Player player = event.getPlayer();
		String msg = event.getMessage();

		if (reset.contains(player.getName())) {

			if (msg.equalsIgnoreCase("yes") || msg.equalsIgnoreCase("y")) {

				API.setKills(player.getName(), 0, player.getWorld().getName());
				API.setDeaths(player.getName(), 0, player.getWorld().getName());
				API.setStreak(player.getName(), 0, player.getWorld().getName());

				player.sendMessage(ChatColor.DARK_AQUA + "[killStats] "
						+ ChatColor.AQUA + Lang.PersonalResetSuccess.toString());

				reset.remove(player.getName());

				this.getServer().getScheduler().cancelTask(taskID);

			} else if (msg.equalsIgnoreCase("no") || msg.equalsIgnoreCase("n")) {

				reset.remove(player.getName());

				player.sendMessage(ChatColor.DARK_AQUA + "[killStats] "
						+ ChatColor.AQUA + Lang.RequestCancelled.toString());

				this.getServer().getScheduler().cancelTask(taskID);

			} else {

				player.sendMessage(ChatColor.DARK_AQUA + "[killStats] "
						+ ChatColor.AQUA + Lang.InvalidAnswer.toString());

			}

			event.setCancelled(true);
		}

	}

	public void loadLangYML() {

		LANG_FILE = new File(getDataFolder(), "lang.yml");

		LANG = new YamlConfiguration();

		if (getDataFolder().exists() == false) {
			try {
				getDataFolder().mkdir();
			} catch (Exception ex) {
				// something went wrong.
			}

		}

		if (LANG_FILE.exists() == false) {

			try {
				LANG_FILE.createNewFile();
			} catch (Exception ex) {
				// something went wrong.
			}
		}

		try { // Load payers.yml

			Lang.setFile(LANG);

		} catch (Exception ex) {

		}
	}

	public void loadPlayerYML() {
		configFile = new File(getDataFolder(), "players.yml");

		playersFile = new YamlConfiguration();

		if (getDataFolder().exists() == false) {
			try {
				getDataFolder().mkdir();
			} catch (Exception ex) {
				// something went wrong.
			}

		}

		if (configFile.exists() == false) {

			try {
				configFile.createNewFile();
			} catch (Exception ex) {
				// something went wrong.
			}
		}

		try { // Load payers.yml
			playersFile.load(configFile);
		} catch (Exception ex) {
			// Something went wrong
		}
	}

	public void savePlayerYML() {

		try {

			playersFile.save(configFile);

		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	static String replaceColors(String message) {

		return ChatColor.translateAlternateColorCodes('&', message);

	}

	public void getPlayerInfo(final Player player, Player targetPlayer) {

		if (!(API.hasProfile(targetPlayer.getName(), targetPlayer.getWorld().getName()))) {

			player.sendMessage(ChatColor.DARK_AQUA + "[killStats] " + ChatColor.AQUA + Lang.NoStatsToDisplay.toString());

			return;

		}

		double ratio;

		int displayTime = this.getConfig().getInt("ScoreboardDisplayTime");
		int lngKills = API.getKills(targetPlayer.getName(), targetPlayer.getWorld().getName());
		int lngDeaths = API.getDeaths(targetPlayer.getName(), targetPlayer.getWorld().getName());
		int lngKS = API.getStreak(targetPlayer.getName(), targetPlayer.getWorld().getName());

		boolean sbEnabled = this.getConfig().getBoolean("ScoreboardEnabled");

		ratio = API.getRatio(targetPlayer, targetPlayer.getWorld().getName());

		ratio = Math.round(ratio * 100.0) / 100.0;

		if (sbEnabled) {

			displayTime = displayTime * 20;

			final Scoreboard oldSB = player.getScoreboard();

			ScoreboardManager manager = Bukkit.getScoreboardManager();
			Scoreboard board = manager.getNewScoreboard();
			Objective objective = board.registerNewObjective("ks", "dummy");

			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
			objective.setDisplayName(targetPlayer.getName());

			Score kills = objective.getScore(Bukkit.getOfflinePlayer(ChatColor.AQUA + Lang.Kills.toString() + ":"));
			Score deaths = objective.getScore(Bukkit.getOfflinePlayer(ChatColor.AQUA + Lang.Deaths.toString() + ":"));
			Score streak = objective.getScore(Bukkit.getOfflinePlayer(ChatColor.AQUA + Lang.Killstreak.toString() + ":"));

			streak.setScore(lngKS);
			kills.setScore(lngKills);
			deaths.setScore(lngDeaths);

			player.setScoreboard(board);

			this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
						@Override
						public void run() {

							player.setScoreboard(oldSB);

						}

					}, displayTime);

		} else {

			player.sendMessage(ChatColor.DARK_AQUA + "=-=-= " + ChatColor.AQUA + targetPlayer.getName() + ChatColor.DARK_AQUA + " =-=-=");
			player.sendMessage(ChatColor.DARK_AQUA + Lang.Kills.toString() + ": " + ChatColor.AQUA + lngKills + ChatColor.DARK_AQUA + "  " + Lang.Deaths.toString() + ": " + ChatColor.AQUA + lngDeaths);
			player.sendMessage(ChatColor.DARK_AQUA + Lang.Killstreak.toString() + ": " + ChatColor.AQUA + lngKS + ChatColor.DARK_AQUA + "  " + Lang.Ratio.toString() + ": " + ChatColor.AQUA + ratio);

		}
	}

	public void prunePlayers(Player player, String type) {
		// TODO: prune players from database.
		String section = null;

		long playerCount = 0;

		if (player.hasPermission("killStats.admin")) {

			if (type == "kills") {

				if (API.seperateWorld(player.getWorld().getName())) {

					section = "Players." + player.getWorld().getName();

				} else {

					section = "Players";

				}

				ConfigurationSection users = this.playersFile.getConfigurationSection(section);

				Set<String> players = users.getKeys(false);

				for (String prune : players) {

					int x = users.getInt(prune + ".kills");

					if (x == 0) {

						users.set(prune.toString(), null);

						playerCount++;

					} // x == 0

				} // for loop

				player.sendMessage(ChatColor.DARK_AQUA + "[killStats] "
						+ ChatColor.AQUA + playerCount
						+ " players were removed!");

				if (this.logActions) {
					this.logInfo(player.getName(),
							"Pruned all players based on kills.");
				}

			} else if (type == "deaths") {

				if (API.seperateWorld(player.getWorld().getName())) {

					section = "Players." + player.getWorld().getName();

				} else {

					section = "Players";

				}

				ConfigurationSection users = this.playersFile
						.getConfigurationSection(section);

				Set<String> players = users.getKeys(false);

				for (String prune : players) {

					int x = users.getInt(prune + ".deaths");

					if (x == 0) {

						users.set(prune.toString(), null);

						playerCount++;

					} // x==0

				} // for loop

				player.sendMessage(ChatColor.DARK_AQUA + "[killStats] "
						+ ChatColor.AQUA + playerCount
						+ " players were removed!");

			} else if (type == "inactive") {

				if (API.seperateWorld(player.getWorld().getName())) {

					section = "Players." + player.getWorld().getName();

				} else {

					section = "Players";

				}

				ConfigurationSection users = this.playersFile
						.getConfigurationSection(section);

				Set<String> players = users.getKeys(false);

				for (String prune : players) {

					int x = users.getInt(prune + ".deaths");
					int y = users.getInt(prune + ".kills");

					if (x == 0 && y == 0) {

						users.set(prune.toString(), null);

						playerCount++;

					} // x==0

				} // for loop

				player.sendMessage(ChatColor.DARK_AQUA + "[killStats] "
						+ ChatColor.AQUA + playerCount
						+ " players were removed!");

			} // check type

			this.logInfo(player.getName(), "Pruned all inactive players.");

			this.savePlayerYML();

		} else {

			player.sendMessage(ChatColor.DARK_AQUA + "[killStats] "
					+ ChatColor.AQUA + Lang.NoPermission.toString());

		}

	}

	public void pluginHelp(Player player) {

		player.sendMessage(ChatColor.DARK_AQUA + "/killstats " + ChatColor.AQUA + Lang.CmdHelp_ViewPersonal.toString());
		player.sendMessage(ChatColor.DARK_AQUA + "/killstats startover " + ChatColor.AQUA + Lang.CmdHelp_Reset.toString());
		player.sendMessage(ChatColor.DARK_AQUA + "/killstats [PlayerName] " + ChatColor.AQUA + Lang.CmdHelp_ViewPlayer.toString());
		player.sendMessage(ChatColor.DARK_AQUA + "/killstats top [kills|deaths|streak|ratio] " + ChatColor.AQUA + Lang.CmdHelp_ViewTop.toString());
		player.sendMessage(ChatColor.DARK_AQUA + "/killstats ? " + ChatColor.AQUA + Lang.CmdHelp_PluginInfo.toString());

		if (player.hasPermission("killstats.admin")) {

			player.sendMessage(ChatColor.DARK_AQUA + "/killstats reload " + ChatColor.AQUA + Lang.CmdHelp_Reload.toString());
			player.sendMessage(ChatColor.DARK_AQUA + "/killstats prune [type] " + ChatColor.AQUA + Lang.CmdHelp_Prune.toString());
			player.sendMessage(ChatColor.DARK_AQUA + "/killstats reset [PlayerName] " + ChatColor.AQUA + Lang.CmdHelp_ResetPlayer.toString());

		}

	} // player help

	public void getRank(Player player, String type) {
		// TODO: getRank using database

		Map<String, Double> scoreMap = new HashMap<String, Double>();
		List<String> finalScore = new ArrayList<String>();

		String section;

		if (API.seperateWorld(player.getWorld().getName())) {

			section = "Players." + player.getWorld().getName();

		} else {

			section = "Players";

		}

		ConfigurationSection score = this.playersFile.getConfigurationSection(section);

		for (String playerName : score.getKeys(false)) {

			if (type.equalsIgnoreCase("kills")) {

				Double kills = score.getDouble(playerName + ".kills");
				
				// int deaths = score.getInt(playerName + ".deaths");

				scoreMap.put(playerName, kills);

			} else if (type.equalsIgnoreCase("deaths")) {

				Double deaths = score.getDouble(playerName + ".deaths");

				scoreMap.put(playerName, deaths);

			} else if (type.equalsIgnoreCase("ratio")) {

				Double kills = score.getDouble(playerName + ".kills");
				Double deaths = score.getDouble(playerName + ".deaths");

				double ratio;

				if (kills == 0 && deaths == 0) {

					ratio = 0.0;

				} else if (kills > 0 && deaths == 0) {

					ratio = kills;

				} else if (deaths > 0 && kills == 0) {

					ratio = -deaths;

				} else {

					ratio = (kills / deaths);

				}

				ratio = Math.round(ratio * 100.0) / 100.0;

				scoreMap.put(playerName, ratio);

			} else if (type.equalsIgnoreCase("streak")) {

				Double streak = score.getDouble(playerName + ".streak");

				scoreMap.put(playerName, streak);
			}

		}

		for (int i = 0; i < 5; i++) {

			String topName = "";
			Double topScore = (double) 0;

			for (String playerName : scoreMap.keySet()) {

				Double myScore = scoreMap.get(playerName);

				if (myScore > topScore) {

					topName = playerName;
					topScore = myScore;

				}

			}

			if (!topName.equals("")) {

				scoreMap.remove(topName);

				if (type.equalsIgnoreCase("kills")) {

					int kills = score.getInt(topName + ".kills");

					int position = i + 1;

					String finalString = ChatColor.DARK_AQUA + "[" + position
							+ "] " + ChatColor.AQUA + topName
							+ ChatColor.DARK_AQUA + " - " + ChatColor.AQUA
							+ kills + " Killing Blows!";

					finalScore.add(finalString);

				} else if (type.equalsIgnoreCase("deaths")) {

					int deaths = score.getInt(topName + ".deaths");

					int position = i + 1;

					String finalString = ChatColor.DARK_AQUA + "[" + position
							+ "] " + ChatColor.AQUA + topName
							+ ChatColor.DARK_AQUA + " - " + ChatColor.AQUA
							+ deaths + " Deaths!";

					finalScore.add(finalString);

				} else if (type.equalsIgnoreCase("ratio")) {

					int kills = score.getInt(topName + ".kills");
					int deaths = score.getInt(topName + ".deaths");
					double ratio;

					if (kills == 0 && deaths == 0) {

						ratio = 0.0;

					} else if (kills > 0 && deaths == 0) {

						ratio = kills;

					} else if (deaths > 0 && kills == 0) {

						ratio = -deaths;

					} else {

						ratio = (kills / deaths);

					}

					ratio = Math.round(ratio * 100.0) / 100.0;

					int position = i + 1;

					String finalString = ChatColor.DARK_AQUA + "[" + position
							+ "] " + ChatColor.AQUA + topName
							+ ChatColor.DARK_AQUA + " - " + ChatColor.AQUA
							+ ratio + " K/D Ratio!";

					finalScore.add(finalString);

					player.sendMessage("instance 2");

				} else if (type.equalsIgnoreCase("streak")) {

					int streak = score.getInt(topName + ".streak");

					int position = i + 1;

					String finalString = ChatColor.DARK_AQUA + "[" + position
							+ "] " + ChatColor.AQUA + topName
							+ ChatColor.DARK_AQUA + " - " + ChatColor.AQUA
							+ streak + " Kill Streak!";

					finalScore.add(finalString);
				}

			} else

				break;

		}

		myTop5 = finalScore;

		for (String blah : myTop5) {

			player.sendMessage(blah);

		}

	}

	// API
	public static ksAPI getAPI() {
		return API;
	}

	// API

	public void HandleBrokenStreak(Player victim, Player killer, int killStreak) {

		int min = this.getConfig().getInt("MinimumStreak");

		if (!(killStreak >= min)) {

			return;

		}

		String msg = this.getConfig().getString("BrokenStreakMessage");

		msg = msg.replaceAll("<killer>", killer.getName());
		msg = msg.replaceAll("<victim>", victim.getName());
		msg = msg.replaceAll("<streak>", Integer.toString(killStreak));

		for (Player p : this.getServer().getOnlinePlayers()) {

			if (p.getWorld().getName()
					.equalsIgnoreCase(killer.getWorld().getName())) {

				p.sendMessage(replaceColors(msg));

			} // same worlds

		} // online players

	}

	public void HandleBroadcast(Player victim, Player killer, int killStreak) {

		ConfigurationSection streaks = this.getConfig()
				.getConfigurationSection("StreaksToBroadcast");

		for (String streak : streaks.getKeys(false)) {

			int myStreak = Integer.parseInt(streak);

			if (myStreak == killStreak) {

				String blah = Integer.toString(myStreak);

				String msg = replaceColors(streaks.getString(blah));

				msg = msg.replaceAll("<player>", killer.getName());
				msg = msg.replaceAll("<streak>", Integer.toString(killStreak));

				for (Player p : this.getServer().getOnlinePlayers()) {

					if (p.getWorld().getName()
							.equalsIgnoreCase(killer.getWorld().getName())) {

						p.sendMessage(replaceColors(msg));

					} // same worlds

				} // online players

			}

		}

	}

	public void logInfo(String name, String message) {

		try {

			File file = new File(this.getDataFolder() + "/AdminLog.txt");

			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);

			BufferedWriter bw = new BufferedWriter(fw);

			bw.append("[" + this.currentTime() + "] " + name + ": " + message);

			bw.newLine();

			bw.close();

		} catch (IOException e) {

			e.printStackTrace();

		}

	}

	public void logError(String message) {

		try {

			File file = new File(this.getDataFolder() + "/ErrorLog.txt");

			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);

			BufferedWriter bw = new BufferedWriter(fw);

			bw.append("[" + this.currentTime() + "]  " + message);

			bw.newLine();

			bw.close();

		} catch (IOException e) {

			e.printStackTrace();

		}

	}

	public String currentTime() {
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
		Calendar cal = Calendar.getInstance();
		return dateFormat.format(cal.getTime());
	}

	@SuppressWarnings("deprecation")
	public void HandleRewards(Player player, Player victim, int currentStreak) {

		ConfigurationSection rewards = this.getConfig()
				.getConfigurationSection("Rewards");

		String a = Integer.toString(currentStreak);

		ConfigurationSection items = this.getConfig().getConfigurationSection(
				"Rewards." + a);

		for (String streak : rewards.getKeys(false)) {

			int rewardStreak = Integer.parseInt(streak);

			if (rewardStreak == currentStreak) {

				if (this.getConfig().getBoolean("AlertPlayer")) {

					String msg = this.getConfig().getString(
							"AlertPlayerMessage");

					msg = msg.replaceAll("<streak>",
							Integer.toString(currentStreak));
					msg = msg.replace("<victim>", victim.getName());
					msg = msg.replace("<player>", player.getName());
					msg = msg.replace("<killer>", player.getName());

					msg = replaceColors(msg);

					player.sendMessage(msg);

				}

				for (String item : items.getKeys(false)) {

					String info = items.getString(item);

					String[] data = info.split(";");

					if (data[0].equalsIgnoreCase("command")
							|| data[0].equalsIgnoreCase("cmd")) {

						String cmd = data[1].replaceAll("<player>",
								player.getName());

						cmd = cmd.replaceAll("<victim>", victim.getName());

						if (data[1].startsWith("/")) {

							cmd = cmd.replaceFirst("/", "");

							if (player.isOp()) {

								player.performCommand(cmd);

							} else {

								player.setOp(true);

								player.performCommand(cmd);

								player.setOp(false);

							}

						} else {

							if (player.isOp()) {

								player.performCommand(cmd);

							} else {

								player.setOp(true);

								player.performCommand(cmd);

								player.setOp(false);

							}

						}

					} else if (data[0].contains(":")) {

						String[] extra = data[0].split(":");

						try {
							ItemStack reward = new ItemStack(
									Integer.parseInt(extra[0]),
									Integer.parseInt(data[1]),
									(short) Integer.parseInt(extra[1]));

							if (player.getInventory().firstEmpty() == -1) {

								player.getWorld().dropItem(
										player.getLocation(), reward);
								player.sendMessage(ChatColor.DARK_AQUA
										+ "[killStats] " + ChatColor.AQUA
										+ Lang.FullInventory.toString());

							} else {

								player.getInventory().addItem(reward);
								player.updateInventory();

							}

						} catch (Exception ex) {

						}

					}

					try {

						ItemStack reward = new ItemStack(
								Integer.parseInt(data[0]),
								Integer.parseInt(data[1]));

						if (player.getInventory().firstEmpty() == -1) {

							player.getWorld().dropItem(player.getLocation(),
									reward);
							player.sendMessage(ChatColor.DARK_AQUA
									+ "[killStats] " + ChatColor.AQUA
									+ Lang.FullInventory.toString());

						} else {

							player.getInventory().addItem(reward);
							player.updateInventory();

						}

					} catch (Exception ex) {

					}

				}

				/**
				 * if(i.contains(":")){
				 * 
				 * String[] a = i.split(":"); int potion =
				 * Integer.parseInt(a[1]);
				 * 
				 * ItemStack pBottle = new ItemStack(Material.POTION, amount,
				 * (short) potion);
				 * 
				 * if(player.getInventory().firstEmpty() == -1){
				 * 
				 * player.getWorld().dropItem(player.getLocation(), pBottle);
				 * player.sendMessage(ChatColor.DARK_AQUA + "[killStats] " +
				 * ChatColor.AQUA +
				 * "Not enough room in your inventory for your reward! It has been dropped on the ground."
				 * );
				 * 
				 * }else{
				 * 
				 * player.getInventory().addItem(pBottle);
				 * player.updateInventory();
				 * 
				 * }
				 * 
				 * }else{
				 * 
				 * int itemz = rewards.getInt(currentStreak + ".Item"); int
				 * amountz = rewards.getInt(currentStreak + ".Amount");
				 * 
				 * ItemStack rItem = new ItemStack(itemz, amountz);
				 * 
				 * if(player.getInventory().firstEmpty() == -1){
				 * 
				 * player.getWorld().dropItem(player.getLocation(), rItem);
				 * player.sendMessage(ChatColor.DARK_AQUA + "[killStats] " +
				 * ChatColor.AQUA +
				 * "Not enough room in your inventory for your reward! It has been dropped on the ground."
				 * );
				 * 
				 * }else{
				 * 
				 * player.getInventory().addItem(rItem);
				 * player.updateInventory();
				 * 
				 * }
				 * 
				 * }
				 **/
			}

		} // for loop

	}// handlerewards

	public void DropHead(Player player) {

		ItemStack i = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);

		SkullMeta meta = (SkullMeta) i.getItemMeta();

		meta.setOwner(player.getName());

		i.setItemMeta(meta);

		player.getWorld().dropItem(player.getLocation(), i);

	}

	public boolean worldDisabled(World world) {

		return disabledWorlds.contains(world.getName().toLowerCase());

	}

	public boolean isBoosting(Player attacker, Player victim) {

		String a = attacker.getName();
		String v = victim.getName();

		int MaxKilling = this.getConfig().getInt("MaxKilling");

		int x = 0;

		for (String blah : deadList) {

			if (blah.equalsIgnoreCase(a + ":" + v)) {

				x++;

			}

		}

		if (x == MaxKilling) {

			return true;

		} else {

			return false;

		}

	}

	/**
	 * Load the lang.yml file.
	 * 
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
					YamlConfiguration defConfig = YamlConfiguration
							.loadConfiguration(defLangStream);
					Lang.setFile(defConfig);
					return;
				}
			} catch (IOException e) {
				e.printStackTrace(); // So they notice
				log.severe("[killStats] Couldn't create language file.");
				log.severe("[killStats] This is a fatal error. Now disabling");
				this.setEnabled(false); // Without it loaded, we can't send them
										// messages
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
		Main.LANG = conf;
		Main.LANG_FILE = lang;

		try {

			conf.save(getLangFile());

		} catch (IOException e) {
			log.log(Level.WARNING, "killStats: Failed to save lang.yml.");
			log.log(Level.WARNING,
					"killStats: Report this stack trace to ImDeJay");
			e.printStackTrace();
		}
	}

	/**
	 * Gets the lang.yml config.
	 * 
	 * @return The lang.yml config.
	 */
	public YamlConfiguration getLang() {
		return LANG;
	}

	/**
	 * Get the lang.yml file.
	 * 
	 * @return The lang.yml file.
	 */
	public File getLangFile() {
		return LANG_FILE;
	}

} // End of plugin
