package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.console.commands.generic.CommandResult;
import dev.mayuna.lostarkbot.managers.ServerDashboardManager;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.lostarkfetcher.objects.api.LostArkServer;
import dev.mayuna.lostarkfetcher.objects.api.LostArkServers;
import dev.mayuna.lostarkfetcher.objects.api.other.LostArkRegion;
import dev.mayuna.mayuslibrary.arguments.ArgumentParser;
import dev.mayuna.mayuslibrary.graphics.console.colors.Color;
import dev.mayuna.mayuslibrary.graphics.console.colors.Colors;

public class LostArkConsoleCommand extends AbstractConsoleCommand {

    public LostArkConsoleCommand() {
        this.name = "lost-ark";
        this.syntax = "<show-cache|update-cache>";
    }

    @Override
    public CommandResult execute(String arguments) {
        ArgumentParser argumentParser = new ArgumentParser(arguments);

        if (argumentParser.hasArgumentAtIndex(0)) {
            switch (argumentParser.getArgumentAtIndex(0).getValue()) {
                case "show-cache" -> {
                    Logger.info("Previous Lost Ark Servers cache: ");
                    print(ServerDashboardManager.getPreviousLostArkServersCache());
                    Logger.info("Current Lost Ark Servers cache: ");
                    print(ServerDashboardManager.getCurrentLostArkServersCache());
                }
                case "update-cache" -> {
                    Logger.info("Updating Lost Ark Servers cache...");

                    try {
                        ServerDashboardManager.updateCache();
                        Logger.success("Successfully updated Lost Ark Servers cache.");
                    } catch (Exception exception) {
                        Logger.throwing(exception);
                        Logger.error("Exception occurred while updating Lost Ark Servers cache!");
                    }

                    Logger.success("Updating done.");
                }
                default -> {
                    return CommandResult.INCORRECT_SYNTAX;
                }
            }

            return CommandResult.SUCCESS;
        }

        return CommandResult.INCORRECT_SYNTAX;
    }

    private void print(LostArkServers lostArkServers) {
        Logger.info("Is null? " + (lostArkServers == null));

        if (lostArkServers != null) {
            Logger.info("There is " + lostArkServers.get().size() + " servers");

            int counter = 0;
            for (LostArkServer serverName : lostArkServers.get()) {
                LostArkRegion region = serverName.getRegion();
                String regionName;

                if (region == null) {
                    regionName = new Color().setBackground(Colors.RED).setForeground(Colors.BLACK).build() + "NOT SET" + Color.RESET;
                } else {
                    regionName = region.name();
                }

                Logger.info("[" + counter + "]: " + serverName.getName() + " // " + serverName.getStatus().name() + " - " + regionName);

                counter++;
            }
        } else {
            Logger.info("Servers are null.");
        }

        Logger.success("Listing done.");
    }
}
