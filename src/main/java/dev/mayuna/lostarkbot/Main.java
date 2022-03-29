package dev.mayuna.lostarkbot;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import dev.mayuna.lostarkbot.commands.*;
import dev.mayuna.lostarkbot.console.ConsoleCommandManager;
import dev.mayuna.lostarkbot.helpers.ServerDashboardHelper;
import dev.mayuna.lostarkbot.listeners.CommandListener;
import dev.mayuna.lostarkbot.managers.GuildDataManager;
import dev.mayuna.lostarkbot.managers.LanguageManager;
import dev.mayuna.lostarkbot.managers.NotificationsManager;
import dev.mayuna.lostarkbot.managers.PresenceManager;
import dev.mayuna.lostarkbot.util.Config;
import dev.mayuna.lostarkbot.util.Constants;
import dev.mayuna.lostarkbot.util.LegacyDashboardsLoader;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayusjdautils.data.MayuCoreListener;
import dev.mayuna.mayusjdautils.utils.DiscordUtils;
import dev.mayuna.mayusjdautils.utils.MessageInfo;
import dev.mayuna.mayuslibrary.exceptionreporting.ExceptionListener;
import dev.mayuna.mayuslibrary.exceptionreporting.ExceptionReporter;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageChannel;

public class Main {

    // Discord
    private static @Getter JDA jda;
    private static @Getter CommandClientBuilder client;

    // Runtime
    private static boolean configLoaded = false;
    private static boolean fullyLoaded = false;

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        Logger.init();

        Logger.info("Starting up Mayu's Lost Ark Bot @ v" + Constants.VERSION + "...");
        Logger.info("Made by mayuna#8016");

        Logger.info("Initializing Console Commands...");
        ConsoleCommandManager.init();

        Logger.info("Loading library settings...");
        loadLibrarySettings();

        Logger.info("Loading config...");
        if (!Config.load()) {
            Logger.error("There was fatal error while loading Config! Cannot proceed.");
            System.exit(-1);
        }
        configLoaded = true;

        Logger.info("Loading JDA stuff...");
        client = new CommandClientBuilder()
                .setStatus(OnlineStatus.IDLE)
                .setActivity(Activity.playing("Loading..."))
                .useHelpBuilder(false)
                .setOwnerId(String.valueOf(Config.getOwnerID()))
                .setPrefix(Config.getPrefix())
                .setAlternativePrefix(Constants.ALTERNATIVE_PREFIX)
                .setListener(new CommandListener());

        Logger.info("Loading commands...");
        loadCommands();

        Logger.info("Logging into Discord...");
        loginIntoDiscord();
        Logger.success("Logged in!");

        Logger.info("Loading managers...");
        loadManagers();

        fullyLoaded = true;
        Logger.success("Loading done! (took " + (System.currentTimeMillis() - start) + "ms)");
    }

    private static void loadManagers() {
        LanguageManager.load();

        if (!GuildDataManager.loadAll()) {
            Logger.error("There was fatal error while loading guilds! Cannot proceed.");
            System.exit(-1);
        }

        GuildDataManager.loadAllGuildData();
        NotificationsManager.load();

        LegacyDashboardsLoader.load();
        ServerDashboardHelper.startDashboardUpdateTimer();
        PresenceManager.startPresenceTimer();
    }

    private static void loadCommands() {
        client.addSlashCommands(new AboutCommand(), new LostArkCommand(), new HelpCommand(), new NotificationsCommand());
    }

    private static void loginIntoDiscord() {
        try {
            JDABuilder jdaBuilder = JDABuilder.createDefault(Config.getToken())
                    .addEventListeners(client.build())
                    .addEventListeners(new MayuCoreListener());
            jda = jdaBuilder.build().awaitReady();
        } catch (Exception exception) {
            exception.printStackTrace();
            Logger.error("Error occurred while logging into Discord! Cannot proceed.");
            System.exit(-1);
        }
    }

    private static void loadLibrarySettings() {
        ExceptionReporter.registerExceptionReporter();
        ExceptionReporter.getInstance().addListener(new ExceptionListener("default", "mayuna", exceptionReport -> {
            exceptionReport.getThrowable().printStackTrace();

            if (configLoaded) {
                Logger.error("Exception occurred! Sending it to Lost Ark Bot's exception Message channel.");

                if (Main.getJda() != null && Config.getExceptionMessageChannelID() != 0) {
                    MessageChannel messageChannel = Main.getJda().getTextChannelById(Config.getExceptionMessageChannelID());
                    if (messageChannel != null) {
                        MessageInfo.sendExceptionMessage(messageChannel, exceptionReport.getThrowable());
                    } else {
                        Logger.error("Unable to send exception to Exception message channel! (Invalid ExceptionMessageChannelID)");
                    }
                } else {
                    Logger.error("Unable to send exception to Exception message channel! (JDA is null / ExceptionMessageChannelID is not set)");
                }
            }

        }));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (configLoaded) {
                Logger.info("Shutting down...");

                if (fullyLoaded) {
                    Logger.info("Saving config and guilds...");
                    Config.save();
                    GuildDataManager.saveAll();
                } else {
                    Logger.warn("Bot did not fully loaded! Config and guilds won't be saved.");
                }

                Logger.info("o/");
            }
        }));

        DiscordUtils.setDefaultEmbed(new EmbedBuilder().setDescription("Loading..."));
        MayuCoreListener.enableExperimentalInteractionBehavior = true;
        MessageInfo.useSystemEmotes = true;
    }
}
