package dev.mayuna.lostarkbot.objects.features.lostark;

import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.objects.abstracts.Hashable;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.lostarkfetcher.objects.api.LostArkForum;
import dev.mayuna.lostarkfetcher.objects.api.LostArkForumTopic;
import lombok.Getter;

public class WrappedForumTopic implements Hashable {

    private @Getter int forumCategoryId;
    private @Getter String forumName;
    private @Getter String topicTitle;
    private @Getter String topicBody;
    private @Getter long createdAt;
    private @Getter String url;

    public WrappedForumTopic(int forumCategoryId, String forumName, LostArkForum.TopicList.Topic topic, LostArkForumTopic forumTopic) {
        this.forumCategoryId = forumCategoryId;
        this.forumName = forumName;
        this.topicTitle = topic.getTitle();
        this.url = topic.getUrl();

        if (forumTopic.getPostStream().getPosts().length >= 1) {
            topicBody = forumTopic.getPostStream().getPosts()[0].getCookedClean();
            createdAt = Utils.toUnixTimestampForumTopic(forumTopic.getPostStream().getPosts()[0].getCreatedAt());
        }
    }

    public WrappedForumTopic(int forumCategoryId, String forumName, LostArkForum.TopicList.Topic topic) {
        this(forumCategoryId, forumName, topic, Main.getLostArkFetcher().fetchForumTopic(topic).execute().join());
    }

    public String getDiscordPostTime() {
        return "<t:" + createdAt + ">";
    }

    public String getDiscordPostTimeAgo() {
        return "<t:" + createdAt + ":R>";
    }

    public boolean isValid() {
        return forumCategoryId != 0 && topicTitle != null && topicBody != null && createdAt != 0 && url != null;
    }

    @Override
    public String hash() {
        return topicTitle + "_" + createdAt;
    }
}
