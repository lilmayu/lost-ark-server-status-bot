package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.console.commands.generic.CommandResult;
import dev.mayuna.lostarkbot.managers.GuildDataManager;
import dev.mayuna.lostarkbot.objects.core.GuildData;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayuslibrary.arguments.ArgumentParser;

public class GuildConsoleCommand extends AbstractConsoleCommand {

    public GuildConsoleCommand() {
        this.name = "guild";
        this.syntax = "<save|load|remove|info> <id>";
    }

    @Override
    public CommandResult execute(String arguments) {
        ArgumentParser argumentParser = new ArgumentParser(arguments);

        if (argumentParser.hasArgumentAtIndex(1)) {
            long guildID = argumentParser.getArgumentAtIndex(0).getValueAsNumber().longValue();

            switch (argumentParser.getArgumentAtIndex(0).getValue()) {
                case "save" -> {
                    GuildData guildData = GuildDataManager.getGuildData(guildID);

                    if (guildData == null) {
                        Logger.error("There is no Guild Data with ID " + guildID);
                        return CommandResult.SUCCESS;
                    }

                    GuildDataManager.saveGuildData(guildData);
                    Logger.success("Saved Guild Data " + guildData.getRawGuildID() + "(" + guildData.getName() + ")!");
                }
                case "load" -> {
                    GuildData guildData = GuildDataManager.loadGuildData(GuildData.getGuildDataFile(guildID));

                    if (guildData == null) {
                        Logger.error("Could not load Guild Data " + guildID);
                        return CommandResult.SUCCESS;
                    }

                    if (!guildData.updateEntries(Main.getMayuShardManager().get())) {
                        Logger.error("Could not update entries for Guild Data " + guildID + "(" + guildData.getName() + ")");
                        return CommandResult.SUCCESS;
                    }

                    GuildDataManager.addOrReplaceGuildData(guildData);

                    Logger.success("Loaded Guild Data " + guildData.getRawGuildID() + " (" + guildData.getName() + ")!");
                }
                case "remove" -> {
                    GuildData guildData = GuildDataManager.getGuildData(guildID);

                    if (guildData == null) {
                        Logger.error("There is no Guild Data with ID " + guildID);
                        return CommandResult.SUCCESS;
                    }

                    GuildDataManager.removeGuildData(guildData.getRawGuildID());

                    if (GuildDataManager.deleteGuildData(guildData.getRawGuildID())) {
                        Logger.success("Successfully deleted Guild Data " + guildData.getRawGuildID() + " (" + guildData.getName() + ")!");
                    } else {
                        Logger.warn("Could not delete Guild Data " + guildData.getRawGuildID() + " (" + guildData.getName() + ")! Probably does not exist. However, it was removed from loaded ones.");
                    }
                }
                case "info" -> {
                    GuildData guildData = GuildDataManager.getGuildData(guildID);

                    if (guildData == null) {
                        Logger.error("There is no Guild Data with ID " + guildID);
                        return CommandResult.SUCCESS;
                    }

                    guildData.updateEntries(Main.getMayuShardManager().get());

                    Logger.info("=== " + guildData.getRawGuildID() + " (" + guildData.getName() + ") ===");
                    Logger.info("> Dashboards: " + guildData.getLoadedServerDashboards().size());
                    Logger.info("> Name......: " + guildData.getGuild().getName());
                    Logger.info("> Members...: " + guildData.getGuild().getMemberCount());
                }
                default -> {
                    return CommandResult.INCORRECT_SYNTAX;
                }
            }

            return CommandResult.SUCCESS;
        }

        return CommandResult.INCORRECT_SYNTAX;
    }
}
