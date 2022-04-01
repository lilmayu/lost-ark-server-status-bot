package dev.mayuna.lostarkbot.console.commands.generic;

public abstract class AbstractConsoleCommand {

    public String name;
    public String syntax;

    public abstract CommandResult execute(String arguments);

}
