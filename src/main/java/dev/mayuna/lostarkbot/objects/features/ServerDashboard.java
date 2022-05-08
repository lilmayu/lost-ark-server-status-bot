package dev.mayuna.lostarkbot.objects.features;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.data.GuildDataManager;
import dev.mayuna.lostarkbot.managers.LanguageManager;
import dev.mayuna.lostarkbot.managers.ServerDashboardManager;
import dev.mayuna.lostarkbot.objects.other.LostArkRegion;
import dev.mayuna.lostarkbot.util.*;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayusjdautils.exceptions.NonDiscordException;
import dev.mayuna.mayusjdautils.managed.ManagedGuildMessage;
import dev.mayuna.mayusjdautils.util.RestActionMethod;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.util.ArrayList;
import java.util.List;

public class ServerDashboard {

    private @Getter @Expose @SerializedName("managedMessage") ManagedGuildMessage managedGuildMessage;

    private @Getter @Setter @Expose String langCode = "en";
    private @Getter @Expose List<String> hiddenRegions = new ArrayList<>();
    private @Getter @Expose List<String> favoriteServers = new ArrayList<>();

    public ServerDashboard() {
    }

    public ServerDashboard(ManagedGuildMessage managedGuildMessage) {
        this.managedGuildMessage = managedGuildMessage;
    }

    public String getName() {
        return managedGuildMessage.getName();
    }

    public void save() {
        GuildData guildData = GuildDataManager.getOrCreateGuildData(managedGuildMessage.getGuild());
        guildData.save();
    }

    public boolean addToFavorites(String serverName) {
        serverName = Utils.getCorrectServerName(serverName);

        if (serverName == null) {
            return false;
        }

        if (!favoriteServers.contains(serverName)) {
            favoriteServers.add(serverName);

            return true;
        }

        return false;
    }

    public boolean removeFromFavorites(String serverName) {
        String correctServerName = Utils.getCorrectServerName(serverName);

        if (correctServerName != null) {
            if (favoriteServers.remove(serverName)) {
                return true;
            }
        }

        return favoriteServers.remove(serverName);
    }

    public boolean addToHiddenRegions(String region) {
        region = LostArkRegion.getCorrect(region);

        if (region == null) {
            return false;
        }

        if (!hiddenRegions.contains(region)) {
            hiddenRegions.add(region);

            return true;
        }

        return false;
    }

    public boolean removeFromHiddenRegions(String region) {
        String correctRegionName = LostArkRegion.getCorrect(region);

        if (correctRegionName != null) {
            if (hiddenRegions.remove(correctRegionName)) {
                return true;
            }
        }

        return hiddenRegions.remove(region);
    }

    public void showAllRegions() {
        hiddenRegions.clear();
    }

    public void hideAllRegions() {
        hiddenRegions.clear();
        for (LostArkRegion region : LostArkRegion.values()) {
            hiddenRegions.add(region.name());
        }
    }

    public LanguagePack getLanguage() {
        LanguagePack languagePack = LanguageManager.getLanguageByCode(langCode);

        if (languagePack == null) {
            languagePack = LanguageManager.getDefaultLanguage();
            langCode = languagePack.getLangCode();;
        }

        return languagePack;
    }


    /**
     * Updates {@link ServerDashboard}
     *
     * @return {@link Waiter <Boolean>}. In {@link Waiter#getObject()} is boolean, which determines, if update was successful (true) or failure (false)
     */
    public Waiter<Boolean> update() {
        Waiter<Boolean> waiter = new Waiter<>(false);

        ManagedGuildMessage managedGuildMessage = this.getManagedGuildMessage();

        try {
            if (PermissionUtils.isMissingPermissions(managedGuildMessage.getGuild().getSelfMember(), managedGuildMessage.getTextChannel())) {
                Logger.flow("Bot has missing permissions in channel " + managedGuildMessage.getTextChannel() + " (" + managedGuildMessage.getGuild() + ")! Dashboard will be not updated.");

                waiter.setObject(false);
                waiter.proceed();
                return waiter;
            }
        } catch (PermissionException | ErrorResponseException exception) {
            Logger.get().flow(exception);
            Logger.warn("Failed to check for permissions for Server Dashboard " + this.getName() + "! (Guild: " + this.managedGuildMessage.getRawGuildID() + "; Text Channel: " + this.managedGuildMessage.getRawTextChannelID() + "; Message: " + this.managedGuildMessage.getRawMessageID() + " (Permission or Response Exception)");

            waiter.setObject(false);
            waiter.proceed();
            return waiter;
        } catch (Exception exception) {
            Logger.throwing(exception);
            Logger.error("Failed to check for permissions for Server Dashboard " + this.getName() + "! (Guild: " + this.managedGuildMessage.getRawGuildID() + "; Text Channel: " + this.managedGuildMessage.getRawTextChannelID() + "; Message: " + this.managedGuildMessage.getRawMessageID() + " (Unknown Exception)");

            waiter.setObject(false);
            waiter.proceed();
            return waiter;
        }

        managedGuildMessage.updateEntries(Main.getMayuShardManager().get(), RestActionMethod.QUEUE, entriesSuccess -> {
            Message message = new MessageBuilder().setEmbeds(EmbedUtils.createEmbed(this, ServerDashboardManager.getLostArkServersCache()).build()).build();

            try {
                managedGuildMessage.sendOrEditMessage(message, RestActionMethod.QUEUE, success -> {
                    Logger.flow("Successfully updated dashboard " + this.getName() + " (Guild: " + this.managedGuildMessage.getRawGuildID() + "; Text Channel: " + this.managedGuildMessage.getRawTextChannelID() + "; Message: " + this.managedGuildMessage.getRawMessageID() + ") with result " + success);

                    waiter.setObject(true);
                    waiter.proceed();
                }, exception -> {
                    if (exception instanceof NonDiscordException) {
                        Logger.throwing(exception);
                        Logger.error("Dashboard " + this.getName() + " resulted in exception while updating! (Guild: " + this.managedGuildMessage.getRawGuildID() + "; Text Channel: " + this.managedGuildMessage.getRawTextChannelID() + "; Message: " + this.managedGuildMessage.getRawMessageID() + " (NonDiscordException)");
                    } else {
                        Logger.get().flow(exception);
                        Logger.warn("Dashboard " + this.getName() + " resulted in exception while updating! (Guild: " + this.managedGuildMessage.getRawGuildID() + "; Text Channel: " + this.managedGuildMessage.getRawTextChannelID() + "; Message: " + this.managedGuildMessage.getRawMessageID() + " (Permission or Response Exception)");
                    }

                    waiter.setObject(false);
                    waiter.proceed();
                });
            } catch (PermissionException | ErrorResponseException exception) {
                Logger.get().flow(exception);
                Logger.warn("Dashboard " + this.getName() + " resulted in exception while updating! (Guild: " + this.managedGuildMessage.getRawGuildID() + "; Text Channel: " + this.managedGuildMessage.getRawTextChannelID() + "; Message: " + this.managedGuildMessage.getRawMessageID() + " (Permission or Response Exception)");

                waiter.setObject(false);
                waiter.proceed();
            } catch (Exception exception) {
                Logger.throwing(exception);
                Logger.error("Dashboard " + this.getName() + " resulted in exception while updating! (Guild: " + this.managedGuildMessage.getRawGuildID() + "; Text Channel: " + this.managedGuildMessage.getRawTextChannelID() + "; Message: " + this.managedGuildMessage.getRawMessageID() + " (Unknown Exception)");

                waiter.setObject(false);
                waiter.proceed();
            }
        }, failure -> {
            if (failure instanceof PermissionException || failure instanceof ErrorResponseException) {
                Logger.get().flow(failure);
                Logger.warn("Failed to update entries for Server Dashboard " + this.getName() + " (Guild: " + this.managedGuildMessage.getRawGuildID() + "; Text Channel: " + this.managedGuildMessage.getRawTextChannelID() + "; Message: " + this.managedGuildMessage.getRawMessageID() + ")! (Probably user removed channel/kicked bot or bot does not have permissions)");
            } else {
                Logger.throwing(failure);

                Logger.error("Failed to update entries for Server Dashboard " + this.getName() + " (Guild: " + this.managedGuildMessage.getRawGuildID() + "; Text Channel: " + this.managedGuildMessage.getRawTextChannelID() + "; Message: " + this.managedGuildMessage.getRawMessageID() + ")! (Unknown Exception)");
            }

            waiter.setObject(false);
            waiter.proceed();
        });

        Utils.waitByConfigValue(UpdateType.SERVER_DASHBOARD);
        return waiter;
    }
}
