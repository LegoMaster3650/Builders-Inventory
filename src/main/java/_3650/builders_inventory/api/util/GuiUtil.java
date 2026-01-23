package _3650.builders_inventory.api.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class GuiUtil {
	
	private static final Identifier SPRITE_SLOT_HIGHLIGHT_BACK = Identifier.withDefaultNamespace("container/slot_highlight_back");
	private static final Identifier SPRITE_SLOT_HIGHLIGHT_FRONT = Identifier.withDefaultNamespace("container/slot_highlight_front");
	
	public static void blitScreenBackground(GuiGraphics gui, Identifier atlasId, int x, int y, int width, int height) {
		gui.blit(RenderPipelines.GUI_TEXTURED, atlasId, x, y, 0, 0, width, height, 256, 256);
	}
	
	public static void renderSlotHighlightBack(GuiGraphics gui, int x, int y) {
		gui.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITE_SLOT_HIGHLIGHT_BACK, x - 4, y - 4, 24, 24);
	}
	
	public static void renderSlotHighlightFront(GuiGraphics gui, int x, int y) {
		gui.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITE_SLOT_HIGHLIGHT_FRONT, x - 4, y - 4, 24, 24);
	}
	
}
