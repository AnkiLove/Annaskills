package dev.aurelium.auraskills.common.scheduler;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import dev.aurelium.auraskills.common.AuraSkillsPlugin;
import dev.aurelium.auraskills.common.user.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class Scheduler {

    private final AuraSkillsPlugin plugin;

    private final ExecutorService asyncExecutor = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder().setNameFormat("auraskills-async-task-%d").build());
    private final ScheduledExecutorService asyncScheduler = Executors.newScheduledThreadPool(0,
            new ThreadFactoryBuilder().setNameFormat("auraskills-async-scheduler-%d").build());

    public Scheduler(final AuraSkillsPlugin plugin) {
        this.plugin = plugin;
    }

    public abstract Task executeSync(final Runnable runnable);

    public Task executeAsync(final Runnable runnable) {
        return new SubmittedTask(asyncExecutor.submit(runnable));
    }

    public abstract Task scheduleSync(final Runnable runnable, final long delay, final TimeUnit timeUnit);

    public Task scheduleAsync(final Runnable runnable, final long delay, final TimeUnit timeUnit) {
        return new ScheduledTask(asyncScheduler.schedule(runnable, delay, timeUnit));
    }

    public abstract Task timerSync(final TaskRunnable runnable, final long delay, final long period, final TimeUnit timeUnit);

    public abstract Task timerAsync(final TaskRunnable runnable, final long delay, final long period, final TimeUnit timeUnit);

    public void executeAtUser(final User user, final Runnable runnable) {
        executeSync(runnable);
    }

    public Task scheduleAtUser(final User user, final Runnable runnable, final long delay, final TimeUnit timeUnit) {
        return scheduleSync(runnable, delay, timeUnit);
    }

    public Task timerAtUser(final User user, final TaskRunnable runnable, final long delay, final long period,
                            final TimeUnit timeUnit) {
        return timerSync(runnable, delay, period, timeUnit);
    }

    public void forEachOnlineUser(final Consumer<User> consumer) {
        for (User user : plugin.getUserManager().getOnlineUsers()) {
            executeAtUser(user, () -> consumer.accept(user));
        }
    }

    public Task timerForEachOnlineUser(final Consumer<User> consumer, final long delay, final long period,
                                       final TimeUnit timeUnit) {
        TaskRunnable dispatcher = new TaskRunnable() {
            @Override
            public void run() {
                forEachOnlineUser(consumer);
            }
        };
        return timerSync(dispatcher, delay, period, timeUnit);
    }

    // Should be run by the implementation when server is shutdown
    public void shutdown() {
        asyncExecutor.shutdown();
        asyncScheduler.shutdown();

        try {
            boolean asyncExecutorDone = asyncExecutor.awaitTermination(2, TimeUnit.SECONDS);
            boolean asyncSchedulerDone = asyncScheduler.awaitTermination(2, TimeUnit.SECONDS);

            if (!asyncExecutorDone || !asyncSchedulerDone) {
                plugin.logger().warn("Scheduler had incomplete tasks when shutting down");
            }
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

}
