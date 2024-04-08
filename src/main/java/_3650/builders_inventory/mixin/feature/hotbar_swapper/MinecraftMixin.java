package _3650.builders_inventory.mixin.feature.hotbar_swapper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import _3650.builders_inventory.feature.hotbar_swapper.HotbarSwapper;
import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	
	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;handleKeybinds()V"))
	private void builders_inventory_hotbarswapper_hotbarKeys(CallbackInfo ci) {
		HotbarSwapper.preKeybinds((Minecraft)(Object)this);
	}
	
}
