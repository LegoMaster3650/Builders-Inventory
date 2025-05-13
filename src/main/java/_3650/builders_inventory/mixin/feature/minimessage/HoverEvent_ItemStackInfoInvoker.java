package _3650.builders_inventory.mixin.feature.minimessage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.item.Item;

@Mixin(HoverEvent.ItemStackInfo.class)
public interface HoverEvent_ItemStackInfoInvoker {
	
	@Invoker("<init>")
	public static HoverEvent.ItemStackInfo construct(Holder<Item> item, int count, DataComponentPatch components) {
		// dummy method body
		return null;
	}
	
}
