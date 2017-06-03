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
	Cooldowns cd = new Cooldowns(); //Instance of cooldowns.java
	final int seconds = 20; //Cooldown time
	private final String prefix = ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Frisk" + ChatColor.DARK_GRAY + "]" + ChatColor.GOLD;

	@Override
	public void onEnable()
	{
		//Giving credit where credit is due.
		getLogger().info("[Frisk]: Enabled");
		getLogger().info("[Frisk]: Created by TheBeyonder");
		Bukkit.getServer().getPluginManager().registerEvents(this, this); //Registering all events for the plugin
		this.saveDefaultConfig(); //Saving el config
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
		//Gives guards 40 health
		if (e.getPlayer().hasPermission("frisk.health"))
		{
			e.getPlayer().setMaxHealth(40);
			e.getPlayer().setHealth(40);
		}
		
		//Added in-case a guard gets demoted (Don't want them to have 40 health anymore)
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
		Player guard = e.getPlayer(); //Player who right-clicked with stick
		Player prisoner = (Player)e.getRightClicked(); //Player who got right-clicked
		if (prisoner instanceof Player)
		{
			if (guard.getItemInHand().equals(new ItemStack(Material.STICK, 1))
			    && guard.hasPermission("frisk.frisk")) //Makes sure regular joes can't frisk
			{
				if (!cd.hasCooldown(guard)) { //Path if they're not on cooldown
					List<String> contra = getConfig().getStringList("contraband"); //Gets contraband IDs from config
					PlayerInventory pris = prisoner.getInventory(); //Gets an instance of the prisoner's inventory
					for (int i = 0; i < contra.size(); i++) {
						Integer id = Integer.parseInt(contra.get(i)); //Only IDs should be stored in config
						if (pris.contains(Material.getMaterial(id))) //Do they have contraband?
						{
							playerGuilty = true; //Yep they do have contraband.
							ItemStack[] guardInv = removeInventoryItems(pris, guard.getInventory(), Material.getMaterial(id));
							for (ItemStack is : guardInv)
							{
								guard.getInventory().addItem(is); //Putting contraband into guard's inventory
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
					guard.damage(4); //They were bad.
				}
			} else {
				return;
			}
		}
	}

	@EventHandler
	public void sweetReleaseOfDeath(PlayerDeathEvent e)
	{
		List<String> contra = getConfig().getStringList("contraband"); //List of contraband IDs from config
		List<ItemStack> drops = e.getDrops(); //Items being dropped by players
		List<ItemStack> nonContra = new ArrayList<ItemStack>(); //Preemptively creating a list for non-contraband
		Player guard = e.getEntity();
		boolean isContraband;
		
		if (e.getEntity() instanceof Player) //Making sure it's not a dog
		{
			if (guard.hasPermission("frisk.frisk")) //Do they have permission?
			{
				Iterator<ItemStack> iter = e.getDrops().iterator(); //So we can iterate
				 
				for (ItemStack is : drops)
				{
					isContraband = false;
					for (int i = 0; i < contra.size(); i++) {
						String key = contra.get(i);
						Integer id = Integer.parseInt(key);
						if (is.getTypeId() == id) //Marking item as contraband
						{
							isContraband = true;
						}
					}
					if (isContraband == false) //IT'S CLEAN SO ADD IT TO THAT LIST FROM UP THERE! ^
					{
						nonContra.add(is);
					}
				}
				
				for (ItemStack is : nonContra)
				{
					if (drops.contains(is))
					{
						drops.remove(is); //Removing non-contraband items from drop
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
				pinv.remove(is); //Remove from player inventory
				guardInv.add(is); //Add to list of items to give to guard
			}
		}
		return guardInv.toArray(new ItemStack[guardInv.size()]); //Return list of items to give to guard
	}
	

}
