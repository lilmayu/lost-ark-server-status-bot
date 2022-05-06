package dev.mayuna.lostarkbot.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.mayuna.lostarkbot.util.Constants;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.lostarkbot.util.config.Config;
import dev.mayuna.mayusjdautils.util.DiscordUtils;
import dev.mayuna.mayuslibrary.util.ArrayUtils;
import net.dv8tion.jda.api.EmbedBuilder;

public class AboutCommand extends SlashCommand {

    public AboutCommand() {
        this.name = "about";
        this.help = "About this bot";

        this.guildOnly = false;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Utils.makeEphemeral(event, true);

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

        for (String contributor : Config.get().getBot().getContributors()) {
            contributors += "`" + contributor + "`";

            if (!ArrayUtils.getLast(Config.get().getBot().getContributors().toArray()).equals(contributor)) {
                contributors += ", ";
            }
        }

        return contributors;
    }
}
