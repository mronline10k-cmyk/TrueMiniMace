package com.minimace.mace;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MaceListener implements Listener {

    private final TrueMiniMace plugin;
    private final MiniMaceItem itemManager;
    private final Map<UUID, Long> windBurstCooldowns = new HashMap<>();
    private final Map<UUID, Double> playerFallDistances = new HashMap<>();

    public MaceListener(TrueMiniMace plugin) {
        this.plugin = plugin;
        this.itemManager = plugin.getItemManager();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        // Track how far the player has fallen since last being on ground
        if (player.isOnGround()) {
            playerFallDistances.put(player.getUniqueId(), 0.0);
        } else {
            double fallDist = playerFallDistances.getOrDefault(player.getUniqueId(), 0.0);
            double currentFall = player.getFallDistance();
            if (currentFall > fallDist) {
                playerFallDistances.put(player.getUniqueId(), currentFall);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!itemManager.isMiniMace(player.getInventory().getItemInMainHand())) return;

        Entity target = event.getEntity();
        double baseDamage = plugin.getConfig().getDouble("item.base-damage", 6.0);
        double fallMultiplier = plugin.getConfig().getDouble("mechanics.fall-damage-multiplier", 1.5);
        double maxBonus = plugin.getConfig().getDouble("mechanics.max-bonus-damage", 20.0);
        double minFall = plugin.getConfig().getDouble("mechanics.min-fall-distance", 1.5);

        double fallDistance = playerFallDistances.getOrDefault(player.getUniqueId(), 0.0);
        double totalDamage = baseDamage;

        // Critical hit (falling)
        if (fallDistance >= minFall && !player.isOnGround()) {
            double bonusDamage = Math.min(fallDistance * fallMultiplier, maxBonus);
            totalDamage += bonusDamage;

            // Wind burst on critical hit
            applyWindBurst(player);

            // Reset fall distance tracking
            playerFallDistances.put(player.getUniqueId(), 0.0);

            // Visual feedback
            player.sendMessage("§c§lMini Mace Critical! §7+" + String.format("%.1f", bonusDamage) + " damage");
        }

        event.setDamage(totalDamage);
    }

    private void applyWindBurst(Player player) {
        long cooldown = plugin.getConfig().getLong("mechanics.wind-burst-cooldown", 10) * 50L; // convert ticks to ms
        long now = System.currentTimeMillis();
        UUID uuid = player.getUniqueId();

        if (windBurstCooldowns.containsKey(uuid)) {
            if (now - windBurstCooldowns.get(uuid) < cooldown) {
                return; // still on cooldown
            }
        }
        windBurstCooldowns.put(uuid, now);

        double power = plugin.getConfig().getDouble("mechanics.wind-burst-power", 1.5);
        double upward = plugin.getConfig().getDouble("mechanics.wind-burst-upward", 0.8);
        double forward = plugin.getConfig().getDouble("mechanics.wind-burst-forward", 0.6);

        Vector direction = player.getLocation().getDirection().normalize();
        Vector launch = direction.multiply(forward).setY(upward);
        launch = launch.multiply(power);

        player.setVelocity(player.getVelocity().add(launch));

        // Particles
        if (plugin.getConfig().getBoolean("mechanics.particles", true)) {
            Location loc = player.getLocation().add(0, 0.5, 0);
            player.getWorld().spawnParticle(Particle.CLOUD, loc, 20, 0.3, 0.3, 0.3, 0.05);
            player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc, 5, 0.5, 0.5, 0.5, 0);
        }

        // Sound
        if (plugin.getConfig().getBoolean("mechanics.sounds", true)) {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BREEZE_WIND_BURST, 1.0f, 1.0f);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.8f);
        }
    }
}
