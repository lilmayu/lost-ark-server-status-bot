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

public class DashboardAllRegionsCommand extends SlashCommand {

    public DashboardAllRegionsCommand() {
        this.name = "all-regions";
        this.help = "Hides/shows all regions from/in current Server dashboard";

        List<OptionData> options = new ArrayList<>(1);
        options.add(Utils.getShowHideArgument());
        this.options = options;

        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Utils.makeEphemeral(event, true);
        TextChannel textChannel = event.getTextChannel();
        InteractionHook interactionHook = event.getHook();

        OptionMapping actionOption = AutoMessageUtils.getOptionMapping(event, "action");

        if (actionOption == null) {
            return;
        }

        if (!AutoMessageUtils.isEverythingAlrightDashboard(textChannel, interactionHook)) {
            return;
        }

        ServerDashboard dashboard = ServerDashboardHelper.getServerDashboard(textChannel);

        switch (actionOption.getAsString()) {
            case "show" -> {
                dashboard.showAllRegions();
                dashboard.update();
                interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully showed all regions!").build()).queue();
            }
            case "hide" -> {
                dashboard.hideAllRegions();
                dashboard.update();
                interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully hidden all regions!").build()).queue();
            }
        }
    }
}
