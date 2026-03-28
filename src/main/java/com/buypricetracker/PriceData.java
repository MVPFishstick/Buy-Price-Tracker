package com.buypricetracker;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.Optional;

public final class PriceData {
    private static final String ROOT_KEY = "buypricetracker";
    private static final String PRICE_KEY = "bought_price";
    private static final String BOUGHT_AT_KEY = "bought_at";

    private PriceData() {
    }

    public static Optional<PurchaseInfo> getPurchaseInfo(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(ROOT_KEY, NbtElement.COMPOUND_TYPE)) {
            return Optional.empty();
        }

        NbtCompound root = nbt.getCompound(ROOT_KEY);
        if (!root.contains(PRICE_KEY, NbtElement.LONG_TYPE)) {
            return Optional.empty();
        }

        long price = root.getLong(PRICE_KEY);
        long boughtAt = root.contains(BOUGHT_AT_KEY, NbtElement.LONG_TYPE) ? root.getLong(BOUGHT_AT_KEY) : 0L;
        return Optional.of(new PurchaseInfo(price, boughtAt));
    }

    public static boolean hasPrice(ItemStack stack) {
        return getPurchaseInfo(stack).isPresent();
    }

    public static void writePriceIfAbsent(ItemStack stack, long price, long boughtAtMillis) {
        if (price <= 0 || hasPrice(stack)) {
            return;
        }

        NbtCompound root = stack.getOrCreateSubNbt(ROOT_KEY);
        root.putLong(PRICE_KEY, price);
        if (boughtAtMillis > 0) {
            root.putLong(BOUGHT_AT_KEY, boughtAtMillis);
        }
    }

    public record PurchaseInfo(long price, long boughtAtMillis) {
    }
}
