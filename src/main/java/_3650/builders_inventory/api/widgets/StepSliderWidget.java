package _3650.builders_inventory.api.widgets;

import java.util.List;
import java.util.function.IntConsumer;

import _3650.builders_inventory.BuildersInventory;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class StepSliderWidget extends AbstractWidget {
	
	private static final ResourceLocation SPRITE_BACKGROUND = BuildersInventory.modLoc("util/slider_background");
	private static final ResourceLocation SPRITE_BACKGROUND_CANCEL = BuildersInventory.modLoc("util/slider_background_cancel");
	private static final ResourceLocation SPRITE_NOTCH = BuildersInventory.modLoc("util/slider_notch");
	private static final ResourceLocation SPRITE_BAR = BuildersInventory.modLoc("util/slider_bar");
	private static final ResourceLocation SPRITE_BAR_HIGHLIGHTED = BuildersInventory.modLoc("util/slider_bar_highlighted");
	private static final ResourceLocation SPRITE_BUTTON_CANCEL = BuildersInventory.modLoc("util/button_cancel");
	private static final ResourceLocation SPRITE_BUTTON_CANCEL_HIGHLIGHTED = BuildersInventory.modLoc("util/button_cancel_highlighted");
	
	private static final IntConsumer NO_CANCEL = x -> {};
	
	private final boolean canCancel;
	private final int z;
	private final int min;
	private final int max;
	public final int initialValue;
	private final Font font;
	private final Int2ObjectFunction<List<Component>> tooltipFormat;
	private final IntConsumer onChange;
	private final IntConsumer onCancel;
	
	private final int range;
	private final int innerNotch;
	private final int innerWidth;
	private final int notchStep;
	
	public int value;
	private boolean dragging = false;
	
	public StepSliderWidget(int x, int y, int z, int min, int max, int initialValue, Font font, Int2ObjectFunction<List<Component>> tooltipFormat, IntConsumer onChange) {
		this(x, y, z, min, max, initialValue, font, tooltipFormat, onChange, NO_CANCEL);
	}
	
	public StepSliderWidget(int x, int y, int z, int min, int max, int initialValue, Font font, Int2ObjectFunction<List<Component>> tooltipFormat, IntConsumer onChange, IntConsumer onCancel) {
		super(x, y, calculateWidth(min, max, onCancel != NO_CANCEL), 23, Component.empty());
		this.canCancel = onCancel != NO_CANCEL;
		this.z = z;
		this.min = min;
		this.max = max;
		this.value = initialValue;
		this.initialValue = initialValue;
		this.font = font;
		this.tooltipFormat = tooltipFormat;
		this.onChange = onChange;
		this.range = max - min;
		this.innerNotch = max - min - 1;
		this.innerWidth = this.width - (this.canCancel ? 25 : 12);
		this.notchStep = (this.innerWidth - 1) / (max - min);
		this.onCancel = onCancel;
	}
	
	private static int calculateWidth(int min, int max, boolean canCancel) {
		if (min >= max) throw new IllegalArgumentException("Slider minimum value " + min + " must be less than maximum value " + max);
		final int segments = max - min;
		final int step = switch(segments) {
		case 1 -> 50; //  50 segment width
		case 2 -> 30; //  60 segment width
		case 3 -> 25; //  75 segment width
		case 4 -> 22; //  88 segment width
		case 5 -> 20; // 100 segment width
		case 6 -> 18; // 108 segment width
		case 7 -> 16; // 112 segment width
		default -> calculateVisualStep(segments);
		};
		
		return step * segments + (canCancel ? 26 : 13); // +6 for left edge, +1 for right notch, +6 for right edge, +13? for cancel extension
	}
	
	private static int calculateVisualStep(int segments) {
		if (segments > 48) {
			return 2;
		} else {
			return Math.round(116f / segments);
		}
	}
	
	@Override
	protected void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		gui.pose().pushPose();
		gui.pose().translate(0, 0, this.z);
		
		gui.blitSprite(RenderType::guiTextured, this.canCancel ? SPRITE_BACKGROUND_CANCEL : SPRITE_BACKGROUND, this.getX(), this.getY(), this.getWidth(), this.getHeight());
		
		for (int i = 1; i <= this.innerNotch; i++) {
			final int snx = 6 + Math.round(i * this.notchStep);
			gui.blitSprite(RenderType::guiTextured, SPRITE_NOTCH, this.getX() + snx, this.getY() + 9, 1, 5);
		}
		
		final int x = mouseX - this.getX();
		final int y = mouseY - this.getY();
		if (this.value >= this.min && this.value <= this.max) {
			final int sbx = 4 + Math.round((this.value - this.min) * this.notchStep);
			final ResourceLocation barSprite = (dragging || x >= sbx && x < sbx + 4 && y >= 6 && y < 18) ? SPRITE_BAR_HIGHLIGHTED : SPRITE_BAR;
			gui.blitSprite(RenderType::guiTextured, barSprite, this.getX() + sbx, this.getY() + 5, 5, 13);
		}
		
		if (x >= 4 && x < this.innerWidth + 8 && y >= 4 && y < 18) {
			final int newVal = Mth.clamp((int) Math.round(this.min + (x - 6.0) / this.innerWidth * this.range), this.min, this.max);
			final List<Component> tooltip = this.tooltipFormat.apply(newVal);
			if (!tooltip.isEmpty()) gui.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
		}
		
		if (this.canCancel) {
			final int cancelX = this.width - 16;
			final boolean hoveringCancel = !dragging && x >= cancelX && x < cancelX + 12 && y >= 4 && y < 16;
			final ResourceLocation cancelSprite = hoveringCancel ? SPRITE_BUTTON_CANCEL_HIGHLIGHTED : SPRITE_BUTTON_CANCEL;
			gui.blitSprite(RenderType::guiTextured, cancelSprite, this.getX() + cancelX, this.getY() + 4, 12, 12);
			if (hoveringCancel) {
				gui.renderComponentTooltip(this.font, List.of(
						Component.translatable("container.builders_inventory.util.tooltip.button.cancel").withStyle(ChatFormatting.WHITE),
						Component.translatable("container.builders_inventory.util.tooltip.button.cancel.desc").withStyle(ChatFormatting.GRAY)
						), mouseX, mouseY);
			}
		}
		
		gui.pose().popPose();
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.isValidClickButton(button)) {
			final int x = Mth.floor(mouseX) - this.getX();
			final int y = Mth.floor(mouseY) - this.getY();
			if (x >= 4 && x < this.innerWidth + 8 && y >= 4 && y < 18) {
				final int newVal = Mth.clamp((int) Math.round(this.min + (x - 6.0) / this.innerWidth * this.range), this.min, this.max);
				if (this.value != newVal) {
					this.value = newVal;
					this.onChange.accept(newVal);
				}
				
				Minecraft mc = Minecraft.getInstance();
				this.playDownSound(mc.getSoundManager());
				dragging = true;
				return true;
			}
			if (this.canCancel) {
				final int cancelX = this.width - 16;
				if (!dragging && x >= cancelX && x <= cancelX + 12 && y >= 4 && y < 16) {
					Minecraft mc = Minecraft.getInstance();
					this.playDownSound(mc.getSoundManager());
					
					this.onCancel.accept(this.initialValue);
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (this.isValidClickButton(button) && dragging) {
			final int x = Mth.floor(mouseX) - this.getX();
			
			final int newVal = Mth.clamp((int) Math.round(this.min + (x - 6.0) / this.innerWidth * this.range), this.min, this.max);
			if (this.value != newVal) {
				this.value = newVal;
				this.onChange.accept(newVal);
			}
			
			return true;
		}
		return false;
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (this.isValidClickButton(button)) {
			dragging = false;
			return true;
		}
		return false;
	}
	
	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		
	}
	
}
