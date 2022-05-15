package dev.mayuna.lostarkbot.commands.notifications;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.mayuna.lostarkbot.commands.notifications.subcommands.*;
import dev.mayuna.lostarkbot.util.Utils;

public class NotifyRootCommand extends SlashCommand {

    public NotifyRootCommand() {
        this.name = "notify";
        this.help = "Root command of Notification system";

        this.children = new SlashCommand[]{
                new NotifyCreateCommand(),
                new NotifyRemoveCommand(),
                new NotifyInfoCommand(),

                new NotifyNewsCommand(),
                new NotifyForumsCommand(),

                new NotifyTwitterCommand(),
                new NotifyTwitterFilterCommand(),
                new NotifyTwitterPingCommand(),
                new NotifyTwitterSettingsCommand(),

                new NotifyStatusServerCommand(),
                new NotifyStatusRegionCommand(),
                new NotifyStatusWhitelistCommand(),
                new NotifyStatusBlacklistCommand(),
                new NotifyStatusPingCommand(),

                new NotifyClearCommand()
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        if (!Utils.makeEphemeral(event, true)) {
            return;
        }

        event.getHook().editOriginal("Psst, you should not be here.").queue();
    }
}
