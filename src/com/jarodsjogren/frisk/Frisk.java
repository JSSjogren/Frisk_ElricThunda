package com.jarodsjogren.frisk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class Frisk extends JavaPlugin implements Listener{
	Cooldowns cd = new Cooldowns();
	final int seconds = 20;
	private final String prefix = ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Frisk" + ChatColor.DARK_GRAY + "]" + ChatColor.GOLD;

	@Override
	public void onEnable()
	{
		getLogger().info("[Frisk]: Enabled");
		getLogger().info("[Frisk]: Created by TheBeyonder");
		Bukkit.getServer().getPluginManager().registerEvents(
				this, this);
		this.saveDefaultConfig();
		PluginManager pm = Bukkit.getPluginManager();
        Permission p1 = new Permission("frisk.bypass");
        Permission p2 = new Permission("frisk.frisk");
        Permission p3 = new Permission("frisk.health");
        pm.addPermission(p1);
        pm.addPermission(p2);
        pm.addPermission(p3);

	}

	@Override
	public void onDisable()
	{
		getLogger().info("[Frisk]: Disabled");
		getLogger().info("[Frisk]: Created by TheBeyonder");
		getLogger().info("[Frisk]: Thanks for using my plugin!");
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void welcome2Server(PlayerJoinEvent e)
	{
		if (e.getPlayer().hasPermission("frisk.health"))
		{
			e.getPlayer().setMaxHealth(40);
			e.getPlayer().setHealth(40);
		}
		
		if (!e.getPlayer().hasPermission("frisk.health") && e.getPlayer().getMaxHealth() == 40)
		{
			e.getPlayer().setMaxHealth(20);
			e.getPlayer().setHealth(20);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void touchyTouchy(PlayerInteractEntityEvent e)
	{
		boolean playerGuilty = false;
		Player guard = e.getPlayer();
		Player prisoner = (Player)e.getRightClicked();
		if (prisoner instanceof Player)
		{
			if (guard.getItemInHand().equals(new ItemStack(Material.STICK, 1))
					&& guard.hasPermission("frisk.frisk"))
			{
				if (!cd.hasCooldown(guard)) {
					List<String> contra = getConfig().getStringList("contraband");
					Integer size = contra.size();
					PlayerInventory pris = prisoner.getInventory();
					for (int i = 0; i < contra.size(); i++) {
						String key = contra.get(i);
						Integer id = Integer.parseInt(key);
						if (pris.contains(Material.getMaterial(id)))
						{
							playerGuilty = true;
							ItemStack[] guardInv = removeInventoryItems(pris, guard.getInventory(), Material.getMaterial(id));
							for (ItemStack is : guardInv)
							{
								guard.getInventory().addItem(is);
							}
						}
					}
					//Does the player have any contraband?
					if (playerGuilty == false)
					{
						guard.sendMessage(prefix + " " + prisoner.getName() + " has no contraband!");
					} else {
						guard.updateInventory(); //Necessary for the guards for some reason...
						prisoner.getInventory().setContents(pris.getContents());
						guard.sendMessage(prefix + " You took contraband from " + prisoner.getName()); //Alerting guard
						prisoner.sendMessage(prefix + " Contraband was taken by the guards"); //Alerting prisoner
					}
					//Checks to see if the guard can bypass the timer
					if (!guard.hasPermission("frisk.bypass")) {
						guard.sendMessage(prefix + " Cooldown not bypassed.");
						cd.setCooldown(guard, 20000);
					} else
						guard.sendMessage(prefix + " Cooldown bypassed.");
						
				} else {
					guard.sendMessage(prefix + " You can't frisk a prisoner for " + cd.getCooldown(guard) + " seconds!");
					guard.damage(4);
				}
			} else {
				return;
			}
		}
	}

	@EventHandler
	public void sweetReleaseOfDeath(PlayerDeathEvent e)
	{
		List<String> contra = getConfig().getStringList("contraband");
		List<ItemStack> drops = e.getDrops();
		List<ItemStack> nonContra = new ArrayList<ItemStack>();
		Player guard = e.getEntity();
		boolean isContraband;
		
		if (e.getEntity() instanceof Player)
		{
			if (guard.hasPermission("frisk.frisk"))
			{
				Iterator<ItemStack> iter = e.getDrops().iterator();
				 
				for (ItemStack is : drops)
				{
					isContraband = false;
					for (int i = 0; i < contra.size(); i++) {
						String key = contra.get(i);
						Integer id = Integer.parseInt(key);
						if (is.getTypeId() == id)
						{
							isContraband = true;
						}
					}
					if (isContraband == false)
					{
						nonContra.add(is);
					}
				}
				
				for (ItemStack is : nonContra)
				{
					if (drops.contains(is))
					{
						drops.remove(is);
					}
				}
			} else {
				return;
			}
		}
	}
	
	
	public static ItemStack[] removeInventoryItems(PlayerInventory pinv, PlayerInventory ginv, Material type) {
		ArrayList<ItemStack> guardInv = new ArrayList<ItemStack>();
		for (ItemStack is : pinv.getContents()) {
			if (is != null && is.getType() == type) {
				pinv.remove(is);
				guardInv.add(is);
			}
		}
		return guardInv.toArray(new ItemStack[guardInv.size()]);
	}
	

}
