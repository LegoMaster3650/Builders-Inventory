package _3650.builders_inventory.feature.hotbar_swapper;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.network.chat.Component;

public enum HotbarSwapperBehavior implements NameableEnum {
	PREFER_FULL,
	PREFER_COLUMN,
	SEPARATE,
	;
	public final String key = "enum.builders_inventory.hotbar_swapper.behavior." + this.name().toLowerCase();
	@Override
	public Component getDisplayName() {
		return Component.translatable(key);
	}
}
