package _3650.builders_inventory.mixin.feature.extended_inventory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import _3650.builders_inventory.feature.extended_inventory.ExtendedInventory;
import net.minecraft.client.multiplayer.ClientPacketListener;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
	
	@Inject(method = "clearLevel", at = @At("HEAD"))
	private void builders_inventory_clearLevel(CallbackInfo ci) {
		ExtendedInventory.onQuitWorld((ClientPacketListener)(Object)this);
	}
	
}
