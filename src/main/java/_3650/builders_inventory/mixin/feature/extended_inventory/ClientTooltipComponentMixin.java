package _3650.builders_inventory.mixin.feature.extended_inventory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import _3650.builders_inventory.feature.extended_inventory.ExtendedInventoryOrganizeScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

@Mixin(ClientTooltipComponent.class)
public interface ClientTooltipComponentMixin {
	
	@Inject(method = "create(Lnet/minecraft/world/inventory/tooltip/TooltipComponent;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;", at = @At("HEAD"), cancellable = true)
	private static void builders_inventory_extendedinventory_tooltipComponent(TooltipComponent component, CallbackInfoReturnable<ClientTooltipComponent> cir) {
		if (component instanceof ExtendedInventoryOrganizeScreen.PageTooltipImage pass) cir.setReturnValue(pass);
	}
	
}
