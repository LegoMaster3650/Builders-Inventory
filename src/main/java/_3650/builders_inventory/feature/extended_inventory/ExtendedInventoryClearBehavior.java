package _3650.builders_inventory.feature.extended_inventory;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.network.chat.Component;

public enum ExtendedInventoryClearBehavior implements NameableEnum {
	NONE(false, false),
	CLEAR_PLAYER(true, false),
	CLEAR_EXTENDED(false, true),
	CLEAR_ALL(true, true),
	;
	
	public final boolean player;
	public final boolean extended;
	
	private ExtendedInventoryClearBehavior(boolean player, boolean extended) {
		this.player = player;
		this.extended = extended;
	}
	
	public final String key = "enum.builders_inventory.extended_inventory.clear_behavior." + this.name().toLowerCase();
	@Override
	public Component getDisplayName() {
		return Component.translatable(key);
	}
}
