package dev.mayuna.lostarkbot.managers;

import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.util.UpdateType;
import dev.mayuna.lostarkbot.util.config.Config;
import dev.mayuna.lostarkbot.util.logging.Logger;
import lombok.Getter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;

public class ShardExecutorManager {

    private static final @Getter List<Task> runningTasks = Collections.synchronizedList(new LinkedList<>());
    private static @Getter ThreadPoolExecutor executorService;

    public static void initExecutorService() {
        Logger.info("Initializing Executor Service...");

        executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(Config.get().getBot().getTotalUpdateThreads());
    }

    public static void submit(int shardId, UpdateType updateType, Runnable runnable) {
        executorService.submit(() -> {
            Thread.currentThread().setName("[" + shardId + "] ShardUpdater (" + updateType.name() + ")");

            Task task = new Task(updateType, System.currentTimeMillis(), shardId);
            runningTasks.add(task);
            try {
                runnable.run();
            } catch (Exception exception) {
                Logger.throwing(exception);
                Logger.warn("Task " + updateType + " on shard " + shardId + " resulted in exception!");
            }
            runningTasks.remove(task);
            Logger.debug("On shard " + shardId + " update " + updateType.name() + " took " + task.getElapsedTime() + "ms");
        });
    }

    public static void submitForEachShard(UpdateType updateType, Consumer<Integer> consumer) {
        Logger.debug("Submitting update type " + updateType.name() + " to all shards");

        for (int shardId = 0; shardId < Main.getMayuShardManager().get().getShardsTotal(); shardId++) {
            int finalShardId = shardId;

            submit(shardId, updateType, () -> {
                consumer.accept(finalShardId);
            });
        }
    }

    public record Task(@Getter UpdateType updateType, @Getter long start, @Getter int shardId) {

        public long getElapsedTime() {
            return System.currentTimeMillis() - start;
        }
    }
}
