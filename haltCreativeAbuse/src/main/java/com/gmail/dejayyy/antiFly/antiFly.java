package com.gmail.dejayyy.antiFly;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class antiFly extends JavaPlugin implements Listener {

    public List<String> combatList = new ArrayList<String>();


    public void onEnable() {

        this.getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();

    }

    public void onDisable() {


    }

    public boolean onCommand(CommandSender sender, Command cmd, String cmdL, String[] args) {

        if (!(sender.hasPermission("hca.admin"))) {

            return false;

        }

        if (cmdL.equalsIgnoreCase("hca")) {

            if (args.length == 1) {

                if (args[0].equalsIgnoreCase("reload")) {

                    this.reloadConfig();

                    sender.sendMessage(ChatColor.DARK_AQUA + "[HCA] " + ChatColor.AQUA + "Reload Complete!");

                    return true;

                } else {

                    sender.sendMessage(ChatColor.DARK_AQUA + "[HCA] " + ChatColor.AQUA + "Invalid Arguments!");

                    return true;

                }

            } else {

                sender.sendMessage(ChatColor.DARK_AQUA + "[HCA] " + ChatColor.AQUA + "Invalid Arguments!");

                return true;

            }
        }


        return true;

    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent event) {


    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void projectileLaunch(ProjectileLaunchEvent event) {

        if (event.isCancelled()) {

            return;

        }

        if (event.getEntity().getShooter() instanceof Player) {

            Player player = (Player) event.getEntity().getShooter();

            if (player.getGameMode().equals(GameMode.SURVIVAL) || player.hasPermission("hca.bypass")) {

                return;

            }

            if (this.getConfig().getBoolean("DisableSplahPotions")) {

                if (event.getEntityType().equals(EntityType.SPLASH_POTION)) {

                    event.setCancelled(true);

                    player.sendMessage(ChatColor.AQUA + "You are not permitted to use that while in creative.");

                }

            }

        }


    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerInteractEntity(PlayerInteractEntityEvent event) {

        Player player = event.getPlayer();

        if (player.getGameMode().equals(GameMode.SURVIVAL) || player.hasPermission("hca.bypass")) {

            return;

        }

        for (String blah : this.getConfig().getStringList("RightClicked")) {

            if (event.getRightClicked().getType().getTypeId() == Short.parseShort(blah)) {

                player.sendMessage(ChatColor.AQUA + "You are not permitted to use that while in creative!");

                event.setCancelled(true);

                return;
            }
        }

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        if (player.getGameMode().equals(GameMode.SURVIVAL) || player.hasPermission("hca.bypass")) {

            return;

        }

        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {

            if (event.getClickedBlock().getState() instanceof Sign) {

                player.sendMessage(ChatColor.AQUA + "You are not permitted to use that while in creative!");

                event.setCancelled(true);

                return;

            }


            for (String blah : this.getConfig().getStringList("RightClickBlock")) {

                if (event.getClickedBlock().getTypeId() == Integer.parseInt(blah) || player.getInventory().getItemInHand().getTypeId() == Integer.parseInt(blah)) {

                    player.sendMessage(ChatColor.AQUA + "You are not permitted to use that while in creative!");

                    event.setCancelled(true);

                }
            }
        }

        if (event.getAction().equals(Action.RIGHT_CLICK_AIR)) {

            for (String blah : this.getConfig().getStringList("RightClickAir")) {

                if (player.getItemInHand().getTypeId() == Integer.parseInt(blah)) {

                    player.sendMessage(ChatColor.AQUA + "You are not permitted to use that while in creative!");

                    event.setCancelled(true);

                }
            }

        }


        if (this.getConfig().getBoolean("DisableSpawnEggs")) {
            if (player.getInventory().getItemInHand().getTypeId() == 383) {

                if (!(player.getGameMode().equals(GameMode.SURVIVAL))) {

                    if (player.hasPermission("hca.bypass")) {

                        return;

                    }

                    event.setCancelled(true);

                    player.sendMessage(ChatColor.AQUA + "You are not permitted to use that while in creative!");

                }

            }
        }

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerCMD(PlayerCommandPreprocessEvent event) {


        Player player = event.getPlayer();

        if (!(player.getGameMode().equals(GameMode.SURVIVAL))) {

            if (!(player.hasPermission("hca.bypass"))) {

                if (event.getMessage().contains(" ")) {

                    String[] blah = event.getMessage().split(" ");

                    for (String a : this.getConfig().getStringList("BlockedCommands")) {

                        if (blah[0].equalsIgnoreCase("/" + a)) {

                            event.setCancelled(true);

                            player.sendMessage(ChatColor.AQUA + "You are not permitted to use that command in creative!");

                            return;

                        }
                    }

                } else {

                    for (String a : this.getConfig().getStringList("BlockedCommands")) {

                        if (event.getMessage().equalsIgnoreCase("/" + a)) {

                            event.setCancelled(true);

                            player.sendMessage(ChatColor.AQUA + "You are not permitted to use that command in creative!");

                            return;

                        }
                    }

                }

            }

        }


        List<String> disabled = this.getConfig().getStringList("DisabledCommands");

        if (combatList.contains(event.getPlayer().getName())) {

            for (String blah : disabled) {

                if (event.getMessage().toLowerCase().startsWith("/" + blah.toLowerCase())) {

                    event.getPlayer().sendMessage(ChatColor.AQUA + "Command Disabled!");
                    event.setCancelled(true);

                }

            }
        }
    }

    @EventHandler
    public void playerTP(PlayerTeleportEvent event) {

        Player player = event.getPlayer();

        if (player.hasPermission("hca.bypass")) {

            return;

        }


        if (!(player.getGameMode().equals(GameMode.SURVIVAL))) {

            player.setGameMode(GameMode.SURVIVAL);

        }

    }

    @EventHandler
    public void playerHit(EntityDamageByEntityEvent event) {

        if (event.isCancelled() || event.getDamage() == 0) {

            return;

        }


        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {

            int coolDown = this.getConfig().getInt("seconds");

            final Player attacker = (Player) event.getDamager();
            final Player victim = (Player) event.getEntity();

            if (attacker.hasPermission("hca.pvp.bypass")) {

                return;
            }

            if (victim.hasPermission("hca.pvp.bypass")) {

                return;

            }
            attacker.setAllowFlight(false);
            victim.setAllowFlight(false);

            attacker.getPlayer().setGameMode(GameMode.SURVIVAL);
            victim.getPlayer().setGameMode(GameMode.SURVIVAL);

            combatList.add(attacker.getName());
            combatList.add(victim.getName());

            coolDown = coolDown * 20;

            // Remove both from the lists after 'x' amount of time
            this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                @Override
                public void run() {

                    combatList.remove(attacker.getName());

                    combatList.remove(victim.getName());

                }

            }, coolDown);

        }


    }


}