package dev.mayuna.lostarkbot.commands.dashboard.subcommands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.mayuna.lostarkbot.helpers.ServerDashboardHelper;
import dev.mayuna.lostarkbot.managers.LanguageManager;
import dev.mayuna.lostarkbot.objects.features.LanguagePack;
import dev.mayuna.lostarkbot.objects.features.ServerDashboard;
import dev.mayuna.lostarkbot.util.AutoMessageUtils;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.mayusjdautils.util.MessageInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class DashboardLanguageCommands {

    public static class DashboardLanguageListCommand extends SlashCommand {

        public DashboardLanguageListCommand() {
            this.name = "language-list";
            this.help = "Lists all available languages";
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Utils.makeEphemeral(event, true);
            InteractionHook interactionHook = event.getHook();

            EmbedBuilder embedBuilder = MessageInfo.informationEmbed("");
            embedBuilder.setTitle("Available languages");

            String description = "Format: `code` - name\n\n";
            for (LanguagePack languagePack : LanguageManager.getLoadedLanguages()) {
                description += "`" + languagePack.getLangCode() + "` - " + languagePack.getLangName() + "\n";
            }
            embedBuilder.setDescription(description);
            embedBuilder.setFooter("There are " + LanguageManager.getLoadedLanguages().size() + " languages");
            interactionHook.editOriginalEmbeds(embedBuilder.build()).queue();
        }
    }

    public static class DashboardLanguageCommand extends SlashCommand {

        public DashboardLanguageCommand() {
            this.name = "language";
            this.help = "Changes current dashboard's language";

            List<OptionData> options = new ArrayList<>();
            OptionData languageOption = new OptionData(OptionType.STRING, "code", "Language code. Use /lost-ark language-list for more information", true);
            options.add(languageOption);
            this.options = options;

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Utils.makeEphemeral(event, true);
            TextChannel textChannel = event.getTextChannel();
            InteractionHook interactionHook = event.getHook();

            OptionMapping codeOption = AutoMessageUtils.getOptionMapping(event, "code");

            if (codeOption == null) {
                return;
            }

            if (!AutoMessageUtils.isEverythingAlrightDashboard(textChannel, interactionHook)) {
                return;
            }

            LanguagePack languagePack = LanguageManager.getLanguageByCode(codeOption.getAsString());

            if (languagePack == null) {
                interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("Invalid language code `" + codeOption.getAsString() + "`.\n\nYou can see all language codes in `/dashboard language-list` command")
                                                           .build()).queue();
                return;
            }

            ServerDashboard dashboard = ServerDashboardHelper.getServerDashboard(textChannel);

            dashboard.setLangCode(languagePack.getLangCode());
            dashboard.update();
            dashboard.save();

            interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully changed language to **" + languagePack.getLangName() + "**!").build()).queue();
        }
    }
}
