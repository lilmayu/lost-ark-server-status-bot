package dev.mayuna.lostarkbot.console.commands.generic;

public abstract class AbstractConsoleCommand {

    public String name;

    public abstract void execute(String arguments);

}
