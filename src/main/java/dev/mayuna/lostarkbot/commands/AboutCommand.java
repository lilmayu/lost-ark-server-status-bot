package dev.mayuna.lostarkbot.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import dev.mayuna.lostarkbot.util.Constants;
import dev.mayuna.mayusjdautils.utils.DiscordUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class AboutCommand extends SlashCommand {

    public AboutCommand() {
        this.name = "about";

        this.guildOnly = false;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply(true).complete();

        EmbedBuilder embedBuilder = DiscordUtils.getDefaultEmbed();
        embedBuilder.setDescription("");
        embedBuilder.setTitle("Lost Ark - Server Status Bot");
        embedBuilder.addField("Version", "`" + Constants.VERSION + "`", false);
        embedBuilder.addField("Source code", "Source code will be soon published via GitHub repo.", false);
        embedBuilder.addField("Credits", "Author: `mayuna#8016`", false);

        event.getHook().editOriginalEmbeds(embedBuilder.build()).queue();
    }
}
