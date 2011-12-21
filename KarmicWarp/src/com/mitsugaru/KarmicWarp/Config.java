/**
 * Config file mimicking DiddiZ's Config class
 * file in LB. Tailored for this plugin.
 *
 * @author Mitsugaru
 */
package com.mitsugaru.KarmicWarp;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;

public class Config {
	// Class variables
	private KarmicWarp plugin;
	public boolean debugTime;
	public int listlimit;

	// TODO ability to change config in-game

	// IDEA Ability to change the colors for all parameters
	// such as item name, amount, data value, id value, enchantment name,
	// enchantment lvl
	// page numbers, maybe even header titles
	/**
	 * Constructor and initializer
	 *
	 * @param KarmicShare plugin
	 */
	public Config(KarmicWarp kw) {
		plugin = kw;
		// Grab config
		ConfigurationSection config = plugin.getConfig();
		// Hashmap of defaults
		final Map<String, Object> defaults = new HashMap<String, Object>();
		defaults.put("version", plugin.getDescription().getVersion());
		defaults.put("listlimit", 10);
		// Insert defaults into config file if they're not present
		for (final Entry<String, Object> e : defaults.entrySet())
		{
			if (!config.contains(e.getKey()))
			{
				config.set(e.getKey(), e.getValue());
			}
		}
		// Save config
		plugin.saveConfig();
		// Load variables from config
		listlimit = config.getInt("listlimit", 10);
		debugTime = config.getBoolean("debugTime", false);
		// Check if need to update
		if (Double.parseDouble(plugin.getDescription().getVersion()) > Double
				.parseDouble(config.getString("version")))
		{
			// Update to latest version
			plugin.getLogger().info(
					plugin.getPluginPrefix() + " Updating to v"
							+ plugin.getDescription().getVersion());
			this.update();
		}
	}

	/**
	 * This method is called to make the appropriate changes, most likely only
	 * necessary for database schema modification, for a proper update.
	 */
	private void update() {
		// Update version number in config.yml
		plugin.getConfig().set("version", plugin.getDescription().getVersion());
		plugin.saveConfig();
	}

	/**
	 * Reloads info from yaml file(s)
	 */
	public void reloadConfig() {
		// Initial relaod
		plugin.reloadConfig();
		// Grab config
		ConfigurationSection config = plugin.getConfig();
		listlimit = config.getInt("listlimit", 10);
		debugTime = config.getBoolean("debugTime", false);
		// Check bounds
		this.boundsCheck();
		plugin.getLogger().info(plugin.getPluginPrefix() + " Config reloaded");
	}

	/**
	 * Check the bounds on the parameters to make sure that
	 * all config variables are legal and usable by the plugin
	 */
	private void boundsCheck() {
		// Check that list is actually going to output something, based on limit
		// given
		if (listlimit < 1)
		{
			listlimit = 10;
			plugin.getLogger().warning(
					plugin.getPluginPrefix()
							+ " List limit is lower than 1. Using default.");
		}
	}
}
