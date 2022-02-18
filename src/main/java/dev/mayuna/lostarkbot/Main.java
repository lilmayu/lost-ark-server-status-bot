package dev.mayuna.lostarkbot;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import dev.mayuna.lostarkbot.commands.AboutCommand;
import dev.mayuna.lostarkbot.commands.DebugCommand;
import dev.mayuna.lostarkbot.commands.LostArkCommand;
import dev.mayuna.lostarkbot.console.ConsoleCommandManager;
import dev.mayuna.lostarkbot.listeners.CommandListener;
import dev.mayuna.lostarkbot.managers.PresenceManager;
import dev.mayuna.lostarkbot.managers.ServerDashboardManager;
import dev.mayuna.lostarkbot.util.Config;
import dev.mayuna.lostarkbot.util.Constants;
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
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Main {

    // Discord
    private static @Getter JDA jda;
    private static @Getter CommandClientBuilder client;

    // Runtime
    private static boolean configLoaded = false;

    public static void main(String[] args) {
        Logger.init();

        Logger.info("Starting up Lost Ark - Server Status Bot @ v" + Constants.VERSION + "...");
        Logger.info("Made by mayuna#8016");
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
        client = new CommandClientBuilder().useDefaultGame()
                .useHelpBuilder(false)
                .setOwnerId(String.valueOf(Config.getOwnerID()))
                .setActivity(Activity.playing("sus"))
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

        Logger.success("Loading done!");
    }

    private static void loadManagers() {
        ServerDashboardManager.load();
        ServerDashboardManager.init();
        PresenceManager.init();
    }

    private static void loadCommands() {
        client.addSlashCommands(new AboutCommand(), new LostArkCommand(), new DebugCommand());
    }

    private static void loginIntoDiscord() {
        try {
            JDABuilder jdaBuilder = JDABuilder.createDefault(Config.getToken())
                    .addEventListeners(client.build())
                    .addEventListeners(new MayuCoreListener())
                    .enableIntents(GatewayIntent.GUILD_PRESENCES)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .enableIntents(GatewayIntent.DIRECT_MESSAGES)
                    .enableIntents(GatewayIntent.DIRECT_MESSAGE_REACTIONS);
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

                Config.save();
                ServerDashboardManager.save();

                Logger.info("o/");
            }
        }));

        DiscordUtils.setDefaultEmbed(new EmbedBuilder().setDescription("Loading..."));
        MayuCoreListener.enableExperimentalInteractionBehavior = true;
        MessageInfo.useSystemEmotes = true;
    }
}
