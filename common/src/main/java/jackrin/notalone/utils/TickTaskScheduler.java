package jackrin.notalone.utils;

import net.minecraft.server.MinecraftServer;

import java.util.LinkedList;
import java.util.Queue;

public class TickTaskScheduler {
    private static final Queue<ScheduledTask> tasks = new LinkedList<>();

    public static void tick() {
        tasks.removeIf(ScheduledTask::tick);
    }

    public static void schedule(Runnable task, int delayTicks) {
        tasks.add(new ScheduledTask(task, delayTicks));
    }

    private static class ScheduledTask {
        private final Runnable task;
        private int ticksRemaining;

        ScheduledTask(Runnable task, int delayTicks) {
            this.task = task;
            this.ticksRemaining = delayTicks;
        }

        boolean tick() {
            if (--ticksRemaining <= 0) {
                task.run();
                return true;
            }
            return false;
        }
    }
}
