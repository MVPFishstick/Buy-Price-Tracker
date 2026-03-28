package com.buypricetracker;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public final class HypixelAuctionClient {
    private static final URI ENDED_AUCTIONS_URI = URI.create("https://api.hypixel.net/v2/skyblock/auctions_ended");
    private static final long REFRESH_EVERY_MS = 30_000L;
    private static final long MATCH_WINDOW_MS = 15 * 60_000L;

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();
    private final List<AuctionRecord> recentMine = new CopyOnWriteArrayList<>();

    private volatile long lastRefresh = 0L;
    private volatile boolean fetchInFlight = false;

    public void maybeRefresh(UUID playerUuid) {
        if (playerUuid == null || fetchInFlight) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastRefresh < REFRESH_EVERY_MS) {
            return;
        }

        fetchInFlight = true;
        HttpRequest request = HttpRequest.newBuilder(ENDED_AUCTIONS_URI)
            .timeout(Duration.ofSeconds(5))
            .GET()
            .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(HttpResponse::body)
            .thenAccept(body -> this.recentMineSnapshot(body, playerUuid))
            .exceptionally(err -> null)
            .whenComplete((ok, err) -> {
                lastRefresh = System.currentTimeMillis();
                fetchInFlight = false;
            });
    }

    private void recentMineSnapshot(String body, UUID playerUuid) {
        JsonObject root = JsonParser.parseString(body).getAsJsonObject();
        if (!root.has("success") || !root.get("success").getAsBoolean()) {
            return;
        }

        JsonArray auctions = root.getAsJsonArray("auctions");
        if (auctions == null) {
            return;
        }

        String me = playerUuid.toString().replace("-", "").toLowerCase(Locale.ROOT);
        List<AuctionRecord> parsed = new ArrayList<>();

        for (JsonElement element : auctions) {
            JsonObject auction = element.getAsJsonObject();
            if (!auction.has("buyer") || !me.equalsIgnoreCase(auction.get("buyer").getAsString())) {
                continue;
            }

            String itemName = auction.has("item_name") ? auction.get("item_name").getAsString() : "";
            long price = auction.has("price") ? auction.get("price").getAsLong() : 0L;
            long timestamp = auction.has("timestamp") ? auction.get("timestamp").getAsLong() : 0L;
            if (price <= 0 || itemName.isBlank()) {
                continue;
            }

            parsed.add(new AuctionRecord(normalize(itemName), price, timestamp));
        }

        parsed.sort(Comparator.comparingLong(AuctionRecord::timestamp).reversed());
        recentMine.clear();
        recentMine.addAll(parsed);
    }

    public Optional<AuctionRecord> findMatch(String itemName) {
        if (itemName == null || itemName.isBlank()) {
            return Optional.empty();
        }

        String normalized = normalize(itemName);
        long now = System.currentTimeMillis();

        return recentMine.stream()
            .filter(r -> r.normalizedItemName().equals(normalized))
            .filter(r -> r.timestamp() > 0 && Math.abs(now - r.timestamp()) <= MATCH_WINDOW_MS)
            .findFirst();
    }

    private static String normalize(String input) {
        return input.toLowerCase(Locale.ROOT).replaceAll("§.", "").replaceAll("[^a-z0-9 ]", "").trim().replaceAll("\\s+", " ");
    }

    public record AuctionRecord(String normalizedItemName, long price, long timestamp) {
    }
}
