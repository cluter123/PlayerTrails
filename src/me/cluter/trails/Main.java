package me.cluter.trails;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	public void onEnable() {
		PluginManager pm = Bukkit.getServer().getPluginManager();
		pm.registerEvents(this, this);
		saveDefaultConfig();
		if (getConfig().getString("OnMessage").equals(null) || getConfig().getString("OffMessage").equals(null)) {
			Bukkit.getServer().getLogger().severe(
					ChatColor.RED + "No message values found. Please delete your config and restart the server.");
		}
	}

	ArrayList<String> state = new ArrayList<String>();

	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		if (!p.hasPermission("trail.use")) {
			return;
		}
		if (p.getInventory().contains(Material.getMaterial(getConfig().getString("item").toUpperCase()))
				|| state.contains(p.getName())) {
			Location loc = e.getFrom();
			if (loc.getBlock().isEmpty()
					&& !(loc.getBlock().equals(Material.getMaterial(getConfig().getString("trail").toUpperCase())))) {
				loc.setY(loc.getY() - 1.0D);
				if (Below(loc.getBlock())) {
					loc.setY(loc.getY() + 1.0D);
					loc.getBlock().setType(Material.getMaterial(getConfig().getString("trail").toUpperCase()));
					p.setNoDamageTicks(5);
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
						public void run() {
							loc.getBlock().setType(Material.AIR);
						}
					}, getConfig().getInt("time"));
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (event.getCause() == DamageCause.FIRE
					|| event.getCause() == DamageCause.FIRE_TICK && p.hasPermission("trail.use")
							&& (p.getInventory()
									.contains(Material.getMaterial(getConfig().getString("item").toUpperCase()))
									|| state.contains(p.getName()))) {
				event.setCancelled(true);
			}
		}

	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (label.equalsIgnoreCase("playertrails")) {
			if (args.length == 0) {
				sender.sendMessage(
						"Made by cluter123. Plugin download: http://dev.bukkit.org/bukkit-plugins/player-block-trails/");
				return true;
			}
			if (args[0].equalsIgnoreCase("reload")) {
				if (sender.hasPermission("trail.reload")) {
					reloadConfig();
					sender.sendMessage(ChatColor.GOLD + "PlayerTrails has been reloaded.");
					return true;
				} else {
					sender.sendMessage(ChatColor.RED + "You do not have permission to do this");
				}
			}
		}
		if (label.equalsIgnoreCase("trail")) {
			if (!sender.hasPermission("trail.command")) {
				sender.sendMessage(ChatColor.RED + "You do not have permission to turn on trails.");
				return false;
			}
			if (args.length == 0) {
				sender.sendMessage(ChatColor.RED + "Please specify an argument! (on/off)");
				return false;
			}
			if (args[0].equalsIgnoreCase("on")) {
				if (state.contains(sender.getName())) {
					sender.sendMessage(ChatColor.RED + "Your trail has already been turned on.");
					return false;
				}
				state.add(sender.getName());
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("OnMessage")));
				return true;
			}
			if (args[0].equalsIgnoreCase("off")) {
				if (state.contains(sender.getName())) {
					state.remove(sender.getName());
					sender.sendMessage(
							ChatColor.translateAlternateColorCodes('&', getConfig().getString("OffMessage")));
					return true;
				} else {
					sender.sendMessage(ChatColor.RED + "No trail found to be turned off.");
				}
			}
			if (!(args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off"))) {
				sender.sendMessage(ChatColor.RED + "Error. Please specify an argument! (on/off)");
			}
		}
		return false;
	}

	public boolean Below(Block block) {
		Material material = block.getType();
		return (material == Material.DIRT) || (material == Material.GRASS) || (material == Material.SAND)
				|| (material == Material.SANDSTONE);
	}
}