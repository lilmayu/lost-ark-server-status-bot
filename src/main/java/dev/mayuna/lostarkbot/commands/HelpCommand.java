package dev.mayuna.lostarkbot.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.mayusjdautils.interactive.InteractiveMessage;
import dev.mayuna.mayusjdautils.interactive.objects.Interaction;
import dev.mayuna.mayusjdautils.utils.DiscordUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;

public class HelpCommand extends SlashCommand {

    public HelpCommand() {
        this.name = "help";

        this.guildOnly = false;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Utils.makeEphemeral(event, true);

        getMainMenuMessage().sendMessage(event.getHook(), true);
    }

    private InteractiveMessage getMainMenuMessage() {
        InteractiveMessage message = InteractiveMessage.create();
        MessageBuilder messageBuilder = new MessageBuilder();

        EmbedBuilder embedBuilder = DiscordUtils.getDefaultEmbed();

        embedBuilder.setTitle("Mayu's Lost Ark Bot | Help");
        embedBuilder.setDescription("Select a help section by clicking on the buttons below.\n\n");
        embedBuilder.appendDescription("Tip: You can also see documentation at [docs.mayuna.dev](https://docs.mayuna.dev/en/mayus-lost-ark-bot/Introduction)");

        messageBuilder.setEmbeds(embedBuilder.build());
        message.setMessageBuilder(messageBuilder);

        message.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.PRIMARY, "Command List")), interactionEvent -> getCommandListMessage().editOriginal(interactionEvent.getInteractionHook()));
        message.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.PRIMARY, "Server Dashboards")), interactionEvent -> getServerDashboardsMessage().editOriginal(interactionEvent.getInteractionHook()));
        message.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.PRIMARY, "Notifications")), interactionEvent -> getNotificationsMessage().editOriginal(interactionEvent.getInteractionHook()));
        message.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.SECONDARY, "Official Support")), interactionEvent -> getOfficialSupportMessage().editOriginal(interactionEvent.getInteractionHook()));

        return message;
    }

    private InteractiveMessage getCommandListMessage() {
        InteractiveMessage message = InteractiveMessage.create();
        MessageBuilder messageBuilder = new MessageBuilder();

        EmbedBuilder embedBuilder = DiscordUtils.getDefaultEmbed();

        embedBuilder.setTitle("Mayu's Lost Ark Bot | Command list");
        embedBuilder.setDescription("All commands are [Slash commands](https://support.discord.com/hc/en-us/articles/1500000368501-Slash-Commands-FAQ)");

        embedBuilder.addField("• General",
                              "`/help` - Shows this help menu\n" +
                                      "`/about` - Shows information about this bot", false
        );
        embedBuilder.addField("• Command `/lost-ark`",
                              """
                                      These commands are used for creating and editing [Server dashboards](https://i.imgur.com/KTsW7zY.png).

                                      `/lost-ark create` - Creates Server dashboard
                                      `/lost-ark remove` - Removes Server dashboard
                                      `/lost-ark status` - Shows current Server dashboard status
                                      `/lost-ark update` - Updates Server dashboard manually
                                      `/lost-ark resend` - Resends Server dashboard
                                      `/lost-ark add-favorite` - Adds server into Favorites section
                                      `/lost-ark remove-favorite` - Removes server from Favorites section
                                      `/lost-ark hide-region` - Hides region from Server dashboard
                                      `/lost-ark show-region` - Shows region on Server dashboard
                                      `/lost-ark hide-all-regions` - Hides all regions from Server dashboard
                                      `/lost-ark show-all-regions` - Shows all regions on Server dashboard
                                      `/lost-ark language-list` - Showes list of currently supported languages
                                      `/lost-ark language` - Sets language on Server dashboard""", false
        );
        embedBuilder.addField("• Command `/notifications` (1/2)",
                              """
                                      These commands are used for creating and editing [Notification channels](https://i.imgur.com/yGVdhLX.png).

                                      `/notifications create` - Marks current channel as a Notification channel
                                      `/notifications remove` - Unmarks current channel as a Notification channel
                                      `/notifications news` - Enables or disables News category
                                      `/notifications forums` - Enables or disables Forums category
                                      `/notifications twitter` - Enables or disables Twitter notifications
                                      `/notifications twitter-filter` - Filters specified keywords in tweets
                                      `/notifications twitter-settings` - Twitter settings
                                      `/notifications status-server` - Enables or disables [server tracking](https://i.imgur.com/cvfNohX.png) for server""", false
        );
        embedBuilder.addField("• Command `/notifications` (2/2)",
                              """
                                      `/notifications status-region` - Enables or disables [server tracking](https://i.imgur.com/cvfNohX.png) for region
                                      `/notifications status-whitelist` - Adds or removes whitelisted statuses
                                      `/notifications status-ping` - Adds or removes roles to ping
                                      `/notifications status` - Shows you which notifications you have enabled
                                      `/notifications clear` - Disables/clears specified notifications/lists""", false);

        messageBuilder.setEmbeds(embedBuilder.build());
        message.setMessageBuilder(messageBuilder);

        message.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.PRIMARY, "Back")), interactionEvent -> getMainMenuMessage().editOriginal(interactionEvent.getInteractionHook()));

        return message;
    }

    private InteractiveMessage getServerDashboardsMessage() {
        InteractiveMessage message = InteractiveMessage.create();
        MessageBuilder messageBuilder = new MessageBuilder();

        EmbedBuilder embedBuilder = DiscordUtils.getDefaultEmbed();

        embedBuilder.setTitle("Mayu's Lost Ark Bot | Server Dashboards");
        embedBuilder.setDescription(
                "This bot has a function called [Server dashboards](https://i.imgur.com/KTsW7zY.png). Server dashboards are used to view all current server statuses within the game Lost Ark.");

        embedBuilder.addField("• How to use",
                              """
                                      0. Create an empty Text Channel, which will be used for Server dashboard
                                      1. Create a Server dashboard with `/lost-ark create` command
                                      2. Add your favorite server with `/lost-ark add-favorite` command
                                      3. You can also hide unwanted regions with `/lost-ark hide-region` command
                                      3.1. You can use `/lost-ark hide-all-regions` to hide all regions
                                      4. View supported languages with `/lost-ark language-list` command
                                      5. Set your desired language with `/lost-ark language` command
                                      6. You have successfully created and customized your Server dashboard""", false
        );
        embedBuilder.addField("• Common problems",
                              """
                                      **My Server dashboard does not update!**
                                      You can try resending it with `/lost-ark resend` command. Alternatively, you can also recreate the Server dashboard.

                                      **There are multiple dashboards**
                                      This should not really happen, but if it did, you can delete all of them and use `/lost-ark resend` command to send a new one.

                                      **There are no servers!**
                                      This can happen if Lost Ark's server status website has a bug or if servers are under maintenance. There's nothing I can do to fix it - it is problem with a 3rd party.""",
                              false
        );

        messageBuilder.setEmbeds(embedBuilder.build());
        message.setMessageBuilder(messageBuilder);

        message.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.PRIMARY, "Back")), interactionEvent -> getMainMenuMessage().editOriginal(interactionEvent.getInteractionHook()));

        return message;
    }

    private InteractiveMessage getNotificationsMessage() {
        InteractiveMessage message = InteractiveMessage.create();
        MessageBuilder messageBuilder = new MessageBuilder();

        EmbedBuilder embedBuilder = DiscordUtils.getDefaultEmbed();

        embedBuilder.setTitle("Mayu's Lost Ark Bot | Notifications");
        embedBuilder.setDescription("""
                                            This bot has a function called [Notifications](https://i.imgur.com/yGVdhLX.png). They are a little bit more complicated than Server dashboards.
                                            There are 3 types of notifications: News, Forums and Server changes\s
                                                          
                                            > News
                                            News type corresponds to Notifications which comes from Lost Ark's news page. There are also 4 categories of News: General, Updates, Events, and Release-notes
                                                          
                                            > Forums
                                            Forums type corresponds to Notifications which comes from Lost Ark's forums page. There are also 2 categories of Forums: Maintenance and Downtime
                                                          
                                            > Server changes
                                            This bot can also track [server changes](https://i.imgur.com/cvfNohX.png) - the bot will send into the Notification channel a message with servers that changed their statuses. You can specify which servers to track by server name or by whole region.""");

        embedBuilder.addField("• How to use",
                              """
                                      0. Create an empty Text Channel, which will be used for Notifications
                                      1. Mark the Text Channel as a Notification channel with `/notifications create` command
                                      2. Enable your desired Notifications with commands bellow
                                      2.1. `/notifications news`
                                      2.2. `/notification forums`
                                      2.3. `/notifications status-server`
                                      2.4. `/notifications status-region`
                                      3. You have successfully created and customized your Notification channel""", false
        );
        embedBuilder.addField("• Common problems",
                              """
                                      **Notifications does not show!**
                                      Check if you have all your desired notifications enabled with `/notifications status` command. There could be also an API problem, which you can report on my [support server](https://discord.gg/YMs6wXPqcB).

                                      **There is duplicate text in Notifications**
                                      I've tried to eliminate most duplicate text in News and Forums notifications, however, there is sometimes one word off or something which can trick the check. Sorry about that!""",
                              false
        );

        messageBuilder.setEmbeds(embedBuilder.build());
        message.setMessageBuilder(messageBuilder);

        message.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.PRIMARY, "Back")), interactionEvent -> getMainMenuMessage().editOriginal(interactionEvent.getInteractionHook()));

        return message;
    }

    private InteractiveMessage getOfficialSupportMessage() {
        InteractiveMessage message = InteractiveMessage.create();
        MessageBuilder messageBuilder = new MessageBuilder();

        EmbedBuilder embedBuilder = DiscordUtils.getDefaultEmbed();

        embedBuilder.setTitle("Mayu's Lost Ark Bot | Official Support");
        embedBuilder.setDescription("You can get official support on my [support server](https://discord.gg/YMs6wXPqcB). You can ask questions there about anything related to this bot.");

        messageBuilder.setEmbeds(embedBuilder.build());
        message.setMessageBuilder(messageBuilder);

        message.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.PRIMARY, "Back")), interactionEvent -> getMainMenuMessage().editOriginal(interactionEvent.getInteractionHook()));

        return message;
    }
}
