package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.managers.ServerDashboardManager;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.lostarkscraper.objects.LostArkServer;
import dev.mayuna.lostarkscraper.objects.LostArkServers;

public class LostArkConsoleCommand extends AbstractConsoleCommand {

    public LostArkConsoleCommand() {
        this.name = "lost-ark";
    }

    @Override
    public void execute(String arguments) {
        switch (arguments) {
            case "show-cache" -> {
                Logger.info("Current Lost Ark Servers cache: ");

                LostArkServers servers = ServerDashboardManager.getLostArkServersCache();
                Logger.info("Is null? " + (servers == null));

                if (servers != null) {
                    Logger.info("There is " + servers.getServers().size() + " servers");

                    int counter = 0;
                    for (LostArkServer server : servers.getServers()) {
                        Logger.info("[" + counter + "]: " + server.getName() + " // " + server.getStatus().name());

                        counter++;
                    }
                }

                Logger.info("Listing done.");
            }
            case "update-cache" -> {
                Logger.info("Updating Lost Ark Servers cache...");

                try {
                    ServerDashboardManager.updateCache();
                    Logger.success("Successfully updated Lost Ark Servers cache.");
                } catch (Exception exception) {
                    exception.printStackTrace();
                    Logger.error("Exception occurred while updating Lost Ark Servers cache!");
                }
            }
            default -> {
                Logger.warn("Syntax: /lost-ark <show-cache|update-cache>");
            }
        }
    }
}
