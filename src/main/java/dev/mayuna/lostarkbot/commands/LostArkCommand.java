package dev.mayuna.lostarkbot.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import dev.mayuna.lostarkbot.managers.ServerDashboardManager;
import dev.mayuna.lostarkbot.objects.LostArkRegion;
import dev.mayuna.lostarkbot.objects.ServerDashboard;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayusjdautils.utils.MessageInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class LostArkCommand extends SlashCommand {

    public LostArkCommand() {
        this.name = "lost-ark";

        this.children = new SlashCommand[]{
                new DashboardCreateCommand(),
                new DashboardRemoveCommand(),
                new DashboardStatusCommand(),
                new DashboardUpdateCommand(),
                new DashboardResendCommand(),
                new DashboardSettingsCommand(),
                new DashboardAddFavoriteCommand(),
                new DashboardRemoveFavoriteCommand(),
                new DashboardHideRegionCommand(),
                new DashboardShowRegionCommand()
        };
    }

    private static void makeEphemeral(SlashCommandEvent event, boolean ephemeral) {
        event.deferReply(ephemeral).complete();
    }

    @Override
    protected void execute(SlashCommandEvent event) {
    }

    protected static class DashboardCreateCommand extends SlashCommand {

        public DashboardCreateCommand() {
            this.name = "create";

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            makeEphemeral(event, true);
            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            if (ServerDashboardManager.isServerDashboardInChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("There is already Server Dashboard in this channel!").build()).queue();
                return;
            }

            ServerDashboard dashboard = ServerDashboardManager.createServerDashboard(channel);

            if (dashboard != null) {
                hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully created Server Dashboard.\nYou can edit it with `/lost-ark settings` command!").build())
                        .queue();
            } else {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("There was error while creating Server Dashboard. Please, try again.").build()).queue();
            }

        }
    }

    protected static class DashboardRemoveCommand extends SlashCommand {

        public DashboardRemoveCommand() {
            this.name = "remove";

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            makeEphemeral(event, true);
            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            if (!ServerDashboardManager.isServerDashboardInChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("There is no Server Dashboard in this channel!").build()).queue();
                return;
            }

            if (ServerDashboardManager.deleteServerDashboard(channel)) {
                hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully removed Server Dashboard from this channel!").build()).queue();
            } else {
                hook.editOriginalEmbeds(MessageInfo.warningEmbed("Server Dashboard was removed, however message was unable to be deleted.").build()).queue();
            }
        }
    }

    protected static class DashboardStatusCommand extends SlashCommand {

        public DashboardStatusCommand() {
            this.name = "status";
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            makeEphemeral(event, true);
            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            if (!ServerDashboardManager.isServerDashboardInChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("There is no Server Dashboard in this channel!").build()).queue();
                return;
            }

            ServerDashboard dashboard = ServerDashboardManager.getServerDashboardByChannel(channel);
            long id = dashboard.getManagedMessage().getMessageID();

            hook.editOriginalEmbeds(MessageInfo.informationEmbed("There is Server Dashboard with message ID `" + id + "`.").build()).queue();
        }
    }

    protected static class DashboardUpdateCommand extends SlashCommand {

        public DashboardUpdateCommand() {
            this.name = "update";

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            makeEphemeral(event, true);
            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            if (!ServerDashboardManager.isServerDashboardInChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("There is no Server Dashboard in this channel!").build()).queue();
                return;
            }

            hook.editOriginalEmbeds(MessageInfo.informationEmbed("Updating Server Dashboard...").build()).queue();

            ServerDashboard dashboard = ServerDashboardManager.getServerDashboardByChannel(channel);
            ServerDashboardManager.update(dashboard);
        }
    }

    protected static class DashboardResendCommand extends SlashCommand {

        public DashboardResendCommand() {
            this.name = "resend";

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            makeEphemeral(event, true);
            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            if (!ServerDashboardManager.isServerDashboardInChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("There is no Server Dashboard in this channel!").build()).queue();
                return;
            }

            hook.editOriginalEmbeds(MessageInfo.informationEmbed("Resending Server Dashboard...").build()).queue();

            try {
                ServerDashboard dashboard = ServerDashboardManager.getServerDashboardByChannel(channel);
                dashboard.getManagedMessage().getMessage().delete().complete();
                ServerDashboardManager.update(dashboard);
            } catch (Exception exception) {
                exception.printStackTrace();
                Logger.error("Exception occurred while resending Server Dashboard!");
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Error occurred while resending Server Dashboard! Please, try again.").build()).queue();
            }

        }
    }

    protected static class DashboardSettingsCommand extends SlashCommand {

        public DashboardSettingsCommand() {
            this.name = "settings";

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            makeEphemeral(event, true);

        }
    }

    protected static class DashboardAddFavoriteCommand extends SlashCommand {

        public DashboardAddFavoriteCommand() {
            this.name = "add-favorite";

            List<OptionData> options = new ArrayList<>();
            options.add(new OptionData(OptionType.STRING, "server", "Server name to add into Favorite section", true));
            this.options = options;

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            makeEphemeral(event, true);
            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            if (!ServerDashboardManager.isServerDashboardInChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("There is no Server Dashboard in this channel!").build()).queue();
                return;
            }

            OptionMapping serverOption = event.getOption("server");

            if (serverOption == null) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Missing `server` argument.").build()).queue();
                return;
            }

            String correctServerName = Utils.doesServerExist(serverOption.getAsString());

            if (correctServerName != null) {
                ServerDashboard dashboard = ServerDashboardManager.getServerDashboardByChannel(channel);
                dashboard.getFavoriteServers().add(correctServerName);
                ServerDashboardManager.update(dashboard);
                hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully added server `" + correctServerName + "` into Favorite section!").build()).queue();
            } else {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Server with name `" + serverOption.getAsString() + "` does not exist!").build()).queue();
            }
        }
    }

    protected static class DashboardRemoveFavoriteCommand extends SlashCommand {

        public DashboardRemoveFavoriteCommand() {
            this.name = "remove-favorite";

            List<OptionData> options = new ArrayList<>();
            options.add(new OptionData(OptionType.STRING, "server", "Server name to remove from Favorite section", true));
            this.options = options;

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            makeEphemeral(event, true);
            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            OptionMapping serverOption = event.getOption("server");

            if (serverOption == null) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Missing `server` argument.").build()).queue();
                return;
            }

            String serverName = serverOption.getAsString();

            ServerDashboard dashboard = ServerDashboardManager.getServerDashboardByChannel(channel);
            if (dashboard.getFavoriteServers().contains(serverName)) {
                dashboard.getFavoriteServers().remove(serverName);
                ServerDashboardManager.update(dashboard);

                hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully removed server `" + serverName + "` from Favorite section!").build()).queue();
            } else {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Server `" + serverName + "` is not in Favorite section!").build()).queue();
            }
        }
    }

    protected static class DashboardHideRegionCommand extends SlashCommand {

        public DashboardHideRegionCommand() {
            this.name = "hide-region";

            List<OptionData> options = new ArrayList<>();

            OptionData regionOption = new OptionData(OptionType.STRING, "region", "Region to hide from dashboard", true);
            for (LostArkRegion region : LostArkRegion.values()) {
                regionOption.addChoice(region.getFormattedName(), region.name());
            }
            options.add(regionOption);

            this.options = options;

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            makeEphemeral(event, true);
            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            OptionMapping regionOption = event.getOption("region");

            if (regionOption == null) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Missing `region` argument.").build()).queue();
                return;
            }

            String correctRegion = LostArkRegion.exists(regionOption.getAsString());

            if (correctRegion != null) {
                ServerDashboard dashboard = ServerDashboardManager.getServerDashboardByChannel(channel);
                if (!dashboard.getHiddenRegions().contains(correctRegion)) {
                    dashboard.getHiddenRegions().add(correctRegion);
                    ServerDashboardManager.update(dashboard);
                    hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully hide region `" + regionOption.getName() + "`!").build()).queue();
                } else {
                    hook.editOriginalEmbeds(MessageInfo.successEmbed("Region `" + regionOption.getName() + "` is already hidden!").build()).queue();
                }
            } else {
                hook.editOriginalEmbeds(MessageInfo.successEmbed("Region `" + regionOption.getName() + "` does not exist!").build()).queue();
            }
        }
    }

    protected static class DashboardShowRegionCommand extends SlashCommand {

        public DashboardShowRegionCommand() {
            this.name = "show-region";

            List<OptionData> options = new ArrayList<>();

            OptionData regionOption = new OptionData(OptionType.STRING, "region", "Region to hide from dashboard", true);
            for (LostArkRegion region : LostArkRegion.values()) {
                regionOption.addChoice(region.getFormattedName(), region.name());
            }
            options.add(regionOption);

            this.options = options;

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            makeEphemeral(event, true);

            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            OptionMapping regionOption = event.getOption("region");

            if (regionOption == null) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Missing `region` argument.").build()).queue();
                return;
            }

            String correctRegion = LostArkRegion.exists(regionOption.getAsString());

            if (correctRegion != null) {
                ServerDashboard dashboard = ServerDashboardManager.getServerDashboardByChannel(channel);
                if (dashboard.getHiddenRegions().contains(correctRegion)) {
                    dashboard.getHiddenRegions().remove(correctRegion);
                    ServerDashboardManager.update(dashboard);
                    hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully showed region `" + regionOption.getName() + "`!").build()).queue();
                } else {
                    hook.editOriginalEmbeds(MessageInfo.successEmbed("Region `" + regionOption.getName() + "` is not hidden!").build()).queue();
                }
            } else {
                hook.editOriginalEmbeds(MessageInfo.successEmbed("Region `" + regionOption.getName() + "` does not exist!").build()).queue();
            }

        }
    }
}
