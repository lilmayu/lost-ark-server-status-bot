package dev.mayuna.lostarkbot.console.commands;

import ch.qos.logback.classic.Level;
import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.util.logging.Logger;

public class LoggerConsoleCommand extends AbstractConsoleCommand {

    public LoggerConsoleCommand() {
        this.name = "logger";
    }

    @Override
    public void execute(String arguments) {
        String[] args = arguments.split(" ");

        if (args.length < 1) {
            Logger.error("Invalid syntax! logger <level>");
            return;
        }

        String logLevel = args[0];

        Logger.setLevel(logLevel);
    }
}
