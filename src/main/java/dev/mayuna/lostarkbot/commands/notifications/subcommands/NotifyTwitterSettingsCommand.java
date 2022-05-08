package dev.mayuna.lostarkbot.commands.notifications.subcommands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.mayuna.lostarkbot.helpers.NotificationChannelHelper;
import dev.mayuna.lostarkbot.objects.features.NotificationChannel;
import dev.mayuna.lostarkbot.util.AutoMessageUtils;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.mayusjdautils.interactive.InteractiveMessage;
import dev.mayuna.mayusjdautils.interactive.objects.Interaction;
import dev.mayuna.mayusjdautils.util.DiscordUtils;
import dev.mayuna.mayusjdautils.util.MessageInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;

import java.util.List;
import java.util.UUID;

public class NotifyTwitterSettingsCommand extends SlashCommand {

    public NotifyTwitterSettingsCommand() {
        this.name = "twitter-settings";
        this.help = "Allows you to change Twitter settings in current Notification Channel using menu";

        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Utils.makeEphemeral(event, true);
        TextChannel textChannel = event.getTextChannel();
        InteractionHook interactionHook = event.getHook();

        if (!AutoMessageUtils.isEverythingAlrightNotificationChannel(textChannel, interactionHook)) {
            return;
        }

        NotificationChannel notificationChannel = NotificationChannelHelper.getNotificationChannel(textChannel);
        getBaseMessage(notificationChannel).editOriginal(interactionHook);
    }

    private InteractiveMessage getBaseMessage(NotificationChannel notificationChannel) {
        EmbedBuilder embedBuilder = Utils.getTwitterDefaultEmbed();
        embedBuilder.setTitle("Twitter Settings");

        String description = "";

        description += "Twitter notifications: **" + (notificationChannel.isTwitterEnabled() ? "Enabled" : "Disabled") + "**\n";
        description += "Fancy embeds: **" + (notificationChannel.isTwitterFancyEmbeds() ? "Enabled" : "Disabled") + "**\n";
        description += "Send retweets: **" + (notificationChannel.isTwitterRetweets() ? "Enabled" : "Disabled") + "**\n";
        description += "Send replies: **" + (notificationChannel.isTwitterReplies() ? "Enabled" : "Disabled") + "**\n";
        description += "Send quotes: **" + (notificationChannel.isTwitterQuotes() ? "Enabled" : "Disabled") + "**\n";

        description += "\nYou will receive Tweets into your Notification Channel from these Twitter accounts.\n";
        if (notificationChannel.getTwitterFollowedAccounts().isEmpty()) {
            description += "You do not follow any Twitter account.";
        } else {
            for (String followedUser : notificationChannel.getTwitterFollowedAccounts()) {
                description += "[@" + followedUser + "](https://twitter.com/" + followedUser + ")\n";
            }
        }

        embedBuilder.setDescription(description);

        InteractiveMessage message = InteractiveMessage.create(new MessageBuilder().setEmbeds(embedBuilder.build()));

        message.addInteraction(Interaction.asButton(DiscordUtils.generateButton((notificationChannel.isTwitterEnabled() ? ButtonStyle.SECONDARY : ButtonStyle.PRIMARY),
                                                                                notificationChannel.isTwitterEnabled() ? "Disable Twitter" : "Enable Twitter"
        )), interactionEvent -> {
            notificationChannel.setTwitterEnabled(!notificationChannel.isTwitterEnabled());
            getBaseMessage(notificationChannel).editOriginal(interactionEvent.getInteractionHook());
        });

        message.addInteraction(Interaction.asButton(DiscordUtils.generateButton((notificationChannel.isTwitterFancyEmbeds() ? ButtonStyle.SECONDARY : ButtonStyle.PRIMARY),
                                                                                notificationChannel.isTwitterFancyEmbeds() ? "Disable fancy embeds" : "Enable fancy embeds"
        )), interactionEvent -> {
            notificationChannel.setTwitterFancyEmbeds(!notificationChannel.isTwitterFancyEmbeds());
            getBaseMessage(notificationChannel).editOriginal(interactionEvent.getInteractionHook());
        });

        message.addInteraction(Interaction.asButton(DiscordUtils.generateButton((notificationChannel.isTwitterRetweets() ? ButtonStyle.SECONDARY : ButtonStyle.PRIMARY),
                                                                                notificationChannel.isTwitterRetweets() ? "Disable retweets" : "Enable retweets"
        )), interactionEvent -> {
            notificationChannel.setTwitterRetweets(!notificationChannel.isTwitterRetweets());
            getBaseMessage(notificationChannel).editOriginal(interactionEvent.getInteractionHook());
        });

        message.addInteraction(Interaction.asButton(DiscordUtils.generateButton((notificationChannel.isTwitterReplies() ? ButtonStyle.SECONDARY : ButtonStyle.PRIMARY),
                                                                                notificationChannel.isTwitterReplies() ? "Disable replies" : "Enable replies"
        )), interactionEvent -> {
            notificationChannel.setTwitterReplies(!notificationChannel.isTwitterReplies());
            getBaseMessage(notificationChannel).editOriginal(interactionEvent.getInteractionHook());
        });

        message.addInteraction(Interaction.asButton(DiscordUtils.generateButton((notificationChannel.isTwitterQuotes() ? ButtonStyle.SECONDARY : ButtonStyle.PRIMARY),
                                                                                notificationChannel.isTwitterQuotes() ? "Disable quotes" : "Enable quotes"
        )), interactionEvent -> {
            notificationChannel.setTwitterQuotes(!notificationChannel.isTwitterQuotes());
            getBaseMessage(notificationChannel).editOriginal(interactionEvent.getInteractionHook());
        });

        message.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.PRIMARY, "Edit accounts")), interactionEvent -> {
            getBaseAccountsMessage(notificationChannel).editOriginal(interactionEvent.getInteractionHook());
        });

        return message;
    }

    private InteractiveMessage getBaseAccountsMessage(NotificationChannel notificationChannel) {
        EmbedBuilder embedBuilder = Utils.getTwitterDefaultEmbed();
        embedBuilder.setTitle("Twitter Accounts");

        String description = "";

        description += "You will receive Tweets into your Notification Channel from these Twitter accounts.\n\n";
        if (notificationChannel.getTwitterFollowedAccounts().isEmpty()) {
            description += "You do not follow any Twitter account.";
        } else {
            for (String followedUser : notificationChannel.getTwitterFollowedAccounts()) {
                description += "[@" + followedUser + "](https://twitter.com/" + followedUser + ")\n";
            }
        }

        embedBuilder.setDescription(description);

        InteractiveMessage message = InteractiveMessage.create(new MessageBuilder().setEmbeds(embedBuilder.build()));

        message.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.PRIMARY, "Enable accounts")), interactionEvent -> {
            getEnableAccountsMessage(notificationChannel).editOriginal(interactionEvent.getInteractionHook());
        });

        message.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.PRIMARY, "Disable accounts")), interactionEvent -> {
            getDisableAccountsMessage(notificationChannel).editOriginal(interactionEvent.getInteractionHook());
        });

        message.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.SECONDARY, "Back")), interactionEvent -> {
            getBaseMessage(notificationChannel).editOriginal(interactionEvent.getInteractionHook());
        });

        return message;
    }

    private InteractiveMessage getEnableAccountsMessage(NotificationChannel notificationChannel) {
        EmbedBuilder embedBuilder = Utils.getTwitterDefaultEmbed();
        embedBuilder.setTitle("Twitter Accounts | Enable");
        embedBuilder.setDescription("Please, choose an account you want to follow (enable).");

        InteractiveMessage message = InteractiveMessage.create(new MessageBuilder().setEmbeds(embedBuilder.build()));
        SelectMenu.Builder selectMenuBuilder = SelectMenu.create(UUID.randomUUID().toString());
        selectMenuBuilder.setPlaceholder("Choose an account");
        message.setSelectMenuBuilder(selectMenuBuilder);

        List<String> unfollowedTwitterAccounts = Utils.getUnfollowedUsers(notificationChannel.getTwitterFollowedAccounts());

        if (unfollowedTwitterAccounts.isEmpty()) {
            message.addInteraction(Interaction.asSelectOption(DiscordUtils.generateSelectOption("All accounts are enabled.")), interactionEvent -> {
                getBaseAccountsMessage(notificationChannel).editOriginal(interactionEvent.getInteractionHook());
            });
        } else {
            for (String twitterAccount : unfollowedTwitterAccounts) {
                message.addInteraction(Interaction.asSelectOption(DiscordUtils.generateSelectOption(twitterAccount)), interactionEvent -> {
                    boolean success = notificationChannel.addToFollowedTwitterAccounts(twitterAccount);

                    if (!success) {
                        interactionEvent.getInteractionHook()
                                .editOriginalEmbeds(MessageInfo.errorEmbed("Could not enable account **" + twitterAccount + "**! This account is already enabled.").build())
                                .queue();
                    } else {
                        notificationChannel.save();
                        getBaseAccountsMessage(notificationChannel).editOriginal(interactionEvent.getInteractionHook());
                    }
                });
            }
        }

        return message;
    }

    private InteractiveMessage getDisableAccountsMessage(NotificationChannel notificationChannel) {
        EmbedBuilder embedBuilder = Utils.getTwitterDefaultEmbed();
        embedBuilder.setTitle("Twitter Accounts | Disable");
        embedBuilder.setDescription("Please, choose an account you want to unfollow (disable).");

        InteractiveMessage message = InteractiveMessage.create(new MessageBuilder().setEmbeds(embedBuilder.build()));
        SelectMenu.Builder selectMenuBuilder = SelectMenu.create(UUID.randomUUID().toString());
        selectMenuBuilder.setPlaceholder("Choose an account");
        message.setSelectMenuBuilder(selectMenuBuilder);

        List<String> followedTwitterAccounts = notificationChannel.getTwitterFollowedAccounts();

        if (followedTwitterAccounts.isEmpty()) {
            message.addInteraction(Interaction.asSelectOption(DiscordUtils.generateSelectOption("All accounts are disabled.")), interactionEvent -> {
                getBaseAccountsMessage(notificationChannel).editOriginal(interactionEvent.getInteractionHook());
            });
        } else {
            for (String twitterAccount : followedTwitterAccounts) {
                message.addInteraction(Interaction.asSelectOption(DiscordUtils.generateSelectOption(twitterAccount)), interactionEvent -> {
                    boolean success = notificationChannel.removeFromFollowedTwitterKeywords(twitterAccount);

                    if (!success) {
                        interactionEvent.getInteractionHook()
                                .editOriginalEmbeds(MessageInfo.errorEmbed("Could not disable account **" + twitterAccount + "**! This account is already disabled.").build())
                                .queue();
                    } else {
                        notificationChannel.save();
                        getBaseAccountsMessage(notificationChannel).editOriginal(interactionEvent.getInteractionHook());
                    }
                });
            }
        }

        return message;
    }
}
