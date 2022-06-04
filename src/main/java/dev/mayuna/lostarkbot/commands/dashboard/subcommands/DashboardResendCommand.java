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

public class DashboardResendCommand extends SlashCommand {

    public DashboardResendCommand() {
        this.name = "resend";
        this.help = "Resends current Server dashboard";

        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        if (!Utils.makeEphemeral(event, true)) {
            return;
        }
        TextChannel textChannel = event.getTextChannel();
        InteractionHook interactionHook = event.getHook();

        if (!AutoMessageUtils.isBotFullyLoaded(interactionHook)) {
            return;
        }

        if (!AutoMessageUtils.isEverythingAlrightDashboard(textChannel, interactionHook)) {
            return;
        }

        ServerDashboard dashboard = ServerDashboardHelper.getServerDashboard(textChannel);

        Runnable runnable = () -> {
            dashboard.update().thenAcceptAsync(result -> {
                if (result) {
                    interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Server dashboard has been resent.").build()).queue();
                } else {
                    interactionHook.editOriginalEmbeds(MessageInfo.warningEmbed(
                                                                          "There was a problem updating the Server dashboard.\n\nPlease, try `/dashboard force-update`. You can also recreate current Server dashboard with `/dashboard remove` and `/dashboard create` commands.")
                                                                  .build()).queue();
                }
            });
        };

        try {
            dashboard.getManagedGuildMessage().getMessage().delete().queue(success -> {
                runnable.run();
            }, failure -> {
                runnable.run();
            });
        } catch (Exception ignored) {
        }
    }
}