package dev.aurelium.auraskills.bukkit.scheduler;

import dev.aurelium.auraskills.common.scheduler.Task;
import dev.aurelium.auraskills.common.scheduler.TaskStatus;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.jetbrains.annotations.Nullable;

public class BukkitTaskWrapper implements Task {

    private final ScheduledTask scheduledTask;

    public BukkitTaskWrapper(@Nullable ScheduledTask scheduledTask) {
        this.scheduledTask = scheduledTask;
    }

    @Override
    public TaskStatus getStatus() {
        if (scheduledTask == null || scheduledTask.isCancelled()) {
            return TaskStatus.STOPPED;
        }
        return switch (scheduledTask.getExecutionState()) {
            case FINISHED, CANCELLED, CANCELLED_RUNNING -> TaskStatus.STOPPED;
            case IDLE, RUNNING -> TaskStatus.SCHEDULED;
        };
    }

    @Override
    public void cancel() {
        if (scheduledTask != null) {
            scheduledTask.cancel();
        }
    }
}
