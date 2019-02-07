package com.gmail.dejayyy.killStats.ksLang;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import com.gmail.dejayyy.killStats.ksMain;
import com.gmail.dejayyy.killStats.API.ksAPI;


public class langHandler {

    public static ksAPI api;

    public langHandler(ksMain plugin) {

        api = new ksAPI(this);

    }


    /**
     * An enum for requesting strings from the language file.
     *
     * @author gomeow
     */
    public enum Lang {
        Prefix("Plugin_Prefix", "&7[&3killStats&7] &r"),
        Booster_Alert("Booster_Alert", "&bYour account has been flagged for boosting! Your kills will not be counted toward your killStats profile."),
        Player_Not_Found("Player_Not_Found", "&bPlayer not found!"),
        Broken_Streak_Message("Broken_Streak_Message", "&b%killer.name% &3just ruined &b%victim.name%&3's killstreak of &3%victim.streak%"),
        Full_Inventory_Message("Full_Inventory_Message", "Not enough room in your inventory for your reward! It has been dropped on the ground."),
        Stat_Display_Seperator("Stat_Display_Seperator", "&b=-=-=-=-=-=-="),
        Stat_Display_Kills("Stat_Display_Kills", "&3Kills: &b"),
        Stat_Display_Deaths("Stat_Display_Deaths", "&3Deaths: &b"),
        Stat_Display_KillStreak("Stat_Display_KillStreak", "&3Kill Streak: &b"),
        Stat_Display_Ratio("Stat_Display_Ratio", "&3Ratio: &b"),
        World_Disabled("World_Disabled", "&bStatistics are disabled in this world..."),
        Reward_Message("Reward_Message", "&3You've been rewarded some free items for getting a &b%killer.streak% &3killstreak!"),
        Reload_Complete("Reload_Complete", "&bReload Complete!"),
        No_Permission("No_Permission", "&bYou do not have permission to execute that command."),
        Invalid_Console_Command("Invalid_Console_Command", "&bYou cannot run this command from console..."),
        About_To_Startover("About_To_Startover", "&bYou are about to reset all your stats to 0... Are you sure?(yes/no)"),
        Startover_Time_Expired("Startover_Time_Expired", "&bYou didnt answer within the allotted time, your request to startover was cancelled."),
        Startover_Success("Startover_Success", "&bYour stats have successfully been reset!"),
        Startover_Cancelled("Startover_Cancelled", "&bYour request to startover has been cancelled."),
        Invalid_Answer("Invalid_Answer", "&bThat was an invalid answer, please answer with a yes/no."),
        Scoreboard_Kills("Scoreboard_Kills", "&3Kills:"),
        Scoreboard_Deaths("Scoreboard_Deaths", "&3Deaths:"),
        Scoreboard_KillStreak("Scoreboard_KillStreak", "&3KillStreak:"),
        Scoreboard_Ratio("Scoreboard_Ratio", "&3Ratio:"),
        Scoreboard_Enabled("Scoreboard_Enabled", "&bYou have successfully enabled the scoreboard!"),
        Scoreboard_Disabled("Scoreboard_Disabled", "&bYou have successfully disabled the scoreboard!"),
        Scoreboard_Title("Scoreboard_Title", "&b%victim.name%"),
        Scoreboard_Not_Allowed("Scoreboard_Not_Allowed", "&BSorry, scoreboards are disabled for this plugin."),
        Disabled_Message("Disabled_Message", "&bThe plugin is disabled at the moment. Try again in a bit."),
        Plugin_Disabled("Plugin_Disabled", "&bYou have successfully disabled the plugin."),
        Plugin_Enabled("Plugin_Enabled", "&bYou have successfully enabled the plugin."),
        Help_Personal("Help_Personal", "&3View your personal stats."),
        Help_Other("Help_Other", "&3View another players stats."),
        Help_Scoreboard("Help_Scoreboard", "&3Enable/disable the scoreboard feature."),
        Help_Startover("Help_Startover", "&3Reset all your stats. This cannot be un-done."),
        Help_Top("Help_Top", "&3View the top players of the server."),
        Help_Reset("Help_Reset", "&3Reset a players stats."),
        Help_Head("Help_Head", "&3Get the head of the given player."),
        Help_Rank("Help_Rank", "&3View your personal killStats rankings."),
        Help_Reload("Help_Reload", "&3Reload killStats data files"),
        Help_Enable("Help_Enable", "&3Enable the plugin."),
        Help_Disable("Help_Disable", "&3Disable the plugin"),
        Help_Admin("Help_Admin", "&3Looking for admin commands? Try &b/ksa ?"),
        Reset_Success("Reset_Success", "&3You have successfully reset &b%victim.name%'s &3stats"),
        Scoreboard_Toggle("Scoreboard_Toggle", "&3Did you know you can toggle the scoreboard? Try it!  &b/ks scoreboard"),
        TS_No_Players("TS_No_Players", "&bNo players to display..."),
        TS_Chat_Header_Kills("TS_Chat_Header_Kills", "&b=-=-= Most Kills =-=-="),
        TS_Chat_Header_Deaths("TS_Chat_Header_Deaths", "&b=-=-= Most Deaths =-=-="),
        TS_Chat_Header_Streak("TS_Chat_Header_Streak", "&b=-=-= Best Killstreak =-=-="),
        TS_Chat_Header_Ratio("TS_Chat_Header_Ratio", "&b=-=-= Best Ratio =-=-="),
        TS_Chat_Format("TS_Chat_Format", "&b%stats.position%&7: &b%stats.playername% &7- &b%stats.value%"),
        TS_Chat_Page("TS_Chat_Page", "&bViewing page %stats.currentpage%/%stats.maxpage%."),
        TS_Scoreboard_Page("TS_Scoreboard_Page", "&3Page &b%stats.currentpage%&3/&b%stats.maxpage%"),
        TS_Chat_Last_Update("TS_Chat_Last_Update", "&3Last updated &3%stats.lastupdate% &3minutes ago."),
        Rank_Kills("Rank_Kills", "&3Kills: &b%ranking%"),
        Rank_Deaths("Rank_Deaths", "&3Deaths: &b%ranking%"),
        Rank_Streak("Rank_Streak", "&3Killstreak: &b%ranking%"),
        Rank_Ratio("Rank_Ratio", "&3Ratio: &b%ranking%"),
        Rank_Scoreboard_Header("Rank_Scoreboard_Header", "&bPersonal Rankings"),
        Rank_Chat_Header("Rank_Chat_Header", "&3=-=-= &bPersonal Rankings &3=-=-="),
        Rank_Not_Yet_Ranked("Rank_Not_Yet_Ranked", "&bYou are not yet ranked... You should start pvping!"),
        Scoreboard_Already_Open("Scoreboard_Already_Open", "&4The killStats scoreboard is already being used.. Try again in a few seconds."),
        Sign_Invalid_Format("Sign_Invalid_Format", "&4Invalid sign format."),
        Sign_Top_Kills("Sign_Top_Kills", "&bTop Kills #%ranking%"),
        Sign_Top_Deaths("Sign_Top_Deaths", "&bTop Deaths #%ranking%"),
        Sign_Top_Streak("Sign_Top_Streak", "&bTop Streak #%ranking"),
        Sign_Top_Ratio("Sign_Top_Ratio", "&bTop Ratio #%ranking"),
        Sign_Playername_Color("Sign_Playername_Color", "&b"),
        Sign_Stats_Color("Sign_Stats", "&b"),

        Invalid_Command("Invalid_Command", "&4Invalid command... Need help with commands? Try &b/ks ?");

        private String path;
        private String def;
        private static YamlConfiguration LANG;

        /**
         * Lang enum constructor.
         *
         * @param path  The string path.
         * @param start The default string.
         */
        Lang(String path, String start) {

            this.path = path;
            this.def = start;

        }

        /**
         * Set the {@code YamlConfiguration} to use.
         *
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
         *
         * @return The default value of the path.
         */
        public String getDefault() {
            return this.def;
        }

        /**
         * Get the path to the string.
         *
         * @return The path to the string.
         */
        public String getPath() {

            return this.path;
        }

    }

}
