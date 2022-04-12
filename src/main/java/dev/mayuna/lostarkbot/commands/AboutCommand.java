package dev.mayuna.lostarkbot.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import dev.mayuna.lostarkbot.util.Config;
import dev.mayuna.lostarkbot.util.Constants;
import dev.mayuna.mayusjdautils.utils.DiscordUtils;
import dev.mayuna.mayuslibrary.utils.ArrayUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class AboutCommand extends SlashCommand {

    public AboutCommand() {
        this.name = "about";
        this.help = "About this bot";

        this.guildOnly = false;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply(true).complete();

        EmbedBuilder embedBuilder = DiscordUtils.getDefaultEmbed();
        embedBuilder.setDescription("");
        embedBuilder.setTitle("Mayu's Lost Ark Bot");
        embedBuilder.addField("Version", "`" + Constants.VERSION + "`", false);
        embedBuilder.addField("Source code", "[GitHub Repository](https://github.com/lilmayu/lost-ark-server-status-bot)", false);
        embedBuilder.addField("Support", "[Support server](https://discord.gg/YMs6wXPqcB)", false);
        embedBuilder.addField("Credits", "Author: `mayuna#8016`", false);
        embedBuilder.addField("Contributors", getContributors(), false);

        event.getHook().editOriginalEmbeds(embedBuilder.build()).queue();
    }

    private String getContributors() {
        String contributors = "";

        for (String contributor : Config.getContributors()) {
            contributors += "`" + contributor + "`";

            if (!ArrayUtils.getLast(Config.getContributors().toArray()).equals(contributor)) {
                contributors += ", ";
            }
        }

        return contributors;
    }
}
