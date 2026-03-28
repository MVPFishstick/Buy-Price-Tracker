package com.buypricetracker.mixin;

import com.buypricetracker.AuctionPriceParser;
import com.buypricetracker.BuyPriceTrackerClient;
import com.buypricetracker.HypixelAuctionClient;
import com.buypricetracker.PriceData;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(HandledScreen.class)
public class HandledScreenClickMixin {
    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"))
    private void buypricetracker$captureAuctionPrice(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (slot == null || !slot.hasStack()) {
            return;
        }

        String title = ((Screen) (Object) this).getTitle().getString().toLowerCase(Locale.ROOT);
        if (!title.contains("auction")) {
            return;
        }

        ItemStack stack = slot.getStack();
        if (PriceData.hasPrice(stack)) {
            return;
        }

        HypixelAuctionClient auctions = BuyPriceTrackerClient.auctions();
        auctions.findMatch(stack.getName().getString()).ifPresentOrElse(
            match -> PriceData.writePriceIfAbsent(stack, match.price(), match.timestamp()),
            () -> AuctionPriceParser.tryExtractPrice(stack)
                .ifPresent(price -> PriceData.writePriceIfAbsent(stack, price, System.currentTimeMillis()))
        );
    }
}
