package _3650.builders_inventory.api.widgets.editbox;

import _3650.builders_inventory.BuildersInventory;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.resources.ResourceLocation;

public interface EditBoxTheme {
	
	public ResourceLocation getBackgroundSprite(boolean enabled, boolean focused);
	
	public ResourceLocation getScrollbarSprite(boolean enabled, boolean focused);
	
	public int textColor();
	
	public int disabledTextColor();
	
	public int suggestionColor();
	
	public int lineNumColor();
	
	public int lineNumBackgroundColor();
	
	public int innerPadding();
	
	public int borderThickness();
	
	public int scrollbarWidth();
	
	public int scrollbarPadding();
	
	public int scrollbarEdgeHeight();
	
	public int scrollbarScale();
	
	public static EditBoxTheme options(
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
		return new EditBoxTheme() {
			
			@Override
			public ResourceLocation getBackgroundSprite(boolean enabled, boolean focused) {
				return spritesBackground.get(enabled, focused);
			}
			
			@Override
			public ResourceLocation getScrollbarSprite(boolean enabled, boolean focused) {
				return spritesScrollbar.get(enabled, focused);
			}
			
			@Override
			public int textColor() {
				return textColor;
			}
			
			@Override
			public int disabledTextColor() {
				return disabledTextColor;
			}
			
			@Override
			public int suggestionColor() {
				return suggestionColor;
			}
			
			@Override
			public int lineNumColor() {
				return lineNumColor;
			}
			
			@Override
			public int lineNumBackgroundColor() {
				return lineNumBackgroundColor;
			}
			
			@Override
			public int innerPadding() {
				return innerPadding;
			}
			
			@Override
			public int borderThickness() {
				return borderThickness;
			}
			
			@Override
			public int scrollbarWidth() {
				return scrollbarWidth;
			}
			
			@Override
			public int scrollbarPadding() {
				return scrollbarPadding;
			}
			
			@Override
			public int scrollbarEdgeHeight() {
				return scrollbarEdgeHeight;
			}
			
			@Override
			public int scrollbarScale() {
				return scrollbarScale;
			}
		};
	}
	
	
	public static final EditBoxTheme CUBIC = new EditBoxTheme() {
		
		private static final WidgetSprites SPRITES_BACKGROUND = new WidgetSprites(
				BuildersInventory.modLoc("themes/cubic/text_field"),
				BuildersInventory.modLoc("themes/cubic/text_field_disabled"),
				BuildersInventory.modLoc("themes/cubic/text_field"));
		private static final ResourceLocation SPRITE_SCROLLBAR =
				BuildersInventory.modLoc("themes/cubic/scrollbar");
		
		@Override
		public ResourceLocation getBackgroundSprite(boolean enabled, boolean focused) {
			return SPRITES_BACKGROUND.get(enabled, focused);
		}
		
		@Override
		public ResourceLocation getScrollbarSprite(boolean enabled, boolean focused) {
			return SPRITE_SCROLLBAR;
		}
		
		@Override
		public int textColor() {
			return 0xFFFFFFFF;
		}
		
		@Override
		public int disabledTextColor() {
			return 0xFF707070;
		}
		
		@Override
		public int suggestionColor() {
			return 0xFF808080;
		}
		
		@Override
		public int lineNumColor() {
			return 0xFFFFFFFF;
		}
		
		@Override
		public int lineNumBackgroundColor() {
			return 0xFFB2A180;
		}
		
		@Override
		public int innerPadding() {
			return 5;
		}
		
		@Override
		public int borderThickness() {
			return 2;
		}
		
		@Override
		public int scrollbarWidth() {
			return 6;
		}
		
		@Override
		public int scrollbarPadding() {
			return 0;
		}
		
		@Override
		public int scrollbarEdgeHeight() {
			return 3;
		}
		
		@Override
		public int scrollbarScale() {
			return 2;
		}
		
	};
	
	public static final EditBoxTheme COMMANDER = new EditBoxTheme() {
		
		private static final WidgetSprites SPRITES_BACKGROUND = new WidgetSprites(
				BuildersInventory.modLoc("themes/commander/text_field"),
				BuildersInventory.modLoc("themes/commander/text_field_highlighted"));
		private static final WidgetSprites SPRITES_SCROLLBAR = new WidgetSprites(
				BuildersInventory.modLoc("themes/commander/scrollbar"),
				BuildersInventory.modLoc("themes/commander/scrollbar_highlighted"));
		
		@Override
		public ResourceLocation getBackgroundSprite(boolean enabled, boolean focused) {
			return SPRITES_BACKGROUND.get(enabled, focused);
		}
		
		@Override
		public ResourceLocation getScrollbarSprite(boolean enabled, boolean focused) {
			return SPRITES_SCROLLBAR.get(enabled, focused);
		}
		
		@Override
		public int textColor() {
			return 0xFFE0E0E0;
		}
		
		@Override
		public int disabledTextColor() {
			return 0xFF707070;
		}
		
		@Override
		public int suggestionColor() {
			return 0xFF808080;
		}
		
		@Override
		public int lineNumColor() {
			return 0xFFB3B3B3;
		}
		
		@Override
		public int lineNumBackgroundColor() {
			return 0xFF3D3D3D;
		}
		
		@Override
		public int innerPadding() {
			return 4;
		}
		
		@Override
		public int borderThickness() {
			return 1;
		}
		
		@Override
		public int scrollbarWidth() {
			return 2;
		}
		
		@Override
		public int scrollbarPadding() {
			return 2;
		}
		
		@Override
		public int scrollbarEdgeHeight() {
			return 0;
		}
		
		@Override
		public int scrollbarScale() {
			return 1;
		}
		
	};
	
}
