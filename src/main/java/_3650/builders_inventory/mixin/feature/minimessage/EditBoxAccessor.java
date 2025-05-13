package _3650.builders_inventory.mixin.feature.minimessage;

import java.util.function.BiFunction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.util.FormattedCharSequence;

@Mixin(EditBox.class)
public interface EditBoxAccessor {
	
	@Accessor("formatter")
	public BiFunction<String, Integer, FormattedCharSequence> getFormatter();
	
	@Accessor("canLoseFocus")
	public boolean getCanLoseFocus();
	
}
