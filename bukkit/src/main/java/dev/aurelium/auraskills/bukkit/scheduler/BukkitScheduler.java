package dev.aurelium.auraskills.bukkit.scheduler;

import dev.aurelium.auraskills.bukkit.AuraSkills;
import dev.aurelium.auraskills.bukkit.user.BukkitUser;
import dev.aurelium.auraskills.common.scheduler.Scheduler;
import dev.aurelium.auraskills.common.scheduler.Task;
import dev.aurelium.auraskills.common.scheduler.TaskRunnable;
import dev.aurelium.auraskills.common.user.User;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Scheduler bridge that uses Paper's region scheduler API directly. Paper implements these
 * schedulers as main-thread tasks while Folia routes them to the owning global, region, or
 * entity tick thread.
 */
public class BukkitScheduler extends Scheduler {

    private static final long TICK_MILLIS = 50L;

    private final AuraSkills plugin;
    private final boolean folia;

    public BukkitScheduler(AuraSkills plugin) {
        super(plugin);
        this.plugin = plugin;
        this.folia = detectFolia();
    }

    public boolean isFolia() {
        return folia;
    }

    @Override
    public Task executeSync(Runnable runnable) {
        ScheduledTask task = plugin.getServer().getGlobalRegionScheduler().run(plugin, ignored -> runnable.run());
        return new BukkitTaskWrapper(task);
    }

    @Override
    public Task scheduleSync(Runnable runnable, long delay, TimeUnit timeUnit) {
        ScheduledTask task = plugin.getServer().getGlobalRegionScheduler().runDelayed(
                plugin, ignored -> runnable.run(), toTicks(delay, timeUnit));
        return new BukkitTaskWrapper(task);
    }

    @Override
    public Task timerSync(TaskRunnable runnable, long delay, long period, TimeUnit timeUnit) {
        ScheduledTask scheduledTask = plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(
                plugin,
                ignored -> runnable.run(),
                toTicks(delay, timeUnit),
                toTicks(period, timeUnit));
        Task task = new BukkitTaskWrapper(scheduledTask);
        runnable.injectTask(task);
        return task;
    }

    @Override
    public Task timerAsync(TaskRunnable runnable, long delay, long period, TimeUnit timeUnit) {
        ScheduledTask scheduledTask = plugin.getServer().getAsyncScheduler().runAtFixedRate(
                plugin, ignored -> runnable.run(), normalizedDelay(delay), normalizedPeriod(period), timeUnit);
        Task task = new BukkitTaskWrapper(scheduledTask);
        runnable.injectTask(task);
        return task;
    }

    public CompletableFuture<Void> executeAtLocation(Location location, Consumer<ScheduledTask> consumer) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        plugin.getServer().getRegionScheduler().run(plugin, location, task -> {
            try {
                consumer.accept(task);
                future.complete(null);
            } catch (Throwable throwable) {
                future.completeExceptionally(throwable);
                throw throwable;
            }
        });
        return future;
    }

    public Task scheduleAtLocation(Location location, Runnable runnable, long delay, TimeUnit timeUnit) {
        ScheduledTask task = plugin.getServer().getRegionScheduler().runDelayed(
                plugin, location, ignored -> runnable.run(), toTicks(delay, timeUnit));
        return new BukkitTaskWrapper(task);
    }

    public Task timerAtLocation(Location location, Runnable runnable, long delay, long period, TimeUnit timeUnit) {
        ScheduledTask task = plugin.getServer().getRegionScheduler().runAtFixedRate(
                plugin,
                location,
                ignored -> runnable.run(),
                toTicks(delay, timeUnit),
                toTicks(period, timeUnit));
        return new BukkitTaskWrapper(task);
    }

    public CompletableFuture<Boolean> executeAtEntity(Entity entity, Consumer<ScheduledTask> consumer) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        ScheduledTask task = entity.getScheduler().run(plugin, scheduledTask -> {
            try {
                consumer.accept(scheduledTask);
                future.complete(true);
            } catch (Throwable throwable) {
                future.completeExceptionally(throwable);
                throw throwable;
            }
        }, () -> future.complete(false));
        if (task == null) {
            future.complete(false);
        }
        return future;
    }

    public Task scheduleAtEntity(Entity entity, Runnable runnable, long delay, TimeUnit timeUnit) {
        ScheduledTask task = entity.getScheduler().runDelayed(
                plugin, ignored -> runnable.run(), null, toTicks(delay, timeUnit));
        return new BukkitTaskWrapper(task);
    }

    public Task timerAtEntity(Entity entity, Runnable runnable, long delay, long period, TimeUnit timeUnit) {
        ScheduledTask task = entity.getScheduler().runAtFixedRate(
                plugin,
                ignored -> runnable.run(),
                null,
                toTicks(delay, timeUnit),
                toTicks(period, timeUnit));
        return new BukkitTaskWrapper(task);
    }

    @Override
    public void executeAtUser(User user, Runnable runnable) {
        Player player = BukkitUser.getPlayer(user);
        if (player != null) {
            executeAtEntity(player, ignored -> runnable.run());
        } else {
            executeSync(runnable);
        }
    }

    @Override
    public Task scheduleAtUser(User user, Runnable runnable, long delay, TimeUnit timeUnit) {
        Player player = BukkitUser.getPlayer(user);
        return player != null
                ? scheduleAtEntity(player, runnable, delay, timeUnit)
                : scheduleSync(runnable, delay, timeUnit);
    }

    @Override
    public Task timerAtUser(User user, TaskRunnable runnable, long delay, long period, TimeUnit timeUnit) {
        Player player = BukkitUser.getPlayer(user);
        if (player == null) {
            return timerSync(runnable, delay, period, timeUnit);
        }
        ScheduledTask scheduledTask = player.getScheduler().runAtFixedRate(
                plugin,
                ignored -> runnable.run(),
                null,
                toTicks(delay, timeUnit),
                toTicks(period, timeUnit));
        Task task = new BukkitTaskWrapper(scheduledTask);
        runnable.injectTask(task);
        return task;
    }

    /**
     * Takes the global online-player snapshot and dispatches each operation to that player's
     * owning entity thread. This is required on Folia and remains valid on Paper.
     */
    public void forEachOnlinePlayer(Consumer<Player> consumer) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            executeAtEntity(player, ignored -> consumer.accept(player));
        }
    }

    public void executeAtCommandSender(CommandSender sender, Runnable runnable) {
        if (sender instanceof Entity entity) {
            executeAtEntity(entity, ignored -> runnable.run());
        } else if (sender instanceof BlockCommandSender blockSender) {
            executeAtLocation(blockSender.getBlock().getLocation(), ignored -> runnable.run());
        } else {
            executeSync(runnable);
        }
    }

    public Task timerForEachOnlinePlayer(Consumer<Player> consumer, long delay, long period, TimeUnit timeUnit) {
        TaskRunnable dispatcher = new TaskRunnable() {
            @Override
            public void run() {
                forEachOnlinePlayer(consumer);
            }
        };
        return timerSync(dispatcher, delay, period, timeUnit);
    }

    private long toTicks(long duration, TimeUnit timeUnit) {
        long millis = timeUnit.toMillis(duration);
        if (millis <= 0) {
            return 1L;
        }
        return Math.max(1L, Math.addExact(millis, TICK_MILLIS - 1L) / TICK_MILLIS);
    }

    private long normalizedDelay(long delay) {
        return Math.max(0L, delay);
    }

    private long normalizedPeriod(long period) {
        return Math.max(1L, period);
    }

    private boolean detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
