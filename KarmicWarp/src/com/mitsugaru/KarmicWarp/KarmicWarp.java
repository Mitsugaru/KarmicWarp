package com.mitsugaru.KarmicWarp;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import lib.PatPeter.SQLibrary.SQLite;

public class KarmicWarp extends JavaPlugin {
	//Class variables
	private Logger syslog;
	private SQLite database;
	private final static String prefix = "[KarmicWarp]";
	private Commander commander;
	private Config config;
	private PermCheck perm;

	@Override
	public void onDisable() {
		this.saveConfig();
		// Disconnect from sql database? Dunno if necessary
		if (database.checkConnection())
		{
			// Close connection
			database.close();
		}
		syslog.info(prefix + " Plugin disabled");
	}

	@Override
	public void onEnable() {
		//Logger
		syslog = this.getServer().getLogger();

		//config
		config = new Config(this);

		//Create permission handler
		perm = new PermCheck();

		//Grab commander to handle commands
		commander = new Commander(this);
		getCommand("warp").setExecutor(commander);

		// Connect to sql database
		database = new SQLite(syslog, prefix, "warps", this.getDataFolder()
				.getAbsolutePath());
		// Check if item table exists
		if (!database.checkTable("warps"))
		{
			syslog.info(prefix + " Created warp table");
			database.createTable("CREATE TABLE `warps` (`name` TEXT NOT NULL,`world` TEXT NOT NULL,`x` REAL NOT NULL,`y` REAL NOT NULL, 'z' REAL NOT NULL);");
		}
		syslog.info(prefix + " KarmicWarp v" + this.getDescription().getVersion() + " enabled");
	}

	public PermCheck getPermissionHandler()
	{
		return perm;
	}

	/**
	 * Returns the console log object
	 *
	 * @return Logger object
	 */
	public Logger getLogger() {
		return syslog;
	}

	/**
	 * Returns the plugin's prefix
	 *
	 * @return String of plugin prefix
	 */
	public String getPluginPrefix() {
		return prefix;
	}

	/**
	 *  Returns SQLite database
	 *
	 *  @return SQLite database
	 */
	public SQLite getLiteDB() {
		return database;
	}

	/**
	 * Returns Config object
	 *
	 * @return Config object
	 */
	public Config getPluginConfig() {
		return config;
	}
}
