package com.mitsugaru.KarmicWarp;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commander implements CommandExecutor {
	// Class variables
	private final KarmicWarp kw;
	private final PermCheck perm;
	private final String prefix;
	private final Config config;
	private final static String BAR = "======================";
	public static final String WARP_NAME_REGEX = "[\\p{Alnum}_[\\-]]*";
	private final Map<String, Integer> page = new HashMap<String, Integer>();
	private int limit;

	public Commander(KarmicWarp karmicWarp) {
		kw = karmicWarp;
		prefix = kw.getPluginPrefix();
		config = kw.getPluginConfig();
		perm = kw.getPermissionHandler();
		limit = config.listlimit;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		// TODO Auto-generated method stub
		long time = 0;
		if (config.debugTime)
		{
			time = System.nanoTime();
		}
		// handle commands here
		if (args.length == 0)
		{
			// Version and author
			sender.sendMessage(ChatColor.BLUE + BAR + "=====");
			sender.sendMessage(ChatColor.GREEN + "KarmicWarp v"
					+ kw.getDescription().getVersion());
			sender.sendMessage(ChatColor.GREEN + "Coded by Mitsugaru");
			sender.sendMessage(ChatColor.BLUE + BAR + "=====");
		}
		else
		{
			// Grab command
			final String com = args[0].toLowerCase();
			if (com.equals("help") || com.equals("?"))
			{
				// display help
				this.displayHelp(sender);
			}
			else if (com.equals("create"))
			{
				if (perm.checkPermission(sender, "KarmicWarp.edit"))
				{
					// Create warp
					this.createWarp(sender, args);
				}
				else
				{
					sender.sendMessage(ChatColor.RED + prefix
							+ " Lack permission: KarmicWarp.edit");
				}
			}
			else if (com.equals("remove"))
			{
				if (perm.checkPermission(sender, "KarmicWarp.edit"))
				{
					// Remove warp
					this.removeWarp(sender, args);
				}
				else
				{
					sender.sendMessage(ChatColor.RED + prefix
							+ " Lack permission: KarmicWarp.edit");
				}
			}
			else if(com.equals("update"))
			{
				if(perm.checkPermission(sender, "KarmicWarp.edit"))
				{
					this.updateWarp(sender, args);
				}
				else
				{
					sender.sendMessage(ChatColor.RED + prefix
							+ " Lack permission: KarmicWarp.edit");
				}
			}
			else if (com.equals("list"))
			{
				this.listWarps(sender, args);
			}
			else if (com.equals("prev"))
			{
				// Add player if they don't exist
				if (!page.containsKey(sender.getName()))
				{
					page.put(sender.getName(), 0);
				}
				page.put(sender.getName(), page.get(sender.getName()) - 1);
				this.listWarps(sender, args);
			}
			else if (com.equals("next"))
			{
				// Add player if they don't exist
				if (!page.containsKey(sender.getName()))
				{
					page.put(sender.getName(), 0);
				}
				page.put(sender.getName(), page.get(sender.getName()) + 1);
				this.listWarps(sender, args);
			}
			else
			{
				if (perm.checkPermission(sender, "KarmicWarp.warp"))
				{
					// Possibly a warp name given, attempt to warp
					this.warpCommand(sender, args);
				}
				else
				{
					sender.sendMessage(ChatColor.RED + prefix
							+ " Lack permission: KarmicWarp.warp");
				}
			}
		}

		// Debug time
		if (config.debugTime)
		{
			debugTime(sender, time);
		}
		return true;
	}

	private void updateWarp(CommandSender sender, String[] args) {
		if(sender instanceof Player)
		{
			Player player = (Player) sender;
			if (args.length > 1)
			{
				//Grab warp name
				String name = args[1];
				if(this.warpExists(name))
				{
					// Update warp
					Location l = player.getLocation();
					String query = "UPDATE warps SET world='" + l.getWorld().getName() + "',x='"
							+ l.getX() + "',y='" + l.getY() + "',z='"
							+ l.getZ() + "' WHERE name='" + name + "';";
					kw.getLiteDB().standardQuery(query);
					sender.sendMessage(ChatColor.GREEN + prefix
							+ " Warp updated");
				}
				else
				{
					//Warp does not exist
					sender.sendMessage(ChatColor.RED + prefix
							+ " Warp does not exist.");
				}
			}
			else
			{
				//Need to provide warp name
				sender.sendMessage(ChatColor.RED + prefix
						+ " Missing warp name.");
			}
		}
		else
		{
			//Cannot run as console
			sender.sendMessage(ChatColor.RED + prefix
					+ " Cannot use command as console.");
		}
	}

	private void listWarps(CommandSender sender, String[] args) {
		// Check if player is in page list
		if (!page.containsKey(sender.getName()))
		{
			page.put(sender.getName(), 0);
		}
		if (args.length > 1)
		{
			// Page number given
			try
			{
				// Attempt to parse page number
				int pageAdjust = Integer.parseInt(args[1]);
				page.put(sender.getName(), pageAdjust - 1);
			}
			catch (NumberFormatException e)
			{
				sender.sendMessage(ChatColor.YELLOW + prefix + "'" + args[1]
						+ "' is not a number");
			}
		}

		// Grab list of warps
		ResultSet rs = kw.getLiteDB().select("SELECT * FROM warps;");
		Map<String, Location> list = new HashMap<String, Location>();
		try
		{
			if (rs.next())
			{
				do
				{
					World w = kw.getServer().getWorld(rs.getString("world"));
					// Check if world exists
					if (w != null)
					{
						list.put(
								rs.getString("name"),
								new Location(w, rs.getDouble("x"), rs
										.getDouble("y"), rs.getDouble("z")));
					}
				}
				while (rs.next());
			}
			rs.close();
		}
		catch (SQLException e)
		{
			kw.getLogger().warning(ChatColor.RED + prefix + " SQL exception");
			e.printStackTrace();
		}
		if (list.isEmpty())
		{
			// No warps
			sender.sendMessage(ChatColor.RED + prefix + " No warps");
		}
		else
		{
			// Caluclate amount of pages
			int num = list.size() / limit;
			double rem = (double) list.size() % (double) limit;
			boolean valid = true;
			if (rem != 0)
			{
				num++;
			}
			if (page.get(sender.getName()).intValue() < 0)
			{
				// They tried to use /ks prev when they're on page 0
				sender.sendMessage(ChatColor.YELLOW + prefix
						+ " Page does not exist");
				// reset their current page back to 0
				page.put(sender.getName(), 0);
				valid = false;
			}
			else if ((page.get(sender.getName()).intValue()) * limit > list
					.size())
			{
				// They tried to use /ks next at the end of the list
				sender.sendMessage(ChatColor.YELLOW + prefix
						+ " Page does not exist");
				// Revert to last page
				page.put(sender.getName(), num - 1);
				valid = false;
			}
			if (valid)
			{
				String[] name = list.keySet().toArray(new String[0]);
				Location[] location = list.values().toArray(new Location[0]);
				sender.sendMessage(ChatColor.GRAY + "=====Warp List====" + "Page: " + ChatColor.AQUA + (page.get(sender.getName()) + 1)  + ChatColor.GRAY + " of " + ChatColor.AQUA + num + ChatColor.GRAY + "=====");
				DecimalFormat twoDForm = new DecimalFormat("#.##");
				for (int i = ((page.get(sender.getName()).intValue()) * limit); i < ((page
						.get(sender.getName()).intValue()) * limit) + limit; i++)
				{
					if (i < name.length)
					{
						sender.sendMessage(ChatColor.GREEN
								+ name[i]
								+ ChatColor.BLUE
								+ " : " + ChatColor.RED + location[i].getWorld().getName() + ChatColor.BLUE + "("
								+ ChatColor.GOLD
								+ Double.valueOf(twoDForm.format(location[i]
										.getX()))
								+ ChatColor.BLUE
								+ ","
								+ ChatColor.GOLD
								+ Double.valueOf(twoDForm.format(location[i]
										.getY()))
								+ ChatColor.BLUE
								+ ","
								+ ChatColor.GOLD
								+ Double.valueOf(twoDForm.format(location[i]
										.getZ())) + ChatColor.BLUE + ")");
					}
				}
			}
		}
	}

	/**
	 * Attempts to look up full name based on who's on the server Given a
	 * partial name
	 *
	 * @author Frigid, edited by Raphfrk and petteyg359
	 */
	private String expandName(String Name) {
		int m = 0;
		String Result = "";
		for (int n = 0; n < kw.getServer().getOnlinePlayers().length; n++)
		{
			String str = kw.getServer().getOnlinePlayers()[n].getName();
			if (str.matches("(?i).*" + Name + ".*"))
			{
				m++;
				Result = str;
				if (m == 2)
				{
					return null;
				}
			}
			if (str.equalsIgnoreCase(Name))
				return str;
		}
		if (m == 1)
			return Result;
		if (m > 1)
		{
			return null;
		}
		return Name;
	}

	private void warpCommand(CommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		// Get warp name
		final String name = args[0];
		if (args.length > 1)
		{
			// Check permissions
			if (perm.checkPermission(sender, "KarmicWarp.warp.other"))
			{
				// Get list of players based off of given names
				final Vector<Player> players = new Vector<Player>();
				for (int i = 1; i < args.length; i++)
				{
					// Attempt auto complete
					String temp = expandName(args[i]);
					if (temp == null)
					{
						temp = args[i];
					}
					// Grab player on server using name
					Player tempPlayer = kw.getServer().getPlayer(temp);
					if (tempPlayer != null)
					{
						players.add(tempPlayer);
					}
					else
					{
						// Could not grab player
						sender.sendMessage(ChatColor.RED + prefix
								+ " Could not grab player '" + ChatColor.WHITE
								+ temp + ChatColor.RED + "'");
					}
				}
				if (!players.isEmpty())
				{
					for (Player p : players)
					{
						// Warp player
						this.warpPlayer(sender, p, name);
					}
					sender.sendMessage(ChatColor.GREEN + prefix + " Warped "
							+ ChatColor.WHITE + players.size() + " player(s)");
				}
				else
				{
					sender.sendMessage(ChatColor.RED + prefix
							+ " No players warped");
				}
			}
			else
			{
				sender.sendMessage(ChatColor.RED + prefix
						+ " Lack permission: KarmicWarp.warp.other");
			}
		}
		else
		{
			// They did not provide a name, just warp the sender
			// Check if its a player
			if (sender instanceof Player)
			{
				Player player = (Player) sender;
				// Grab warp from database if it exists
				if (this.warpExists(name))
				{
					// Warp player
					this.warpPlayer(sender, player, name);
					sender.sendMessage(ChatColor.GREEN + prefix + " Warped to "
							+ ChatColor.GOLD + name);
				}
				else
				{
					// Warp does not exist
					sender.sendMessage(ChatColor.RED + prefix
							+ " Warp does not exist");
				}
			}
			else
			{
				sender.sendMessage(ChatColor.RED + prefix
						+ " Cannot warp as console");
			}
		}
	}

	/**
	 * Warp a player to given warp
	 *
	 * @param Player
	 *            to be warped
	 * @param Name
	 *            of warp
	 */
	private void warpPlayer(CommandSender sender, Player player, String warp) {
		try
		{
			String query = "SELECT * FROM warps WHERE name='" + warp + "';";
			ResultSet rs = kw.getLiteDB().select(query);
			if (rs.next())
			{
				World w = kw.getServer().getWorld(rs.getString("world"));
				// Check if world exists
				if (w != null)
				{
					// Teleport player to location
					Location l = new Location(w, rs.getDouble("x"),
							rs.getDouble("y"), rs.getDouble("z"));
					player.teleport(l);
				}
				else
				{
					// World does not exist
					sender.sendMessage(ChatColor.RED + prefix + " World '"
							+ rs.getString("world") + "' does not exist");
				}
			}
			else
			{
				// Warp does not exist
				sender.sendMessage(ChatColor.RED + prefix + " warp lost");
			}
			rs.close();
		}
		catch (SQLException e)
		{
			// INFO Auto-generated catch block
			kw.getLogger().warning(ChatColor.RED + prefix + " SQL exception");
			e.printStackTrace();
		}
	}

	/**
	 * Remove a warp
	 *
	 * @param sender
	 *            of command
	 * @param args
	 *            of command
	 */
	private void removeWarp(CommandSender sender, String[] args) {
		// Attempt to grab name of warp point
		if (args.length > 1)
		{
			final String name = args[1];
			// Check if warp name is valid
			if (this.validWarpName(sender, name))
			{
				if (this.warpExists(name))
				{
					String query = "DELETE FROM warps WHERE name='" + name
							+ "';";
					kw.getLiteDB().standardQuery(query);
					sender.sendMessage(ChatColor.GREEN + prefix
							+ " Warp removed");
				}
				else
				{
					// Warp already exists
					sender.sendMessage(ChatColor.RED + prefix
							+ " Warp does not exist");
				}
			}
		}
		else
		{
			// They did not provide a name
			sender.sendMessage(ChatColor.RED + prefix + " Warp name not given");
		}
	}

	/**
	 * Create a warp
	 *
	 * @param sender
	 *            of command
	 * @param args
	 *            of command
	 */
	private void createWarp(CommandSender sender, String[] args) {
		// They're making a warp point
		// Check if its a player
		if (sender instanceof Player)
		{
			Player player = (Player) sender;
			// Attempt to grab name of warp point
			if (args.length >= 1)
			{
				final String name = args[1];
				if (name.equals("create") || name.equals("remove")
						|| name.equals("list") || name.equals("prev")
						|| name.equals("next") || name.equals("?") || name.equals("update"))
				{
					sender.sendMessage(ChatColor.RED + prefix
							+ " Cannot use warp name of a command.");
				}
				else
				{
					// Check if warp name is valid
					if (this.validWarpName(sender, name))
					{
						// Check if name already exists in database
						if (!this.warpExists(name))
						{
							// We can create the warp, since it doesn't
							// exist
							Location l = player.getLocation();
							String query = "INSERT INTO warps VALUES ('" + name
									+ "','" + l.getWorld().getName() + "','"
									+ l.getX() + "','" + l.getY() + "','"
									+ l.getZ() + "');";
							kw.getLiteDB().standardQuery(query);
							sender.sendMessage(ChatColor.GREEN + prefix
									+ " Warp created");
						}
						else
						{
							// Warp already exists
							sender.sendMessage(ChatColor.RED + prefix
									+ " Warp already exists");
						}
					}
				}

			}
			else
			{
				// They did not provide a name
				player.sendMessage(ChatColor.RED + prefix
						+ " Warp name not given");
			}
		}
		else
		{
			// Cannot run as console
			sender.sendMessage(ChatColor.RED + prefix
					+ " Cannot create warps as console");
		}
	}

	/**
	 * Check if warp exists in database
	 *
	 * @param name
	 *            of the warp
	 * @return true if warp is in database, else false
	 */
	private boolean warpExists(String name) {
		boolean has = false;

		try
		{
			// Check if name already exists in database
			String query = "SELECT COUNT(*) FROM warps WHERE name='" + name
					+ "';";
			ResultSet rs = kw.getLiteDB().select(query);
			if (rs.next())
			{
				if (rs.getInt(1) >= 1)
				{
					// we have a warp with the same name
					has = true;
				}
			}
			rs.close();
		}
		catch (SQLException e)
		{
			// INFO Auto-generated catch block
			kw.getLogger().warning(ChatColor.RED + prefix + " SQL exception");
			e.printStackTrace();
		}

		return has;
	}

	private boolean validWarpName(CommandSender sender, String name) {
		if (!name.matches(WARP_NAME_REGEX))
		{
			sender.sendMessage(ChatColor.RED + prefix
					+ " Warp name must be alphanumeric");
			return false;
		}
		if (name.length() > 20)
		{
			sender.sendMessage(ChatColor.RED + prefix
					+ " Warp name cannot exceed 20 characters");
			return false;
		}
		return true;
	}

	/**
	 * Show help menu to sender
	 *
	 * @param sender
	 *            of command
	 */
	private void displayHelp(CommandSender sender) {
		sender.sendMessage(ChatColor.BLUE + "=====" + ChatColor.RED
				+ "KarmicWarp" + ChatColor.BLUE + "=====");
		if (perm.checkPermission(sender, "KarmicWarp.warp"))
		{
			sender.sendMessage(ChatColor.GREEN + "/warp <warp>"
					+ ChatColor.YELLOW + " : Warp to given warp point");
		}
		if (perm.checkPermission(sender, "KarmicWarp.warp.other"))
		{
			sender.sendMessage(ChatColor.GREEN + "/warp <warp> <player>"
					+ ChatColor.YELLOW + " : Warps a player to a warp point");
		}
		sender.sendMessage(ChatColor.GREEN + "/warp list" + ChatColor.YELLOW
				+ " : Lists available warps");
		sender.sendMessage(ChatColor.GREEN + "/warp <prev | next>"
				+ ChatColor.YELLOW + " : Show previous/next page of list");
		if (perm.checkPermission(sender, "KarmicShare.edit"))
		{
			sender.sendMessage(ChatColor.GREEN + "/warp create <name>"
					+ ChatColor.YELLOW
					+ " : Creates a warp at your current position");
			sender.sendMessage(ChatColor.GREEN + "/warp remove <name>"
					+ ChatColor.YELLOW + " : Removes a warp");
			sender.sendMessage(ChatColor.GREEN + "/warp update <name>"
					+ ChatColor.YELLOW + " : Updates warp to current location");
		}
	}

	/**
	 * Calculate debug time
	 *
	 * @param sender
	 *            of command
	 * @param previous
	 *            time when command was sent
	 */
	private void debugTime(CommandSender sender, long time) {
		time = System.nanoTime() - time;
		sender.sendMessage("[Debug]" + prefix + "Process time: " + time);
	}

}
