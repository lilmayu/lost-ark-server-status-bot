package dev.mayuna.lostarkbot.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.mayusjdautils.interactive.InteractiveMessage;
import dev.mayuna.mayusjdautils.interactive.objects.Interaction;
import dev.mayuna.mayusjdautils.util.DiscordUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

public class HelpCommand extends SlashCommand {

    public HelpCommand() {
        this.name = "help";
        this.help = "Shows you command list and tutorials";

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
        embedBuilder.appendDescription("Tip: You can also see documentation at [docs.mayuna.dev](https://docs.mayuna.dev/en/mayus-dashboard-bot/Introduction)");

        messageBuilder.setEmbeds(embedBuilder.build());
        message.setMessageBuilder(messageBuilder);

        message.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.PRIMARY, "Command List")),
                               interactionEvent -> getCommandListMessage().editOriginal(interactionEvent.getInteractionHook())
        );
        message.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.PRIMARY, "Server Dashboards")),
                               interactionEvent -> getServerDashboardsMessage().editOriginal(interactionEvent.getInteractionHook())
        );
        message.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.PRIMARY, "Notifications")),
                               interactionEvent -> getNotificationsMessage().editOriginal(interactionEvent.getInteractionHook())
        );
        message.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.SECONDARY, "Official Support")),
                               interactionEvent -> getOfficialSupportMessage().editOriginal(interactionEvent.getInteractionHook())
        );

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
        embedBuilder.addField("• Command `/dashboard`",
                              """
                                      These commands are used for managing [Server dashboards](https://i.imgur.com/KTsW7zY.png).

                                      `/dashboard create` - Creates Server dashboard
                                      `/dashboard remove` - Removes Server dashboard
                                      `/dashboard info` - Shows information about Server dashboard
                                      `/dashboard force-update` - Updates Server dashboard manually
                                      `/dashboard resend` - Resends Server dashboard
                                      `/dashboard favorite` - Adds/removes server into/from Favorites section
                                      `/dashboard settings` - Easy settings menu
                                      `/dashboard region` - Shows/hides region on/from Server dashboard
                                      `/dashboard all-regions` - Shows/hides all regions on/from Server dashboard
                                      `/dashboard language-list` - Shows list of currently supported languages
                                      `/dashboard language` - Sets language on Server dashboard""", false
        );
        embedBuilder.addField("• Command `/notify` (1/2)",
                              """
                                      These commands are used for managing [Notification channels](https://i.imgur.com/yGVdhLX.png).

                                      `/notify create` - Marks current channel as a Notification channel
                                      `/notify remove` - Unmarks current channel as a Notification channel
                                      `/notify info` - Shows information about Notification channel
                                      `/notify news` - Enables or disables News category
                                      `/notify forums` - Enables or disables Forums category
                                      `/notify twitter` - Enables or disables Twitter notifications
                                      `/notify twitter-filter` - Filters specified keywords in tweets
                                      `/notify twitter-ping` - Adds/removes roles to ping on new Tweets
                                      `/notify twitter-settings` - Easy Twitter settings menu""", false
        );
        embedBuilder.addField("• Command `/notify` (2/2)",
                              """
                                      `/notify status-server` - Enables or disables [server tracking](https://i.imgur.com/cvfNohX.png) for server
                                      `/notify status-region` - Enables or disables [server tracking](https://i.imgur.com/cvfNohX.png) for region
                                      `/notify status-whitelist` - Allows you to whitelist statuses
                                      `/notify status-ping` - Adds/removes roles to ping on Server status change
                                      `/notify clear` - Disables/clears specified notifications/lists""", false
        );

        messageBuilder.setEmbeds(embedBuilder.build());
        message.setMessageBuilder(messageBuilder);

        message.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.SECONDARY, "Back")),
                               interactionEvent -> getMainMenuMessage().editOriginal(interactionEvent.getInteractionHook())
        );

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
                                      1. Create a Server dashboard with `/dashboard create` command
                                      2. Add your favorite server with `/dashboard favorite` command
                                      3. You can also hide unwanted regions with `/dashboard region` command
                                      3.1. You can use `/dashboard all-regions` to hide all regions
                                      4. View supported languages with `/dashboard language-list` command
                                      5. Set your desired language with `/dashboard language` command
                                      6. You have successfully created and customized your Server dashboard""", false
        );
        embedBuilder.addField("• Common problems",
                              """
                                      **My Server dashboard does not update!**
                                      You can try resending it with `/dashboard resend` command. Alternatively, you can also recreate the Server dashboard.

                                      **There are multiple dashboards**
                                      This should not really happen, but if it did, you can delete all of them and use `/dashboard resend` command to send a new one.

                                      **There are no servers!**
                                      This can happen if Lost Ark's server status website has a bug or if servers are under maintenance. There's nothing I can do to fix it - it is problem with a 3rd party.""",
                              false
        );

        messageBuilder.setEmbeds(embedBuilder.build());
        message.setMessageBuilder(messageBuilder);

        message.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.SECONDARY, "Back")),
                               interactionEvent -> getMainMenuMessage().editOriginal(interactionEvent.getInteractionHook())
        );

        return message;
    }

    private InteractiveMessage getNotificationsMessage() {
        InteractiveMessage message = InteractiveMessage.create();
        MessageBuilder messageBuilder = new MessageBuilder();

        EmbedBuilder embedBuilder = DiscordUtils.getDefaultEmbed();

        embedBuilder.setTitle("Mayu's Lost Ark Bot | Notifications");
        embedBuilder.setDescription("""
                                            This bot has a function called [Notifications](https://i.imgur.com/yGVdhLX.png). They are a little bit more complicated than Server dashboards.
                                            There are 4 types of notifications: News, Forums, Server status changes and Twitter
                                                          
                                            > News
                                            News type corresponds to Notifications which comes from Lost Ark's news page. There are also 4 categories of News: General, Updates, Events, and Release-notes
                                                          
                                            > Forums
                                            Forums type corresponds to Notifications which comes from Lost Ark's forums page. There are also 2 categories of Forums: Maintenance and Downtime
                                                          
                                            > Server status changes
                                            This bot can also track [server status changes](https://i.imgur.com/cvfNohX.png) - the bot will send into the Notification channel a message with servers that changed their statuses. You can specify which servers to track by server name or by whole region. You can also whitelist specific statuses plus set roles which will be pinged.
                                                                                        
                                            > Twitter
                                            You can now also follow [Lost Ark's Twitter accounts](https://i.imgur.com/68bptt7.png) with ease! You can filter their Tweets via a keyword filter, set up roles which will be pinged on new Tweets and more.
                                            """);

        embedBuilder.addField("• How to use",
                              """
                                      0. Create an empty Text Channel, which will be used for Notifications
                                      1. Mark the Text Channel as a Notification channel with `/notify create` command
                                      2. Enable your desired Notifications with commands bellow
                                      2.1. `/notify news`
                                      2.2. `/notify forums`
                                      2.3. `/notify status-server`
                                      2.4. `/notify status-region`
                                      2.5. `/notify status-ping`
                                      2.5. `/notify status-whitelist`
                                      2.6. `/notify twitter-settings`
                                      2.7. `/notify twitter-filter`
                                      2.8. `/notify twitter-ping`
                                      3. You have successfully created and customized your Notification channel""", false
        );
        embedBuilder.addField("• Common problems",
                              """
                                      **Notifications does not show!**
                                      Check if you have all your desired notifications enabled with `/notify status` command. There could be also an API problem, which you can report on my [support server](https://discord.gg/YMs6wXPqcB).

                                      **There is duplicate text in Notifications**
                                      I've tried to eliminate most duplicate text in News and Forums notifications, however, there is sometimes one word off or something which can trick the check. Sorry about that!""",
                              false
        );

        messageBuilder.setEmbeds(embedBuilder.build());
        message.setMessageBuilder(messageBuilder);

        message.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.PRIMARY, "Status whitelisting")),
                               interactionEvent -> getStatusWhitelistingMessage().editOriginal(interactionEvent.getInteractionHook())
        );

        message.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.SECONDARY, "Back")),
                               interactionEvent -> getMainMenuMessage().editOriginal(interactionEvent.getInteractionHook())
        );

        return message;
    }

    private InteractiveMessage getStatusWhitelistingMessage() {
        InteractiveMessage message = InteractiveMessage.create();
        MessageBuilder messageBuilder = new MessageBuilder();

        EmbedBuilder embedBuilder = DiscordUtils.getDefaultEmbed();

        embedBuilder.setTitle("Mayu's Lost Ark Bot | Notifications | Status whitelisting");

        embedBuilder.setDescription("This feature is little bit complicated but in the end, it is very useful.\n\nEverything here is done via `/notify status-whitelist` command.");
        embedBuilder.addField("• Basic description",
                              """
                                      Using this feature, you can whitelist specific Server statuses to be sent.
                                                                            
                                      You can whitelist statuses depending on if the status was **Changed from** or **Changed to**.
                                      """, false
        );

        embedBuilder.addField("• Changed from / Changed to",
                              """
                                      **Changed from** status indicates status, which was in the past - Server has changed its status from **Online** to something else.
                                      **Changed to** status indicates status, which is in the present - Server has changed its status to **Online**.
                                      """,
                              false
        );

        embedBuilder.addField("• Handy example",
                              """
                                      Lets say, you want to receive Server status changes only when servers go under Maintenance.
                                      
                                      This can be done with these two commands:
                                      • `/notify status-whitelist [Change to] [Online]` - Will send all server status changes where the status changes to Online
                                      • `/notify status-whitelist [Change from] [Maintenance]` - Will send all server status changes where the status changes from Maintenance
                                      
                                      If you set both of those whitelists, you will receive only notifications where Servers change their statuses from Maintenance to Online (aka. Maintenance ends).
                                      Combine it with `/notify status-ping` and you will be pinged when the Maintenance ends! Pretty useful I'd say.
                                      """,
                              false
        );

        embedBuilder.addField("• I need help", "If you don't still understand this feature, hit me up on my [support server](https://discord.gg/YMs6wXPqcB) and I will gladly help you.\n\nPro TIP: You can clear your whitelist using `/notify clear` command.", false);

        messageBuilder.setEmbeds(embedBuilder.build());
        message.setMessageBuilder(messageBuilder);

        message.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.SECONDARY, "Back")),
                               interactionEvent -> getNotificationsMessage().editOriginal(interactionEvent.getInteractionHook())
        );

        return message;
    }

    private InteractiveMessage getOfficialSupportMessage() {
        InteractiveMessage message = InteractiveMessage.create();
        MessageBuilder messageBuilder = new MessageBuilder();

        EmbedBuilder embedBuilder = DiscordUtils.getDefaultEmbed();

        embedBuilder.setTitle("Mayu's Lost Ark Bot | Official Support");
        embedBuilder.setDescription(
                "You can get official support on my [support server](https://discord.gg/YMs6wXPqcB). You can ask questions there about anything related to this bot.");

        messageBuilder.setEmbeds(embedBuilder.build());
        message.setMessageBuilder(messageBuilder);

        message.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.SECONDARY, "Back")),
                               interactionEvent -> getMainMenuMessage().editOriginal(interactionEvent.getInteractionHook())
        );

        return message;
    }
}
