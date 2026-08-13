package dev.aurelium.auraskills.bukkit.leaderboard;

import dev.aurelium.auraskills.bukkit.AuraSkills;
import dev.aurelium.auraskills.common.leaderboard.LeaderboardExclusion;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.concurrent.TimeUnit;

public class BukkitLeaderboardExclusion extends LeaderboardExclusion implements Listener {

    public static final String PERMISSION = "auraskills.leaderboard.exclude";
    private final AuraSkills bukkitPlugin;

    public BukkitLeaderboardExclusion(AuraSkills plugin) {
        super(plugin);
        this.bukkitPlugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        bukkitPlugin.getScheduler().scheduleAtEntity(event.getPlayer(), () -> {
            Player player = event.getPlayer();
            if (player.hasPermission(PERMISSION)) {
                addExcludedPlayer(player.getUniqueId());
            } else {
                removeExcludedPlayer(player.getUniqueId());
            }
        }, 1, TimeUnit.SECONDS);
    }

}
