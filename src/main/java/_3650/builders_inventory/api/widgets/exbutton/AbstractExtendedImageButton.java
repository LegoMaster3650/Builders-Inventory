package _3650.builders_inventory.api.widgets.exbutton;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;

public abstract class AbstractExtendedImageButton extends AbstractButton {
	
	public final Supplier<List<Component>> disabledTooltip;
	private boolean resetFocus = false;
	private int centerX;
	private int centerY;
	
	public AbstractExtendedImageButton(int x, int y, int width, int height) {
		this(x, y, width, height, List::of);
	}
	
	public AbstractExtendedImageButton(int x, int y, int width, int height, Supplier<List<Component>> disabledTooltip) {
		super(x, y, width, height, CommonComponents.EMPTY);
		this.disabledTooltip = disabledTooltip;
		this.centerX = x + (width / 2);
		this.centerY = y + (height / 2);
	}
	
	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		this.defaultButtonNarrationText(narrationElementOutput);
	}
	
	@Override
	protected void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		if (this.resetFocus) {
			this.resetFocus = false;
			this.setFocused(false);
		}
		var sprite = this.sprites().get(this.isActive(), this.isHoveredOrFocused());
		gui.blitSprite(sprite, this.getX(), this.getY(), this.width, this.height);
	}
	
	@Override
	public void onPress() {
		this.resetFocus = true;
	}
	
	@Override
	public void playDownSound(SoundManager handler) {
		final SoundEvent clickSound = this.clickSound();
		if (clickSound == null) super.playDownSound(handler);
		else handler.play(SimpleSoundInstance.forUI(clickSound, 1.0f));
	}
	
	public abstract SoundEvent clickSound();
	
	public abstract WidgetSprites sprites();
	
	public abstract List<Component> tooltip();
	
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
