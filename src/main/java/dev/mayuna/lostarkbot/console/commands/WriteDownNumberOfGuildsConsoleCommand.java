package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayuslibrary.exceptionreporting.ExceptionReporter;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WriteDownNumberOfGuildsConsoleCommand extends AbstractConsoleCommand {

    private static final String fileName = "./guilds_count.txt";

    public WriteDownNumberOfGuildsConsoleCommand() {
        this.name = "write-down-number-of-guilds";
    }

    @Override
    public void execute(String arguments) {
        String firstColumn;
        String secondColumn = String.valueOf(Main.getJda().getGuilds().size());

        if (!arguments.isEmpty()) {
            firstColumn = arguments;
        } else {
            firstColumn = new SimpleDateFormat("dd.MM.yyyy").format(new Date());
        }

        try {
            String toWrite = firstColumn + ";" + secondColumn + "\n";
            Files.writeString(Paths.get(fileName), toWrite, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            Logger.info("Wrote down '" + toWrite.replace("\n", "") + "' into file " + fileName);
        } catch (Exception exception) {
            Logger.throwing(exception);
            ExceptionReporter.getInstance().uncaughtException(Thread.currentThread(), exception);

            Logger.error("Exception occurred while processing write-down-number-of-guilds command!");
        }
    }
}
