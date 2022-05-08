package dev.mayuna.lostarkbot.commands.dashboard.subcommands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.mayuna.lostarkbot.helpers.ServerDashboardHelper;
import dev.mayuna.lostarkbot.objects.features.ServerDashboard;
import dev.mayuna.lostarkbot.util.AutoMessageUtils;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.mayusjdautils.util.MessageInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class DashboardRegionCommand extends SlashCommand {

    public DashboardRegionCommand() {
        this.name = "region";
        this.help = "Hides/shows specified region from/in current Server dashboard";

        List<OptionData> options = new ArrayList<>(2);
        options.add(Utils.getShowHideArgument());
        options.add(Utils.getRegionArgument());
        this.options = options;

        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Utils.makeEphemeral(event, true);
        TextChannel textChannel = event.getTextChannel();
        InteractionHook interactionHook = event.getHook();

        OptionMapping actionOption = AutoMessageUtils.getOptionMapping(event, "action");
        OptionMapping regionOption = AutoMessageUtils.getOptionMapping(event, "region");

        if (actionOption == null || regionOption == null) {
            return;
        }

        if (!AutoMessageUtils.isEverythingAlrightDashboard(textChannel, interactionHook)) {
            return;
        }

        String region = regionOption.getAsString();
        ServerDashboard dashboard = ServerDashboardHelper.getServerDashboard(textChannel);

        switch (actionOption.getAsString()) {
            case "show" -> {
                if (dashboard.removeFromHiddenRegions(region)) {
                    dashboard.update();
                    dashboard.save();
                    interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully showed region `" + region + "`!").build()).queue();
                } else {
                    interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("Region `" + region + "` is already added or does not exist!").build()).queue();
                }
            }
            case "hide" -> {
                if (dashboard.addToHiddenRegions(region)) {
                    dashboard.update();
                    dashboard.save();
                    interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully hidden region `" + region + "`!").build()).queue();
                } else {
                    interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("Region `" + region + "` is already hidden!").build()).queue();
                }
            }
        }
    }
}
