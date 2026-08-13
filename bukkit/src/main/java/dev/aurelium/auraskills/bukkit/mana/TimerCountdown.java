package dev.aurelium.auraskills.bukkit.mana;

import dev.aurelium.auraskills.api.event.mana.ManaAbilityRefreshEvent;
import dev.aurelium.auraskills.api.mana.ManaAbility;
import dev.aurelium.auraskills.bukkit.AuraSkills;
import dev.aurelium.auraskills.bukkit.user.BukkitUser;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.mana.ManaAbilityData;
import dev.aurelium.auraskills.common.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class TimerCountdown {

    private final AuraSkills plugin;
    private final int period;

    public TimerCountdown(AuraSkills plugin) {
        this.plugin = plugin;
        this.period = Math.max(plugin.configInt(Option.MANA_COOLDOWN_TIMER_PERIOD), 1);
        startCountdown();
    }

    public void startCountdown() {
        plugin.getScheduler().timerForEachOnlineUser(this::countCooldown, 0, period * 50L, TimeUnit.MILLISECONDS);
        plugin.getScheduler().timerForEachOnlineUser(this::countErrorTimer, 0, 1, TimeUnit.SECONDS);
    }

    private void countCooldown(User user) {
        for (ManaAbilityData data : user.getManaAbilityDataMap().values()) {
            int cooldown = data.getCooldown();
            if (cooldown > period) {
                data.setCooldown(cooldown - period);
            } else if (cooldown > 0) {
                data.setCooldown(0);
                callRefreshEvent(user, data.getManaAbility());
            }
        }
    }

    private void countErrorTimer(User user) {
        for (ManaAbilityData data : user.getManaAbilityDataMap().values()) {
            int errorTimer = data.getErrorTimer();
            if (errorTimer > 0) {
                data.setErrorTimer(errorTimer - 1);
            }
        }
    }

    private void callRefreshEvent(User user, ManaAbility manaAbility) {
        Player player = ((BukkitUser) user).getPlayer();
        if (player != null) {
            ManaAbilityRefreshEvent event = new ManaAbilityRefreshEvent(player, user.toApi(), manaAbility);
            Bukkit.getPluginManager().callEvent(event);
        }
    }

}
