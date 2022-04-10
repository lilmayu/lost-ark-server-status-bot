package dev.mayuna.lostarkbot.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import dev.mayuna.lostarkbot.api.unofficial.objects.ForumsCategory;
import dev.mayuna.lostarkbot.api.unofficial.objects.NewsCategory;
import dev.mayuna.lostarkbot.helpers.NotificationChannelHelper;
import dev.mayuna.lostarkbot.objects.LostArkRegion;
import dev.mayuna.lostarkbot.objects.core.NotificationChannel;
import dev.mayuna.lostarkbot.util.PermissionUtils;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.lostarkscraper.objects.ServerStatus;
import dev.mayuna.mayusjdautils.utils.DiscordUtils;
import dev.mayuna.mayusjdautils.utils.MessageInfo;
import dev.mayuna.mayuslibrary.utils.StringUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class NotificationsCommand extends SlashCommand {

    public NotificationsCommand() {
        this.name = "notifications";

        this.children = new SlashCommand[]{
                new CreateCommand(),
                new RemoveCommand(),
                new StatusCommand(),
                new NewsCommand(),
                new ForumsCommand(),
                new StatusServerCommand(),
                new StatusRegionCommand(),
                new ClearCommand(),
                new StatusWhitelistCommand(),
                new StatusPingCommand(),
                new TwitterCommand(),
                new TwitterFilterCommand()
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {
    }

    protected static class CreateCommand extends SlashCommand {

        public CreateCommand() {
            this.name = "create";
            this.help = "Marks current Text Channel as Notification Channel";

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

            if (NotificationChannelHelper.isNotificationChannelInChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("This channel is already marked as Notification Channel!").build()).queue();
                return;
            }

            NotificationChannel notificationChannel = NotificationChannelHelper.createNotificationChannel(channel);

            if (notificationChannel != null) {
                hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully marked this channel as Notification Channel.").build()).queue();
                notificationChannel.save();
            } else {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("There was error while marking this channel as Notification Channel. Please, try again. Check if bot has all necessary permissions. However, you should not see this message ever.").build()).queue();
            }
        }
    }

    protected static class RemoveCommand extends SlashCommand {

        public RemoveCommand() {
            this.name = "remove";
            this.help = "Unmarks current Text Channel as Notification Channel";

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Utils.makeEphemeral(event, true);
            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            if (!NotificationChannelHelper.isNotificationChannelInChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("This channel isn't marked as Notification Channel!").build()).queue();
                return;
            }

            NotificationChannel notificationChannel = NotificationChannelHelper.getNotificationChannel(channel);

            if (NotificationChannelHelper.deleteNotificationChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully unmarked this channel as Notification Channel.").build()).queue();
                notificationChannel.save();
            } else {
                hook.editOriginalEmbeds(MessageInfo.warningEmbed("Notification channel could not be unmarked. Please, try again. However, you should not see this message ever.").build()).queue();
            }
        }
    }

    protected static class StatusCommand extends SlashCommand {

        public StatusCommand() {
            this.name = "status";
            this.help = "Checks if current Text Channel is marked as Notification Channel";

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Utils.makeEphemeral(event, true);
            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            NotificationChannel notificationChannel = null;
            EmbedBuilder embedBuilder = DiscordUtils.getDefaultEmbed();
            embedBuilder.setTitle("Lost Ark Notifications");

            if (PermissionUtils.checkPermissionsAndSendIfMissing(channel, hook)) {
                return;
            }

            if (!NotificationChannelHelper.isNotificationChannelInChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("This channel isn't marked as Notification Channel!").build()).queue();
                return;
            }

            notificationChannel = NotificationChannelHelper.getNotificationChannel(channel);
            embedBuilder.setDescription("This channel is marked as Notification Channel.");

            String newsCategories = "";
            if (notificationChannel.getNewsCategories().isEmpty()) {
                newsCategories = "No News categories enabled.";
            } else {
                for (NewsCategory newsCategory : notificationChannel.getNewsCategories()) {
                    newsCategories += newsCategory.toString();

                    if (!Utils.isLast(notificationChannel.getNewsCategories(), newsCategory)) {
                        newsCategories += ",\n";
                    }
                }
            }

            String forumsCategories = "";
            if (notificationChannel.getForumsCategories().isEmpty()) {
                forumsCategories = "No Forums categories enabled.";
            } else {
                for (ForumsCategory forumsCategory : notificationChannel.getForumsCategories()) {
                    forumsCategories += forumsCategory.toString();

                    if (!Utils.isLast(notificationChannel.getForumsCategories(), forumsCategory)) {
                        forumsCategories += ",\n";
                    }
                }
            }

            String regions = "";
            if (notificationChannel.getRegions().isEmpty()) {
                regions = "No Region status changes enabled";
            } else {
                for (LostArkRegion lostArkRegion : notificationChannel.getRegions()) {
                    regions += lostArkRegion.getFormattedName();

                    if (!Utils.isLast(notificationChannel.getRegions(), lostArkRegion)) {
                        regions += ",\n";
                    }
                }
            }

            String servers = "";
            if (notificationChannel.getServers().isEmpty()) {
                servers = "No Region status changes enabled";
            } else {
                for (String serverName : notificationChannel.getServers()) {
                    servers += serverName;

                    if (!Utils.isLast(notificationChannel.getServers(), serverName)) {
                        servers += ",\n";
                    }
                }
            }

            String statusWhitelist = "";
            if (notificationChannel.getStatusWhitelist().isEmpty()) {
                statusWhitelist = "Status whitelist is empty.";
            } else {
                for (String status : notificationChannel.getStatusWhitelist()) {
                    if (statusWhitelist.equals("GOOD")) {
                        statusWhitelist += "Online";
                    } else {
                        statusWhitelist += StringUtils.prettyString(status);
                    }

                    if (!Utils.isLast(notificationChannel.getStatusWhitelist(), status)) {
                        statusWhitelist += ", ";
                    }
                }
            }

            String pingRoles = "";
            if (notificationChannel.getRoleIds().isEmpty()) {
                pingRoles = "No roles to ping on server status change.";
            } else {
                List<String> roleIdsToRemove = new LinkedList<>();
                for (String roleId : notificationChannel.getRoleIds()) {
                    Role role = notificationChannel.getManagedTextChannel().getGuild().getRoleById(roleId);

                    if (role != null) {
                        if ((pingRoles + role.getAsMention()).length() > 2000) {
                            continue;
                        }

                        pingRoles += role.getAsMention();
                    } else {
                        roleIdsToRemove.add(roleId);
                    }


                    if (!Utils.isLast(notificationChannel.getRoleIds(), roleId)) {
                        pingRoles += ", ";
                    }
                }
                notificationChannel.getRoleIds().removeAll(roleIdsToRemove);

                if (pingRoles.isEmpty()) {
                    pingRoles = "No roles to ping on server status change.";
                }
            }

            String twitter = "";
            if (!notificationChannel.isTwitterEnabled()) {
                twitter += "Twitter notifications are **disabled**.";
            } else {
                twitter += "Twitter notifications are **enabled**.";
            }

            if (notificationChannel.getTwitterKeywords().isEmpty()) {
                twitter += "\nThere are no filtering keywords.";
            } else {
                twitter += "\nFiltered keywords: ";
                for (String keyword : notificationChannel.getTwitterKeywords()) {
                    twitter += keyword;

                    if (!Utils.isLast(notificationChannel.getTwitterKeywords(), keyword)) {
                        twitter += ", ";
                    }
                }
            }

            embedBuilder.addField("Enabled News Categories", newsCategories, true);
            embedBuilder.addField("Enabled Forums Categories", forumsCategories, true);
            embedBuilder.addBlankField(false);
            embedBuilder.addField("Enabled Region status changes", regions, true);
            embedBuilder.addField("Enabled Server status changes", servers, true);
            embedBuilder.addBlankField(false);
            embedBuilder.addField("Status whitelist", statusWhitelist, true);
            embedBuilder.addField("Ping roles", pingRoles, true);
            embedBuilder.addBlankField(false);
            embedBuilder.addField("Twitter", twitter, true);

            hook.editOriginalEmbeds(embedBuilder.build()).queue();
        }
    }

    protected static class NewsCommand extends SlashCommand {

        public NewsCommand() {
            this.name = "news";

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};

            List<OptionData> options = new ArrayList<>(2);
            options.add(Utils.getEnableDisableArgument());
            options.add(Utils.getNewsCategoryArgument());
            this.options = options;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Utils.makeEphemeral(event, true);
            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            OptionMapping actionOption = event.getOption("action");
            OptionMapping newsCategoryOption = event.getOption("category");

            if (actionOption == null) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Missing `action` argument.").build()).queue();
                return;
            }

            if (newsCategoryOption == null) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Missing `category` argument.").build()).queue();
                return;
            }


            if (PermissionUtils.checkPermissionsAndSendIfMissing(channel, hook)) {
                return;
            }

            if (!NotificationChannelHelper.isNotificationChannelInChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("This channel isn't marked as Notification Channel!").build()).queue();
                return;
            }

            NotificationChannel notificationChannel = NotificationChannelHelper.getNotificationChannel(channel);
            NewsCategory newsCategory = NewsCategory.valueOf(newsCategoryOption.getAsString());

            switch (actionOption.getAsString()) {
                case "enable" -> {
                    if (notificationChannel.enable(newsCategory)) {
                        hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully enabled notifications for News category **" + newsCategory + "**!").build()).queue();
                        notificationChannel.save();
                    } else {
                        hook.editOriginalEmbeds(MessageInfo.errorEmbed("Notifications for News category **" + newsCategory + "** are already enabled!").build()).queue();
                    }
                }
                case "disable" -> {
                    if (notificationChannel.disable(newsCategory)) {
                        hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully disabled notifications for News category **" + newsCategory + "**!").build()).queue();
                        notificationChannel.save();
                    } else {
                        hook.editOriginalEmbeds(MessageInfo.errorEmbed("Notifications for News category **" + newsCategory + "** are already disabled!").build()).queue();
                    }
                }
            }
        }
    }

    protected static class ForumsCommand extends SlashCommand {

        public ForumsCommand() {
            this.name = "forums";

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};

            List<OptionData> options = new ArrayList<>(2);
            options.add(Utils.getEnableDisableArgument());
            options.add(Utils.getForumsCategoryArgument());
            this.options = options;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Utils.makeEphemeral(event, true);
            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            OptionMapping actionOption = event.getOption("action");
            OptionMapping forumsCategoriesOption = event.getOption("category");

            if (actionOption == null) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Missing `action` argument.").build()).queue();
                return;
            }

            if (forumsCategoriesOption == null) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Missing `category` argument.").build()).queue();
                return;
            }


            if (PermissionUtils.checkPermissionsAndSendIfMissing(channel, hook)) {
                return;
            }

            if (!NotificationChannelHelper.isNotificationChannelInChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("This channel isn't marked as Notification Channel!").build()).queue();
                return;
            }

            NotificationChannel notificationChannel = NotificationChannelHelper.getNotificationChannel(channel);
            ForumsCategory forumsCategory = ForumsCategory.valueOf(forumsCategoriesOption.getAsString());

            switch (actionOption.getAsString()) {
                case "enable" -> {
                    if (notificationChannel.enable(forumsCategory)) {
                        hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully enabled notifications for Forums category **" + forumsCategory + "**!").build()).queue();
                        notificationChannel.save();
                    } else {
                        hook.editOriginalEmbeds(MessageInfo.errorEmbed("Notifications for Forums category **" + forumsCategory + "** are already enabled!").build()).queue();
                    }
                }
                case "disable" -> {
                    if (notificationChannel.disable(forumsCategory)) {
                        hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully disabled notifications for Forums category **" + forumsCategory + "**!").build()).queue();
                        notificationChannel.save();
                    } else {
                        hook.editOriginalEmbeds(MessageInfo.errorEmbed("Notifications for Forums category **" + forumsCategory + "** are already disabled!").build()).queue();
                    }
                }
            }
        }
    }

    protected static class TwitterCommand extends SlashCommand {

        public TwitterCommand() {
            this.name = "twitter";

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};

            List<OptionData> options = new ArrayList<>(2);
            options.add(Utils.getEnableDisableArgument());
            this.options = options;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Utils.makeEphemeral(event, true);
            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            OptionMapping actionOption = event.getOption("action");

            if (actionOption == null) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Missing `action` argument.").build()).queue();
                return;
            }


            if (PermissionUtils.checkPermissionsAndSendIfMissing(channel, hook)) {
                return;
            }

            if (!NotificationChannelHelper.isNotificationChannelInChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("This channel isn't marked as Notification Channel!").build()).queue();
                return;
            }

            NotificationChannel notificationChannel = NotificationChannelHelper.getNotificationChannel(channel);

            switch (actionOption.getAsString()) {
                case "enable" -> {
                    notificationChannel.setTwitterEnabled(true);
                    hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully enabled notifications from Twitter!\n\nUse `/notifications twitter-filter` command for keyword filtering.").build()).queue();
                    notificationChannel.save();
                }
                case "disable" -> {
                    notificationChannel.setTwitterEnabled(false);
                    hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully disabled notifications from Twitter!").build()).queue();
                    notificationChannel.save();
                }
            }
        }
    }

    protected static class TwitterFilterCommand extends SlashCommand {

        public TwitterFilterCommand() {
            this.name = "twitter-filter";

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};

            List<OptionData> options = new ArrayList<>(2);
            options.add(Utils.getAddRemoveArgument());
            options.add(new OptionData(OptionType.STRING, "keyword", "Keyword to filter within tweets (non case sensitive)", true));
            this.options = options;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Utils.makeEphemeral(event, true);
            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            OptionMapping actionOption = event.getOption("action");
            OptionMapping keywordOption = event.getOption("keyword");

            if (actionOption == null) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Missing `action` argument.").build()).queue();
                return;
            }

            if (keywordOption == null) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Missing `keyword` argument.").build()).queue();
                return;
            }

            if (PermissionUtils.checkPermissionsAndSendIfMissing(channel, hook)) {
                return;
            }

            if (!NotificationChannelHelper.isNotificationChannelInChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("This channel isn't marked as Notification Channel!").build()).queue();
                return;
            }

            NotificationChannel notificationChannel = NotificationChannelHelper.getNotificationChannel(channel);
            String keyword = keywordOption.getAsString();

            switch (actionOption.getAsString()) {
                case "add" -> {
                    if (notificationChannel.getTwitterKeywords().size() == 30) {
                        hook.editOriginalEmbeds(MessageInfo.errorEmbed("Maximum number of filtering keywords is **30**.").build()).queue();
                        return;
                    }

                    if (notificationChannel.addToTwitterKeywords(keywordOption.getAsString())) {
                        hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully added keyword **" + keyword + "** into filter.").build()).queue();
                        notificationChannel.save();
                    } else {
                        hook.editOriginalEmbeds(MessageInfo.errorEmbed("Keyword **" + keyword + "** is already in filter!").build()).queue();
                    }
                }
                case "remove" -> {
                    if (notificationChannel.removeFromTwitterKeywords(keywordOption.getAsString())) {
                        hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully removed keyword **" + keyword + "** from filter.").build()).queue();
                        notificationChannel.save();
                    } else {
                        hook.editOriginalEmbeds(MessageInfo.errorEmbed("Keyword **" + keyword + "** is not on the filter!").build()).queue();
                    }
                }
            }
        }
    }

    protected static class StatusServerCommand extends SlashCommand {

        public StatusServerCommand() {
            this.name = "status-server";

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};

            List<OptionData> options = new ArrayList<>(2);
            options.add(Utils.getEnableDisableArgument());
            options.add(new OptionData(OptionType.STRING, "server", "Server name", true));
            this.options = options;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Utils.makeEphemeral(event, true);
            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            OptionMapping actionOption = event.getOption("action");
            OptionMapping serverOption = event.getOption("server");

            if (actionOption == null) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Missing `action` argument.").build()).queue();
                return;
            }

            if (serverOption == null) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Missing `server` argument.").build()).queue();
                return;
            }

            String correctServerName = Utils.doesServerExist(serverOption.getAsString());

            if (correctServerName == null) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Server with name `" + serverOption.getAsString() + "` does not exist!").build()).queue();
                return;
            }


            if (PermissionUtils.checkPermissionsAndSendIfMissing(channel, hook)) {
                return;
            }

            if (!NotificationChannelHelper.isNotificationChannelInChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("This channel isn't marked as Notification Channel!").build()).queue();
                return;
            }

            NotificationChannel notificationChannel = NotificationChannelHelper.getNotificationChannel(channel);

            switch (actionOption.getAsString()) {
                case "enable" -> {
                    if (notificationChannel.enable(correctServerName)) {
                        hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully enabled notifications for Server **" + correctServerName + "** status change!").build()).queue();
                        notificationChannel.save();
                    } else {
                        hook.editOriginalEmbeds(MessageInfo.errorEmbed("Notifications for Server **" + correctServerName + "** status change are already enabled!").build()).queue();
                    }
                }
                case "disable" -> {
                    if (notificationChannel.disable(correctServerName)) {
                        hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully disabled notifications for Server **" + correctServerName + "** status change!").build()).queue();
                        notificationChannel.save();
                    } else {
                        hook.editOriginalEmbeds(MessageInfo.errorEmbed("Notifications for Server **" + correctServerName + "** status change are already disabled!").build()).queue();
                    }
                }
            }
        }
    }

    protected static class StatusRegionCommand extends SlashCommand {

        public StatusRegionCommand() {
            this.name = "status-region";

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};

            List<OptionData> options = new ArrayList<>(2);
            options.add(Utils.getEnableDisableArgument());
            options.add(Utils.getRegionArgument());
            this.options = options;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Utils.makeEphemeral(event, true);
            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            OptionMapping actionOption = event.getOption("action");
            OptionMapping regionOption = event.getOption("region");

            if (actionOption == null) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Missing `action` argument.").build()).queue();
                return;
            }

            if (regionOption == null) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Missing `region` argument.").build()).queue();
                return;
            }

            String correctRegion = LostArkRegion.exists(regionOption.getAsString());

            if (LostArkRegion.exists(regionOption.getAsString()) == null) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Region with name `" + regionOption.getAsString() + "` does not exist!").build()).queue();
                return;
            }

            LostArkRegion lostArkRegion = LostArkRegion.valueOf(correctRegion);
            String formattedRegion = lostArkRegion.getFormattedName();


            if (PermissionUtils.checkPermissionsAndSendIfMissing(channel, hook)) {
                return;
            }

            if (!NotificationChannelHelper.isNotificationChannelInChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("This channel isn't marked as Notification Channel!").build()).queue();
                return;
            }

            NotificationChannel notificationChannel = NotificationChannelHelper.getNotificationChannel(channel);

            switch (actionOption.getAsString()) {
                case "enable" -> {
                    if (notificationChannel.enable(lostArkRegion)) {
                        hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully enabled notifications for Region **" + formattedRegion + "** status change!").build()).queue();
                        notificationChannel.save();
                    } else {
                        hook.editOriginalEmbeds(MessageInfo.errorEmbed("Notifications for Region **" + formattedRegion + "** status change are already enabled!").build()).queue();
                    }
                }
                case "disable" -> {
                    if (notificationChannel.disable(lostArkRegion)) {
                        hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully disabled notifications for Region **" + formattedRegion + "** status change!").build()).queue();
                        notificationChannel.save();
                    } else {
                        hook.editOriginalEmbeds(MessageInfo.errorEmbed("Notifications for Region **" + formattedRegion + "** status change are already disabled!").build()).queue();
                    }
                }
            }
        }
    }

    protected static class StatusWhitelistCommand extends SlashCommand {

        public StatusWhitelistCommand() {
            this.name = "status-whitelist";

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};

            List<OptionData> options = new ArrayList<>(2);
            options.add(Utils.getAddRemoveArgument());
            options.add(Utils.getStatusWithOfflineArgument());
            this.options = options;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Utils.makeEphemeral(event, true);
            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            OptionMapping actionOption = event.getOption("action");
            OptionMapping statusOption = event.getOption("status");

            if (actionOption == null) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Missing `action` argument.").build()).queue();
                return;
            }

            if (statusOption == null) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Missing `status` argument.").build()).queue();
                return;
            }

            ServerStatus serverStatus = null;

            try {
                serverStatus = ServerStatus.valueOf(statusOption.getAsString());
            } catch (Exception ignored) {
                if (!statusOption.getAsString().equalsIgnoreCase("OFFLINE")) {
                    hook.editOriginalEmbeds(MessageInfo.errorEmbed("Invalid `status` argument.").build()).queue();
                    return;
                }
            }

            if (PermissionUtils.checkPermissionsAndSendIfMissing(channel, hook)) {
                return;
            }

            if (!NotificationChannelHelper.isNotificationChannelInChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("This channel isn't marked as Notification Channel!").build()).queue();
                return;
            }

            NotificationChannel notificationChannel = NotificationChannelHelper.getNotificationChannel(channel);
            String status = statusOption.getAsString();

            switch (actionOption.getAsString()) {
                case "add" -> {
                    if (notificationChannel.addToWhitelist(status)) {
                        hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully added to server status change whitelist **" + status + "** status!").build()).queue();
                        notificationChannel.save();
                    } else {
                        hook.editOriginalEmbeds(MessageInfo.errorEmbed("Status **" + status + "** is already whitelisted!").build()).queue();
                    }
                }
                case "remove" -> {
                    if (notificationChannel.removeFromWhitelist(status)) {
                        hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully removed from server status change whitelist **" + status + "** status!").build()).queue();
                        notificationChannel.save();
                    } else {
                        hook.editOriginalEmbeds(MessageInfo.errorEmbed("Status **" + status + "** is not on the whitelist!").build()).queue();
                    }
                }
            }
        }
    }

    protected static class StatusPingCommand extends SlashCommand {

        public StatusPingCommand() {
            this.name = "status-ping";

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};

            List<OptionData> options = new ArrayList<>(2);
            options.add(Utils.getAddRemoveArgument());
            options.add(new OptionData(OptionType.ROLE, "role", "Role to ping on servers status change", true));
            this.options = options;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Utils.makeEphemeral(event, true);
            InteractionHook hook = event.getHook();
            TextChannel channel = event.getTextChannel();

            OptionMapping actionOption = event.getOption("action");
            OptionMapping roleOption = event.getOption("role");

            if (actionOption == null) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Missing `action` argument.").build()).queue();
                return;
            }

            if (roleOption == null) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Missing `role` argument.").build()).queue();
                return;
            }

            Role role = roleOption.getAsRole();

            if (PermissionUtils.checkPermissionsAndSendIfMissing(channel, hook)) {
                return;
            }

            if (!NotificationChannelHelper.isNotificationChannelInChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("This channel isn't marked as Notification Channel!").build()).queue();
                return;
            }

            NotificationChannel notificationChannel = NotificationChannelHelper.getNotificationChannel(channel);

            switch (actionOption.getAsString()) {
                case "add" -> {
                    if (notificationChannel.addToRoleIds(role)) {
                        hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully added role "+ role.getAsMention() + " to roles, which will be pinged on Server status change!").build()).queue();
                        notificationChannel.save();
                    } else {
                        hook.editOriginalEmbeds(MessageInfo.errorEmbed("Role " + role.getAsMention() + " is already added!").build()).queue();
                    }
                }
                case "remove" -> {
                    if (notificationChannel.removeFromRoleIds(role)) {
                        hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully removed role "+ role.getAsMention() + " from roles, which will be pinged on Server status change!").build()).queue();
                        notificationChannel.save();
                    } else {
                        hook.editOriginalEmbeds(MessageInfo.errorEmbed("There is no role " + role.getAsMention() + "!").build()).queue();
                    }
                }
            }
        }
    }

    protected static class ClearCommand extends SlashCommand {

        public ClearCommand() {
            this.name = "clear";

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};

            List<OptionData> options = new ArrayList<>(1);
            options.add(Utils.getClearArgument());
            this.options = options;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Utils.makeEphemeral(event, true);
            InteractionHook hook = event.getHook();

            OptionMapping clearOption = event.getOption("clear");

            if (clearOption == null) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("Missing `clear` argument.").build()).queue();
                return;
            }

            TextChannel channel = event.getTextChannel();

            if (PermissionUtils.checkPermissionsAndSendIfMissing(channel, hook)) {
                return;
            }

            if (!NotificationChannelHelper.isNotificationChannelInChannel(channel)) {
                hook.editOriginalEmbeds(MessageInfo.errorEmbed("This channel isn't marked as Notification Channel!").build()).queue();
                return;
            }

            NotificationChannel notificationChannel = NotificationChannelHelper.getNotificationChannel(channel);

            switch (clearOption.getAsString()) {
                case "news" -> {
                    notificationChannel.getNewsCategories().clear();
                    hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully cleared News notifications!").build()).queue();
                }
                case "forums" -> {
                    notificationChannel.getForumsCategories().clear();
                    hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully cleared Forums notifications!").build()).queue();
                }
                case "status_server" -> {
                    notificationChannel.getServers().clear();
                    hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully cleared Server change status notifications!").build()).queue();
                }
                case "status_region" -> {
                    notificationChannel.getRegions().clear();
                    hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully cleared Region change status notifications!").build()).queue();
                }
                case "status_whitelist" -> {
                    notificationChannel.getStatusWhitelist().clear();
                    hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully cleared Status whitelist!").build()).queue();
                }
                case "ping_roles" -> {
                    notificationChannel.getRoleIds().clear();
                    hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully cleared Ping roles!").build()).queue();
                }
                case "twitter_filter" -> {
                    notificationChannel.getTwitterKeywords().clear();
                    hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully cleared Twitter Filter keywords!").build()).queue();
                }
            }

            notificationChannel.save();
        }
    }
}
