package dev.mayuna.lostarkbot.commands.dashboard.subcommands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.mayuna.lostarkbot.helpers.ServerDashboardHelper;
import dev.mayuna.lostarkbot.managers.LanguageManager;
import dev.mayuna.lostarkbot.objects.features.LanguagePack;
import dev.mayuna.lostarkbot.objects.features.ServerDashboard;
import dev.mayuna.lostarkbot.objects.other.LostArkRegion;
import dev.mayuna.lostarkbot.util.AutoMessageUtils;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.lostarkbot.util.logging.Logger;
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

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class DashboardSettingsCommand extends SlashCommand {

    public DashboardSettingsCommand() {
        this.name = "settings";
        this.help = "Easy settings menu for current Dashboard";

        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Utils.makeEphemeral(event, true);
        TextChannel textChannel = event.getTextChannel();
        InteractionHook interactionHook = event.getHook();

        if (!AutoMessageUtils.isEverythingAlrightDashboard(textChannel, interactionHook)) {
            return;
        }

        ServerDashboard dashboard = ServerDashboardHelper.getServerDashboard(textChannel);
        getBaseMessage(dashboard).editOriginal(interactionHook);
    }

    private InteractiveMessage getBaseMessage(ServerDashboard dashboard) {
        EmbedBuilder embedBuilder = MessageInfo.informationEmbed("");
        embedBuilder.setTitle("Dashboard settings");

        LanguagePack languagePack = dashboard.getLanguage();
        embedBuilder.addField("Language", "Current language: **" + languagePack.getLangName() + "** (`" + languagePack.getLangCode() + "`)", false);
        embedBuilder.addField("Hidden regions", Utils.makeVerticalStringList(dashboard.getHiddenRegions(), "There are no hidden regions."), false);
        embedBuilder.addField("Favorite servers", Utils.makeVerticalStringList(dashboard.getFavoriteServers(), "There are no favorite servers."), false);

        MessageBuilder messageBuilder = new MessageBuilder().setEmbeds(embedBuilder.build());
        InteractiveMessage interactiveMessage = InteractiveMessage.create(messageBuilder);

        interactiveMessage.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.PRIMARY, "Language")), interactionEvent -> {
            getChangeLanguageMessage(dashboard).editOriginal(interactionEvent.getInteractionHook());
        });

        interactiveMessage.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.PRIMARY, "Hidden regions")), interactionEvent -> {
            getHiddenRegionsMessage(dashboard).editOriginal(interactionEvent.getInteractionHook());
        });

        interactiveMessage.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.PRIMARY, "Favorite servers")), interactionEvent -> {
            getFavoriteServersMessage(dashboard).editOriginal(interactionEvent.getInteractionHook());
        });

        return interactiveMessage;
    }

    private InteractiveMessage getChangeLanguageMessage(ServerDashboard dashboard) {
        EmbedBuilder embedBuilder = MessageInfo.informationEmbed("");
        embedBuilder.setTitle("Dashboard settings | Language");

        String description = "";
        LanguagePack currentLanguagePack = dashboard.getLanguage();
        description += "Current language: **" + currentLanguagePack.getLangName() + "** (`" + currentLanguagePack.getLangCode() + "`)";
        description += "\n\nThis selection is limited only to 25 languages. Please, use `/dashboard language` command in order to change other languages.";
        embedBuilder.setDescription(description);

        MessageBuilder messageBuilder = new MessageBuilder().setEmbeds(embedBuilder.build());
        InteractiveMessage interactiveMessage = InteractiveMessage.create(messageBuilder);

        SelectMenu.Builder selectMenu = SelectMenu.create(UUID.randomUUID().toString());
        selectMenu.setPlaceholder("Choose a language");
        interactiveMessage.setSelectMenuBuilder(selectMenu);

        List<LanguagePack> languagePacks = LanguageManager.getLoadedLanguages();
        for (int x = 0; x < Math.min(languagePacks.size(), 25); x++) {
            LanguagePack languagePack = languagePacks.get(x);

            interactiveMessage.addInteraction(Interaction.asSelectOption(DiscordUtils.generateSelectOption(languagePack.getLangName())), interactionEvent -> {
                dashboard.setLangCode(languagePack.getLangCode());
                dashboard.update();
                dashboard.save();
                getBaseMessage(dashboard).editOriginal(interactionEvent.getInteractionHook());
            });
        }

        return interactiveMessage;
    }

    private InteractiveMessage getHiddenRegionsMessage(ServerDashboard dashboard) {
        EmbedBuilder embedBuilder = MessageInfo.informationEmbed("");
        embedBuilder.setTitle("Dashboard settings | Hidden regions");

        String description = "";
        description += "**Hidden regions**\n";
        description += Utils.makeVerticalStringList(dashboard.getHiddenRegions(), "There are no hidden regions.") + "\n";
        description += "\nPlease, choose an action.";
        embedBuilder.setDescription(description);

        MessageBuilder messageBuilder = new MessageBuilder().setEmbeds(embedBuilder.build());
        InteractiveMessage interactiveMessage = InteractiveMessage.create(messageBuilder);

        interactiveMessage.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.PRIMARY, "Show region")), interactionEvent -> {
            getShowHideRegionMessage(dashboard, RegionAction.SHOW).editOriginal(interactionEvent.getInteractionHook());
        });
        interactiveMessage.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.PRIMARY, "Hide region")), interactionEvent -> {
            getShowHideRegionMessage(dashboard, RegionAction.HIDE).editOriginal(interactionEvent.getInteractionHook());
        });
        interactiveMessage.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.SECONDARY, "Show all regions")), interactionEvent -> {
            dashboard.showAllRegions();
            dashboard.update();
            dashboard.save();

            getBaseMessage(dashboard).editOriginal(interactionEvent.getInteractionHook());
        });
        interactiveMessage.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.SECONDARY, "Hide all regions")), interactionEvent -> {
            dashboard.hideAllRegions();
            dashboard.update();
            dashboard.save();

            getBaseMessage(dashboard).editOriginal(interactionEvent.getInteractionHook());
        });

        return interactiveMessage;
    }

    private InteractiveMessage getShowHideRegionMessage(ServerDashboard dashboard, RegionAction regionAction) {
        EmbedBuilder embedBuilder = MessageInfo.informationEmbed("");
        embedBuilder.setTitle("Dashboard settings | Hidden regions | " + (regionAction == RegionAction.SHOW ? "Show" : "Hide") + " region");

        String description = "";
        description += "**Hidden regions**\n";
        description += Utils.makeVerticalStringList(dashboard.getHiddenRegions(), "There are no hidden regions.") + "\n";
        description += "\nPlease, choose a region to " + (regionAction == RegionAction.SHOW ? "show" : "hide") + ".";
        embedBuilder.setDescription(description);

        MessageBuilder messageBuilder = new MessageBuilder().setEmbeds(embedBuilder.build());
        InteractiveMessage interactiveMessage = InteractiveMessage.create(messageBuilder);

        SelectMenu.Builder selectMenu = SelectMenu.create(UUID.randomUUID().toString());
        selectMenu.setPlaceholder("Choose a region");
        interactiveMessage.setSelectMenuBuilder(selectMenu);

        List<String> regions = new LinkedList<>();

        if (regionAction == RegionAction.SHOW) {
            for (String region : dashboard.getHiddenRegions()) {
                regions.add(LostArkRegion.getCorrectFormatted(region));
            }
        } else {
            for (LostArkRegion region : LostArkRegion.values()) {
                if (!dashboard.getHiddenRegions().contains(region.name())) {
                    regions.add(region.getFormattedName());
                }
            }
        }

        if (!regions.isEmpty()) {
            for (String region : regions) {
                interactiveMessage.addInteraction(Interaction.asSelectOption(DiscordUtils.generateSelectOption(region)), interactionEvent -> {
                    if (regionAction == RegionAction.SHOW) {
                        dashboard.removeFromHiddenRegions(region);
                    } else {
                        dashboard.addToHiddenRegions(region);
                    }
                    dashboard.update();
                    dashboard.save();

                    getBaseMessage(dashboard).editOriginal(interactionEvent.getInteractionHook());
                });
            }
        } else {
            interactiveMessage.addInteraction(Interaction.asSelectOption(DiscordUtils.generateSelectOption("No option.")), interactionEvent -> {
                getBaseMessage(dashboard).editOriginal(interactionEvent.getInteractionHook());
            });
        }

        return interactiveMessage;
    }

    private InteractiveMessage getFavoriteServersMessage(ServerDashboard dashboard) {
        EmbedBuilder embedBuilder = MessageInfo.warningEmbed("");
        embedBuilder.setTitle("Dashboard settings | Favorite servers");

        String description = "";
        description += "**Favorite servers**\n";
        description += Utils.makeVerticalStringList(dashboard.getFavoriteServers(), "There are no favorite servers.") + "\n";
        description += "\nIn order to edit Favorites section, you must use `/dashboard favorite` command.";
        embedBuilder.setDescription(description);

        MessageBuilder messageBuilder = new MessageBuilder().setEmbeds(embedBuilder.build());
        InteractiveMessage interactiveMessage = InteractiveMessage.create(messageBuilder);

        interactiveMessage.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.PRIMARY, "Back")), interactionEvent -> {
            getBaseMessage(dashboard).editOriginal(interactionEvent.getInteractionHook());
        });

        return interactiveMessage;
    }

    private enum RegionAction {
        SHOW,
        HIDE;
    }
}
