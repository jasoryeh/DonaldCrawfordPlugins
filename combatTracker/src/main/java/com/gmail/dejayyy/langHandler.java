package com.gmail.dejayyy;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

public class langHandler {

    /**
     * An enum for requesting strings from the language file.
     *
     * @author gomeow
     */
    public enum Lang {

        ReloadComplete("ReloadComplete", "Reload Complete."),
        DisabledAndCleared("DisabledAndCleared", "All players taken out of combat, and plugin disabled."),
        PluginDisabled("PluginDisabled", "Plugin has been disabled, all players have been taken out of combat."),
        PluginEnabled("PluginEnabled", "Plugin is now enabled!"),
        CannotRunFromConsole("CannotRunFromConsole", "You goofball, you cant run this command from console!"),
        NoPermission("NoPermission", "You don''t have permission to run this command!"),
        InCombat("InCombat", "You are in combat!"),
        PluginAuthor("PluginAuthor", "Plugin Author: "),
        CombatTime("CombatTime", "Combat Time: &b%a"),
        NotInCombat("NotInCombat", "You are not in combat!"),
        TakenOutOfCombat("TakenOutOfCombat", "%p  was taken out of combat!"),
        PlayerNotFound("PlayerNotFound", "Player not found."),
        isPluginEnabled("isPluginEnabled", "Plugin enabled: "),
        PlayersInCombat("PlayersInCombat", "Players in combat: "),
        cmdHelp_Check("cmdHelp-Check", "Command to see if you are in combat."),
        cmdHelp_Reload("cmdHelp-Reload", "Reload the combatTracker configuration file."),
        cmdHelp_Disable("cmdHelp-Disable", "Disable combatTracker."),
        cmdHelp_Enable("cmdHelp-Enable", "Enable combatTracker."),
        cmdHelp_TakeOutOfCombat("cmdHelp-TakeOutOfCombat", "Take a specified player out of combat.");

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
