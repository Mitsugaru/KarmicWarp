package com.mitsugaru.KarmicWarp;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 * Class to handle permission node checks.
 * Mostly only to support PEX natively, due to
 * SuperPerm compatibility with PEX issues.
 *
 * @author Mitsugaru
 *
 */
public class PermCheck {

	/**
	 * Constructor
	 * May not really be needed. Had thought I needed it
	 * earlier, but now... meh.
	 */
	public PermCheck()
	{
	}

	/**
	 *
	 * @param CommandSender that sent command
	 * @param Permission node to check, as String
	 * @return true if sender has the node, else false
	 */
	public boolean checkPermission(CommandSender sender, String node)
	{
		if(Bukkit.getServer().getPluginManager().isPluginEnabled("PermissionsEx"))
		{
			//Pex only supports player check, no CommandSender objects
			if(sender instanceof Player)
			{
				final Player p = (Player) sender;
				final PermissionManager permissions = PermissionsEx.getPermissionManager();
				//Handle pex check
				if(permissions.has(p, node))
				{
					return true;
				}
			}
		}
		//If not using PEX, OR if sender is not a player
		//Attempt to use SuperPerms
		if(sender.hasPermission(node))
		{
			return true;
		}
		//Else, they don't have permission
		return false;
	}
}
