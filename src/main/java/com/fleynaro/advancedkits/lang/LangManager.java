package com.fleynaro.advancedkits.lang;

import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;
import com.fleynaro.advancedkits.Main;
import java.util.LinkedHashMap;

public class LangManager {

    private final Main ak;
    private final LinkedHashMap<String, Object> defaults = new LinkedHashMap<>();
    private final Config data;

    public LangManager(Main ak) {
        this.ak = ak;
        this.defaults.put("lang-version", 1);
        this.defaults.put("in-game", "Use 'kit <kit name> <player>'");
        this.defaults.put("av-kits", "Available kits: {%0}");
        this.defaults.put("no-kit", "Kit &e{%0} &fdoes not exist");
        this.defaults.put("sel-kit", "&eKit selected: &b{%0}");
        this.defaults.put("cant-afford", "&cYou cannot afford kit: {%0}");
        this.defaults.put("cooldown", "&dYou will be able to get the kit in &c{%0}");
        this.defaults.put("no-perm", "&cYou don't have permission to use kit &4{%0}");
        this.defaults.put("cooldown-format1", "{%0} minutes");
        this.defaults.put("cooldown-format2", "{%0} hours and {%1} minutes");
        this.defaults.put("cooldown-format3", "{%0} hours");
        this.defaults.put("no-sign-on-kit", "&cKit not specified");
        this.defaults.put("no-perm-sign", "&cYou don't have permission to create a kit sign");
        this.defaults.put("no-player", "§cUnknown player: {%0}");
        this.data = new Config(this.ak.getDataFolder() + "/lang.properties", Config.PROPERTIES, new ConfigSection(this.defaults));
    }

    public String getTranslation(String dataKey) {
        return this.getTranslation(dataKey, null);
    }

    public String getTranslation(String dataKey, String[] args) {
        if (!this.defaults.containsKey(dataKey)) {
            this.ak.getLogger().error("Invalid dataKey " + dataKey + " passed to method LangManager::getTranslation()");
            return "";
        }

        String str = this.data.getString(dataKey, (String) this.defaults.get(dataKey));
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                str = str.replace("{%" + i + "}", args[i]);
            }
        }

        return TextFormat.colorize(str);
    }
}