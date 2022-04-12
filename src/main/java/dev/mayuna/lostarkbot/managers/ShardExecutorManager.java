package dev.mayuna.lostarkbot.managers;

import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.util.Config;
import dev.mayuna.lostarkbot.util.UpdateType;
import dev.mayuna.lostarkbot.util.logging.Logger;
import lombok.Getter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ShardExecutorManager {

    private static @Getter ExecutorService executorService;
    private static @Getter int currentlyRunningTasks = 0;

    public static void initExecutorService() {
        Logger.info("Initializing Executor Service...");

        executorService = Executors.newFixedThreadPool(Config.getTotalUpdateThreadPool());
    }

    public static void submit(int shardId, UpdateType updateType, Runnable runnable) {
        executorService.submit(() -> {
            currentlyRunningTasks++;

            Thread.currentThread().setName("[" + shardId + "] ShardUpdater (" + updateType.name() + ")");

            long start = System.currentTimeMillis();
            runnable.run();
            Logger.debug("On shard " + shardId + " update " + updateType.name() + " took " + (System.currentTimeMillis() - start) + "ms");

            currentlyRunningTasks--;
        });
    }

    public static void submitForEachShard(UpdateType updateType, Consumer<Integer> consumer) {
        Logger.flow("Submitting update type " + updateType.name());

        for (int shardId = 0; shardId < Main.getMayuShardManager().get().getShardsTotal(); shardId++) {
            int finalShardId = shardId;

            submit(shardId, updateType, () -> {
                consumer.accept(finalShardId);
            });
        }
    }
}
