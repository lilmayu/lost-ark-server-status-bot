package dev.mayuna.lostarkbot.util;

import dev.mayuna.lostarkbot.objects.LostArkRegion;
import dev.mayuna.lostarkbot.objects.ServerDashboard;
import dev.mayuna.lostarkscraper.objects.LostArkServers;
import dev.mayuna.lostarkscraper.objects.ServerStatus;
import dev.mayuna.mayusjdautils.utils.DiscordUtils;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

public class EmbedUtils {

    public static EmbedBuilder createEmbed(ServerDashboard serverDashboard, LostArkServers servers) {
        EmbedBuilder embedBuilder = DiscordUtils.getDefaultEmbed();
        embedBuilder.setTitle("Lost Ark - Server Dashboard");
        embedBuilder.setDescription("`" + servers.getLastUpdated() + "`\n\n<:circle_green:943546669558018139> Online <:circle_red:943546670229114911> Busy <:circle_blue:943546670115848202> Full <:circle_yellow:943546669688049725> Maintenance");

        LinkedHashMap<String, String> regionFields = new LinkedHashMap<>();

        for (LostArkRegion region : LostArkRegion.values()) {
            if (serverDashboard.getHiddenRegions().contains(region.name())) {
                continue;
            }

            String fieldName = region.getFormattedName();
            StringBuilder fieldValue = new StringBuilder();

            for (Map.Entry<String, ServerStatus> entry : Utils.getServersByRegion(region, servers).entrySet()) {
                fieldValue.append(Utils.getServerLine(entry.getKey(), entry.getValue())).append("\n");
            }

            regionFields.put(fieldName, fieldValue.toString());
        }

        LinkedHashMap<String, String> sortedRegionFields = new LinkedHashMap<>();
        regionFields.entrySet()
                .stream()
                .sorted(Comparator.comparing(entry -> entry.getValue().length() * -1)) // * -1 for reversed sorting
                .forEachOrdered(x -> sortedRegionFields.put(x.getKey(), x.getValue()));

        int counter = 0;
        for (Map.Entry<String, String> entry : sortedRegionFields.entrySet()) {
            embedBuilder.addField(entry.getKey(), entry.getValue(), true);

            if (counter == 1 && regionFields.size() > 2) {
                embedBuilder.addBlankField(false);
            }

            counter++;
        }


        if (!serverDashboard.getFavoriteServers().isEmpty()) { // Favorite servers
            String fieldValue = "";

            for (String serverName : serverDashboard.getFavoriteServers()) {
                ServerStatus serverStatus = Utils.getServerStatus(serverName, servers);

                if (serverStatus == null) {
                    fieldValue += "<:circle_black:943546670166188142> " + serverName;
                } else {
                    fieldValue += Utils.getServerLine(serverName, serverStatus) + "\n";
                }
            }

            embedBuilder.addField("Favorites", fieldValue, false);
        }

        return embedBuilder;
    }
}
