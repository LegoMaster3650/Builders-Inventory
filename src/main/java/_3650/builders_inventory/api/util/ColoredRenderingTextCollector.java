package _3650.builders_inventory.api.util;

import java.util.function.Consumer;

import _3650.builders_inventory.mixin.feature.minimessage.GuiGraphicsExtractorInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.renderer.state.gui.GuiTextRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;

public class ColoredRenderingTextCollector implements ActiveTextCollector, Consumer<Style> {
	
	public static ColoredRenderingTextCollector create(GuiGraphicsExtractor gui) {
		return create(gui, 0xFFFFFFFF);
	}
	
	public static ColoredRenderingTextCollector create(GuiGraphicsExtractor gui, int color) {
		return create(gui, GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_ONLY, color);
	}
	
	public static ColoredRenderingTextCollector create(GuiGraphicsExtractor gui, GuiGraphicsExtractor.HoveredTextEffects hoveredTextEffects, int color) {
		return new ColoredRenderingTextCollector(gui, defaultParameters(gui), hoveredTextEffects, color);
	}
	
	private static Parameters defaultParameters(GuiGraphicsExtractor gui) {
		return ((GuiGraphicsExtractorInvoker)gui).callCreateDefaultTextParameters(1f);
	}
	
	private final Minecraft minecraft;
	private final GuiGraphicsExtractor gui;
	private final GuiGraphicsExtractor.HoveredTextEffects hoveredTextEffects;
	private Parameters defaultParameters;
	private int color;
	
	private ColoredRenderingTextCollector(GuiGraphicsExtractor gui, Parameters defaultParameters, GuiGraphicsExtractor.HoveredTextEffects hoveredTextEffects, int color) {
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
			((GuiGraphicsExtractorInvoker)gui).setHoveredTextStyle(style);
		}
		
		if (this.hoveredTextEffects.allowCursorChanges && style.getClickEvent() != null) {
			((GuiGraphicsExtractorInvoker)gui).setClickableTextStyle(style);
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
			this.gui.guiRenderState.addText(renderState);
		}
		
		if (styleHoverCheck) {
			var guiMouse = ((GuiGraphicsExtractorInvoker)gui);
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
