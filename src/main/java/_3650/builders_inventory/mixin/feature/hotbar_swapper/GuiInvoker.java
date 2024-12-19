package _3650.builders_inventory.mixin.feature.hotbar_swapper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mixin(Gui.class)
public interface GuiInvoker {
	
	@Accessor("toolHighlightTimer")
	public void setToolHighlightTimer(int toolHighlightTimer);
	
	@Invoker("renderSlot")
	public void callRenderSlot(GuiGraphics guiGraphics, int x, int y, DeltaTracker deltaTick, Player player, ItemStack stack, int seed);
	
}
