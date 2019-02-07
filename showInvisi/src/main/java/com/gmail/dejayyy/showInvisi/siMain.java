package com.gmail.dejayyy.showInvisi;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

public class siMain extends JavaPlugin {

    public boolean onCommand(CommandSender sender, Command cmd, String cmdl, String[] args) {

        if (!(sender instanceof Player)) {

            return true;

        }

        Player player = (Player) sender;

        if (cmdl.equalsIgnoreCase("showinvisible") || cmdl.equalsIgnoreCase("si")) {

            if (player.hasPermission("showinvisible.use")) {

                List<Entity> players = player.getNearbyEntities(20, 20, 20);

                List<String> i = new ArrayList<String>();

                for (Entity e : players) {

                    if (e instanceof Player) {

                        Player p = ((Player) e).getPlayer();

                        if (p.hasPotionEffect(PotionEffectType.INVISIBILITY)) {

                            i.add(p.getName());

                        }
                    }
                }

                if (i.size() == 0) {

                    player.sendMessage(ChatColor.DARK_AQUA + "There are no invisible players nearby.");

                } else {

                    player.sendMessage(ChatColor.DARK_AQUA + "There are " + ChatColor.AQUA + i.size() + ChatColor.DARK_AQUA + " invisible players nearby: ");

                    for (String name : i) {

                        int index = i.indexOf(name) + 1;

                        player.sendMessage(ChatColor.DARK_AQUA + "[" + index + "] " + ChatColor.AQUA + name);

                    }

                }
            }
        }

        return true;

    }

}
