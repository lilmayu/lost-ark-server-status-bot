package dev.mayuna.lostarkbot.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import dev.mayuna.lostarkbot.api.unofficial.objects.ForumsCategory;
import dev.mayuna.lostarkbot.api.unofficial.objects.NewsCategory;
import dev.mayuna.lostarkbot.helpers.NotificationChannelHelper;
import dev.mayuna.lostarkbot.objects.LostArkRegion;
import dev.mayuna.lostarkbot.objects.NotificationChannel;
import dev.mayuna.lostarkbot.util.PermissionUtils;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.mayusjdautils.utils.DiscordUtils;
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
                new ClearCommand()
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

            embedBuilder.addField("Enabled News Categories", newsCategories, true);
            embedBuilder.addField("Enabled Forums Categories", forumsCategories, true);
            embedBuilder.addBlankField(false);
            embedBuilder.addField("Enabled Region status changes", regions, true);
            embedBuilder.addField("Enabled Server status changes", servers, true);

            hook.editOriginalEmbeds(embedBuilder.build()).queue();
        }
    }

    protected static class NewsCommand extends SlashCommand {

        public NewsCommand() {
            this.name = "news";

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};

            List<OptionData> options = new ArrayList<>(2);
            options.add(Utils.getActionArgument());
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
            options.add(Utils.getActionArgument());
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

    protected static class StatusServerCommand extends SlashCommand {

        public StatusServerCommand() {
            this.name = "status-server";

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};

            List<OptionData> options = new ArrayList<>(2);
            options.add(Utils.getActionArgument());
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
            options.add(Utils.getActionArgument());
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
            }

            notificationChannel.save();
        }
    }
}
