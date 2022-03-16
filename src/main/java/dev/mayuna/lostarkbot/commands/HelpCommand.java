package dev.mayuna.lostarkbot.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.mayusjdautils.utils.DiscordUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class HelpCommand extends SlashCommand {

    public HelpCommand() {
        this.name = "help";

        this.guildOnly = false;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Utils.makeEphemeral(event, true);

        EmbedBuilder embedBuilder = DiscordUtils.getDefaultEmbed();
        embedBuilder.setDescription("Here is list of currently usable commands.");

        embedBuilder.setTitle("Help command");
        embedBuilder.addField("General",
                              "`/about` - Shows information about this bot\n" +
                                      "`/help` - Shows this message", false
        );
        embedBuilder.addField("Lost-Ark Commands",
                              "All commands must be sent to Text Channel, where Server Dashboard exist.\n" +
                                      "`/lost-ark create` - Creates Server Dashboard\n" +
                                      "`/lost-ark remove` - Removes Server Dashboard\n" +
                                      "`/lost-ark status` - Shows current Server Dashboard status\n" +
                                      "`/lost-ark update` - Force updates Server Dashboard\n" +
                                      "`/lost-ark resend` - Resends Server Dashboard\n" +
                                      "`/lost-ark add-favorite <server>` - Adds specified server into Favorites section\n" +
                                      "`/lost-ark remove-favorite <server>` - Removes specified server from Favorites section\n" +
                                      "`/lost-ark hide-region <region>` - Hides region from Server Dashboard\n" +
                                      "`/lost-ark show-region <region>` - Shows region on Server Dashboard\n" +
                                      "`/lost-ark hide-all-regions` - Hides all regions from Server Dashboard\n" +
                                      "`/lost-ark show-all-regions` - Shows all regions on Server Dashboard\n" +
                                      "`/lost-ark language-list` - Shows list of currently supported langauges\n" +
                                      "`/lost-ark language <language code>` - Sets language on Server Dashboard", false
        );

        event.getHook().editOriginalEmbeds(embedBuilder.build()).queue();
    }
}
