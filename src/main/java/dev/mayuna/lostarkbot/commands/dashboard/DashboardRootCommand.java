package dev.mayuna.lostarkbot.commands.dashboard;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.mayuna.lostarkbot.commands.dashboard.subcommands.*;
import dev.mayuna.lostarkbot.util.Utils;

public class DashboardRootCommand extends SlashCommand {

    public DashboardRootCommand() {
        this.name = "dashboard";
        this.help = "Root command of Lost Ark's server dashboard system";

        this.children = new SlashCommand[]{
                new DashboardCreateCommand(), // /dashboard create
                new DashboardRemoveCommand(), // /dashboard remove
                new DashboardInfoCommand(),   // /dashboard info
                new DashboardForceUpdateCommand(), // /dashboard force-update
                new DashboardResendCommand(),
                new DashboardFavoriteCommand(),
                new DashboardRegionCommand(),
                new DashboardAllRegionsCommand()
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Utils.makeEphemeral(event, true);

        event.getHook().editOriginal("Psst, you should not be here.").queue();
    }
}
