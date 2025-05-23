package _3650.builders_inventory.api.widgets.slider;

import java.util.List;
import java.util.function.IntConsumer;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.client.InputType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class StepSliderWidget extends AbstractWidget {
	
	public static StepSliderWidget standard(
			SliderWidgetTheme theme,
			int x,
			int y,
			int z,
			int min,
			int max,
			int initialValue,
			Font font,
			Int2ObjectFunction<List<Component>> tooltipFormat,
			IntConsumer onChange
			) {
		return internalBuild(
				theme,
				x,y,z,
				min,max,
				initialValue,
				0,
				font,
				tooltipFormat,
				onChange,
				noop -> {},
				false
				);
	}
	
	public static StepSliderWidget standardMinSpacing(
			SliderWidgetTheme theme,
			int x,
			int y,
			int z,
			int min,
			int max,
			int initialValue,
			int minSegmentSpacing,
			Font font,
			Int2ObjectFunction<List<Component>> tooltipFormat,
			IntConsumer onChange
			) {
		return internalBuild(
				theme,
				x,y,z,
				min,max,
				initialValue,
				minSegmentSpacing,
				font,
				tooltipFormat,
				onChange,
				noop -> {},
				false
				);
	}
	
	public static StepSliderWidget cancel(
			SliderWidgetTheme theme,
			int x,
			int y,
			int z,
			int min,
			int max,
			int initialValue,
			Font font,
			Int2ObjectFunction<List<Component>> tooltipFormat,
			IntConsumer onChange,
			IntConsumer onCancel
			) {
		return internalBuild(
				theme,
				x,y,z,
				min,max,
				initialValue,
				0,
				font,
				tooltipFormat,
				onChange,
				onCancel,
				true
				);
	}
	
	public static StepSliderWidget cancelMinSpacing(
			SliderWidgetTheme theme,
			int x,
			int y,
			int z,
			int min,
			int max,
			int initialValue,
			int minSegmentSpacing,
			Font font,
			Int2ObjectFunction<List<Component>> tooltipFormat,
			IntConsumer onChange,
			IntConsumer onCancel
			) {
		return internalBuild(
				theme,
				x,y,z,
				min,max,
				initialValue,
				minSegmentSpacing,
				font,
				tooltipFormat,
				onChange,
				onCancel,
				true
				);
	}
	
	private static StepSliderWidget internalBuild(
			SliderWidgetTheme theme,
			int x,
			int y,
			int z,
			int min,
			int max,
			int initialValue,
			int minSegmentSpacing,
			Font font,
			Int2ObjectFunction<List<Component>> tooltipFormat,
			IntConsumer onChange,
			IntConsumer onCancel,
			boolean canCancel
			) {
		if (min >= max) throw new IllegalArgumentException("Slider minimum value " + min + " must be less than maximum value " + max);
		final int segments = max - min;
		final int step = Math.max(switch(segments) {
		case 1 -> 50; //  50 segment width
		case 2 -> 30; //  60 segment width
		case 3 -> 25; //  75 segment width
		case 4 -> 22; //  88 segment width
		case 5 -> 20; // 100 segment width
		case 6 -> 18; // 108 segment width
		case 7 -> 16; // 112 segment width
		default -> segments > 48 ? 2 : Math.round(116f / segments);
		}, minSegmentSpacing);
		final int innerWidth = step * segments;
		final int borderWidth = (theme.border + theme.horizontalPadding) * 2;
		final int rightPadding = 3 + (canCancel ? 13 + 2 * theme.cancelButtonPadding : 0); // +3 for R notch, +13 for cancel extension
		final int width = innerWidth + borderWidth + rightPadding;
		
		return new StepSliderWidget(
				theme,
				x,y,z,
				min,max,
				initialValue,
				font,
				tooltipFormat,
				onChange,
				onCancel,
				width,
				theme.height,
				canCancel,
				segments,
				step,
				innerWidth
				);
	}
	
	private final SliderWidgetTheme theme;
	private final int z;
	private final int min;
	private final int max;
	public final int initialValue;
	private final Font font;
	private final Int2ObjectFunction<List<Component>> tooltipFormat;
	private final IntConsumer onChange;
	private final IntConsumer onCancel;
	
	private final boolean canCancel;
	private final int range;
	private final int notchStep;
	private final int innerWidth;
	private final int minX;
	private final int maxX;
	private final int centerY;
	private final int halfBarHeight;
	
	public int value;
	private boolean dragging = false;
	private boolean focusDragging = false;
	
	private StepSliderWidget(
			SliderWidgetTheme theme,
			int x,
			int y,
			int z,
			int min,
			int max,
			int initialValue,
			Font font,
			Int2ObjectFunction<List<Component>> tooltipFormat,
			IntConsumer onChange,
			IntConsumer onCancel,
			int width,
			int height,
			boolean canCancel,
			int segments,
			int step,
			int innerWidth
			) {
		super(x, y, width, height, Component.empty());
		this.theme = theme;
		this.z = z;
		this.min = min;
		this.max = max;
		this.value = initialValue;
		this.initialValue = initialValue;
		this.font = font;
		this.tooltipFormat = tooltipFormat;
		this.onChange = onChange;
		this.onCancel = onCancel;
		
		this.canCancel = canCancel;
		this.range = segments;
		this.notchStep = step;
		this.innerWidth = innerWidth;
		this.minX = theme.border + theme.horizontalPadding;
		this.maxX = theme.border + theme.horizontalPadding + this.innerWidth + 3;
		this.centerY = Math.floorDiv(this.height, 2);
		this.halfBarHeight = Math.floorDiv(theme.barHeight, 2);
	}
	
	@Override
	protected void renderWidget(GuiGraphics gui, int mouseXi, int mouseYi, float partialTick) {
		final var theme = this.theme;
		gui.pose().pushPose();
		gui.pose().translate(0, 0, this.z);
		
		final ResourceLocation bgSprite = theme.spritesBackground.get(this.isActive(), this.isFocused() && !this.focusDragging);
		gui.blitSprite(RenderType::guiTextured, bgSprite, this.getX(), this.getY(), this.width, this.height);
		
		this.drawGuides(gui);
		
		if (this.value >= this.min && this.value <= this.max) {
			final int sbx = this.getX() + this.minX - 1 + Math.round((this.value - this.min) * this.notchStep);
			final int sby = this.getY() + this.centerY - this.halfBarHeight;
			final int sbwidth = 5;
			final int sbheight = theme.barHeight;
			final ResourceLocation barSprite = theme.spritesBar.get(this.isActive(), (this.dragging || mouseXi >= sbx && mouseXi < sbx + sbwidth && mouseYi >= sby && mouseYi < sby + sbheight));
			gui.blitSprite(RenderType::guiTextured, barSprite, sbx, sby, sbwidth, sbheight);
		}
		
		final int x = mouseXi - this.getX();
		final int y = mouseYi - this.getY();
		if (!this.active) {
			if (x >= 0 && x < this.width && y >= 0 && y < this.height) {
				final List<Component> tooltip = this.tooltipFormat.apply(this.value);
				if (!tooltip.isEmpty()) gui.renderComponentTooltip(this.font, tooltip, mouseXi, mouseYi);
			}
		} else if (x >= this.minX - Math.max(theme.horizontalPadding, 1) && x < this.maxX + Math.max(theme.horizontalPadding, 1) && y >= theme.border && y < this.height - theme.border) {
			Minecraft mc = Minecraft.getInstance();
			Window window = mc.getWindow();
			double mouseX = (mc.mouseHandler.xpos() * ((double)window.getGuiScaledWidth() / window.getScreenWidth()));
			final int newVal = Mth.clamp((int)Math.round(this.min + (mouseX - this.getX() - this.minX - 1.5) / this.innerWidth * this.range), this.min, this.max);
			final List<Component> tooltip = this.tooltipFormat.apply(newVal);
			if (!tooltip.isEmpty()) gui.renderComponentTooltip(this.font, tooltip, mouseXi, mouseYi);
		} else if (this.focusDragging && this.dragging) {
			final int sbx = this.getX() + this.minX - 1 + Math.round((this.value - this.min) * this.notchStep);
			final int sby = this.getY() + this.centerY - this.halfBarHeight;
			final List<Component> tooltip = this.tooltipFormat.apply(this.value);
			if (!tooltip.isEmpty()) gui.renderComponentTooltip(this.font, tooltip, sbx, sby);
		}
		
		if (this.canCancel) {
			final int cancelX = this.width - theme.border - 12 - theme.cancelButtonPadding;
			final boolean hoveringCancel = this.active && !this.dragging && x >= cancelX && x < cancelX + 12 && y >= theme.border && y < theme.border + 12;
			final ResourceLocation cancelSprite = theme.spritesCancelButton.get(this.isActive(), hoveringCancel);
			gui.blitSprite(RenderType::guiTextured, cancelSprite, this.getX() + cancelX, this.getY() + theme.border + theme.cancelButtonPadding, 12, 12);
			if (hoveringCancel) {
				gui.renderComponentTooltip(this.font, List.of(
						Component.translatable("container.builders_inventory.util.tooltip.button.cancel").withStyle(ChatFormatting.WHITE),
						Component.translatable("container.builders_inventory.util.tooltip.button.cancel.desc").withStyle(ChatFormatting.GRAY)
						), mouseXi, mouseYi);
			}
		}
		
		gui.pose().popPose();
	}
	
	@SuppressWarnings("deprecation")
	private void drawGuides(GuiGraphics gui) {
		final int minX = this.getX() + this.minX;
		final int maxX = this.getX() + this.maxX;
		final int centerY = this.getY() + this.centerY;
		final int guideColor = this.active ? (this.isFocused() && !this.focusDragging ? this.theme.guideColorBGHighlighted : this.theme.guideColor) : this.theme.guideColorDisabled;
		gui.fill(minX, centerY, maxX, centerY + 1, guideColor);
		gui.fill(minX + 1, centerY - 3, minX + 2, centerY + 4, guideColor);
		gui.fill(maxX - 2, centerY - 3, maxX - 1, centerY + 4, guideColor);
		final int innerNotchCount = this.range - 1;
		for (int i = 1; i <= innerNotchCount; i++) {
			final int snx = minX + 1 + Math.round(i * this.notchStep);
			gui.fill(snx, centerY - 2, snx + 1, centerY + 3, guideColor);
		}
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!this.isActive()) return false;
		if (!this.isValidClickButton(button)) return false;
		
		final var theme = this.theme;
		final double x = mouseX - this.getX();
		final double y = mouseY - this.getY();
		if (x >= this.minX - Math.max(theme.horizontalPadding, 1) && x < this.maxX + Math.max(theme.horizontalPadding, 1) && y >= theme.border && y < this.height - theme.border) {
			final int newVal = Mth.clamp((int)Math.round(this.min + (mouseX - this.getX() - this.minX - 1.5) / this.innerWidth * this.range), this.min, this.max);
			if (this.value != newVal) {
				this.value = newVal;
				this.onChange.accept(newVal);
			}
			
			Minecraft mc = Minecraft.getInstance();
			this.playDownSound(mc.getSoundManager());
			this.dragging = true;
			return true;
		} else {
			this.dragging = false;
		}
		
		if (this.canCancel) {
			final int cancelX = this.width - theme.border - 12 - theme.cancelButtonPadding;
			if (!this.dragging && x >= cancelX && x <= cancelX + 12 && y >= 4 && y < 16) {
				Minecraft mc = Minecraft.getInstance();
				this.playDownSound(mc.getSoundManager());
				
				this.onCancel.accept(this.initialValue);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (!this.isActive()) return false;
		if (!this.isValidClickButton(button)) return false;
		if (!this.dragging) return false;
		
		final int newVal = Mth.clamp((int)Math.round(this.min + (mouseX - this.getX() - this.minX - 1.5) / this.innerWidth * this.range), this.min, this.max);
		if (this.value != newVal) {
			this.value = newVal;
			this.onChange.accept(newVal);
		}
		
		return true;
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (!this.isActive()) return false;
		if (!this.isValidClickButton(button)) return false;
		this.dragging = false;
		return true;
	}
	
	@Override
	public void setFocused(boolean focused) {
		super.setFocused(focused);
		if (!focused) {
			this.dragging = false;
			this.focusDragging = false;
		} else {
			final var mc = Minecraft.getInstance();
			final var lastInput = mc.getLastInputType();
			if (lastInput == InputType.MOUSE || lastInput == InputType.KEYBOARD_TAB) {
				this.dragging = true;
				this.focusDragging = true;
			}
		}
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (CommonInputs.selected(keyCode)) {
			this.focusDragging = !this.focusDragging;
			this.dragging = this.focusDragging;
			return true;
		} else {
			if (this.focusDragging) {
				final boolean left = keyCode == InputConstants.KEY_LEFT;
				final boolean right = keyCode == InputConstants.KEY_RIGHT;
				if (left || right) {
					this.dragging = true;
					final int diff = left ? -1 : 1;
					final int newVal = Mth.clamp(this.value + diff, this.min, this.max);
					if (newVal != this.value) {
						this.value = newVal;
						this.onChange.accept(newVal);
					}
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		
	}
	
	@Override
	public void setWidth(int width) {
		throw new UnsupportedOperationException("Changing slider size through code is not supported");
	}
	
	@Override
	public void setHeight(int height) {
		throw new UnsupportedOperationException("Changing slider size through code is not supported");
	}
	
}
