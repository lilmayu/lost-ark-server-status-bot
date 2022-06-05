package dev.mayuna.lostarkbot.objects.features.lostark;

import dev.mayuna.lostarkbot.util.LostArkUtils;
import dev.mayuna.lostarkfetcher.objects.api.LostArkForum;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

public class WrappedForumCategoryName {

    private final @Getter int id;
    private final @Getter String name;
    private @Getter List<WrappedForumCategoryName> subcategories;

    public WrappedForumCategoryName(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public WrappedForumCategoryName(int id, LostArkForum lostArkForum) {
        this(id, LostArkUtils.getForumCategoryName(id, lostArkForum));
    }

    public String getVerboseName() {
        if (!name.contains("Forum ID")) {
            return name + " (" + id + ")";
        }

        return name;
    }

    public boolean isUnknown() {
        return name.contains("Unknown");
    }

    public void addSubcategory(int id, String name) {
        if (subcategories == null) {
            subcategories = new LinkedList<>();
        }

        synchronized (subcategories) {
            subcategories.add(new WrappedForumCategoryName(id, name));
        }
    }

    public void addSubcategory(int id, LostArkForum lostArkForum) {
        if (subcategories == null) {
            subcategories = new LinkedList<>();
        }

        synchronized (subcategories) {
            subcategories.add(new WrappedForumCategoryName(id, lostArkForum));
        }
    }

    public boolean hasSubcategories() {
        return subcategories != null;
    }
}
