package com.mitsugaru.KarmicWarp;

import org.bukkit.plugin.java.JavaPlugin;

import lib.Mitsugaru.SQLibrary.SQLite;

public class KarmicWarp extends JavaPlugin {
	//Class variables
	private SQLite database;
	private final static String prefix = "[KarmicWarp]";
	private Commander commander;
	private Config config;
	private PermCheck perm;

	//TODO Request: Sign warps
	@Override
	public void onDisable() {
		this.saveConfig();
		// Disconnect from sql database? Dunno if necessary
		if (database.checkConnection())
		{
			// Close connection
			database.close();
		}
		getLogger().info(prefix + " Plugin disabled");
	}

	@Override
	public void onLoad()
	{
		//config
		config = new Config(this);
		// Connect to sql database
		database = new SQLite(getLogger(), prefix, "warps", this.getDataFolder()
				.getAbsolutePath());
		// Check if warp table exists
		if (!database.checkTable("warps"))
		{
			getLogger().info(prefix + " Created warp table");
			database.createTable("CREATE TABLE `warps` (`name` TEXT NOT NULL,`world` TEXT NOT NULL,`x` REAL NOT NULL,`y` REAL NOT NULL, 'z' REAL NOT NULL);");
		}
	}

	@Override
	public void onEnable() {
		//Create permission handler
		perm = new PermCheck();

		//Grab commander to handle commands
		commander = new Commander(this);
		getCommand("warp").setExecutor(commander);

		getLogger().info(prefix + " KarmicWarp v" + this.getDescription().getVersion() + " enabled");
	}

	public PermCheck getPermissionHandler()
	{
		return perm;
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
