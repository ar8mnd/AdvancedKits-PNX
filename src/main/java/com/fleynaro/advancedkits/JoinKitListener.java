package com.fleynaro.advancedkits;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;

class JoinKitListener implements Listener {

    private Main ak;

    public JoinKitListener(Main ak) {
        this.ak = ak;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (!p.hasPlayedBefore()) {
            Kit kit = ak.getKit(ak.getConfig().getString("first-join-kit.kit"));
            if (kit != null) {
                kit.addTo(p);
            }
        }
    }
}
