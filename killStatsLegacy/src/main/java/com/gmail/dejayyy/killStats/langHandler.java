package com.gmail.dejayyy.killStats;

import org.bukkit.configuration.file.YamlConfiguration;

import org.bukkit.ChatColor;

public class langHandler {

	/**
	* An enum for requesting strings from the language file.
	* @author gomeow
	*/
	public enum Lang {
		
	    ConfigOutOfDate("ConfigOutOfDate", "KillStats configuration is out of date, some features may not work. You may want to update it!"),
	    ViewOriginalConfig("ViewOriginalConfig", "View the file 'OriginalConfig.txt' to see what was changed, or just completely regenerate it."),
	    CannotRunFromConsole("CannotRunFromConsole", "You goofball, you cant run that command from console!"),
	    NoStatsToDisplay("NoStatsToDisplay", "No stats to display..."),
	    AboutToReset("AboutToReset", "You are about to reset all of your stats and start with nothing..."),
	    AnswerWithYesNo("AnswerWithYesNo", "Please answer in chat with yes/no within the next 15 seconds."),
	    RequestTimedOut("RequestTimedOut", "You took too long to respond, your request to start over was cancelled."),
	    ReloadComplte("ReloadComplte", "Reload complete."),
	    NoPermission("NoPermission", "Sorry, you don't have permission to run this command."),
	    PlayerNotFound("PlayerNotFound", "Player not found."),
	    ResetSuccess("ResetSuccess", "killStats profile reset was successful."),
	    PersonalResetSuccess("PersonalResetSuccess", "Your stats have been successfully reset!"),
	    RequestCancelled("RequestCancelled", "Your request to start over was cancelled."),
	    InvalidAnswer("InvalidAnswer", "That is not a valid answer... Please answer with yes/no."),
	    Kills("Kills", "Kills"),
	    Deaths("Deaths", "Deaths"),
	    Ratio("Ratio", "Ratio"),
	    Killstreak("Killstreak", "Killstreak"),
	    CmdHelp_ViewPersonal("CmdHelp-ViewPersonal", "View your personal KillStats."),
	    CmdHelp_Reset("CmdHelp-Reset", "Reset all of your stats, this cannot be un-done."),
	    CmdHelp_ViewPlayer("CmdHelp-ViewPlayer", "View another players KillStats."),
	    CmdHelp_ViewTop("CmdHelp-ViewTop", "View the top players of the server based on certain criteria."),
	    CmdHelp_PluginInfo("CmdHelp-PluginInfo", "View information about the plugin."),
	    CmdHelp_Reload("CmdHelp-Reload", "Reload the KillStats config and playerdata."),
	    CmdHelp_Prune(" CmdHelp-Prune", "Prune players if they have 0 kills/deaths. Type = kills/deaths/inactive"),
	    CmdHelp_ResetPlayer("CmdHelp-ResetPlayer", "Reset a players KillStats."),
	    FullInventory("FullInventory", "Not enough room in your inventory for your reward! It has been dropped on the ground."),
	    AreYouSure("AreYouSure", "Are you sure you want to do this?");
	 
	    private String path;
	    private String def;
	    private static YamlConfiguration LANG;

	    /**
	     * Lang enum constructor.
	     * @param path The string path.
	     * @param start The default string.
	     */
	    Lang(String path, String start) {
	        this.path = path;
	        this.def = start;
	    }

	    /**
	     * Set the {@code YamlConfiguration} to use.
	     * @param config The config to set.
	     */
	    public static void setFile(YamlConfiguration config) {
	        LANG = config;
	    }

	    @Override
	    public String toString() {
	        return ChatColor.translateAlternateColorCodes('&', LANG.getString(this.path, def));
	    }

	    /**
	     * Get the default value of the path.
	     * @return The default value of the path.
	     */
	    public String getDefault() {
	        return this.def;
	    }

	    /**
	     * Get the path to the string.
	     * @return The path to the string.
	     */
	    public String getPath() {
	        return this.path;
	    }
	}
	
}
