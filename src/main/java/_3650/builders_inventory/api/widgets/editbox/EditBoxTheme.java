package _3650.builders_inventory.api.widgets.editbox;

import _3650.builders_inventory.BuildersInventory;
import _3650.builders_inventory.api.util.ThemeUtil;
import net.minecraft.client.gui.components.WidgetSprites;

public class EditBoxTheme {
	
	public final WidgetSprites spritesBackground;
	public final WidgetSprites spritesScrollbar;
	public final int textColor;
	public final int disabledTextColor;
	public final int suggestionColor;
	public final int lineNumColor;
	public final int lineNumBackgroundColor;
	public final int innerPadding;
	public final int borderThickness;
	public final int scrollbarWidth;
	public final int scrollbarPadding;
	public final int scrollbarEdgeHeight;
	public final int scrollbarScale;
	
	public EditBoxTheme(
			WidgetSprites spritesBackground,
			WidgetSprites spritesScrollbar,
			int textColor,
			int disabledTextColor,
			int suggestionColor,
			int lineNumColor,
			int lineNumBackgroundColor,
			int innerPadding,
			int borderThickness,
			int scrollbarWidth,
			int scrollbarPadding,
			int scrollbarEdgeHeight,
			int scrollbarScale
			) {
		this.spritesBackground = spritesBackground;
		this.spritesScrollbar = spritesScrollbar;
		this.textColor = ThemeUtil.visibleColor(textColor);
		this.disabledTextColor = ThemeUtil.visibleColor(disabledTextColor);
		this.suggestionColor = ThemeUtil.visibleColor(suggestionColor);
		this.lineNumColor = ThemeUtil.visibleColor(lineNumColor);
		this.lineNumBackgroundColor = ThemeUtil.visibleColor(lineNumBackgroundColor);
		this.innerPadding = innerPadding;
		this.borderThickness = borderThickness;
		this.scrollbarWidth = scrollbarWidth;
		this.scrollbarPadding = scrollbarPadding;
		this.scrollbarEdgeHeight = scrollbarEdgeHeight;
		this.scrollbarScale = scrollbarScale;
	}
	
	public static final EditBoxTheme CUBIC = buildCubic();
	
	@Deprecated
	public static EditBoxTheme buildCubic() {
		return new EditBoxTheme(
				new WidgetSprites(
						BuildersInventory.modLoc("themes/cubic/text_field/text_field"),
						BuildersInventory.modLoc("themes/cubic/text_field/text_field_disabled"),
						BuildersInventory.modLoc("themes/cubic/text_field/text_field")),
				new WidgetSprites(
						BuildersInventory.modLoc("themes/cubic/text_field/scrollbar"),
						BuildersInventory.modLoc("themes/cubic/text_field/scrollbar_highlighted")),
				0xFFFFFFFF,
				0xFF707070,
				0xFF808080,
				0xFFFFFFFF,
				0xFFB2A180,
				5,
				2,
				6,
				0,
				3,
				2);
	}
	
	public static final EditBoxTheme COMMANDER = buildCommander();
	
	@Deprecated
	public static EditBoxTheme buildCommander() {
		return new EditBoxTheme(
				new WidgetSprites(
						BuildersInventory.modLoc("themes/commander/text_field/text_field"),
						BuildersInventory.modLoc("themes/commander/text_field/text_field_highlighted")),
				new WidgetSprites(
						BuildersInventory.modLoc("themes/commander/text_field/scrollbar"),
						BuildersInventory.modLoc("themes/commander/text_field/scrollbar_highlighted")),
				0xFFE0E0E0,
				0xFF707070,
				0xFF808080,
				0xFFB3B3B3,
				0xFF3D3D3D,
				4,
				1,
				2,
				2,
				0,
				1);
	}
	
}
