package com.gmail.dejayyy;

import org.bukkit.entity.Player;

import com.gmail.dejayyy.Main;

public class ctAPI {

    private Main plugin;

    public ctAPI(Main plugin) {

        this.plugin = plugin;

    }

    //API

    public static ctAPI getAPI() {

        return Main.getAPI();

    }
    //API

    public boolean isInCombat(Player player) {

        return plugin.combat.containsKey(player.getName());

    } //isInCombat

}
