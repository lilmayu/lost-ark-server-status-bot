package dev.mayuna.lostarkbot.objects.features;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.data.GuildDataManager;
import dev.mayuna.lostarkbot.managers.LanguageManager;
import dev.mayuna.lostarkbot.managers.PersistentServerCacheManager;
import dev.mayuna.lostarkbot.managers.ServerDashboardManager;
import dev.mayuna.lostarkbot.util.*;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.lostarkfetcher.objects.api.LostArkServer;
import dev.mayuna.lostarkfetcher.objects.api.LostArkServers;
import dev.mayuna.lostarkfetcher.objects.api.other.LostArkRegion;
import dev.mayuna.mayusjdautils.exceptions.NonDiscordException;
import dev.mayuna.mayusjdautils.managed.ManagedGuildMessage;
import dev.mayuna.mayusjdautils.util.CallbackResult;
import dev.mayuna.mayusjdautils.util.DiscordUtils;
import dev.mayuna.mayusjdautils.util.RestActionMethod;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.util.*;
import java.util.concurrent.CompletableFuture;

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

    /**
     * Processes backwards compatibility stuff
     */
    public void processBackwardsCompatibility() {
        if (hiddenRegions.contains("WEST_NORTH_AMERICA")) {
            hiddenRegions.remove("WEST_NORTH_AMERICA");
            hiddenRegions.add("NORTH_AMERICA_WEST");
        }

        if (hiddenRegions.contains("EAST_NORTH_AMERICA")) {
            hiddenRegions.remove("EAST_NORTH_AMERICA");
            hiddenRegions.add("NORTH_AMERICA_EAST");
        }

        if (hiddenRegions.contains("CENTRAL_EUROPE")) {
            hiddenRegions.remove("CENTRAL_EUROPE");
            hiddenRegions.add("EUROPE_CENTRAL");
        }
    }

    /**
     * Returns {@link ManagedGuildMessage#getName()}
     *
     * @return Returns {@link ManagedGuildMessage#getName()}
     */
    public String getName() {
        return managedGuildMessage.getName();
    }

    /**
     * Saves current Guild Data which holds this dashboard
     */
    public void save() {
        GuildData guildData = GuildDataManager.getOrCreateGuildData(managedGuildMessage.getGuild());
        guildData.save();
    }

    /**
     * Adds server to favorites
     *
     * @param serverName Server name
     *
     * @return True if added, false if not
     */
    public boolean addToFavorites(String serverName) {
        LostArkServer lostArkServer = PersistentServerCacheManager.getServerByName(serverName);

        if (lostArkServer == null) {
            return false;
        }

        serverName = lostArkServer.getName();

        if (!favoriteServers.contains(serverName)) {
            favoriteServers.add(serverName);
            return true;
        }

        return false;
    }

    /**
     * Removes server from favorite section
     *
     * @param serverName Server name, can be non-existing
     *
     * @return True if the favoriteServers list was changed
     */
    public boolean removeFromFavorites(String serverName) {
        return favoriteServers.remove(serverName);
    }

    /**
     * Adds region into hidden ones
     *
     * @param region Region name, can be pretty (Europe Central) or enum-like (EUROPE_CENTRAL). Enum-like will be saved into hiddenRegions list.
     *
     * @return True if it was added, false otherwise (or if it does not exist)
     */
    public boolean addToHiddenRegions(String region) {
        LostArkRegion lostArkRegion = LostArkRegion.get(region);

        if (lostArkRegion == null) {
            return false;
        }

        region = lostArkRegion.name();

        if (!hiddenRegions.contains(region)) {
            hiddenRegions.add(region);

            return true;
        }

        return false;
    }

    /**
     * Removes region from hidden ones. Can be non-existing
     *
     * @param region Region name
     *
     * @return True if the hiddenRegions list was changed
     */
    public boolean removeFromHiddenRegions(String region) {
        return hiddenRegions.remove(region);
    }

    /**
     * Basically clears the hiddenRegions list
     */
    public void showAllRegions() {
        hiddenRegions.clear();
    }

    /**
     * Clears and adds all regions into the hiddenRegions list
     */
    public void hideAllRegions() {
        hiddenRegions.clear();
        for (LostArkRegion region : LostArkRegion.values()) {
            hiddenRegions.add(region.name());
        }
    }

    /**
     * Gets {@link LanguagePack}
     *
     * @return {@link LanguagePack}, if language cannot be found by the langCode field, default language will be used (from
     * {@link LanguageManager#getDefaultLanguage()}) and it will also automatically set the language to the default one
     */
    public LanguagePack getLanguage() {
        LanguagePack languagePack = LanguageManager.getLanguageByCode(langCode);

        if (languagePack == null) {
            languagePack = LanguageManager.getDefaultLanguage();
            langCode = languagePack.getLangCode();
        }

        return languagePack;
    }


    /**
     * Updates {@link ServerDashboard}
     *
     * @return {@link CompletableFuture}. In {@link CompletableFuture} is boolean, which determines, if update was successful (true) or failure
     * (false)
     */
    public CompletableFuture<Boolean> update() {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();

        ManagedGuildMessage managedGuildMessage = this.getManagedGuildMessage();

        try {
            if (PermissionUtils.isMissingPermissions(managedGuildMessage.getGuild().getSelfMember(), managedGuildMessage.getTextChannel())) {
                Logger.flow("Bot has missing permissions in channel " + managedGuildMessage.getTextChannel() + " (" + managedGuildMessage.getGuild() + ")! Dashboard will be not updated.");

                completableFuture.complete(false);
                return completableFuture;
            }
        } catch (PermissionException | ErrorResponseException exception) {
            Logger.get().flow(exception);
            Logger.warn("Failed to check for permissions for Server Dashboard " + this.getName() + "! (Guild: " + this.managedGuildMessage.getRawGuildID() + "; Text Channel: " + this.managedGuildMessage.getRawTextChannelID() + "; Message: " + this.managedGuildMessage.getRawMessageID() + " (Permission or Response Exception)");

            completableFuture.complete(false);
            return completableFuture;
        } catch (Exception exception) {
            Logger.throwing(exception);
            Logger.error("Failed to check for permissions for Server Dashboard " + this.getName() + "! (Guild: " + this.managedGuildMessage.getRawGuildID() + "; Text Channel: " + this.managedGuildMessage.getRawTextChannelID() + "; Message: " + this.managedGuildMessage.getRawMessageID() + " (Unknown Exception)");

            completableFuture.complete(false);
            return completableFuture;
        }

        SpecialRateLimiter.waitIfRateLimited();
        managedGuildMessage.updateEntries(Main.getMayuShardManager().get(), RestActionMethod.QUEUE, entriesSuccess -> {
            if (entriesSuccess != CallbackResult.NOTHING) {
                SpecialRateLimiter.madeRequest();
            }

            Message message = createMessage(ServerDashboardManager.getCurrentLostArkServersCache());

            try {
                SpecialRateLimiter.waitIfRateLimited();
                managedGuildMessage.sendOrEditMessage(message, RestActionMethod.QUEUE, success -> {
                    Logger.flow("Successfully updated dashboard " + this.getName() + " (Guild: " + this.managedGuildMessage.getRawGuildID() + "; Text Channel: " + this.managedGuildMessage.getRawTextChannelID() + "; Message: " + this.managedGuildMessage.getRawMessageID() + ") with result " + success);

                    completableFuture.complete(true);
                }, exception -> {
                    if (exception instanceof NonDiscordException) {
                        Logger.throwing(exception);
                        Logger.error("Dashboard " + this.getName() + " resulted in exception while updating! (Guild: " + this.managedGuildMessage.getRawGuildID() + "; Text Channel: " + this.managedGuildMessage.getRawTextChannelID() + "; Message: " + this.managedGuildMessage.getRawMessageID() + " (NonDiscordException)");
                    } else {
                        Logger.get().flow(exception);
                        Logger.warn("Dashboard " + this.getName() + " resulted in exception while updating! (Guild: " + this.managedGuildMessage.getRawGuildID() + "; Text Channel: " + this.managedGuildMessage.getRawTextChannelID() + "; Message: " + this.managedGuildMessage.getRawMessageID() + " (Permission or Response Exception)");
                    }

                    completableFuture.complete(false);
                });
                SpecialRateLimiter.madeRequest();
            } catch (PermissionException | ErrorResponseException exception) {
                Logger.get().flow(exception);
                Logger.warn("Dashboard " + this.getName() + " resulted in exception while updating! (Guild: " + this.managedGuildMessage.getRawGuildID() + "; Text Channel: " + this.managedGuildMessage.getRawTextChannelID() + "; Message: " + this.managedGuildMessage.getRawMessageID() + " (Permission or Response Exception)");

                completableFuture.complete(false);
            } catch (Exception exception) {
                Logger.throwing(exception);
                Logger.error("Dashboard " + this.getName() + " resulted in exception while updating! (Guild: " + this.managedGuildMessage.getRawGuildID() + "; Text Channel: " + this.managedGuildMessage.getRawTextChannelID() + "; Message: " + this.managedGuildMessage.getRawMessageID() + " (Unknown Exception)");

                completableFuture.complete(false);
            }
        }, failure -> {
            if (failure instanceof PermissionException || failure instanceof ErrorResponseException) {
                Logger.get().flow(failure);
                Logger.warn("Failed to update entries for Server Dashboard " + this.getName() + " (Guild: " + this.managedGuildMessage.getRawGuildID() + "; Text Channel: " + this.managedGuildMessage.getRawTextChannelID() + "; Message: " + this.managedGuildMessage.getRawMessageID() + ")! (Probably user removed channel/kicked bot or bot does not have permissions)");
            } else {
                Logger.throwing(failure);

                Logger.error("Failed to update entries for Server Dashboard " + this.getName() + " (Guild: " + this.managedGuildMessage.getRawGuildID() + "; Text Channel: " + this.managedGuildMessage.getRawTextChannelID() + "; Message: " + this.managedGuildMessage.getRawMessageID() + ")! (Unknown Exception)");
            }

            completableFuture.complete(false);
        });

        Utils.waitByConfigValue(UpdateType.SERVER_DASHBOARD);

        return completableFuture;
    }

    public Message createMessage(LostArkServers lostArkServers) {
        if (lostArkServers == null) {
            lostArkServers = new LostArkServers(new ArrayList<>(1), "Error");
        }

        LanguagePack languagePack = getLanguage();

        EmbedBuilder embedBuilder = DiscordUtils.getDefaultEmbed();
        embedBuilder.setTitle(languagePack.getTitle());

        String onlinePlayers = ServerDashboardManager.getOnlinePlayersCache();
        String lastUpdated = lostArkServers.getLastUpdatedTime().replace("Last updated: ", "");
        
        String fullDescription = "";
        fullDescription += "`" + lastUpdated + "`\n";
        fullDescription += languagePack.getCurrentPlayers().replace("{players}", onlinePlayers) + "\n\n";
        fullDescription += Constants.ONLINE_EMOTE + " " + languagePack.getOnline() + " ";
        fullDescription += Constants.BUSY_EMOTE + " " + languagePack.getBusy() + " ";
        fullDescription += Constants.FULL_EMOTE + " " + languagePack.getFull() + " ";
        fullDescription += Constants.MAINTENANCE_EMOTE + " " + languagePack.getMaintenance();
        embedBuilder.setDescription(fullDescription);

        LinkedHashMap<String, String> regionFields = new LinkedHashMap<>();

        for (LostArkRegion region : LostArkRegion.values()) {
            if (this.getHiddenRegions().contains(region.name())) {
                continue;
            }

            String fieldName = languagePack.getTranslatedRegionName(region);
            StringBuilder fieldValue = new StringBuilder();

            for (LostArkServer lostArkServer : lostArkServers.getServersByRegion(region)) { // TODO: Pokud se nějaký server nenajde tady a bude v persistent cache, tak ho dát jako offline
                String toAppend = Utils.getServerLine(lostArkServer) + "\n";

                if (fieldValue.length() + toAppend.length() < 1024) {
                    fieldValue.append(toAppend);
                } else {
                    Logger.warn("Cannot fit line into Field for region " + region.name() + ": " + toAppend);
                    break;
                }
            }

            regionFields.put(fieldName, fieldValue.toString());
        }

        LinkedHashMap<String, String> sortedRegionFields = new LinkedHashMap<>();
        regionFields.entrySet()
                    .stream()
                    .sorted(Comparator.comparing(entry -> entry.getValue().length() * -1)) // * -1 for reversed sorting
                    .forEachOrdered(x -> sortedRegionFields.put(x.getKey(), x.getValue()));

        int counter = 0;
        for (Map.Entry<String, String> entry : sortedRegionFields.entrySet()) {
            String fieldValue = entry.getValue();

            if (fieldValue.isEmpty()) {
                embedBuilder.addField(entry.getKey(), languagePack.getNoServers(), true);
            } else {
                embedBuilder.addField(entry.getKey(), fieldValue, true);
            }

            if (counter == 1 && regionFields.size() > 2) {
                embedBuilder.addBlankField(false);
                counter = -1;
            }

            counter++;
        }

        if (!this.getFavoriteServers().isEmpty()) { // Favorite servers
            String fieldValue = "";

            for (String serverName : this.getFavoriteServers()) {
                LostArkServer lostArkServer = PersistentServerCacheManager.getServerByName(serverName);

                if (lostArkServer != null) {
                    String toAppend = Utils.getServerLine(lostArkServer);

                    if (fieldValue.length() + toAppend.length() < 1024) {
                        fieldValue += toAppend;
                    } else {
                        Logger.warn("Cannot fit line into Field for Favorite: " + toAppend);
                        break;
                    }
                }
            }

            embedBuilder.addField(languagePack.getFavorite(), fieldValue, false);
        }

        embedBuilder.setFooter(languagePack.getUpdateFooter());

        return new MessageBuilder().setEmbeds(embedBuilder.build()).build();
    }
}
