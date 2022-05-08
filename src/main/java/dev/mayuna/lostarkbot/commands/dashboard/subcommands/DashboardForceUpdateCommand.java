package dev.mayuna.lostarkbot.commands.dashboard.subcommands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.mayuna.lostarkbot.helpers.ServerDashboardHelper;
import dev.mayuna.lostarkbot.util.AutoMessageUtils;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.mayusjdautils.util.MessageInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class DashboardForceUpdateCommand extends SlashCommand {

    public DashboardForceUpdateCommand() {
        this.name = "force-update";
        this.help = "Force updates current Server dashboard";

        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Utils.makeEphemeral(event, true);
        TextChannel textChannel = event.getTextChannel();
        InteractionHook interactionHook = event.getHook();

        if (!AutoMessageUtils.isEverythingAlrightDashboard(textChannel, interactionHook)) {
            return;
        }

        ServerDashboardHelper.getServerDashboard(textChannel).update();

        interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Server Dashboard updated!\n\nNote: The server dashboard updates itself every 5 minute.").build()).queue();
    }
}
