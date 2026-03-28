package com.buypricetracker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

import java.util.UUID;

public class BuyPriceTrackerClient implements ClientModInitializer {
    private static final HypixelAuctionClient HYPIXEL_AUCTIONS = new HypixelAuctionClient();

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {
        if (client.player == null || client.getNetworkHandler() == null || client.getCurrentServerEntry() == null) {
            return;
        }

        String address = client.getCurrentServerEntry().address;
        if (address == null || !address.toLowerCase().contains("hypixel")) {
            return;
        }

        UUID uuid = client.player.getUuid();
        HYPIXEL_AUCTIONS.maybeRefresh(uuid);
    }

    public static HypixelAuctionClient auctions() {
        return HYPIXEL_AUCTIONS;
    }
}
