package dev.mayuna.lostarkbot.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import dev.mayuna.lostarkbot.helpers.ServerDashboardHelper;
import dev.mayuna.lostarkbot.managers.LanguageManager;
import dev.mayuna.lostarkbot.objects.LostArkRegion;
import dev.mayuna.lostarkbot.objects.core.LanguagePack;
import dev.mayuna.lostarkbot.objects.core.ServerDashboard;
import dev.mayuna.lostarkbot.util.PermissionUtils;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayusjdautils.utils.MessageInfo;
import net.dv8tion.jda.api.EmbedBuilder;
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
        this.help = "Root command of Lost Ark's server dashboard system";

        this.children = new SlashCommand[]{
                new DashboardCreateCommand(),
                new DashboardRemoveCommand(),
                new DashboardStatusCommand(),
                new DashboardUpdateCommand(),
                new DashboardResendCommand(),
                new DashboardAddFavoriteCommand(),
                new DashboardRemoveFavoriteCommand(),
                new DashboardHideRegionCommand(),
                new DashboardShowRegionCommand(),
                new DashboardHideAllRegionsCommand(),
                new DashboardShowAllRegionsCommand(),
                new LanguageListCommand(),
                new LanguageCommand()
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {
    }

    protected static class DashboardCreateCommand extends SlashCommand {

        public DashboardCreateCommand() {
            this.name = "create";
            this.help = "Creates Server dashboard";

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Utils.makeEphemeral(event, true);
            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            if (PermissionUtils.checkPermissionsAndSendIfMissing(channel, hook)) {
                return;
            }

            if (ServerDashboardHelper.isServerDashboardInChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("There is already Server Dashboard in this channel!").build()).queue();
                return;
            }

            ServerDashboard dashboard = ServerDashboardHelper.createServerDashboard(channel);

            if (dashboard != null) {
                hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully created Server Dashboard.").build())
                        .queue();
            } else {
                if (!channel.canTalk()) {
                    hook.editOriginalEmbeds(MessageInfo.errorEmbed(
                                    "Bot cannot send messages in this channel. Please, check bot's permissions in your server! (Write Messages and View Channel permissions)").build())
                            .queue();
                } else {
                    hook.editOriginalEmbeds(MessageInfo.errorEmbed("There was error while creating Server Dashboard. Please, try again. Check if bot has View Channel permission!")
                                                    .build()).queue();
                }
            }
        }
    }

    protected static class DashboardRemoveCommand extends SlashCommand {

        public DashboardRemoveCommand() {
            this.name = "remove";
            this.help = "Removes Server dashboard";

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Utils.makeEphemeral(event, true);
            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            if (!ServerDashboardHelper.isServerDashboardInChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("There is no Server Dashboard in this channel!").build()).queue();
                return;
            }

            if (ServerDashboardHelper.deleteServerDashboard(channel)) {
                hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully removed Server Dashboard from this channel!").build()).queue();
            } else {
                hook.editOriginalEmbeds(MessageInfo.warningEmbed(
                        "Server Dashboard was removed, however message was unable to be deleted. Probably this bot does not have **View Channel** permission?").build()).queue();
            }
        }
    }

    protected static class DashboardStatusCommand extends SlashCommand {

        public DashboardStatusCommand() {
            this.name = "status";
            this.help = "Shows debug information for Server dashboard";
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Utils.makeEphemeral(event, true);
            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            if (PermissionUtils.checkPermissionsAndSendIfMissing(channel, hook)) {
                return;
            }

            if (!ServerDashboardHelper.isServerDashboardInChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("There is no Server Dashboard in this channel!").build()).queue();
                return;
            }

            ServerDashboard dashboard = ServerDashboardHelper.getServerDashboard(channel);
            long id = dashboard.getManagedGuildMessage().getRawGuildID();

            hook.editOriginalEmbeds(MessageInfo.informationEmbed("There is Server Dashboard with message ID `" + id + "`.").build()).queue();
        }
    }

    protected static class DashboardUpdateCommand extends SlashCommand {

        public DashboardUpdateCommand() {
            this.name = "update";
            this.help = "Forces Server dashboard to update itself";

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Utils.makeEphemeral(event, true);
            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            if (PermissionUtils.checkPermissionsAndSendIfMissing(channel, hook)) {
                return;
            }

            if (!ServerDashboardHelper.isServerDashboardInChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("There is no Server Dashboard in this channel!").build()).queue();
                return;
            }

            ServerDashboard dashboard = ServerDashboardHelper.getServerDashboard(channel);
            ServerDashboardHelper.updateServerDashboard(dashboard);

            hook.editOriginalEmbeds(MessageInfo.successEmbed("Server Dashboard updated!\n\nNote: The server dashboard updates itself every 5 minute.").build()).queue();
        }
    }

    protected static class DashboardResendCommand extends SlashCommand {

        public DashboardResendCommand() {
            this.name = "resend";
            this.help = "Forces Server dashboard to resend itself";

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Utils.makeEphemeral(event, true);
            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            if (PermissionUtils.checkPermissionsAndSendIfMissing(channel, hook)) {
                return;
            }

            if (!ServerDashboardHelper.isServerDashboardInChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("There is no Server Dashboard in this channel!").build()).queue();
                return;
            }

            try {
                ServerDashboard dashboard = ServerDashboardHelper.getServerDashboard(channel);
                dashboard.getManagedGuildMessage().getMessage().delete().complete();
                ServerDashboardHelper.updateServerDashboard(dashboard);

                hook.editOriginalEmbeds(MessageInfo.successEmbed("Server Dashboard resent!").build()).queue();
            } catch (Exception exception) {
                Logger.throwing(exception);
                Logger.error("Exception occurred while resending Server Dashboard!");
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Error occurred while resending Server Dashboard! Please, try again.").build()).queue();
            }

        }
    }

    protected static class DashboardAddFavoriteCommand extends SlashCommand {

        public DashboardAddFavoriteCommand() {
            this.name = "add-favorite";
            this.help = "Adds specified server into Favorites section";

            List<OptionData> options = new ArrayList<>();
            options.add(new OptionData(OptionType.STRING, "server", "Server name to add into Favorite section", true));
            this.options = options;

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Utils.makeEphemeral(event, true);
            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            if (PermissionUtils.checkPermissionsAndSendIfMissing(channel, hook)) {
                return;
            }

            OptionMapping serverOption = event.getOption("server");

            if (serverOption == null) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Missing `server` argument.").build()).queue();
                return;
            }

            if (!ServerDashboardHelper.isServerDashboardInChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("There is no Server Dashboard in this channel!").build()).queue();
                return;
            }

            String correctServerName = Utils.doesServerExist(serverOption.getAsString());

            if (correctServerName != null) {
                ServerDashboard dashboard = ServerDashboardHelper.getServerDashboard(channel);
                dashboard.getFavoriteServers().add(correctServerName);
                ServerDashboardHelper.updateServerDashboard(dashboard);
                hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully added server `" + correctServerName + "` into Favorite section!").build()).queue();
            } else {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Server with name `" + serverOption.getAsString() + "` does not exist!").build()).queue();
            }
        }
    }

    protected static class DashboardRemoveFavoriteCommand extends SlashCommand {

        public DashboardRemoveFavoriteCommand() {
            this.name = "remove-favorite";
            this.help = "Removes specified server from Favorites section";

            List<OptionData> options = new ArrayList<>();
            options.add(new OptionData(OptionType.STRING, "server", "Server name to remove from Favorite section", true));
            this.options = options;

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Utils.makeEphemeral(event, true);
            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            if (PermissionUtils.checkPermissionsAndSendIfMissing(channel, hook)) {
                return;
            }

            OptionMapping serverOption = event.getOption("server");

            if (serverOption == null) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Missing `server` argument.").build()).queue();
                return;
            }

            if (!ServerDashboardHelper.isServerDashboardInChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("There is no Server Dashboard in this channel!").build()).queue();
                return;
            }

            String serverName = serverOption.getAsString();

            ServerDashboard dashboard = ServerDashboardHelper.getServerDashboard(channel);
            if (dashboard.getFavoriteServers().contains(serverName)) {
                dashboard.getFavoriteServers().remove(serverName);
                ServerDashboardHelper.updateServerDashboard(dashboard);

                hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully removed server `" + serverName + "` from Favorite section!").build()).queue();
            } else {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Server `" + serverName + "` is not in Favorite section!").build()).queue();
            }
        }
    }

    protected static class DashboardHideRegionCommand extends SlashCommand {

        public DashboardHideRegionCommand() {
            this.name = "hide-region";
            this.help = "Hides specified region from Server dashboard";

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
            Utils.makeEphemeral(event, true);
            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            if (PermissionUtils.checkPermissionsAndSendIfMissing(channel, hook)) {
                return;
            }

            OptionMapping regionOption = event.getOption("region");

            if (regionOption == null) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Missing `region` argument.").build()).queue();
                return;
            }

            if (!ServerDashboardHelper.isServerDashboardInChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("There is no Server Dashboard in this channel!").build()).queue();
                return;
            }

            String correctRegion = LostArkRegion.exists(regionOption.getAsString());

            if (correctRegion != null) {
                ServerDashboard dashboard = ServerDashboardHelper.getServerDashboard(channel);
                if (!dashboard.getHiddenRegions().contains(correctRegion)) {
                    dashboard.getHiddenRegions().add(correctRegion);
                    ServerDashboardHelper.updateServerDashboard(dashboard);
                    hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully hide region `" + correctRegion + "`!").build()).queue();
                } else {
                    hook.editOriginalEmbeds(MessageInfo.successEmbed("Region `" + correctRegion + "` is already hidden!").build()).queue();
                }
            } else {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Region `" + regionOption.getAsString() + "` does not exist!").build()).queue();
            }
        }
    }

    protected static class DashboardShowRegionCommand extends SlashCommand {

        public DashboardShowRegionCommand() {
            this.name = "show-region";
            this.help = "Shows specified region in Server dashboard";

            List<OptionData> options = new ArrayList<>();

            OptionData regionOption = new OptionData(OptionType.STRING, "region", "Region to show if it is hidden", true);
            for (LostArkRegion region : LostArkRegion.values()) {
                regionOption.addChoice(region.getFormattedName(), region.name());
            }
            options.add(regionOption);

            this.options = options;

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Utils.makeEphemeral(event, true);

            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            if (PermissionUtils.checkPermissionsAndSendIfMissing(channel, hook)) {
                return;
            }

            OptionMapping regionOption = event.getOption("region");

            if (regionOption == null) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Missing `region` argument.").build()).queue();
                return;
            }

            if (!ServerDashboardHelper.isServerDashboardInChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("There is no Server Dashboard in this channel!").build()).queue();
                return;
            }

            String correctRegion = LostArkRegion.exists(regionOption.getAsString());

            if (correctRegion != null) {
                ServerDashboard dashboard = ServerDashboardHelper.getServerDashboard(channel);
                if (dashboard.getHiddenRegions().contains(correctRegion)) {
                    dashboard.getHiddenRegions().remove(correctRegion);
                    ServerDashboardHelper.updateServerDashboard(dashboard);
                    hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully showed region `" + correctRegion + "`!").build()).queue();
                } else {
                    hook.editOriginalEmbeds(MessageInfo.successEmbed("Region `" + correctRegion + "` is not hidden!").build()).queue();
                }
            } else {
                hook.editOriginalEmbeds(MessageInfo.successEmbed("Region `" + regionOption.getAsString() + "` does not exist!").build()).queue();
            }

        }
    }

    protected static class DashboardHideAllRegionsCommand extends SlashCommand {

        public DashboardHideAllRegionsCommand() {
            this.name = "hide-all-regions";
            this.help = "Hides all regions from Server dashboard";

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Utils.makeEphemeral(event, true);
            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            if (PermissionUtils.checkPermissionsAndSendIfMissing(channel, hook)) {
                return;
            }

            if (!ServerDashboardHelper.isServerDashboardInChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("There is no Server Dashboard in this channel!").build()).queue();
                return;
            }

            ServerDashboard dashboard = ServerDashboardHelper.getServerDashboard(channel);
            dashboard.getHiddenRegions().clear();
            for (LostArkRegion region : LostArkRegion.values()) {
                dashboard.getHiddenRegions().add(region.name());
            }
            ServerDashboardHelper.updateServerDashboard(dashboard);
            hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully hide all regions!").build()).queue();
        }
    }

    protected static class DashboardShowAllRegionsCommand extends SlashCommand {

        public DashboardShowAllRegionsCommand() {
            this.name = "show-all-regions";
            this.help = "Shows all regions on Server dashboard";

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Utils.makeEphemeral(event, true);
            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            if (PermissionUtils.checkPermissionsAndSendIfMissing(channel, hook)) {
                return;
            }

            if (!ServerDashboardHelper.isServerDashboardInChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("There is no Server Dashboard in this channel!").build()).queue();
                return;
            }

            ServerDashboard dashboard = ServerDashboardHelper.getServerDashboard(channel);
            dashboard.getHiddenRegions().clear();
            ServerDashboardHelper.updateServerDashboard(dashboard);
            hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully showed all regions!").build()).queue();
        }
    }

    protected static class LanguageListCommand extends SlashCommand {

        public LanguageListCommand() {
            this.name = "language-list";
            this.help = "Lists all available languages";
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Utils.makeEphemeral(event, true);
            InteractionHook hook = event.getHook();

            EmbedBuilder embedBuilder = MessageInfo.informationEmbed("");
            embedBuilder.setTitle("Available languages");

            String description = "Format: `code` - name\n\n";
            for (LanguagePack languagePack : LanguageManager.getLoadedLanguages()) {
                description += "`" + languagePack.getLangCode() + "` - " + languagePack.getLangName() + "\n";
            }
            embedBuilder.setDescription(description);
            embedBuilder.setFooter("There are " + LanguageManager.getLoadedLanguages().size() + " languages");
            hook.editOriginalEmbeds(embedBuilder.build()).queue();
        }
    }

    protected static class LanguageCommand extends SlashCommand {

        public LanguageCommand() {
            this.name = "language";
            this.help = "Changes dashboard's language";

            List<OptionData> options = new ArrayList<>();
            OptionData languageOption = new OptionData(OptionType.STRING, "code", "Language code. You can see all languages via /lost-ark language-list", true);
            options.add(languageOption);
            this.options = options;

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Utils.makeEphemeral(event, true);
            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            if (PermissionUtils.checkPermissionsAndSendIfMissing(channel, hook)) {
                return;
            }

            OptionMapping languageOption = event.getOption("code");

            if (languageOption == null) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Missing `code` argument.").build()).queue();
                return;
            }

            LanguagePack languagePack = LanguageManager.getLanguageByCode(languageOption.getAsString());

            if (languagePack == null) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Invalid language code `" + languageOption.getAsString() + "`.\n\nYou can see all languages via `/lost-ark language-list` command")
                                                .build()).queue();
                return;
            }

            if (!ServerDashboardHelper.isServerDashboardInChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("There is no Server Dashboard in this channel!").build()).queue();
                return;
            }

            ServerDashboard dashboard = ServerDashboardHelper.getServerDashboard(channel);
            dashboard.setLangCode(languagePack.getLangCode());
            ServerDashboardHelper.updateServerDashboard(dashboard);

            hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully changed language to **" + languagePack.getLangName() + "**!").build()).queue();
        }
    }
}
