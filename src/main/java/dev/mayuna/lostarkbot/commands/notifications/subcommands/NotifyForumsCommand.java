package dev.mayuna.lostarkbot.commands.notifications.subcommands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.mayuna.lostarkbot.helpers.NotificationChannelHelper;
import dev.mayuna.lostarkbot.managers.NotificationsManager;
import dev.mayuna.lostarkbot.objects.features.NotificationChannel;
import dev.mayuna.lostarkbot.objects.features.lostark.WrappedForumCategoryName;
import dev.mayuna.lostarkbot.util.AutoMessageUtils;
import dev.mayuna.lostarkbot.util.LostArkUtils;
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

import java.util.UUID;

public class NotifyForumsCommand extends SlashCommand {

    public NotifyForumsCommand() {
        this.name = "forums";
        this.help = "Allows you to enable/disable specific Lost Ark Forum post notifications";

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

        if (!AutoMessageUtils.isEverythingAlrightNotificationChannel(textChannel, interactionHook)) {
            return;
        }

        NotificationChannel notificationChannel = NotificationChannelHelper.getNotificationChannel(textChannel);

        getBaseMessage(notificationChannel).editOriginal(interactionHook);
    }

    private InteractiveMessage getBaseMessage(NotificationChannel notificationChannel) {
        EmbedBuilder embedBuilder = DiscordUtils.getDefaultEmbed();
        embedBuilder.setTitle("Lost Ark Forums Chooser");

        embedBuilder.setDescription(getDescription(notificationChannel));
        embedBuilder.appendDescription("\n\nPlease, choose a Main Forum Category.");

        InteractiveMessage interactiveMessage = InteractiveMessage.create(new MessageBuilder().setEmbeds(embedBuilder.build()));
        SelectMenu.Builder selectMenuBuilder = SelectMenu.create(UUID.randomUUID().toString());
        selectMenuBuilder.setPlaceholder("Choose a Main Forum Category");
        interactiveMessage.setSelectMenuBuilder(selectMenuBuilder);

        synchronized (NotificationsManager.getForumCategoryNames()) {
            int counter = -1;
            for (WrappedForumCategoryName wrappedForumCategoryName : NotificationsManager.getForumCategoryNames()) {
                counter++;
                if (counter == 25) {
                    break;
                }

                interactiveMessage.addInteraction(Interaction.asSelectOption(DiscordUtils.generateSelectOption(wrappedForumCategoryName.getVerboseName())),
                                                  interactionEvent -> {
                                                      getSubcategoryMessage(notificationChannel, wrappedForumCategoryName).editOriginal(
                                                              interactionEvent.getInteractionHook());
                                                  }
                );
            }
        }

        return interactiveMessage;
    }

    private InteractiveMessage getSubcategoryMessage(NotificationChannel notificationChannel, WrappedForumCategoryName wrappedForumCategoryName) {
        EmbedBuilder embedBuilder = DiscordUtils.getDefaultEmbed();
        embedBuilder.setTitle("Lost Ark Forums Chooser | " + wrappedForumCategoryName.getVerboseName());

        embedBuilder.setDescription(getDescription(notificationChannel));
        embedBuilder.appendDescription("\n\nYou have selected main category **" + wrappedForumCategoryName.getVerboseName() + "**\n\nPlease, choose a Sub Forum Category.\nYou can also choose current Main Category if you'd like.");

        InteractiveMessage interactiveMessage = InteractiveMessage.create(new MessageBuilder().setEmbeds(embedBuilder.build()));
        SelectMenu.Builder selectMenuBuilder = SelectMenu.create(UUID.randomUUID().toString());
        selectMenuBuilder.setPlaceholder("Choose a Sub Forum Category");
        interactiveMessage.setSelectMenuBuilder(selectMenuBuilder);

        interactiveMessage.addInteraction(Interaction.asSelectOption(DiscordUtils.generateSelectOption("> This Main Category (" + wrappedForumCategoryName.getVerboseName() + ")")),
                                          interactionEvent -> {
                                              getConfirmationMessage(notificationChannel,
                                                                     wrappedForumCategoryName
                                              ).editOriginal(interactionEvent.getInteractionHook());
                                          }
        );

        if (wrappedForumCategoryName.hasSubcategories()) {
            synchronized (wrappedForumCategoryName.getSubcategories()) {
                int counter = -1;
                for (WrappedForumCategoryName wrappedForumCategoryNameSubcategory : wrappedForumCategoryName.getSubcategories()) {
                    counter++;
                    if(counter == 24) {
                        break;
                    }

                    interactiveMessage.addInteraction(Interaction.asSelectOption(DiscordUtils.generateSelectOption(wrappedForumCategoryNameSubcategory.getVerboseName())),
                                                      interactionEvent -> {
                                                          getConfirmationMessage(notificationChannel,
                                                                                 wrappedForumCategoryNameSubcategory
                                                          ).editOriginal(
                                                                  interactionEvent.getInteractionHook());
                                                      }
                    );
                }
            }
        }

        interactiveMessage.addInteraction(Interaction.asSelectOption(DiscordUtils.generateSelectOption("> Back")),
                                          interactionEvent -> {
                                              getBaseMessage(notificationChannel).editOriginal(interactionEvent.getInteractionHook());
                                          }
        );

        return interactiveMessage;
    }

    private InteractiveMessage getConfirmationMessage(NotificationChannel notificationChannel, WrappedForumCategoryName wrappedForumCategoryName) {
        EmbedBuilder embedBuilder = DiscordUtils.getDefaultEmbed();
        embedBuilder.setTitle("Lost Ark Forums Chooser | " + wrappedForumCategoryName.getVerboseName());

        embedBuilder.setDescription(getDescription(notificationChannel));
        embedBuilder.appendDescription("\n\nYou have selected subcategory **" + wrappedForumCategoryName.getVerboseName() + "**\n\nPlease, choose an action.");

        InteractiveMessage interactiveMessage = InteractiveMessage.create(new MessageBuilder().setEmbeds(embedBuilder.build()));

        interactiveMessage.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.SECONDARY, "Enable")), interactionEvent -> {
            if (notificationChannel.enableForumCategory(wrappedForumCategoryName.getId())) {
                getSuccessMessage(notificationChannel, true, wrappedForumCategoryName).editOriginal(interactionEvent.getInteractionHook());
            } else {
                getFailedMessage(notificationChannel, true, wrappedForumCategoryName).editOriginal(interactionEvent.getInteractionHook());
            }
        });

        interactiveMessage.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.SECONDARY, "Disable")), interactionEvent -> {
            if (notificationChannel.disableForumCategory(wrappedForumCategoryName.getId())) {
                getSuccessMessage(notificationChannel, false, wrappedForumCategoryName).editOriginal(interactionEvent.getInteractionHook());
            } else {
                getFailedMessage(notificationChannel, false, wrappedForumCategoryName).editOriginal(interactionEvent.getInteractionHook());
            }
        });

        interactiveMessage.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.SECONDARY, "Cancel")), interactionEvent -> {
            if (wrappedForumCategoryName.hasSubcategories()) {
                getSubcategoryMessage(notificationChannel, wrappedForumCategoryName);
            } else {
                getBaseMessage(notificationChannel);
            }
        });

        return interactiveMessage;
    }

    private InteractiveMessage getSuccessMessage(NotificationChannel notificationChannel, boolean adding, WrappedForumCategoryName wrappedForumCategoryName) {
        EmbedBuilder embedBuilder = MessageInfo.successEmbed("");

        embedBuilder.setTitle("Lost Ark Forums Chooser | " + wrappedForumCategoryName.getVerboseName());

        String description = "";

        if (adding) {
            description += "Successfully enabled **" + wrappedForumCategoryName.getVerboseName() + "** category!\nYou **will** now receive new posts from this Forum.";
        } else {
            description += "Successfully disabled **" + wrappedForumCategoryName.getVerboseName() + "** category!\nYou **won't** receive new posts from this Forum anymore.";
        }

        embedBuilder.setDescription(description);

        InteractiveMessage interactiveMessage = InteractiveMessage.create(new MessageBuilder().setEmbeds(embedBuilder.build()));

        interactiveMessage.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.SECONDARY, "Back")), interactionEvent -> {
            getBaseMessage(notificationChannel).editOriginal(interactionEvent.getInteractionHook());
        });

        return interactiveMessage;
    }

    private InteractiveMessage getFailedMessage(NotificationChannel notificationChannel, boolean adding, WrappedForumCategoryName wrappedForumCategoryName) {
        EmbedBuilder embedBuilder = MessageInfo.errorEmbed("");

        embedBuilder.setTitle("Lost Ark Forums Chooser | " + wrappedForumCategoryName.getVerboseName());

        String description = "";

        if (adding) {
            description += "There was a problem while enabling **" + wrappedForumCategoryName.getVerboseName() + "** category.\n\nPossible reasons:\n - This category is already enabled\n - This category does not exist";
        } else {
            description += "There was a problem while disabling **" + wrappedForumCategoryName.getVerboseName() + "** category.\n\nPossible reasons:\n - This category is already disabled\n - This category does not exist";
        }

        embedBuilder.setDescription(description);

        InteractiveMessage interactiveMessage = InteractiveMessage.create(new MessageBuilder().setEmbeds(embedBuilder.build()));

        interactiveMessage.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.SECONDARY, "Back")), interactionEvent -> {
            getBaseMessage(notificationChannel).editOriginal(interactionEvent.getInteractionHook());
        });

        return interactiveMessage;
    }

    private String getDescription(NotificationChannel notificationChannel) {
        String description = "";

        if (notificationChannel.getForumCategories().isEmpty()) {
            description += "**No enabled Forum categories**";
        } else {
            description += "**Enabled Forum categories**\n";
            for (int forumCategoryId : notificationChannel.getForumCategories()) {
                WrappedForumCategoryName wrappedForumCategoryName = LostArkUtils.getForumCategoryName(forumCategoryId);

                description += wrappedForumCategoryName.getVerboseName() + "\n";
            }
        }

        return description;
    }
}
