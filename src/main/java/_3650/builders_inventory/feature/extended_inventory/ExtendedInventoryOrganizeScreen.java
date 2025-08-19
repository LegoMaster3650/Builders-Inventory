package _3650.builders_inventory.feature.extended_inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;

import _3650.builders_inventory.BuildersInventory;
import _3650.builders_inventory.ModKeybinds;
import _3650.builders_inventory.api.util.GuiUtil;
import _3650.builders_inventory.api.widgets.exbutton.ExtendedImageButton;
import _3650.builders_inventory.api.widgets.exbutton.ExtendedImageButtonGui;
import _3650.builders_inventory.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public class ExtendedInventoryOrganizeScreen extends Screen {
	
	private static final ResourceLocation BACKGROUND = BuildersInventory.modLoc("textures/gui/container/extended_inventory/organize.png");
	
	private static final ResourceLocation SPRITE_CREATIVE_SCROLLER = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller");
	private static final ResourceLocation SPRITE_CREATIVE_SCROLLER_DISABLED = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller_disabled");
	
	private static final WidgetSprites SPRITES_BUTTON_BACK = new WidgetSprites(
			BuildersInventory.modLoc("extended_inventory/organize/button_back"),
			BuildersInventory.modLoc("extended_inventory/organize/button_back_highlighted"));
	
	private static final ResourceLocation SPRITE_TILE = BuildersInventory.modLoc("extended_inventory/organize/tile");
	private static final ResourceLocation SPRITE_TILE_SELECTED = BuildersInventory.modLoc("extended_inventory/organize/tile_highlighted");
	private static final ResourceLocation SPRITE_TILE_SHADOW = BuildersInventory.modLoc("extended_inventory/organize/tile_shadow");
	private static final WidgetSprites SPRITES_TILE = new WidgetSprites(
			SPRITE_TILE,
			SPRITE_TILE_SELECTED);
	private static final ResourceLocation SPRITE_TILE_ACTIVE = BuildersInventory.modLoc("extended_inventory/organize/tile_active");
	private static final ResourceLocation SPRITE_TILE_ACTIVE_SELECTED = BuildersInventory.modLoc("extended_inventory/organize/tile_active_highlighted");
	private static final WidgetSprites SPRITES_TILE_ACTIVE = new WidgetSprites(
			SPRITE_TILE_ACTIVE,
			SPRITE_TILE_ACTIVE_SELECTED);
	private static final ResourceLocation SPRITE_TILE_CREATE = BuildersInventory.modLoc("extended_inventory/organize/tile_create");
	private static final ResourceLocation SPRITE_TILE_CREATE_SELECTED = BuildersInventory.modLoc("extended_inventory/organize/tile_create_highlighted");
	@SuppressWarnings("unused")
	private static final WidgetSprites SPRITES_TILE_CREATE = new WidgetSprites(
			SPRITE_TILE_CREATE,
			SPRITE_TILE_CREATE_SELECTED);
	@SuppressWarnings("unused")
	private static final ResourceLocation SPRITE_TILE_CREATE_DIM = BuildersInventory.modLoc("extended_inventory/organize/tile_create_dim");
	
	private static final ResourceLocation SPRITE_BACKGROUND = BuildersInventory.modLoc("extended_inventory/organize/hover_background");
	private static final ResourceLocation SPRITE_BACKGROUND_LOCKED = BuildersInventory.modLoc("extended_inventory/organize/hover_background_locked");
	private static final ResourceLocation SPRITE_BACKGROUND_INVALID = BuildersInventory.modLoc("extended_inventory/organize/hover_background_invalid");
	
	private static final int TILE_MIN_X = 8;
	private static final int TILE_MIN_Y = 17;
	
	private final ExtendedImageButtonGui exGui = new ExtendedImageButtonGui();
	private final int imageWidth;
	private final int imageHeight;
	
	private int leftPos;
	private int topPos;
	
	private final ArrayList<PageTile> tiles = new ArrayList<>(ExtendedInventoryPages.size());
	
	private boolean doubleClick = false;
	private long lastClickTime = 0L; // me when my last click was at January 1st 1970
	private int lastClickButton = -1;
	private int lastClickIndex = -1;
	
	private int dragTileIndex = -1;
	private PageTile dragTile = null;
	private int dragOffsetX = 0;
	private int dragOffsetY = 0;
	private double dragDeltaX = 0;
	private double dragDeltaY = 0;
	private boolean dragPickup = false;
	private int dragHoveredIndex = -1;
	
	private PageTile hoveredTile = null;
	
	private int scrollRow = 0;
	private double scrollAmount = 0.0;
	private boolean scrolling = false;
	
	public ExtendedInventoryOrganizeScreen() {
		super(Component.translatable("container.builders_inventory.extended_inventory.organize"));
		this.imageWidth = 213;
		this.imageHeight = 204;
	}
	
	@Override
	protected void init() {
		super.init();
		this.exGui.init();
		this.leftPos = (this.width - this.imageWidth) / 2;
		this.topPos = (this.height - this.imageHeight) / 2;
		
		this.addRenderableWidget(new ExtendedImageButton(this.leftPos + 193, this.topPos + 4, 12, 12,
				SPRITES_BUTTON_BACK,
				button -> {
					ExtendedInventory.open(this.minecraft);
				},
				Component.translatable("container.builders_inventory.extended_inventory.organize.tooltip.button.back").withStyle(ChatFormatting.WHITE)));
		
		int scrollRow = this.scrollRow;
		int scrollRowMax = scrollRow + 10;
		for (int i = 0; i < ExtendedInventoryPages.size(); i++) {
			int column = i % 10;
			int row = i / 10;
			var tile = new PageTile(this.leftPos + tileX(column), this.topPos + tileY(row - scrollRow), this, ExtendedInventoryPages.get(i), i);
			tile.visible = row >= scrollRow && row < scrollRowMax;
			tiles.add(tile);
		}
	}
	
	private static int tileX(int column) {
		return TILE_MIN_X + (column * 18);
	}
	
	private static int tileY(int row) {
		return TILE_MIN_Y + (row * 18);
	}
	
	private void scrolledToPos(double mouseX, double mouseY) {
		double scroll = (mouseY - this.topPos - 17 - 7.5) / (180 - 15);
		this.scrolledTo(Mth.clamp(scroll, 0.0, 1.0), mouseX, mouseY);
	}
	
	private void scrolledTo(double scroll, double mouseX, double mouseY) {
		this.scrollAmount = scroll;
		int row = (int)((scroll * getScrollRows()) + 0.5);
		if (row != this.scrollRow) this.setScrollRow(row, mouseX, mouseY);
	}
	
	private void setScrollRow(int scrollRow, double mouseX, double mouseY) {
		int oldRow = this.scrollRow;
		this.scrollRow = scrollRow;
		for (int i = 0; i < this.tiles.size(); i++) {
			final var tile = this.tiles.get(i);
			final int sy = tile.y - ((scrollRow - oldRow) * 18);
			final boolean continueSlowMove = tile.visible;
			final var relY = sy - TILE_MIN_Y - this.topPos;
			tile.visible = relY >= 0 && relY < 180 && tile != this.dragTile;
			if (continueSlowMove && tile.visible && tile.slowMoveTime > 0L) {
				tile.slowMove(tile.x, sy);
			} else {
				tile.slowMoveTime = 0L;
				tile.y = sy;
			}
		}
		if (this.dragTile != null) {
			var hovered = this.findNearestTile((int)mouseX, (int)mouseY);
			if (hovered.index != this.dragHoveredIndex) {
				this.shiftTiles(this.dragHoveredIndex, hovered.index);
				this.dragHoveredIndex = hovered.index;
			}
		}
	}
	
	private int getScrollRows() {
		return Mth.positiveCeilDiv(this.tiles.size(), 10) - 10;
	}
	
	// WARNING: NO guarantee that start < end
	/** ONLY CALL AFTER TRANSFORMING INDEX WITH SCROLL */
	private void shiftTiles(int start, int end) {
		if (start == end) return;
		
		// true  = move left  (drag moved right)
		// false = move right (drag moved left)
		boolean dir = start < end;
		
		int low = dir ? start : end;
		int high = dir ? end : start;
		
		for (int i = low; i <= high; i++) {
			if (i == this.dragTileIndex) continue;
			int newInd = dir ? i - 1 : i + 1;
			if (dir && i < this.dragTileIndex || !dir && i > this.dragTileIndex) newInd = i;
			if (newInd == end) continue;
			var tile = this.tiles.get(i); // index already transformed by scroll
			int row = newInd / 10;
			int relRow = row - this.scrollRow;
			if (relRow < 0 || relRow >= 10) tile.visible = false;
			tile.slowMove(this.leftPos + tileX(newInd % 10), this.topPos + tileY(relRow));
			if (tile.slowMoveZ < PageTile.TILE_Z_3) tile.slowMoveZ = (row != i / 10) ? PageTile.TILE_Z_2 : PageTile.TILE_Z_1; // do not transform row for this operation, it's just checking if the tile row changed
		}
	}
	
	/** please ensure start != end and that the index is already transformed */
	private void reorderTiles(int start, int end) {
		boolean dir = start < end;
		int from = (dir ? start : end);
		int to = (dir ? end : start) + 1;
		int dist = dir ? -1 : 1;
		for (int i = from; i < to; i++) {
			var tile = this.tiles.get(i);
			tile.index = i == start ? end : (i + dist);
			tile.page.setChanged();
		}
		Collections.rotate(this.tiles.subList(from, to), dist);
		ExtendedInventoryPages.rotatePages(from, to, dist);
		int curPage = ExtendedInventory.getPage();
		ExtendedInventory.setPage(curPage == start ? end : curPage + dist);
	}
	
	@Nullable
	private TileFindResult findTile(int x, int y) {
		x -= 8 + this.leftPos;
		y -= 17 + this.topPos;
		if (x >= 0 && x < 180 && y >= 0 && y < 180) {
			int col = x / 18;
			int rx = x % 18;
			int row = y / 18;
			int ry = y % 18;
			int index = col + (row * 10);
			index += (this.scrollRow * 10);
			if (index < this.tiles.size()) return new TileFindResult(index, this.tiles.get(index), rx - 1, ry - 1, col, row);
		}
		return null;
	}
	
	@NotNull
	private TileFindResult findNearestTile(int x, int y) {
		x -= 8 + this.leftPos;
		y -= 17 + this.topPos;
		if (x < 0) x = 0;
		else if (x >= 180) x = 179;
		if (y < 0) y = 0;
		else if (y >= 180) y = 179;
		
		int col = x / 18;
		int rx = x % 18;
		int row = y / 18;
		int ry = y % 18;
		
		int index = col + (row * 10);
		index += (this.scrollRow * 10);
		if (index >= tiles.size()) index = tiles.size() - 1;
		return new TileFindResult(index, tiles.get(index), rx - 1, ry - 1, col, row);
	}
	
	@Override
	protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget) {
		this.exGui.addRenderableWidget(widget);
		return super.addRenderableWidget(widget);
	}
	
	@Override
	protected void clearWidgets() {
		this.exGui.clearWidgets();
		this.tiles.clear();
		super.clearWidgets();
	}
	
	@Override
	public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		super.render(gui, mouseX, mouseY, partialTick);
		gui.drawString(this.font, this.title, this.leftPos + 8, this.topPos + 6, 0xFF404040, false);
		
		if (this.dragTile != null) {
			int col = this.dragHoveredIndex % 10;
			int row = (this.dragHoveredIndex / 10) - this.scrollRow;
			gui.blitSprite(RenderType::guiTextured, SPRITE_TILE_SHADOW, this.leftPos + tileX(col) + 1, this.topPos + tileY(row) + 1, 16, 16);
			this.renderTiles(gui, mouseX, mouseY, partialTick);
//			gui.fill(this.leftPos + tileX(col) + 1, this.topPos + tileY(row) + 1, this.leftPos + tileX(col) + 1 + 16, this.topPos + tileY(row) + 1 + 16, 1, 0xBB77BB77); //DEBUG THING
			gui.blitSprite(RenderType::guiTextured, this.dragTileIndex == ExtendedInventory.getPage() ? SPRITE_TILE_ACTIVE_SELECTED : SPRITE_TILE_SELECTED, mouseX - this.dragOffsetX, mouseY - this.dragOffsetY, PageTile.TILE_Z_4, 16, 16);
			if (this.dragTile.page.icon.isEmpty()) {
				this.renderTileText(gui, this.dragTileIndex + 1, mouseX - this.dragOffsetX + 8, mouseY - this.dragOffsetY + 4, PageTile.TILE_Z_4);
			} else {
				this.renderTileIcon(gui, this.dragTile.page.icon, this.dragTile.page.iconScaleDown, mouseX - this.dragOffsetX, mouseY - this.dragOffsetY, PageTile.TILE_Z_4);
			}
			if (!this.dragPickup) this.renderTileTooltip(this.dragTile, this.dragTileIndex, gui, mouseX, mouseY, PageTile.TILE_Z_4);
		} else {
			this.renderTiles(gui, mouseX, mouseY, partialTick);
			final var hover = findTile(mouseX, mouseY);
			this.hoveredTile = hover == null ? null : hover.tile;
			this.renderTooltip(gui, hover, mouseX, mouseY, PageTile.TILE_Z_4);
		}
	}
	
	private void renderTiles(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		for (var tile : this.tiles) {
			tile.render(gui, mouseX, mouseY, partialTick);
		}
	}
	
	protected void renderTooltip(GuiGraphics gui, TileFindResult hover, int mouseX, int mouseY, int z) {
		if (hover != null && hover.tile.isActive()) {
			renderTileTooltip(hover.tile, hover.index, gui, mouseX, mouseY, z);
			return;
		}
		this.exGui.renderTooltip(this.font, gui, mouseX, mouseY);
	}
	
	void renderTileText(GuiGraphics gui, int number, int x, int y, int z) {
		String text = String.valueOf(number);
		gui.pose().pushPose();
		gui.pose().translate(x, y, z);
		if (text.length() > 2) {
			float scale = 2f / text.length();
			gui.pose().translate(0, 4 * (1f - scale), 0); // (8*scale - 8) / 2
			gui.pose().scale(scale, scale, 1f);
		}
		gui.drawCenteredString(this.font, text, 0, 0, 0xFFFFFFFF);
		gui.pose().popPose();
	}
	
	void renderTileIcon(GuiGraphics gui, ItemStack icon, int iconScaleDown, int x, int y, int z) {
		gui.pose().pushPose();
		gui.pose().translate(x, y, z);
		if (iconScaleDown > 0) {
			final int guiScale = (int) (this.minecraft.getWindow().getGuiScale() + 0.5);
			final float iconScale = iconScaleDown >= guiScale ? (1f / guiScale) : (1f - (iconScaleDown / (float)guiScale));
			gui.pose().translate((8 * (1f - iconScale)), (8 * (1f - iconScale)), 0);
			gui.pose().scale(iconScale, iconScale, 1);
		}
		gui.renderItem(icon, 0, 0);
		gui.renderItemDecorations(this.font, icon, 0, 0);
		gui.pose().popPose();
	}
	
	private void renderTileTooltip(PageTile tile, int index, GuiGraphics gui, int mouseX, int mouseY, int z) {
		gui.pose().pushPose();
		gui.pose().translate(0, 0, z);
		gui.renderTooltip(this.font,
				List.of(ExtendedInventory.pageTitle(index)),
				Optional.of(new PageTooltipImage(tile.page)),
				mouseX,
				mouseY);
		gui.pose().popPose();
	}
	
	private boolean isMouseInScrollbar(int mouseX, int mouseY) {
		return mouseX >= 192 && mouseX < 192 + 14 && mouseY >= 17 && mouseY < 17 + 180;
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (super.mouseClicked(mouseX, mouseY, button)) {
			return true;
		} else if (this.canScroll() && this.isMouseInScrollbar((int)mouseX - this.leftPos, (int)mouseY - this.topPos)) {
			this.scrolling = true;
			this.scrolledToPos(mouseX, mouseY);
			return true;
		} else {
			boolean consume = false;
			
			long time = Util.getMillis();
			
			var tile = findTile((int)mouseX, (int)mouseY);
			if (tile != null) {
				this.startDrag(tile.tile, tile.index, tile.rx, tile.ry);
				consume = true;
			}
			
			this.doubleClick = tile != null && tile.index == this.lastClickIndex && time - this.lastClickTime < 300L && this.lastClickButton == button;
			this.lastClickTime = time;
			this.lastClickButton = button;
			this.lastClickIndex = tile == null ? -1 : tile.index;
			return consume;
		}
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
			return true;
		} else if (this.scrolling) {
			this.scrolledToPos(mouseX, mouseY);
			return true;
		} else {
			if (this.dragTile != null) {
				this.dragDeltaX += dragX;
				this.dragDeltaY += dragY;
				if (!this.dragPickup) {
					if (Math.abs(this.dragDeltaX) < 1.5f && Math.abs(this.dragDeltaY) < 1.5f) return true;
					else this.dragPickup = true;
				}
				var hovered = this.findNearestTile((int)mouseX, (int)mouseY);
				if (hovered.index != this.dragHoveredIndex) {
					this.shiftTiles(this.dragHoveredIndex, hovered.index);
					this.dragHoveredIndex = hovered.index;
				}
				return true;
			}
			
			return false;
		}
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		this.scrolling = false;
		if (super.mouseReleased(mouseX, mouseY, button)) {
			return true;
		} else {
			boolean consume = false;
			
			if (this.dragTile != null) {
				int col = this.dragHoveredIndex % 10;
				int row = (this.dragHoveredIndex / 10) - this.scrollRow;
				this.endDrag((int)mouseX, (int)mouseY, this.leftPos + tileX(col), this.topPos + tileY(row));
				if (this.dragTileIndex != this.dragHoveredIndex) {
					this.reorderTiles(this.dragTileIndex, this.dragHoveredIndex);
				}
				consume = true;
			}
			
			if (this.doubleClick) {
				this.doubleClick = false;
				if (this.lastClickIndex > -1 && Util.getMillis() - this.lastClickTime < 300L) {
					this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
					ExtendedInventory.setPage(this.lastClickIndex);
					ExtendedInventory.open(this.minecraft);
					consume = true;
				}
			}
			
			return consume;
		}
	}
	
	private void startDrag(PageTile tile, int index, int rx, int ry) {
		tile.visible = false;
		this.dragTile = tile;
		this.dragTileIndex = index;
		this.dragOffsetX = rx;
		this.dragOffsetY = ry;
		this.dragDeltaX = 0;
		this.dragDeltaY = 0;
		this.dragPickup = false;
		this.dragHoveredIndex = index;
	}
	
	private void endDrag(int mouseX, int mouseY, int targetX, int targetY) {
		this.dragTile.visible = true;
		this.dragTile.slowMoveFrom(mouseX - this.dragOffsetX, mouseY - this.dragOffsetY, targetX, targetY);
		this.dragTile.slowMoveZ = PageTile.TILE_Z_3;
		this.dragTile.slowMoveShadow = true;
		this.dragTile = null;
	}
	
	@Override
	public void renderBackground(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		this.renderTransparentBackground(gui);
		GuiUtil.blitScreenBackground(gui, BACKGROUND, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
		
		ResourceLocation scrollSprite = this.canScroll() ? SPRITE_CREATIVE_SCROLLER : SPRITE_CREATIVE_SCROLLER_DISABLED;
		gui.blitSprite(RenderType::guiTextured, scrollSprite, this.leftPos + 193, this.topPos + 18 + (int)((178 - 15) * this.scrollAmount), 12, 15);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (!this.canScroll() || this.scrolling) {
			return false;
		} else {
			this.scrolledTo(Mth.clamp(this.scrollAmount - (scrollY / this.getScrollRows()), 0.0, 1.0), mouseX, mouseY);
			return true;
		}
	}
	
	private boolean canScroll() {
		return this.tiles.size() > 100;
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
			this.onClose();
			return true;
		} else if (ModKeybinds.OPEN_EXTENDED_INVENTORY.matches(keyCode, scanCode)) {
			ExtendedInventory.open(this.minecraft);
			return true;
		} else return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public void onClose() {
		if (Config.instance().extended_inventory_close_to_main) {
			ExtendedInventory.open(this.minecraft);
		} else super.onClose();
	}
	
	@Override
	public boolean isPauseScreen() {
		return false;
	}
	
	private static class PageTile {
		
		public static final int TILE_Z_1 = 200;
		public static final int TILE_Z_2 = 400;
		public static final int TILE_Z_3 = 600;
		public static final int TILE_Z_4 = 800;
		
		public int x;
		public int y;
		public boolean visible = true;
		public boolean active = true;
		
		public final ExtendedInventoryOrganizeScreen screen;
		public final ExtendedInventoryPage page;
		public int index = -1;
		private long slowMoveTime = 0L;
		private int slowMoveStartX = 0;
		private int slowMoveStartY = 0;
		private float slowMoveLength = 200f;
		public int slowMoveZ = 0;
		public boolean slowMoveShadow = false;
		
		public PageTile(int x, int y, ExtendedInventoryOrganizeScreen screen, ExtendedInventoryPage page, int index) {
			this.x = x;
			this.y = y;
			this.screen = screen;
			this.page = page;
			this.index = index;
		}
		
		public void slowMoveFrom(int originX, int originY, int targetX, int targetY) {
			this.slowMoveTime = Util.getMillis();
			this.slowMoveStartX = originX;
			this.slowMoveStartY = originY;
			int distX = originX - targetX;
			int distY = originY - targetY;
			int distSqr = distX * distX + distY * distY;
			this.slowMoveLength = Math.min((distSqr / 3f) + 50f, 200f);
			this.slowMoveZ = 0;
			this.slowMoveShadow = false;
			this.setPosition(targetX, targetY);
		}
		
		public void slowMove(int targetX, int targetY) {
			int x = this.x + 1;
			int y = this.y + 1;
			if (this.slowMoveTime <= 0L) this.slowMoveFrom(x, y, targetX, targetY);
			float progress = (Util.getMillis() - this.slowMoveTime) / this.slowMoveLength;
			if (progress >= 1.0f) {
				this.slowMoveFrom(x, y, targetX, targetY);
			} else {
				int mx = this.slowMoveStartX + (int)((x - this.slowMoveStartX) * progress);
				int my = this.slowMoveStartY + (int)((y - this.slowMoveStartY) * progress);
				this.slowMoveFrom(mx, my, targetX, targetY);
			}
		}
		
		protected void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
			if (!this.visible) return;
			int x = this.x + 1;
			int y = this.y + 1;
			boolean moving = this.slowMoveTime > 0L;
			WidgetSprites sprites = this.index == ExtendedInventory.getPage() ? SPRITES_TILE_ACTIVE : SPRITES_TILE;
			ResourceLocation sprite = sprites.get(this.isActive(), this.screen.dragTile == null && this == this.screen.hoveredTile);
			if (moving) {
				float progress = (Util.getMillis() - this.slowMoveTime) / this.slowMoveLength;
				if (progress >= 1.0f) {
					progress = 1.0f;
					this.slowMoveTime = 0L;
				}
				int mx = this.slowMoveStartX + (int)((x - this.slowMoveStartX) * progress);
				int my = this.slowMoveStartY + (int)((y - this.slowMoveStartY) * progress);
				if (this.slowMoveShadow) gui.blitSprite(RenderType::guiTextured, SPRITE_TILE_SHADOW, x, y, 16, 16);
				gui.pose().pushPose();
				gui.pose().translate(0, 0, this.slowMoveZ);
				gui.blitSprite(RenderType::guiTextured, sprite, mx, my, 16, 16);
				gui.pose().popPose();
				if (this.page.icon.isEmpty()) {
					this.screen.renderTileText(gui, this.index + 1, mx + 8, my + 4, this.slowMoveZ);
				} else {
					this.screen.renderTileIcon(gui, this.page.icon, this.page.iconScaleDown, mx, my, this.slowMoveZ);
				}
			} else {
				gui.blitSprite(RenderType::guiTextured, sprite, x, y, 16, 16);
				if (this.page.icon.isEmpty()) {
					this.screen.renderTileText(gui, this.index + 1, x + 8, y + 4, 0);
				} else {
					this.screen.renderTileIcon(gui, this.page.icon, this.page.iconScaleDown, x, y, 0);
				}
			}
		}
		
		private void setPosition(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		public boolean isActive() {
			return this.visible && this.active;
		}
		
	}
	
	public static class PageTooltipImage implements ClientTooltipComponent, TooltipComponent {
		
		private final ExtendedInventoryPage page;
		
		public PageTooltipImage(ExtendedInventoryPage page) {
			this.page = page;
		}
		
		@Override
		public int getWidth(Font font) {
			return 174;
		}
		
		@Override
		public int getHeight(Font font) {
			return 120;
		}
		
		@Override
		public void renderImage(Font font, int x, int y, int width, int height, GuiGraphics gui) {
			ResourceLocation background = this.page.valid ? this.page.isLocked() ? SPRITE_BACKGROUND_LOCKED : SPRITE_BACKGROUND : SPRITE_BACKGROUND_INVALID;
			gui.blitSprite(RenderType::guiTextured, background, x, y, this.getWidth(font), this.getHeight(font));
			if (!this.page.valid) return;
			int slot = 0;
			for (int row = 0; row < 6; row++) {
				int slotY = y + (row * 18) + 6 + 1;
				for (int col = 0; col < 9; col++) {
					int slotX = x + (col * 18) + 6 + 1;
					ItemStack stack = this.page.get(slot++);
					gui.renderItem(stack, slotX, slotY);
					gui.renderItemDecorations(font, stack, slotX, slotY);
				}
			}
		}
		
	}
	
	private static class TileFindResult {
		
		public final int index;
		public final PageTile tile;
		public final int rx;
		public final int ry;
		@SuppressWarnings("unused")
		public final int col;
		@SuppressWarnings("unused")
		public final int row;
		
		public TileFindResult(int index, PageTile tile, int rx, int ry, int col, int row) {
			this.index = index;
			this.tile = tile;
			this.rx = rx;
			this.ry = ry;
			this.col = col;
			this.row = row;
		}
		
	}
	
}
