package jss.notfine.mixins.early.minecraft.gleam;

import jss.notfine.core.NotFineSettings;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ItemRenderer.class)
public abstract class MixinItemRenderer {

    @Redirect(
        method = "renderItem(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;ILnet/minecraftforge/client/IItemRenderer$ItemRenderType;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;hasEffect(I)Z"
        ),
        remap = false
    )
    private boolean toggleGleam(ItemStack stack, int pass) {
        return NotFineSettings.Settings.MODE_GLEAM_WORLD.isValueBase() && stack.hasEffect(pass);
    }

}
