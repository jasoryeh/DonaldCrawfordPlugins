package com.gmail.dejayyy.coloredTab;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class tabMain extends JavaPlugin implements Listener {

    public static Permission perm = null;


    public void onEnable() {

        this.getServer().getPluginManager().registerEvents(this, this);

        this.saveDefaultConfig();

    }

    public void onDisableI() {


    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {

        if (!(this.getConfig().isConfigurationSection("Groups"))) {

            return;

        }


        Permission perm = VaultAdapter.getPermissions();

        Player player = event.getPlayer();

        ConfigurationSection groups = this.getConfig().getConfigurationSection("Groups");


        for (String groupName : groups.getKeys(false)) {

            String color = groups.getString(groupName);

            if (perm.playerInGroup(player, groupName)) {

                try {

                    if (player.getName().length() > 16) {

                        player.setPlayerListName(replaceColors(color) + player.getName().substring(0, 14));

                    } else {

                        player.setPlayerListName(replaceColors(color) + player.getName());

                    }


                } catch (Exception ex) {


                }


            }

        }

    }

    static String replaceColors(String message) {

        return ChatColor.translateAlternateColorCodes('&', message);

    }

}
