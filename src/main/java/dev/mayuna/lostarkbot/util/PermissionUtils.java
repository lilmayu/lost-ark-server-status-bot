package dev.mayuna.lostarkbot.util;

import dev.mayuna.mayusjdautils.utils.MessageInfo;
import dev.mayuna.mayuslibrary.utils.ArrayUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionUtils {

    private static final Permission[] requiredPermissions = new Permission[]{
            Permission.MESSAGE_WRITE,
            Permission.MESSAGE_EMBED_LINKS,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_HISTORY,
            Permission.VIEW_CHANNEL
    };

    /**
     * Returns list of missing permissions
     *
     * @param member       Member, usually SelfMember
     * @param guildChannel Guild channel to check
     *
     * @return Empty if there are no missing permissions
     */
    public static List<Permission> getMissingPermissions(Member member, GuildChannel guildChannel) {
        List<Permission> missingPermissions = new ArrayList<>(Arrays.stream(requiredPermissions).toList());
        missingPermissions.removeAll(member.getPermissions(guildChannel));
        return missingPermissions;
    }

    public static boolean isMissingPermissions(Member member, GuildChannel guildChannel) {
        return !getMissingPermissions(member, guildChannel).isEmpty();
    }

    public static void sendMessageAboutMissingPermissions(InteractionHook interactionHook, List<Permission> missingPermissions) {
        String missingPermissionsString = "";

        for (Permission permission : missingPermissions) {
            missingPermissionsString += "**" + permission.getName() + "**";

            if (ArrayUtils.getLast(missingPermissions.toArray()) != permission) {
                missingPermissionsString += ", ";
            }
        }

        interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("Bot does not have required permission(s): " + missingPermissionsString).build()).queue();
    }

    public static boolean checkPermissionsAndSendIfMissing(TextChannel textChannel, InteractionHook interactionHook) {
        List<Permission> missingPermissions = getMissingPermissions(textChannel.getGuild().getSelfMember(), textChannel);

        if (missingPermissions.isEmpty()) {
            return false;
        }

        sendMessageAboutMissingPermissions(interactionHook, missingPermissions);
        return true;
    }
}
