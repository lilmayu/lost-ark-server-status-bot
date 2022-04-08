package dev.mayuna.lostarkbot.managers;

import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.lostarkbot.util.logging.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GatewayPingEvent;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.List;
import java.util.stream.Collectors;

public class MayuShardManager {

    private final ShardManager shardManager;

    public MayuShardManager(ShardManager shardManager) {
        this.shardManager = shardManager;
    }

    public void waitOnAll() throws InterruptedException {
        Logger.info("Waiting till all shards are connected...");

        int counter = 0;
        long start = System.currentTimeMillis();

        do {
            long now = System.currentTimeMillis();

            if (counter == 10) {
                int connectedShards = getShardsWithStatus(JDA.Status.CONNECTED).size();
                int notConnectedShards = getShardsWithoutStatus(JDA.Status.CONNECTED).size();
                double timePerShard = -1;

                if (connectedShards != 0) {
                    timePerShard = (now - start) / (double) connectedShards;
                }

                double timeRemaining = notConnectedShards * timePerShard;

                Logger.info("Waiting on " + getShardsWithoutStatus(JDA.Status.CONNECTED).size() + " shards... ETA: " + Utils.getTimerWithoutMillis((long) Math.ceil(timeRemaining)));

                counter = -1;
            }
            counter++;

            Thread.sleep(1000);
        } while(!areAllShardsConnected());

        Logger.success("All shards are connected. Proceeding...");
    }

    public void onGatewayPing(GatewayPingEvent event) {
        JDA shardJDA = event.getEntity();
        JDA.ShardInfo shardInfo = shardJDA.getShardInfo();
        int shardId = shardInfo.getShardId();

        Logger.flow("Received Gateway ping from shard " + shardId);
    }

    public boolean isShardConnected(int shardId) {
        JDA shard = shardManager.getShardById(shardId);

        if (shard == null) {
            return false;
        }

        return shard.getStatus() == JDA.Status.CONNECTED;
    }

    public boolean areAllShardsConnected() {
        for (JDA shard : shardManager.getShards()) {
            if (shard.getStatus() != JDA.Status.CONNECTED) {
                return false;
            }
        }

        return true;
    }

    public List<JDA> getShardsWithStatus(JDA.Status status) {
        return shardManager.getShards().stream().filter(jda -> jda.getStatus() == status).collect(Collectors.toList());
    }

    public List<JDA> getShardsWithoutStatus(JDA.Status status) {
        return shardManager.getShards().stream().filter(jda -> jda.getStatus() != status).collect(Collectors.toList());
    }

    public ShardManager get() {
        return shardManager;
    }
}
