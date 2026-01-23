package _3650.builders_inventory.mixin.feature.minimessage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.components.EditBox;

@Mixin(EditBox.class)
public interface EditBoxAccessor {
	
	@Accessor("canLoseFocus")
	public boolean getCanLoseFocus();
	
	@Accessor("displayPos")
	public int getDisplayPos();
	
	@Accessor("textX")
	public int getTextX();
	
	@Accessor("textY")
	public int getTextY();
	
}
