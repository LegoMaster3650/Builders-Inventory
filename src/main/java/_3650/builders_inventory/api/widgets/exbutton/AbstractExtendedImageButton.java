package _3650.builders_inventory.api.widgets.exbutton;

import java.util.List;
import java.util.function.Supplier;

import com.mojang.blaze3d.platform.cursor.CursorTypes;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;

public abstract class AbstractExtendedImageButton extends AbstractButton {
	
	public final Supplier<List<Component>> disabledTooltip;
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
	
	public boolean renderTooltip(Font font, GuiGraphics gui, int mouseX, int mouseY) {
		final var tooltip = this.tooltip();
		if (this.isActive() && this.isHoveredOrFocused() && !tooltip.isEmpty()) {
			gui.setComponentTooltipForNextFrame(font,
					tooltip,
					this.isHovered() ? mouseX : this.getCenterX(),
					this.isHovered() ? mouseY : this.getCenterY());
			return true;
		} else {
			final var disabledTooltip = this.disabledTooltip.get();
			if (this.visible && !this.active && this.isHoveredOrFocused() && !disabledTooltip.isEmpty()) {
				gui.setComponentTooltipForNextFrame(font,
						disabledTooltip,
						this.isHovered() ? mouseX : this.getCenterX(),
						this.isHovered() ? mouseY : this.getCenterY());
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		this.defaultButtonNarrationText(narrationElementOutput);
	}
	
	@Override
	protected void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		var sprite = this.sprites().get(this.isActive(), this.isHoveredOrFocused());
		gui.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, this.getX(), this.getY(), this.width, this.height);
		if (this.isHovered()) {
			gui.requestCursor(this.isActive() ? CursorTypes.POINTING_HAND : CursorTypes.NOT_ALLOWED);
		}
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
