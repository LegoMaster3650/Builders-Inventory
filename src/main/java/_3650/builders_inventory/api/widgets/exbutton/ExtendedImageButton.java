package _3650.builders_inventory.api.widgets.exbutton;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;

public class ExtendedImageButton extends AbstractExtendedImageButton {
	
	private final SoundEvent clickSound;
	private final WidgetSprites sprites;
	private final OnPress onPress;
	private final List<Component> tooltip;
	
	public ExtendedImageButton(int x, int y, int width, int height,
			WidgetSprites sprites, OnPress onPress
			) {
		this(x, y, width, height,
				List::of,
				null, sprites, onPress, List.of());
	}
	
	public ExtendedImageButton(int x, int y, int width, int height,
			SoundEvent clickSound, WidgetSprites sprites, OnPress onPress
			) {
		this(x, y, width, height,
				List::of,
				clickSound, sprites, onPress, List.of());
	}
	
	public ExtendedImageButton(int x, int y, int width, int height,
			WidgetSprites sprites, OnPress onPress, Component... tooltip
			) {
		this(x, y, width, height,
				List::of,
				null, sprites, onPress, List.of(tooltip));
	}
	
	public ExtendedImageButton(int x, int y, int width, int height,
			SoundEvent clickSound, WidgetSprites sprites, OnPress onPress, Component... tooltip
			) {
		this(x, y, width, height,
				List::of,
				clickSound, sprites, onPress, List.of(tooltip));
	}
	
	public ExtendedImageButton(int x, int y, int width, int height,
			Supplier<List<Component>> disabledTooltip,
			WidgetSprites sprites, OnPress onPress, Component... tooltip
			) {
		this(x, y, width, height,
				disabledTooltip,
				null, sprites, onPress, List.of(tooltip));
	}
	
	public ExtendedImageButton(int x, int y, int width, int height,
			WidgetSprites sprites, OnPress onPress, List<Component> tooltip
			) {
		this(x, y, width, height,
				List::of,
				null, sprites, onPress, tooltip);
	}
	
	public ExtendedImageButton(int x, int y, int width, int height,
			SoundEvent clickSound, WidgetSprites sprites, OnPress onPress, List<Component> tooltip
			) {
		this(x, y, width, height,
				List::of,
				clickSound, sprites, onPress, tooltip);
	}
	
	public ExtendedImageButton(int x, int y, int width, int height,
			Supplier<List<Component>> disabledTooltip,
			WidgetSprites sprites, OnPress onPress, List<Component> tooltip
			) {
		this(x, y, width, height,
				disabledTooltip,
				null, sprites, onPress, tooltip);
	}
	
	public ExtendedImageButton(int x, int y, int width, int height,
			Supplier<List<Component>> disabledTooltip,
			SoundEvent clickSound, WidgetSprites sprites, OnPress onPress, List<Component> tooltip
			) {
		super(x, y, width, height, disabledTooltip);
		this.clickSound = clickSound;
		this.sprites = sprites;
		this.onPress = onPress;
		this.tooltip = tooltip;
	}
	
	@Override
	public SoundEvent clickSound() {
		return this.clickSound;
	}
	
	@Override
	public WidgetSprites sprites() {
		return this.sprites;
	}
	
	@Override
	public List<Component> tooltip() {
		return this.tooltip;
	}
	
	@Override
	public void onPress() {
		super.onPress();
		this.onPress.onPress(this);
	}
	
	@FunctionalInterface
	public static interface OnPress {
		public void onPress(ExtendedImageButton button);
	}
	
}
