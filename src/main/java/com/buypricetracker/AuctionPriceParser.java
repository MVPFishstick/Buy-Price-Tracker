package com.buypricetracker;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.OptionalLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AuctionPriceParser {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("([0-9][0-9,]*)");

    private AuctionPriceParser() {
    }

    public static OptionalLong tryExtractPrice(ItemStack stack) {
        NbtCompound root = stack.getNbt();
        if (root == null || !root.contains("display", NbtElement.COMPOUND_TYPE)) {
            return OptionalLong.empty();
        }

        NbtCompound display = root.getCompound("display");
        if (!display.contains("Lore", NbtElement.LIST_TYPE)) {
            return OptionalLong.empty();
        }

        NbtList lore = display.getList("Lore", NbtElement.STRING_TYPE);
        for (int i = 0; i < lore.size(); i++) {
            String line = lore.getString(i).toLowerCase();
            if (!line.contains("price") && !line.contains("buy") && !line.contains("coin")) {
                continue;
            }

            Matcher matcher = NUMBER_PATTERN.matcher(line);
            if (!matcher.find()) {
                continue;
            }

            String digits = matcher.group(1).replace(",", "");
            try {
                return OptionalLong.of(Long.parseLong(digits));
            } catch (NumberFormatException ignored) {
                // Continue scanning if parsing fails.
            }
        }

        return OptionalLong.empty();
    }
}
