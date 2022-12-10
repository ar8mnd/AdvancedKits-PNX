package com.fleynaro.advancedkits;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.Listener;
import com.fleynaro.advancedkits.economy.EconomyManager;
import com.fleynaro.advancedkits.lang.LangManager;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Main extends PluginBase implements Listener {

    public static Main instance;
    public Map<String, Kit> kits = new HashMap<>();
    EconomyManager economy;
    boolean permManager;
    LangManager langManager;

    @Override
    public void onEnable() {
        instance = this;

        this.saveDefaultConfig();
        this.saveResource("lang/" + this.getServer().getLanguage().getLang().toUpperCase() + ".properties", "/lang.properties", false);
        this.loadKits();
        this.economy = new EconomyManager(this);
        this.langManager = new LangManager(this);
        this.permManager = this.getConfig().getBoolean("permissions");

        this.getServer().getScheduler().scheduleRepeatingTask(this, new Runnable() {
            private int min = 0;
            private final int everyMin = getConfig().getInt("autosave");

            public void run() {
                for (Kit kit : kits.values()) {
                    kit.processCoolDown();
                }
                if (++this.min % everyMin == 0) {
                    for (Kit kit : kits.values()) {
                        kit.save();
                    }
                }
            }
        }, 1200);

        this.getServer().getPluginManager().registerEvents(new EventListener(this), this);

        if (getConfig().getBoolean("first-join-kit.enabled")) {
            this.getServer().getPluginManager().registerEvents(new JoinKitListener(this), this);
        }
    }

    @Override
    public void onDisable() {
        for (Kit kit : kits.values()) {
            kit.save();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("kit")) {
            if (args.length == 2 && sender.isOp()) {
                Kit kit = this.getKit(args[0]);
                if (kit == null) {
                    sender.sendMessage(this.langManager.getTranslation("no-kit", new String[]{args[0]}));
                    return true;
                }
                Player p = getServer().getPlayer(args[1]);
                if (p == null) {
                    sender.sendMessage(this.langManager.getTranslation("no-player", new String[]{args[1]}));
                    return true;
                }
                kit.addTo(p);
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage(this.langManager.getTranslation("in-game"));
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage(this.langManager.getTranslation("av-kits", new String[]{this.getKitList()}));
                return true;
            }
            if (args.length == 1) {
                Kit kit = this.getKit(args[0]);
                if (kit == null) {
                    sender.sendMessage(this.langManager.getTranslation("no-kit", new String[]{args[0]}));
                    return true;
                }
                kit.handleRequest((Player) sender);
                return true;
            }
            return false;
        }
        return true;
    }

    private void loadKits() {
        this.saveResource("kits.yml");

        Config cfgKits = new Config(this.getDataFolder() + "/kits.yml", Config.YAML);

        this.kits.clear();

        for (Map.Entry<String, Object> entry : cfgKits.getAll().entrySet()) {
            this.kits.put(entry.getKey(), new Kit(this, (ConfigSection) entry.getValue(), entry.getKey()));
        }
    }

    public String getKitList() {
        StringBuilder allKits = new StringBuilder();

        for (String kitName : kits.keySet()) {
            allKits.append(kitName).append("|");
        }

        return allKits.substring(0, allKits.length() - 1);
    }

    public Kit getKit(String kit) {
        Map<String, Kit> lowerKeys = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        lowerKeys.putAll(kits);

        if (lowerKeys.containsKey(kit.toLowerCase())) {
            return lowerKeys.get(kit.toLowerCase());
        }

        return null;
    }
}