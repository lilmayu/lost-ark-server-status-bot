package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.managers.GuildDataManager;
import dev.mayuna.lostarkbot.objects.GuildData;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayuslibrary.utils.NumberUtils;

public class GuildConsoleCommand extends AbstractConsoleCommand {

    public GuildConsoleCommand() {
        this.name = "guild";
    }

    @Override
    public void execute(String arguments) {
        String[] args = arguments.split(" ");

        if (args.length < 2) {
            invalidSyntax();
            return;
        }

        if (!NumberUtils.isLong(args[1])) {
            Logger.error("Argument '" + args[1] + "' is not Long!");
            return;
        }

        long guildID = NumberUtils.parseLong(args[1]);

        switch(args[0]) {
            case "save" -> {
                GuildData guildData = GuildDataManager.getGuildData(guildID);

                if (guildData == null) {
                    Logger.error("There is no Guild Data with ID " + guildID);
                    return;
                }

                GuildDataManager.saveGuildData(guildData);
                Logger.success("Saved Guild Data " + guildData.getRawGuildID() + "(" + guildData.getName() + ")");
            }
            case "load" -> {
                GuildData guildData = GuildDataManager.loadGuildData(GuildData.getGuildDataFile(guildID));

                if (guildData == null) {
                    Logger.error("Could not load Guild Data " + guildID);
                    return;
                }

                if (!guildData.updateEntries(Main.getJda())) {
                    Logger.error("Could not update entries for Guild Data " + guildID + "(" + guildData.getName() + ")");
                    return;
                }

                GuildDataManager.addOrReplaceGuildData(guildData);

                Logger.success("Loaded Guild Data " + guildData.getRawGuildID() + " (" + guildData.getName() + ")");
            }
            case "remove" -> {
                GuildData guildData = GuildDataManager.getGuildData(guildID);

                if (guildData == null) {
                    Logger.error("There is no Guild Data with ID " + guildID);
                    return;
                }

                GuildDataManager.removeGuildData(guildData.getRawGuildID());

                if (GuildDataManager.deleteGuildData(guildData.getRawGuildID())) {
                    Logger.success("Successfully deleted Guild Data " + guildData.getRawGuildID() + " (" + guildData.getName() + ")");
                } else {
                    Logger.warn("Could not delete Guild Data " + guildData.getRawGuildID() + " (" + guildData.getName() + ")! Probably does not exist. However, it was removed from loaded ones.");
                }
            }
            case "info" -> {
                GuildData guildData = GuildDataManager.getGuildData(guildID);

                if (guildData == null) {
                    Logger.error("There is no Guild Data with ID " + guildID);
                    return;
                }

                guildData.updateEntries(Main.getJda());

                Logger.info("=== " + guildData.getRawGuildID() + " (" + guildData.getName() + ") ===");
                Logger.info("> Dashboards: " + guildData.getLoadedServerDashboards().size());
                Logger.info("> Name......: " + guildData.getGuild().getName());
                Logger.info("> Members...: " + guildData.getGuild().getMemberCount());
            }
        }
    }

    private void invalidSyntax() {
        Logger.error("Invalid syntax! guild <save|load|remove|info> <guildID>");
    }
}
