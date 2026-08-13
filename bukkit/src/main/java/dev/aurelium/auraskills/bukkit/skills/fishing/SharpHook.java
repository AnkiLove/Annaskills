package dev.aurelium.auraskills.bukkit.skills.fishing;

import dev.aurelium.auraskills.api.mana.ManaAbilities;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.bukkit.AuraSkills;
import dev.aurelium.auraskills.bukkit.mana.ManaAbilityProvider;
import dev.aurelium.auraskills.common.mana.ManaAbilityData;
import dev.aurelium.auraskills.common.message.type.ManaAbilityMessage;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class SharpHook extends ManaAbilityProvider {

    public SharpHook(AuraSkills plugin) {
        super(plugin, ManaAbilities.SHARP_HOOK, ManaAbilityMessage.SHARP_HOOK_USE, null);
    }

    @Override
    public void onActivate(Player player, User user) {
        if (manaAbility.optionBoolean("enable_sound", true)) {
            player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1f, 1.5f);
        }
    }

    @Override
    public void onStop(Player player, User user) {

    }

    @EventHandler
    public void sharpHook(PlayerInteractEvent event) {
        if (isDisabled()) return;
        // If left click with fishing rod
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.FISHING_ROD) return;

        if (shouldIgnoreItem(item)) return;

        Player player = event.getPlayer();
        if (failsChecks(player)) return;

        User user = plugin.getUser(player);
        FishHook fishHook = player.getFishHook();
        if (fishHook == null) return;

        plugin.getScheduler().executeAtEntity(fishHook, ignored -> {
            if (!fishHook.isValid() || !player.equals(fishHook.getShooter())) return;
            Entity hooked = fishHook.getHookedEntity();
            if (!(hooked instanceof LivingEntity livingEntity)) return;

            plugin.getScheduler().executeAtEntity(player, playerTask -> prepareSharpHook(player, user, livingEntity));
        });
    }

    private void prepareSharpHook(Player player, User user, LivingEntity caught) {
        ManaAbilityData data = user.getManaAbilityData(manaAbility);
        int cooldown = data.getCooldown();
        if (cooldown != 0) {
            if (data.getErrorTimer() == 0) {
                Locale locale = user.getLocale();
                plugin.getAbilityManager().sendMessage(player, TextUtil.replace(
                        plugin.getMsg(ManaAbilityMessage.NOT_READY, locale),
                        "{cooldown}",
                        NumberUtil.format1((double) cooldown / 20)));
                data.setErrorTimer(2);
            }
            return;
        }
        if (insufficientMana(user, getManaCost(user))) return;

        double damage = manaAbility.getValue(user.getManaAbilityLevel(manaAbility));
        Location playerLocation = player.getLocation();
        plugin.getScheduler().executeAtEntity(caught, ignored -> damageHookedEntity(player, caught, playerLocation, damage));
    }

    private void damageHookedEntity(Player player, LivingEntity caught, Location playerLocation, double damage) {
        if (caught.isDead() || !caught.isValid() || !isWithinRange(playerLocation, caught)) return;

        double healthBefore = caught.getHealth();
        if (Bukkit.isOwnedByCurrentRegion(player)) {
            caught.damage(damage, player);
        } else {
            caught.damage(damage);
        }
        double healthAfter = caught.getHealth();

        if (!manaAbility.optionBoolean("disable_health_check", false) && healthBefore == healthAfter) {
            return;
        }

        plugin.getScheduler().executeAtEntity(player, ignored -> checkActivation(player));
    }

    private boolean isWithinRange(Location damagerLocation, LivingEntity hooked) {
        Location hookedLocation = hooked.getLocation();
        World damagerWorld = damagerLocation.getWorld();
        World hookedWorld = hookedLocation.getWorld();
        if (damagerWorld == null || hookedWorld == null || !damagerWorld.equals(hookedWorld)) return false;
        return damagerLocation.distanceSquared(hookedLocation) <= 1089;
    }

    @Override
    protected int getDuration(User user) {
        return 0;
    }

}
