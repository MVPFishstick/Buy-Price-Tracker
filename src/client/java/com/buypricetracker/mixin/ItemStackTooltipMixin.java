package com.buypricetracker.mixin;

import com.buypricetracker.PriceData;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Mixin(ItemStack.class)
public class ItemStackTooltipMixin {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z", Locale.US)
        .withZone(ZoneId.systemDefault());

    @Inject(method = "getTooltip", at = @At("RETURN"))
    private void buypricetracker$appendBoughtPrice(CallbackInfoReturnable<List<Text>> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        PriceData.getPurchaseInfo(stack).ifPresent(info -> {
            String formatted = NumberFormat.getIntegerInstance(Locale.US).format(info.price());
            cir.getReturnValue().add(Text.literal("Bought for: " + formatted + " coins"));

            if (info.boughtAtMillis() > 0) {
                String when = TIME_FORMAT.format(Instant.ofEpochMilli(info.boughtAtMillis()));
                cir.getReturnValue().add(Text.literal("Bought at: " + when));
            }
        });
    }
}
