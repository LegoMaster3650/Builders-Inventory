package _3650.builders_inventory.api.widgets.slider;

import _3650.builders_inventory.BuildersInventory;
import _3650.builders_inventory.api.util.ThemeUtil;
import net.minecraft.client.gui.components.WidgetSprites;

public class SliderWidgetTheme {
	
	public final WidgetSprites spritesBackground;
	public final WidgetSprites spritesBar;
	public final int barHeight;
	public final WidgetSprites spritesCancelButton;
	public final int guideColor;
	public final int guideColorBGHighlighted;
	public final int guideColorDisabled;
	public final int height;
	public final int border;
	public final int horizontalPadding;
	public final int cancelButtonPadding;
	
	private SliderWidgetTheme(
			WidgetSprites spritesBackground,
			WidgetSprites spritesBar,
			int barHeight,
			WidgetSprites spritesCancelButton,
			int guideColor,
			int guideColorBGHighlighted,
			int guideColorDisabled,
			int height,
			int border,
			int horizontalPadding,
			int cancelButtonPadding
			) {
		this.spritesBackground = spritesBackground;
		this.spritesBar = spritesBar;
		this.barHeight = barHeight;
		this.spritesCancelButton = spritesCancelButton;
		this.guideColor = ThemeUtil.visibleColor(guideColor);
		this.guideColorBGHighlighted = ThemeUtil.visibleColor(guideColorBGHighlighted);
		this.guideColorDisabled = ThemeUtil.visibleColor(guideColorDisabled);
		this.height = height;
		this.border = border;
		this.horizontalPadding = horizontalPadding;
		this.cancelButtonPadding = cancelButtonPadding;
		
	}
	
	public static final SliderWidgetTheme CUBIC = buildCubic();
	
	@Deprecated
	public static SliderWidgetTheme buildCubic() {
		return new SliderWidgetTheme(
				new WidgetSprites(
						BuildersInventory.modLoc("themes/cubic/slider/slider_background"),
						BuildersInventory.modLoc("themes/cubic/slider/slider_background_disabled"),
						BuildersInventory.modLoc("themes/cubic/slider/slider_background_highlighted")),
				new WidgetSprites(
						BuildersInventory.modLoc("themes/cubic/slider/slider_bar"),
						BuildersInventory.modLoc("themes/cubic/slider/slider_bar_disabled"),
						BuildersInventory.modLoc("themes/cubic/slider/slider_bar_highlighted")),
				13,
				new WidgetSprites(
						BuildersInventory.modLoc("themes/cubic/slider/button_cancel"),
						BuildersInventory.modLoc("themes/commander/slider/button_cancel_disabled"),
						BuildersInventory.modLoc("themes/cubic/slider/button_cancel_highlighted")),
				0xFF373737,
				0xFF212849,
				0xFF0F0F0F,
				23,
				4,
				1,
				0);
	}
	
	public static final SliderWidgetTheme COMMANDER = buildCommander();
	
	//TODO make commander theme good
	@Deprecated
	public static SliderWidgetTheme buildCommander() {
		return new SliderWidgetTheme(
				new WidgetSprites(
						BuildersInventory.modLoc("themes/commander/slider/slider_background"),
						BuildersInventory.modLoc("themes/commander/slider/slider_background_disabled"),
						BuildersInventory.modLoc("themes/commander/slider/slider_background_highlighted")),
				new WidgetSprites(
						BuildersInventory.modLoc("themes/commander/slider/slider_bar"),
						BuildersInventory.modLoc("themes/commander/slider/slider_bar_disabled"),
						BuildersInventory.modLoc("themes/commander/slider/slider_bar_highlighted")),
				13,
				new WidgetSprites(
						BuildersInventory.modLoc("themes/commander/slider/button_cancel"),
						BuildersInventory.modLoc("themes/commander/slider/button_cancel_disabled"),
						BuildersInventory.modLoc("themes/commander/slider/button_cancel_highlighted")),
				0xFFE0E0E0,
				0xFFE0E0E0,
				0xFF333333,
				19,
				1,
				0,
				1);
	}
	
}
