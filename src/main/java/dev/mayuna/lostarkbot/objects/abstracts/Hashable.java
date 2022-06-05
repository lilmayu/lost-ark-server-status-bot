package dev.mayuna.lostarkbot.objects.abstracts;

import dev.mayuna.lostarkbot.util.HashUtils;
import dev.mayuna.lostarkfetcher.objects.api.LostArkForum;
import dev.mayuna.lostarkfetcher.objects.api.LostArkNews;

public interface Hashable {

    String hash();

    static Hashable create(LostArkNews lostArkNews) {
        return () -> HashUtils.hashMD5(lostArkNews.getTitle() + "_" + lostArkNews.getPublishDate());
    }

    static Hashable create(LostArkForum.TopicList.Topic lostArkForumTopic) {
        return () -> HashUtils.hashMD5(lostArkForumTopic.getTitle() + "_" + lostArkForumTopic.getCreatedAt());
    }
}
