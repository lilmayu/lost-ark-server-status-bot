package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.console.commands.generic.CommandResult;
import dev.mayuna.lostarkbot.managers.MayuShardManager;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayuslibrary.arguments.ArgumentParser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.text.DecimalFormat;

public class ShardsConsoleCommand extends AbstractConsoleCommand {

    public ShardsConsoleCommand() {
        this.name = "shards";
        this.syntax = "";
    }

    @Override
    public CommandResult execute(String arguments) {
        ArgumentParser argumentParser = new ArgumentParser(arguments);

        MayuShardManager mayuShardManager = Main.getMayuShardManager();

        if (mayuShardManager == null) {
            Logger.warn("MayuShardManager is not loaded yet.");
            return CommandResult.SUCCESS;
        }

        ShardManager shardManager = mayuShardManager.get();

        if (!argumentParser.hasArgumentAtIndex(0)) {
            double allGuilds = shardManager.getGuilds().size();

            Logger.info("== Shards - " + shardManager.getShardsTotal() + " ==");
            Logger.info("= There are approx. " + new DecimalFormat("#.##").format((allGuilds / shardManager.getShardsTotal())) + " guilds per shard");

            int counter = 0;
            for (JDA shard : shardManager.getShards()) {
                Logger.info("[" + counter + "]: " + shard.getStatus() + " | Guilds: " + shard.getGuilds().size());

                counter++;
            }
        }


        return CommandResult.SUCCESS;
    }
}
