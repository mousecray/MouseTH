package ru.mousecray.mouseth.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.IoSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.mousecray.mouseth.patch.RawJsonPatcher;

import java.io.InputStream;

@Mixin(FallbackResourceManager.class)
public class FallbackResourceManagerMixin {
    @Inject(method = "wrapForDebug", at = @At("RETURN"), cancellable = true)
    private static void onWrapForDebug(
            ResourceLocation location, PackResources packResources, IoSupplier<InputStream> streamSupplier,
            CallbackInfoReturnable<IoSupplier<InputStream>> cir
    ) {
        IoSupplier<InputStream> original = cir.getReturnValue();
        if (original != null) cir.setReturnValue(RawJsonPatcher.wrapSupplier(location, original));
    }
}