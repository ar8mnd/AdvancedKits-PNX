package com.fleynaro.advancedkits.economy;

import angga7togk.economyapi.EconomyAPI;
import angga7togk.economyapi.database.EconomyDB;
import cn.nukkit.Player;
import com.fleynaro.advancedkits.Main;

public class EconomyManager {

    private EconomyAPI api;

    public EconomyManager(Main plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("EconomyAPI") != null) {
            api = EconomyAPI.getInstance();
        }
    }

    public boolean grantKit(Player player, int money) {
        return this.api != null && EconomyDB.reduceMoney(player, money);
    }
}