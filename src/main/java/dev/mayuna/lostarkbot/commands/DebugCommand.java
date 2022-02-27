package dev.mayuna.lostarkbot.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.managers.DataManager;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.mayusjdautils.utils.DiscordUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class DebugCommand extends SlashCommand {

    public DebugCommand() {
        this.name = "debug";

        this.guildOnly = false;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Utils.makeEphemeral(event, true);
        InteractionHook hook = event.getHook();

        EmbedBuilder embedBuilder = DiscordUtils.getDefaultEmbed();
        embedBuilder.setTitle("Debug");
        embedBuilder.setDescription("");

        embedBuilder.addField("Bot Information",
                              "Currently on **" + Main.getJda().getGuilds().size() + "** guilds\n" +
                                      "There are **" + DataManager.getDashboardCount() + "** dashboards",
                              false
        );

        hook.editOriginalEmbeds(embedBuilder.build()).complete();
    }
}
