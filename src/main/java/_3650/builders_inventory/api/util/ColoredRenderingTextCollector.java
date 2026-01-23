package _3650.builders_inventory.api.util;

import java.util.function.Consumer;

import _3650.builders_inventory.mixin.feature.minimessage.GuiGraphicsInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.render.state.GuiTextRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;

public class ColoredRenderingTextCollector implements ActiveTextCollector, Consumer<Style> {
	
	public static ColoredRenderingTextCollector create(GuiGraphics gui) {
		return create(gui, 0xFFFFFFFF);
	}
	
	public static ColoredRenderingTextCollector create(GuiGraphics gui, int color) {
		return create(gui, GuiGraphics.HoveredTextEffects.TOOLTIP_ONLY, color);
	}
	
	public static ColoredRenderingTextCollector create(GuiGraphics gui, GuiGraphics.HoveredTextEffects hoveredTextEffects, int color) {
		return new ColoredRenderingTextCollector(gui, defaultParameters(gui), hoveredTextEffects, color);
	}
	
	private static Parameters defaultParameters(GuiGraphics gui) {
		return ((GuiGraphicsInvoker)gui).callCreateDefaultTextParameters(1f);
	}
	
	private final Minecraft minecraft;
	private final GuiGraphics gui;
	private final GuiGraphics.HoveredTextEffects hoveredTextEffects;
	private Parameters defaultParameters;
	private int color;
	
	private ColoredRenderingTextCollector(GuiGraphics gui, Parameters defaultParameters, GuiGraphics.HoveredTextEffects hoveredTextEffects, int color) {
		this.minecraft = Minecraft.getInstance();
		this.gui = gui;
		this.hoveredTextEffects = hoveredTextEffects;
		this.defaultParameters = defaultParameters;
		this.color = color;
	}
	
	@Override
	public Parameters defaultParameters() {
		return this.defaultParameters;
	}
	
	@Override
	public void defaultParameters(Parameters defaultParameters) {
		this.defaultParameters = defaultParameters;
	}
	
	public void setColor(int color) {
		this.color = color;
	}
	
	@Override
	public void accept(Style style) {
		if (this.hoveredTextEffects.allowTooltip && style.getHoverEvent() != null) {
			((GuiGraphicsInvoker)gui).setHoveredTextStyle(style);
		}
		
		if (this.hoveredTextEffects.allowCursorChanges && style.getClickEvent() != null) {
			((GuiGraphicsInvoker)gui).setClickableTextStyle(style);
		}
	}
	
	@Override
	public void accept(int x, int y, Component component) {
		this.internalRender(x, y, defaultParameters, component.getVisualOrderText());
	}
	
	@Override
	public void accept(int x, int y, FormattedCharSequence text) {
		this.internalRender(x, y, defaultParameters, text);
	}
	
	@Override
	public void accept(TextAlignment textAlignment, int x, int y, Parameters parameters, FormattedCharSequence text) {
		int leftX = textAlignment.calculateLeft(x, this.minecraft.font, text);
		this.internalRender(leftX, y, parameters, text);
	}
	
	private void internalRender(int x, int y, Parameters parameters, FormattedCharSequence text) {
		boolean styleHoverCheck = this.hoveredTextEffects.allowTooltip || this.hoveredTextEffects.allowCursorChanges;
		int textColor = ARGB.color(ARGB.as8BitChannel(parameters.opacity()), this.color);
		GuiTextRenderState renderState = new GuiTextRenderState(
				this.minecraft.font,
				text,
				parameters.pose(),
				x,
				y,
				textColor,
				0,
				true,
				styleHoverCheck,
				parameters.scissor());
		if (textColor > 0xFFFFFF) {
			this.gui.guiRenderState.submitText(renderState);
		}
		
		if (styleHoverCheck) {
			var guiMouse = ((GuiGraphicsInvoker)gui);
			ActiveTextCollector.findElementUnderCursor(renderState, guiMouse.getMouseX(), guiMouse.getMouseY(), this);
		}
	}
	
	@Override
	public void acceptScrolling(Component text, int centerX, int left, int right, int top, int bottom, Parameters parameters) {
		int width = this.minecraft.font.width(text);
		int height = 9;
		this.defaultScrollingHelper(text, centerX, left, right, top, bottom, width, height, parameters);
	}
	
}
