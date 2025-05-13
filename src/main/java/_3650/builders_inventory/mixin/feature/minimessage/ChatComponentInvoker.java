package _3650.builders_inventory.mixin.feature.minimessage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.components.ChatComponent;

@Mixin(ChatComponent.class)
public interface ChatComponentInvoker {
	
	@Invoker("getLineHeight")
	public int callGetLineHeight();
	
}
