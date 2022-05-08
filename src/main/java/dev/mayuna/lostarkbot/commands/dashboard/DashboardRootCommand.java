package dev.mayuna.lostarkbot.commands.dashboard;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.mayuna.lostarkbot.commands.dashboard.subcommands.*;
import dev.mayuna.lostarkbot.util.Utils;

public class DashboardRootCommand extends SlashCommand {

    public DashboardRootCommand() {
        this.name = "dashboard";
        this.help = "Root command of Server dashboard system";

        this.children = new SlashCommand[]{
                new DashboardCreateCommand(),
                new DashboardRemoveCommand(),
                new DashboardInfoCommand(),
                new DashboardForceUpdateCommand(),
                new DashboardResendCommand(),
                new DashboardFavoriteCommand(),
                new DashboardRegionCommand(),
                new DashboardAllRegionsCommand(),
                new DashboardLanguageCommands.DashboardLanguageListCommand(),
                new DashboardLanguageCommands.DashboardLanguageCommand(),
                new DashboardSettingsCommand()
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Utils.makeEphemeral(event, true);

        event.getHook().editOriginal("Psst, you should not be here.").queue();
    }
}
