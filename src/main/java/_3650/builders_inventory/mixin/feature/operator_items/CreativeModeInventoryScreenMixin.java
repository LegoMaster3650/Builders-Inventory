package _3650.builders_inventory.mixin.feature.operator_items;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import _3650.builders_inventory.config.Config;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.entity.player.Player;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin {
	
	@Redirect(method = "hasPermissions", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;canUseGameMasterBlocks()Z"))
	private boolean builders_inventory_operatoritems_override(Player player) {
		return Config.instance().operator_items_force ? player.getAbilities().instabuild : player.canUseGameMasterBlocks();
	}
	
}
