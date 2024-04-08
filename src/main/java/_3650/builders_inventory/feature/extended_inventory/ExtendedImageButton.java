package _3650.builders_inventory.feature.extended_inventory;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;

public class ExtendedImageButton extends ImageButton {
	
	private final SoundEvent clickSound;
	public final List<Component> tooltip;
	public final Supplier<List<Component>> disabledTooltip;
	
	private boolean resetFocus = false;
	private int centerX;
	private int centerY;
	
	public ExtendedImageButton(int x, int y, int width, int height, WidgetSprites sprites, Button.OnPress onPress) {
		this(x, y, width, height, sprites, onPress, null, List.of(), () -> List.of());
	}
	
	public ExtendedImageButton(int x, int y, int width, int height, WidgetSprites sprites, Button.OnPress onPress, SoundEvent clickSound) {
		this(x, y, width, height, sprites, onPress, clickSound, List.of(), () -> List.of());
	}
	
	public ExtendedImageButton(int x, int y, int width, int height, WidgetSprites sprites, Button.OnPress onPress, Component... tooltip) {
		this(x, y, width, height, sprites, onPress, null, List.of(tooltip), () -> List.of());
	}
	
	public ExtendedImageButton(int x, int y, int width, int height, WidgetSprites sprites, Button.OnPress onPress, SoundEvent clickSound, Component... tooltip) {
		this(x, y, width, height, sprites, onPress, clickSound, List.of(tooltip), () -> List.of());
	}
	
	public ExtendedImageButton(int x, int y, int width, int height, WidgetSprites sprites, Button.OnPress onPress, List<Component> tooltip) {
		this(x, y, width, height, sprites, onPress, null, tooltip, () -> List.of());
	}
	
	public ExtendedImageButton(int x, int y, int width, int height, WidgetSprites sprites, Button.OnPress onPress, List<Component> tooltip, Supplier<List<Component>> disabledTooltip) {
		this(x, y, width, height, sprites, onPress, null, tooltip, disabledTooltip);
	}
	
	public ExtendedImageButton(int x, int y, int width, int height, WidgetSprites sprites, Button.OnPress onPress, SoundEvent clickSound, List<Component> tooltip) {
		this(x, y, width, height, sprites, onPress, clickSound, tooltip, () -> List.of());
	}
	
	public ExtendedImageButton(int x, int y, int width, int height, WidgetSprites sprites, Button.OnPress onPress, SoundEvent clickSound, List<Component> tooltip, Supplier<List<Component>> disabledTooltip) {
		super(x, y, width, height, sprites, onPress);
		this.clickSound = clickSound;
		this.tooltip = tooltip;
		this.disabledTooltip = disabledTooltip;
		this.centerX = x + (width / 2);
		this.centerY = y + (height / 2);
	}
	
	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		if (this.resetFocus) {
			this.resetFocus = false;
			this.setFocused(false);
		}
		super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
	}
	
	@Override
	public void onPress() {
		this.resetFocus = true;
		super.onPress();
	}
	
	@Override
	public void playDownSound(SoundManager handler) {
		if (this.clickSound == null) super.playDownSound(handler);
		else handler.play(SimpleSoundInstance.forUI(this.clickSound, 1.0f));
	}
	
	@Override
	public void setX(int x) {
		super.setX(x);
		this.centerX = x + (this.width / 2);
	}
	
	@Override
	public void setY(int y) {
		super.setY(y);
		this.centerY = y + (this.height / 2);
	}
	
	public int getCenterX() {
		return this.centerX;
	}
	
	public int getCenterY() {
		return this.centerY;
	}
	
}
