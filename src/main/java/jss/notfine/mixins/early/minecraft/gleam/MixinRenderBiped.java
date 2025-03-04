package jss.notfine.mixins.early.minecraft.gleam;

import jss.notfine.core.NotFineSettings;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = RenderBiped.class)
public abstract class MixinRenderBiped {

    @Redirect(
        method = "shouldRenderPass(Lnet/minecraft/entity/EntityLiving;IF)I",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;isItemEnchanted()Z"
        )
    )
    private boolean toggleGleam(ItemStack stack) {
        return NotFineSettings.Settings.MODE_GLEAM_WORLD.isValueBase() && stack.isItemEnchanted();
    }

}
