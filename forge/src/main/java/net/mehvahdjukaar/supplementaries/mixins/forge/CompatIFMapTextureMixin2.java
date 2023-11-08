package net.mehvahdjukaar.supplementaries.mixins.forge;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.NativeImage;
import fabric.net.raphimc.immediatelyfast.feature.map_atlas_generation.MapAtlasTexture;
import fabric.net.raphimc.immediatelyfast.injection.mixins.map_atlas_generation.MixinMapRenderer_MapTexture;
import net.mehvahdjukaar.moonlight.api.misc.OptionalMixin;
import net.mehvahdjukaar.supplementaries.common.misc.ColoredMapHandler;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@OptionalMixin(value = "forge.net.raphimc.immediatelyfast.injection.mixins.map_atlas_generation.MixinMapRenderer_MapTexture")
@Mixin(value = MapRenderer.MapInstance.class, priority = 1500)
public class CompatIFMapTextureMixin2 {

    @Shadow
    private MapItemSavedData data;

    @TargetHandler(
            mixin = "forge.net.raphimc.immediatelyfast.injection.mixins.map_atlas_generation.MixinMapRenderer_MapTexture",
            name = "updateAtlasTexture"
    )
    @WrapOperation(method = "@MixinSquared:Handler",
            require = 0,
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/NativeImage;upload(IIIIIIIZZ)V"))
    public void supplementaries_IFupdateColoredTexture(NativeImage instance,
                                                       int level, int xOffset, int yOffset, int unpackSkipPixels, int unpackSkipRows, int width, int height, boolean mipmap, boolean autoClose,
                                                       Operation<Void> operation) {
        ColoredMapHandler.getColorData(this.data).processTexture(instance, xOffset, yOffset, this.data.colors);
        operation.call(instance, level, xOffset, yOffset, unpackSkipPixels, unpackSkipRows, width, height, true, autoClose);
    }

}