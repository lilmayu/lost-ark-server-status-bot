package dev.mayuna.lostarkbot;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import dev.mayuna.lostarkbot.commands.AboutCommand;
import dev.mayuna.lostarkbot.commands.HelpCommand;
import dev.mayuna.lostarkbot.commands.LostArkCommand;
import dev.mayuna.lostarkbot.commands.NotificationsCommand;
import dev.mayuna.lostarkbot.commands.dashboard.DashboardRootCommand;
import dev.mayuna.lostarkbot.console.ConsoleCommandManager;
import dev.mayuna.lostarkbot.console.commands.*;
import dev.mayuna.lostarkbot.data.GuildDataManager;
import dev.mayuna.lostarkbot.listeners.CommandListener;
import dev.mayuna.lostarkbot.listeners.ShardWatcher;
import dev.mayuna.lostarkbot.managers.*;
import dev.mayuna.lostarkbot.util.Constants;
import dev.mayuna.lostarkbot.util.config.Config;
import dev.mayuna.lostarkbot.util.config.LegacyConfig;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayusjdautils.data.MayuCoreListener;
import dev.mayuna.mayusjdautils.util.DiscordUtils;
import dev.mayuna.mayusjdautils.util.MessageInfo;
import dev.mayuna.mayuslibrary.exceptionreporting.ExceptionListener;
import dev.mayuna.mayuslibrary.exceptionreporting.ExceptionReporter;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class Main {

    // Discord
    private static @Getter MayuShardManager mayuShardManager;
    private static @Getter CommandClientBuilder client;

    // Runtime
    private static boolean configLoaded = false;
    private static boolean fullyLoaded = false;
    private static @Setter boolean stopping = false;

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();

        File logsFolder = new File("./logs/");
        if (!logsFolder.exists()) {
            if (!logsFolder.mkdirs()) {
                Logger.fatal("Could not create " + logsFolder.getAbsolutePath() + " folder!");
                return;
            }
        }

        Logger.info("Starting up Mayu's Lost Ark Bot @ v" + Constants.VERSION + "...");
        Logger.info("> Made by mayuna#8016");

        Logger.info("Initializing Console Commands...");
        ConsoleCommandManager.init();
        loadConsoleCommands();

        Logger.info("Loading library settings...");
        loadLibrarySettings();

        Logger.info("Loading config...");
        LegacyConfig.load();
        if (!Config.load()) {
            Logger.fatal("There was fatal error while loading Config! Cannot proceed.");
            System.exit(-1);
        }
        configLoaded = true;

        Logger.info("Initializing Executor Service (Update Service)...");
        ShardExecutorManager.initExecutorService();

        Logger.info("Loading JDA Utilities...");
        loadJdaUtilities();

        Logger.info("Loading commands...");
        loadCommands();

        Logger.info("Loading JDA...");
        loadJda();

        Logger.info("Loading managers...");
        loadManagers();

        fullyLoaded = true;
        Logger.success("Loading done! (took " + (System.currentTimeMillis() - start) + "ms)");
    }

    private static void loadJdaUtilities() {
        client = new CommandClientBuilder()
                .setStatus(OnlineStatus.IDLE)
                .setActivity(Activity.playing("Loading core..."))
                .useHelpBuilder(false)
                .setOwnerId(String.valueOf(Config.get().getBot().getOwnerId()))
                .setListener(new CommandListener());

        RestAction.setDefaultTimeout(2L, TimeUnit.MINUTES);
    }

    private static void loadJda() {
        Logger.info("Building shard manager... Total shards: " + Config.get().getBot().getTotalShards());

        DefaultShardManagerBuilder shardBuilder = DefaultShardManagerBuilder.createLight(Config.get().getBot().getToken())
                .setShardsTotal(Config.get().getBot().getTotalShards())
                .setStatusProvider(shardId -> OnlineStatus.IDLE)
                .setActivityProvider(PresenceManager::getActivityProvider)
                .addEventListeners(client.build())
                .addEventListeners(new ShardWatcher())
                .addEventListeners(new MayuCoreListener());

        try {
            mayuShardManager = new MayuShardManager(shardBuilder.build());
            mayuShardManager.waitOnAll();
        } catch (Exception exception) {
            Logger.throwing(exception);
            Logger.fatal("Error occurred while logging into Discord! Cannot proceed.");
            System.exit(-1);
        }
    }

    private static void loadManagers() {
        LanguageManager.load();

        GuildDataManager.init(mayuShardManager.get().getShardsTotal());

        if (!GuildDataManager.loadAllFiles()) {
            Logger.fatal("There was fatal error while loading guilds! Cannot proceed.");
            System.exit(-1);
        }

        GuildDataManager.loadAllGuildDataFeatures();
        ServerDashboardManager.init();
        NotificationsManager.init();

        TwitterManager.initStream();

        PresenceManager.startPresenceTimer();
    }

    private static void loadCommands() {
        client.addSlashCommands(new AboutCommand(),
                                new LostArkCommand(),
                                new HelpCommand(),
                                new NotificationsCommand(),
                                new DashboardRootCommand()
        );
    }

    private static void loadConsoleCommands() {
        ConsoleCommandManager.registerCommands(new DebugConsoleCommand(),
                                               new GuildConsoleCommand(),
                                               new LangConsoleCommand(),
                                               new LoadDataConsoleCommand(),
                                               new LostArkConsoleCommand(),
                                               new SaveDataConsoleCommand(),
                                               new WriteDownNumberOfGuildsConsoleCommand(),
                                               new NotificationsConsoleCommand(),
                                               new ShardsConsoleCommand(),
                                               new TopggConsoleCommand()
        );
    }

    private static void loadLibrarySettings() {
        ExceptionReporter.registerExceptionReporter();
        ExceptionReporter.getInstance().addListener(new ExceptionListener("default", "mayuna", exceptionReport -> {
            Logger.throwing(exceptionReport.getThrowable());
            Logger.warn("Exception occurred! Sending it to Lost Ark Bot's exception Message channel.");

            if (configLoaded) {
                if (Main.getMayuShardManager().get() != null && Config.get().getBot().getExceptionMessageChannelID() != 0) {
                    MessageChannel messageChannel = Main.getMayuShardManager().get().getTextChannelById(Config.get().getBot().getExceptionMessageChannelID());
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
            Logger.info("Shutting down...");

            if (fullyLoaded && !stopping) {
                Logger.info("Saving GuildData...");
                GuildDataManager.saveAll();
            } else {
                Logger.warn("Bot did not fully loaded! GuildData won't be saved.");
            }

            Logger.info("o/");
        }));

        DiscordUtils.setDefaultEmbed(new EmbedBuilder().setDescription("Loading, please wait... If you see this message for too long, something happened."));
        MayuCoreListener.enableExperimentalInteractionBehavior = true;
        MessageInfo.useSystemEmotes = true;
    }
}
