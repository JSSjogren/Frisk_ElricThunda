package com.jarodsjogren.frisk;

import java.util.HashMap;

import org.bukkit.entity.Player;

public class Cooldowns {
    private static HashMap<String, Long> cooldowns = new HashMap<String, Long>();
    
    /**
     * Retrieve the number of milliseconds left until a given cooldown expires.
     * <p>
     * Check for a negative value to determine if a given cooldown has expired. <br>
     * Cooldowns that have never been defined will return {@link Long#MIN_VALUE}.
     * @param player - the player.
     * @return Number of milliseconds until the cooldown expires.
     */
    public long getCooldown(Player player) {
        return calculateRemainder(cooldowns.get(player.getName())) / 1000;
    }
    
    /**
     * Update a cooldown for the specified player.
     * @param player - the player.
     * @param delay - number of milliseconds until the cooldown will expire again.
     * @return The previous number of milliseconds until expiration.
     */
    public long setCooldown(Player player, long delay) {
        return calculateRemainder(
                cooldowns.put(player.getName(), System.currentTimeMillis() + delay));
    }
    
    /**
     * Determine if a given cooldown has expired. If it has, refresh the cooldown. If not, do nothing.
     * @param player - the player.
     * @return TRUE if the cooldown was expired/FALSE if it wasn't expired
     */
    public boolean hasCooldown(Player player) {
        if (getCooldown(player) <= 0)
            return false;
        else 
        	return true;
    }
    
    /**
     * Remove any cooldowns associated with the given player.
     * @param player - the player we will reset.
     */
    public static void removeCooldowns(Player player) {
        cooldowns.remove(player.getName());
    }
    
    private static long calculateRemainder(Long expireTime) {
        return expireTime != null ? expireTime - System.currentTimeMillis() : Long.MIN_VALUE;
    }
}