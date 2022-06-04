package dev.mayuna.lostarkbot.commands.notifications.subcommands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.mayuna.lostarkbot.old.api.unofficial.objects.ForumsCategory;
import dev.mayuna.lostarkbot.old.api.unofficial.objects.NewsCategory;
import dev.mayuna.lostarkbot.helpers.NotificationChannelHelper;
import dev.mayuna.lostarkbot.objects.features.NotificationChannel;
import dev.mayuna.lostarkbot.objects.other.StatusWhitelistObject;
import dev.mayuna.lostarkbot.util.AutoMessageUtils;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.lostarkfetcher.objects.api.other.LostArkRegion;
import dev.mayuna.mayusjdautils.util.MessageInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.LinkedList;
import java.util.List;

public class NotifyInfoCommand extends SlashCommand {

    public NotifyInfoCommand() {
        this.name = "info";
        this.help = "Shows you information about current Notification Channel";

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

        NotificationChannel notificationChannel;
        EmbedBuilder embedBuilder = MessageInfo.informationEmbed("");

        if (!AutoMessageUtils.isEverythingAlrightNotificationChannel(textChannel, interactionHook)) {
            return;
        }

        notificationChannel = NotificationChannelHelper.getNotificationChannel(textChannel);

        String description = "";
        description += "This channel is marked as Notification Channel.\n\n**Technical stuff**\n";
        description += "Guild ID: `" + textChannel.getGuild().getId() + "`\n";
        description += "Message Channel ID: `" + textChannel.getId() + "`\n";
        description += "NotificationChannel UUID: `" + notificationChannel.getName() + "`";
        embedBuilder.setDescription(description);

        String newsCategories = "";
        if (notificationChannel.getNewsTags().isEmpty()) {
            newsCategories = "No News categories enabled.";
        } else {
            for (String newsTag : notificationChannel.getNewsTags()) {
                newsCategories += newsTag.toString();

                if (!Utils.isLast(notificationChannel.getNewsTags(), newsTag)) {
                    newsCategories += ",\n";
                }
            }
        }

        String forumsCategories = "";
        if (notificationChannel.getForumCategories().isEmpty()) {
            forumsCategories = "No Forums categories enabled.";
        } else {
            for (int forumCategoryId : notificationChannel.getForumCategories()) {
                forumsCategories += forumCategoryId;

                if (!Utils.isLast(notificationChannel.getForumCategories(), forumCategoryId)) {
                    forumsCategories += ",\n";
                }
            }
        }

        String regions = "";
        if (notificationChannel.getStatusChangeRegions().isEmpty()) {
            regions = "No specific Region status changes enabled";
        } else {
            for (LostArkRegion lostArkRegion : notificationChannel.getStatusChangeRegions()) {
                regions += lostArkRegion.getPrettyName();

                if (!Utils.isLast(notificationChannel.getStatusChangeRegions(), lostArkRegion)) {
                    regions += ",\n";
                }
            }
        }

        String servers = "";
        if (notificationChannel.getStatusChangeServers().isEmpty()) {
            servers = "No specific Server status changes enabled";
        } else {
            for (String serverName : notificationChannel.getStatusChangeServers()) {
                servers += serverName;

                if (!Utils.isLast(notificationChannel.getStatusChangeServers(), serverName)) {
                    servers += ",\n";
                }
            }
        }

        String statusWhitelistBlacklist = "";
        String whitelist = "";
        String blacklist = "";
        if (notificationChannel.getStatusWhitelistObjects().isEmpty()) {
            whitelist = "Status whitelist is empty.";
        } else {
            List<StatusWhitelistObject> fromStatuses = Utils.getStatusWhitelistObjectsByType(notificationChannel.getStatusWhitelistObjects(), StatusWhitelistObject.Type.FROM);
            List<StatusWhitelistObject> toStatuses = Utils.getStatusWhitelistObjectsByType(notificationChannel.getStatusWhitelistObjects(), StatusWhitelistObject.Type.TO);

            String from = "**Changed from**\n" + Utils.makeVerticalStringList(fromStatuses, "No whitelisted statuses.");
            String to = "**Changed to**\n" + Utils.makeVerticalStringList(toStatuses, "No whitelisted statuses.");

            whitelist = from + "\n" + to;
        }
        if (notificationChannel.getStatusBlacklistObjects().isEmpty()) {
            blacklist = "Status blacklist is empty.";
        } else {
            List<StatusWhitelistObject> fromStatuses = Utils.getStatusWhitelistObjectsByType(notificationChannel.getStatusBlacklistObjects(), StatusWhitelistObject.Type.FROM);
            List<StatusWhitelistObject> toStatuses = Utils.getStatusWhitelistObjectsByType(notificationChannel.getStatusBlacklistObjects(), StatusWhitelistObject.Type.TO);

            String from = "**Changed from**\n" + Utils.makeVerticalStringList(fromStatuses, "No blacklisted statuses.");
            String to = "**Changed to**\n" + Utils.makeVerticalStringList(toStatuses, "No blacklisted statuses.");

            blacklist = from + "\n" + to;
        }
        statusWhitelistBlacklist = "> **Whitelist**\n" + whitelist + "\n\n> **Blacklist**\n" + blacklist;

        String statusChangePingRoles = "";
        if (notificationChannel.getStatusPingRolesIds().isEmpty()) {
            statusChangePingRoles = "No roles to ping on server status change.";
        } else {
            List<String> roleIdsToRemove = new LinkedList<>();
            for (String roleId : notificationChannel.getStatusPingRolesIds()) {
                Role role = notificationChannel.getManagedTextChannel().getGuild().getRoleById(roleId);

                if (role != null) {
                    if ((statusChangePingRoles + role.getAsMention()).length() > 2000) {
                        continue;
                    }

                    statusChangePingRoles += role.getAsMention();
                } else {
                    roleIdsToRemove.add(roleId);
                }


                if (!Utils.isLast(notificationChannel.getStatusPingRolesIds(), roleId)) {
                    statusChangePingRoles += ", ";
                }
            }
            notificationChannel.getStatusPingRolesIds().removeAll(roleIdsToRemove);

            if (statusChangePingRoles.isEmpty()) {
                statusChangePingRoles = "No roles.";
            }
        }

        String twitter = "";
        if (!notificationChannel.isTwitterEnabled()) {
            twitter += "• Twitter notifications are **disabled**.";
        } else {
            twitter += "• Twitter notifications are **enabled**.";
        }

        if (notificationChannel.getTwitterKeywords().isEmpty()) {
            twitter += "\n• There are no filtering keywords.";
        } else {
            twitter += "\n• Filtered keywords: ";
            for (String keyword : notificationChannel.getTwitterKeywords()) {
                twitter += keyword;

                if (!Utils.isLast(notificationChannel.getTwitterKeywords(), keyword)) {
                    twitter += ", ";
                }
            }
        }

        if (notificationChannel.getTwitterFollowedAccounts().isEmpty()) {
            twitter += "\n• You do not follow any accounts.";
        } else {
            twitter += "\n• Followed accounts: ";
            for (String accountName : notificationChannel.getTwitterFollowedAccounts()) {
                twitter += "[@" + accountName + "](https://twitter.com/" + accountName + ")";

                if (!Utils.isLast(notificationChannel.getTwitterFollowedAccounts(), accountName)) {
                    twitter += ", ";
                }
            }
        }

        if (notificationChannel.getTwitterPingRolesIds().isEmpty()) {
            twitter += "\n• No roles to ping on new Tweets.";
        } else {
            twitter += "\n• Roles to ping on new Tweets: ";

            List<String> roleIdsToRemove = new LinkedList<>();
            for (String roleId : notificationChannel.getTwitterPingRolesIds()) {
                Role role = notificationChannel.getManagedTextChannel().getGuild().getRoleById(roleId);

                if (role != null) {
                    if ((twitter + role.getAsMention()).length() > 2000) {
                        continue;
                    }

                    twitter += role.getAsMention();
                } else {
                    roleIdsToRemove.add(roleId);
                }


                if (!Utils.isLast(notificationChannel.getTwitterPingRolesIds(), roleId)) {
                    twitter += ", ";
                }
            }
            notificationChannel.getTwitterPingRolesIds().removeAll(roleIdsToRemove);
        }

        embedBuilder.addField("Enabled News Categories", newsCategories, true);
        embedBuilder.addField("Enabled Forums Categories", forumsCategories, true);
        embedBuilder.addBlankField(false);
        embedBuilder.addField("Enabled Region status changes", regions, true);
        embedBuilder.addField("Enabled Server status changes", servers, true);
        embedBuilder.addBlankField(false);
        embedBuilder.addField("Status whitelist/blacklist", statusWhitelistBlacklist, true);
        embedBuilder.addField("Roles to ping on Server status change", statusChangePingRoles, true);
        embedBuilder.addBlankField(false);
        embedBuilder.addField("Twitter", twitter, true);

        interactionHook.editOriginalEmbeds(embedBuilder.build()).queue();
    }
}
