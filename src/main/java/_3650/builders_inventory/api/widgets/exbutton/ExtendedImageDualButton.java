package _3650.builders_inventory.api.widgets.exbutton;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;

public class ExtendedImageDualButton extends AbstractExtendedImageButton {
	
	private final SoundEvent clickSound1;
	private final SoundEvent clickSound2;
	private final WidgetSprites sprites1;
	private final WidgetSprites sprites2;
	private final OnPress onPress1;
	private final OnPress onPress2;
	private final List<Component> tooltip1;
	private final List<Component> tooltip2;
	
	public boolean secondMode = false;
	
	public ExtendedImageDualButton(int x, int y, int width, int height,
			WidgetSprites sprites1, OnPress onPress1,
			WidgetSprites sprites2, OnPress onPress2
			) {
		this(x, y, width, height,
				() -> List.of(),
				null, sprites1, onPress1, List.of(),
				null, sprites2, onPress2, List.of());
	}
	
	public ExtendedImageDualButton(int x, int y, int width, int height,
			SoundEvent clickSound1, WidgetSprites sprites1, OnPress onPress1,
			SoundEvent clickSound2, WidgetSprites sprites2, OnPress onPress2
			) {
		this(x, y, width, height,
				() -> List.of(),
				clickSound1, sprites1, onPress1, List.of(),
				clickSound2, sprites2, onPress2, List.of());
	}
	
	public ExtendedImageDualButton(int x, int y, int width, int height,
			WidgetSprites sprites1, OnPress onPress1, List<Component> tooltip1,
			WidgetSprites sprites2, OnPress onPress2, List<Component> tooltip2
			) {
		this(x, y, width, height,
				() -> List.of(),
				null, sprites1, onPress1, tooltip1,
				null, sprites2, onPress2, tooltip2);
	}
	
	public ExtendedImageDualButton(int x, int y, int width, int height,
			SoundEvent clickSound1, WidgetSprites sprites1, OnPress onPress1, List<Component> tooltip1,
			SoundEvent clickSound2, WidgetSprites sprites2, OnPress onPress2, List<Component> tooltip2
			) {
		this(x, y, width, height,
				() -> List.of(),
				clickSound1, sprites1, onPress1, tooltip1,
				clickSound2, sprites2, onPress2, tooltip2);
	}
	
	public ExtendedImageDualButton(int x, int y, int width, int height,
			Supplier<List<Component>> disabledTooltip,
			WidgetSprites sprites1, OnPress onPress1, List<Component> tooltip1,
			WidgetSprites sprites2, OnPress onPress2, List<Component> tooltip2
			) {
		this(x, y, width, height,
				disabledTooltip,
				null, sprites1, onPress1, tooltip1,
				null, sprites2, onPress2, tooltip2);
	}
	
	public ExtendedImageDualButton(int x, int y, int width, int height,
			Supplier<List<Component>> disabledTooltip,
			SoundEvent clickSound1, WidgetSprites sprites1, OnPress onPress1, List<Component> tooltip1,
			SoundEvent clickSound2, WidgetSprites sprites2, OnPress onPress2, List<Component> tooltip2) {
		super(x, y, width, height, disabledTooltip);
		this.clickSound1 = clickSound1;
		this.sprites1 = sprites1;
		this.onPress1 = onPress1;
		this.tooltip1 = tooltip1;
		this.clickSound2 = clickSound2;
		this.sprites2 = sprites2;
		this.onPress2 = onPress2;
		this.tooltip2 = tooltip2;
	}
	
	@Override
	public SoundEvent clickSound() {
		return this.secondMode ? this.clickSound2 : this.clickSound1;
	}
	
	@Override
	public WidgetSprites sprites() {
		return this.secondMode ? this.sprites2 : this.sprites1;
	}
	
	@Override
	public List<Component> tooltip() {
		return this.secondMode ? this.tooltip2 : this.tooltip1;
	}
	
	@Override
	public void onPress() {
		super.onPress();
		if (this.secondMode) this.onPress2.onPress(this);
		else this.onPress1.onPress(this);
	}
	
	@FunctionalInterface
	public static interface OnPress {
		public void onPress(ExtendedImageDualButton button);
	}
	
}
