package dev.mayuna.lostarkbot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.mayuna.lostarkbot.util.StringUtils;
import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void testReplaceTwitterElements() {
        assertEquals("test reply [#sus](https://twitter.com/search?q=%23sus) and user tag [@mayuna](https://twitter.com/mayuna)", StringUtils.replaceTwitterElements("test reply #sus and user tag @mayuna"));
        assertEquals("[#hash](https://twitter.com/search?q=%23hash)#tag", StringUtils.replaceTwitterElements("#hash#tag"));
        assertEquals("[@tag](https://twitter.com/tag)#tag", StringUtils.replaceTwitterElements("@tag#tag"));
        assertEquals("[@tag](https://twitter.com/tag) [#hashtag](https://twitter.com/search?q=%23hashtag)", StringUtils.replaceTwitterElements("@tag #hashtag"));
    }
}
