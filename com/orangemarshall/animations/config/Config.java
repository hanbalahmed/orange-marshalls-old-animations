package com.orangemarshall.animations.config;

import java.io.File;

public class Config extends ConfigurationHolder {

    private static Config instance;
    @ConfigurationHolder.ConfigOpt(
        category = "Old Animations",
        name = "Enable old blockhitting"
    )
    public boolean blockhit = true;
    @ConfigurationHolder.ConfigOpt(
        category = "Old Animations",
        name = "Enable punching while consuming food / potions, drawing a bow, ..."
    )
    public boolean punching = true;
    @ConfigurationHolder.ConfigOpt(
        category = "Old Animations",
        name = "Disable hearts in hotbar flashing"
    )
    public boolean flashingHearts = true;
    @ConfigurationHolder.ConfigOpt(
        category = "Old Animations",
        name = "Enable armor turning red on taking damage"
    )
    public boolean redArmor = true;
    @ConfigurationHolder.ConfigOpt(
        category = "Old Animations",
        name = "Enable deep red for armor turning red"
    )
    public boolean deepRed = true;
    @ConfigurationHolder.ConfigOpt(
        category = "Old Animations",
        name = "Enable 1st person rod scaling"
    )
    public boolean oldRod = true;
    @ConfigurationHolder.ConfigOpt(
        category = "Old Animations",
        name = "Enable 3rd person sword block position"
    )
    public boolean thirdPersonBlocking = true;
    @ConfigurationHolder.ConfigOpt(
        category = "Old Animations",
        name = "Enable old enchantment glint"
    )
    public boolean oldEnchantGlint = true;

    public Config(File configFile, String configVersion) {
        super("§e§lOld Animations Config", configFile, configVersion);
        Config.instance = this;
    }

    public static Config getInstance() {
        return Config.instance;
    }
}
