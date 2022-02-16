package dev.mayuna.lostarkbot.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import dev.mayuna.lostarkbot.managers.ServerWidgetManager;
import dev.mayuna.lostarkbot.objects.ServerWidget;
import dev.mayuna.mayusjdautils.utils.MessageInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class ServerWidgetCommand extends SlashCommand {

    public ServerWidgetCommand() {
        this.name = "server-widget";

        this.children = new SlashCommand[]{
                new CreateCommand(),
                new RemoveCommand(),
                new StatusCommand()
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {
    }

    protected static class CreateCommand extends SlashCommand {

        public CreateCommand() {
            this.name = "create";

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            event.deferReply(true).complete();

            ServerWidget serverWidget = ServerWidgetManager.createServerWidget(event.getTextChannel());

            if (serverWidget == null) {
                event.getHook()
                        .editOriginal(MessageInfo.error(
                                "Unable to create Server Widget in this channel. Please, check if there is already one or if bot has permission to send messages in this channel."))
                        .queue();
            } else {
                event.getHook()
                        .editOriginal(MessageInfo.success(
                                "Successfully created Server Widget in this channel! You can use `/server-widget edit` to open GUI editor."))
                        .queue();
            }
        }
    }

    protected static class RemoveCommand extends SlashCommand {

        public RemoveCommand() {
            this.name = "remove";

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            event.deferReply(true).complete();

            if (ServerWidgetManager.deleteServerWidget(event.getTextChannel())) {
                event.getHook().editOriginal(MessageInfo.success("Successfully removed Server Widget from this channel!")).queue();
            } else {
                event.getHook().editOriginal(MessageInfo.warning("Unable to removed Server Widget. You will have to delete it yourself.")).queue();
            }
        }
    }

    protected static class StatusCommand extends SlashCommand {

        public StatusCommand() {
            this.name = "status";
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            event.deferReply(true).complete();

            ServerWidget serverWidget = ServerWidgetManager.getServerWidgetByChannel(event.getTextChannel());

            if (serverWidget == null) {
                event.getHook().editOriginal(MessageInfo.information("There is no Server Widget in this channel.")).queue();
            } else {
                event.getHook().editOriginal(MessageInfo.information("There is Server Widget in this channel with message ID `" + serverWidget.getManagedMessage().getMessageID() + "`.")).queue();
            }
        }
    }
}
