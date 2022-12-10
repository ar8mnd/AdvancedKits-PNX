package com.fleynaro.advancedkits;

import cn.nukkit.Player;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import java.util.LinkedHashMap;

public class Kit {

    private final Main ak;
    private final ConfigSection data;
    private final String name;
    private int cost = 0;
    private final int coolDown;
    private final Config coolDowns;
    private final ConfigSection coolDownsPlayer;

    public Kit(Main ak, ConfigSection data, String name) {
        this.ak = ak;
        this.data = data;
        this.name = name;
        this.coolDown = this.getCoolDownMinutes();
        if (this.data.containsKey("money")) {
            this.cost = this.data.getInt("money");
        }

        coolDowns = new Config(this.ak.getDataFolder() + "/cooldowns/" + this.name.toLowerCase() + ".yml", Config.YAML,
                new ConfigSection(new LinkedHashMap<String, Object>() {
                    {
                        put("players", new ConfigSection());
                    }
                }));
        coolDownsPlayer = this.coolDowns.getSection("players");
    }

    public boolean handleRequest(Player player) {
        if (this.testPermission(player)) {
            if (!this.coolDownsPlayer.exists(player.getName().toLowerCase()) || player.hasPermission("advancedkits.bypasscooldown")) {
                if (this.cost > 0) {
                    if (this.ak.economy.grantKit(player, this.cost)) {
                        this.addTo(player);
                        player.sendMessage(this.ak.langManager.getTranslation("sel-kit", new String[] { this.name }));
                        return true;
                    } else {
                        player.sendMessage(this.ak.langManager.getTranslation("cant-afford", new String[] { this.name }));
                    }
                } else {
                    this.addTo(player);
                    player.sendMessage(this.ak.langManager.getTranslation("sel-kit", new String[] { this.name }));
                    return true;
                }
            } else {
                player.sendMessage(this.ak.langManager.getTranslation("cooldown",
                        new String[] { this.getCoolDownLeft(player) + "" }));
            }
        } else {
            player.sendMessage(this.ak.langManager.getTranslation("no-perm", new String[] { this.name }));
        }
        return false;
    }

    public void addTo(Player player) {
        PlayerInventory inv = player.getInventory();

        if (ak.getConfig().getBoolean("clear-inventory")) {
            inv.clearAll();
            player.removeAllEffects();
        }

        for (String itemInfo : this.data.getStringList("items")) {
            Item item = this.loadItem(itemInfo);
            inv.setItem(inv.firstEmpty(item), item);
        }

        if (this.data.containsKey("helmet") && !this.data.getString("helmet").isEmpty()) {
            inv.setHelmet(this.loadItem(this.data.getString("helmet")));
        }
        if (this.data.containsKey("chestplate") && !this.data.getString("chestplate").isEmpty()) {
            inv.setChestplate(this.loadItem(this.data.getString("chestplate")));
        }
        if (this.data.containsKey("leggings") && !this.data.getString("leggings").isEmpty()) {
            inv.setLeggings(this.loadItem(this.data.getString("leggings")));
        }
        if (this.data.containsKey("boots") && !this.data.getString("boots").isEmpty()) {
            inv.setBoots(this.loadItem(this.data.getString("boots")));
        }

        if (this.data.containsKey("effects") && this.data.isList("effects")) {
            for (String effectInfo : this.data.getStringList("effects")) {
                player.addEffect(this.loadEffect(effectInfo));
            }
        }

        if (this.data.containsKey("commands") && this.data.isList("commands")) {
            for (String cmdInfo : this.data.getStringList("commands")) {
                this.ak.getServer().dispatchCommand(new ConsoleCommandSender(),
                        cmdInfo.replace("{player}", player.getName()));
            }
        }

        if (this.coolDown > 0) {
            this.coolDownsPlayer.set(player.getName().toLowerCase(), this.coolDown);
        }
    }

    private Item loadItem(String info) {
        String[] itemInfo = info.split(":");

        Item item = Item.get(Integer.parseInt(itemInfo[0]), Integer.parseInt(itemInfo[1]),
        Integer.parseInt(itemInfo[2]));
        if (itemInfo.length > 3 && !itemInfo[3].equals("default")) {
            item.setCustomName(itemInfo[3]);
        }

        for (int i = 4; i <= itemInfo.length - 2; i += 2) {
            Enchantment enchant = Enchantment.getEnchantment(Integer.parseInt(itemInfo[i]));
            enchant.setLevel(Integer.parseInt(itemInfo[i + 1]), false);
            item.addEnchantment(enchant);
        }

        return item;
    }

    private Effect loadEffect(String info) {
        String[] effectInfo = info.split(":");
        Effect e = Effect.getEffectByName(effectInfo[0]);

        if (e != null) {
            return e.setDuration(Integer.parseInt(effectInfo[1]) * 20).setAmplifier(Integer.parseInt(effectInfo[2]));
        }

        return null;
    }

    private int getCoolDownMinutes() {
        int min = 0;
        if (this.data.containsKey("cooldown")) {
            ConfigSection cooldown = (ConfigSection) this.data.get("cooldown");
            if (cooldown.containsKey("minutes")) {
                min += cooldown.getInt("minutes");
            }
            if (cooldown.containsKey("hours")) {
                min += cooldown.getInt("hours") * 60;
            }
        } else {
            min = 1440;
        }

        return min;
    }

    private String getCoolDownLeft(Player player) {
        int minutes = this.coolDownsPlayer.getInt(player.getName().toLowerCase());
        if (minutes < 60) {
            return this.ak.langManager.getTranslation("cooldown-format1", new String[] { "" + minutes });
        }
        int modulo = minutes % 60;
        if (modulo != 0) {
            return this.ak.langManager.getTranslation("cooldown-format2",
                    new String[] { "" + Math.floor(minutes / 60), "" + modulo });
        }

        return this.ak.langManager.getTranslation("cooldown-format3", new String[] { "" + (minutes / 60) });
    }

    public void processCoolDown() {
        for (String pName : this.coolDownsPlayer.getAll().keySet()) {
            int remainMinute = this.coolDownsPlayer.getInt(pName) - 1;
            this.coolDownsPlayer.set(pName, remainMinute);
            if (remainMinute <= 0) {
                this.coolDownsPlayer.remove(pName);
            }
        }
    }

    private boolean testPermission(Player player) {
        return this.ak.permManager ?
                player.hasPermission("advancedkits." + this.name.toLowerCase()) :
                (!this.data.containsKey("worlds") || this.data.getStringList("worlds").contains(player.getLevel().getName()));
    }

    public void save() {
        this.coolDowns.save();
    }
}
