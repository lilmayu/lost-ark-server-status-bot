package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.api.ApiRestAction;
import dev.mayuna.lostarkbot.api.unofficial.Forums;
import dev.mayuna.lostarkbot.api.unofficial.News;
import dev.mayuna.lostarkbot.api.unofficial.UnofficialLostArkApi;
import dev.mayuna.lostarkbot.api.unofficial.objects.ForumsCategory;
import dev.mayuna.lostarkbot.api.unofficial.objects.NewsCategory;
import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.util.logging.Logger;

public class ApiConsoleCommand extends AbstractConsoleCommand {

    public ApiConsoleCommand() {
        this.name = "api";
    }

    @Override
    public void execute(String arguments) {
        String[] args = arguments.split(" ");

        if (args.length < 2) {
            Logger.error("Invalid syntax! Syntax: api <news|forums> <<updates|events|release-notes|general>|<Maintenance|Downtime>>");
            return;
        }


        switch (args[0]) {
            case "news" -> {
                NewsCategory newsCategory = NewsCategory.fromString(args[1]);

                if (newsCategory == null) {
                    Logger.error("Invalid category! Categories: updates|events|release-notes|general");
                    return;
                }

                Logger.info("Sending request...");

                ApiRestAction<News> apiRestAction = new UnofficialLostArkApi().fetchNews(newsCategory);
                apiRestAction.onHttpError(httpError -> {
                    Logger.warn("HTTPError: " + httpError.getCode());
                    httpError.getException().printStackTrace();
                });
                apiRestAction.onApiError(apiError -> {
                    Logger.warn("APIError: " + apiError.getError());
                });
                apiRestAction.onSuccess(((jsonObject, news) -> {
                    Logger.debug("Answered json: ");
                    System.out.println(jsonObject.toString());
                    Logger.success("Answer: " + news.toString());
                }));

                apiRestAction.execute();
            }
            case "forums" -> {
                ForumsCategory forumsCategory = ForumsCategory.fromString(args[1]);

                if (forumsCategory == null) {
                    Logger.error("Invalid category! Categories: Maintenance|Downtime");
                    return;
                }

                Logger.info("Sending request...");

                ApiRestAction<Forums> apiRestAction = new UnofficialLostArkApi().fetchForumPosts(forumsCategory);
                apiRestAction.onHttpError(httpError -> {
                    Logger.warn("HTTPError: " + httpError.getCode());
                    httpError.getException().printStackTrace();
                });
                apiRestAction.onApiError(apiError -> {
                    Logger.warn("APIError: " + apiError.getError());
                });
                apiRestAction.onSuccess(((jsonObject, news) -> {
                    Logger.debug("Answered json: ");
                    System.out.println(jsonObject.toString());
                    Logger.success("Answer: " + news.toString());
                }));

                apiRestAction.execute();
            }
            default -> {
                Logger.error("Invalid syntax! Syntax: api <news|forums> <<updates|events|release-notes|general>|<Maintenance|Downtime>>");
            }
        }
    }
}
