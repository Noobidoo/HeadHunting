package me.lordspyder.HeadHunting;

import java.io.File;
import java.util.ArrayList;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class HeadHunting extends JavaPlugin implements Listener {


	String prefix = ChatColor.GOLD + "[" + ChatColor.AQUA + "HeadHunting" + ChatColor.GOLD + "] "; 
	ArrayList<String> hunting = new ArrayList<String>();

	public static Economy econ = null;

	FileConfiguration config;
	File cfile;

	public void onEnable() {
		config = getConfig();
		config.options().copyDefaults(true);
		saveDefaultConfig();
		cfile = new File(getDataFolder(), "config.yml");
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		if (!setupEconomy() ) {
			getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String Comandlabel, String[] args) {
		Player p = (Player) sender;
		if (cmd.getName().equalsIgnoreCase("hunting")) {
			if (args.length == 0) {
				sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("SpecifyAPlayer")));
				return true;
			}
			if(args[0].equals(p.getName())) {
				p.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("HeadHuntingYourSelf")));
				return true;
			}
			Player target = Bukkit.getServer().getPlayer(args[0]);
			if (target == null) {
				sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("NotFindPlayer") + args[0] ));
				return true;
			}
			EconomyResponse r = econ.withdrawPlayer(p.getName(), 25);
			if (r.transactionSuccess()) {
				hunting.add(target.getName());
				sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("HeadHuntingOn") + target.getName()));
				target.setPlayerListName(ChatColor.RED + target.getName());
				return true;
			} else {
				p.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("NottHaveMoney")));
				return true;
			}
		}
		return true;
	}
	@SuppressWarnings({ "deprecation", "unused" })
	@EventHandler
	public void onDeath(PlayerDeathEvent e){
		Player player = e.getEntity();
		if(player.getKiller() != null && hunting.contains(player.getName())){
			String killer = player.getKiller().getName();
			e.setDeathMessage(prefix + player.getName() + ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("WasSlainBy") + killer));
			player.setPlayerListName(ChatColor.WHITE + player.getName());
			EconomyResponse r = econ.depositPlayer(killer, 25);
			hunting.remove(player);

		}
	}
}