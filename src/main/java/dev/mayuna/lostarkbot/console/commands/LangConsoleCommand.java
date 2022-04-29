package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.console.commands.generic.CommandResult;
import dev.mayuna.lostarkbot.managers.LanguageManager;
import dev.mayuna.lostarkbot.objects.features.LanguagePack;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayuslibrary.arguments.ArgumentParser;

public class LangConsoleCommand extends AbstractConsoleCommand {

    public LangConsoleCommand() {
        this.name = "lang";
        this.syntax = "<list|view <code>>";
    }

    @Override
    public CommandResult execute(String arguments) {
        ArgumentParser argumentParser = new ArgumentParser(arguments);

        if (argumentParser.hasArgumentAtIndex(0)) {
            switch (argumentParser.getArgumentAtIndex(0).getValue()) {
                case "list" -> {
                    Logger.info("=== Loaded Languages - " + LanguageManager.getLoadedLanguages().size() + " ===");
                    for (LanguagePack languagePack : LanguageManager.getLoadedLanguages()) {
                        Logger.info("[" + languagePack.getLangCode() + "]: " + languagePack.getLangName());
                    }
                    Logger.success("Listing done.");
                }
                case "view" -> {
                    if (!argumentParser.hasArgumentAtIndex(1)) {
                        return CommandResult.INCORRECT_SYNTAX;
                    }

                    String langCode = argumentParser.getArgumentAtIndex(1).getValue();
                    Logger.info("[" + langCode + "]: " + LanguageManager.getLanguageByCode(langCode));
                }
                default -> {
                    return CommandResult.INCORRECT_SYNTAX;
                }
            }

            return CommandResult.SUCCESS;
        }

        return CommandResult.INCORRECT_SYNTAX;
    }
}
