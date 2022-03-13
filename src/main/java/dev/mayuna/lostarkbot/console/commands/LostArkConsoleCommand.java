package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.helpers.ServerDashboardHelper;
import dev.mayuna.lostarkbot.objects.LostArkRegion;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.lostarkscraper.objects.LostArkServer;
import dev.mayuna.lostarkscraper.objects.LostArkServers;
import dev.mayuna.mayuslibrary.console.colors.Color;
import dev.mayuna.mayuslibrary.console.colors.Colors;

public class LostArkConsoleCommand extends AbstractConsoleCommand {

    public LostArkConsoleCommand() {
        this.name = "lost-ark";
    }

    @Override
    public void execute(String arguments) {
        switch (arguments) {
            case "show-cache" -> {
                Logger.info("Current Lost Ark Servers cache: ");

                LostArkServers servers = ServerDashboardHelper.getLostArkServersCache();
                Logger.info("Is null? " + (servers == null));

                if (servers != null) {
                    Logger.info("There is " + servers.getServers().size() + " servers");

                    int counter = 0;
                    for (LostArkServer server : servers.getServers()) {
                        LostArkRegion region = Utils.getRegionForServer(server.getName());
                        String regionName;

                        if (region == null) {
                            regionName = new Color().setBackground(Colors.RED).setForeground(Colors.BLACK).build() + "NOT SET" + Color.RESET;
                        } else {
                            regionName = region.name();
                        }

                        Logger.info("[" + counter + "]: " + server.getName() + " // " + server.getStatus().name() + " - " + regionName);

                        counter++;
                    }
                }

                Logger.info("Listing done.");
            }
            case "update-cache" -> {
                Logger.info("Updating Lost Ark Servers cache...");

                try {
                    ServerDashboardHelper.updateCache();
                    Logger.success("Successfully updated Lost Ark Servers cache.");
                } catch (Exception exception) {
                    exception.printStackTrace();
                    Logger.error("Exception occurred while updating Lost Ark Servers cache!");
                }
            }
            default -> {
                Logger.error("Syntax: lost-ark <show-cache|update-cache>");
            }
        }
    }
}
