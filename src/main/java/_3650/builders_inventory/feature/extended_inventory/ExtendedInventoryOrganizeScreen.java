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
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public class ExtendedInventoryOrganizeScreen extends Screen {
	
	private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "textures/gui/container/extended_inventory_organize.png");
	
	private static final ResourceLocation SPRITE_CREATIVE_SCROLLER = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller");
	private static final ResourceLocation SPRITE_CREATIVE_SCROLLER_DISABLED = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller_disabled");
	
	private static final WidgetSprites SPRITES_BUTTON_BACK = new WidgetSprites(
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/organize/button_back"),
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/organize/button_back_highlighted"));
	
	private static final ResourceLocation SPRITE_TILE = ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/organize/tile");
	private static final ResourceLocation SPRITE_TILE_SELECTED = ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/organize/tile_highlighted");
	private static final ResourceLocation SPRITE_TILE_SHADOW = ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/organize/tile_shadow");
	private static final WidgetSprites SPRITES_TILE = new WidgetSprites(
			SPRITE_TILE,
			SPRITE_TILE_SELECTED);
	private static final ResourceLocation SPRITE_TILE_ACTIVE = ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/organize/tile_active");
	private static final ResourceLocation SPRITE_TILE_ACTIVE_SELECTED = ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/organize/tile_active_highlighted");
	private static final WidgetSprites SPRITES_TILE_ACTIVE = new WidgetSprites(
			SPRITE_TILE_ACTIVE,
			SPRITE_TILE_ACTIVE_SELECTED);
	private static final ResourceLocation SPRITE_TILE_CREATE = ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/organize/tile_create");
	private static final ResourceLocation SPRITE_TILE_CREATE_SELECTED = ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/organize/tile_create_highlighted");
	@SuppressWarnings("unused")
	private static final WidgetSprites SPRITES_TILE_CREATE = new WidgetSprites(
			SPRITE_TILE_CREATE,
			SPRITE_TILE_CREATE_SELECTED);
	@SuppressWarnings("unused")
	private static final ResourceLocation SPRITE_TILE_CREATE_DIM = ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/organize/tile_create_dim");
	
	private static final ResourceLocation SPRITE_BACKGROUND = ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/organize/hover_background");
	private static final ResourceLocation SPRITE_BACKGROUND_LOCKED = ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/organize/hover_background_locked");
	private static final ResourceLocation SPRITE_BACKGROUND_INVALID = ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/organize/hover_background_invalid");
	
	private final ExtendedImageButtonGui exGui = new ExtendedImageButtonGui();
	private final int imageWidth;
	private final int imageHeight;
	
	private int leftPos;
	private int topPos;
	
	private final ArrayList<PageTileWidget> tiles = new ArrayList<>(ExtendedInventoryPages.size());
	
	private boolean doubleClick = false;
	private long lastClickTime = 0L; // me when my last click was at January 1st 1970
	private int lastClickButton = -1;
	private int lastClickIndex = -1;
	
	private int dragTileIndex = -1;
	private PageTileWidget dragTile = null;
	private int dragOffsetX = 0;
	private int dragOffsetY = 0;
	private double dragDeltaX = 0;
	private double dragDeltaY = 0;
	private boolean dragPickup = false;
	private int lastHoveredIndex = -1;
	
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
		
		this.addRenderableWidget(new ExtendedImageButton(this.leftPos + 193, this.topPos + 4, 12, 12, SPRITES_BUTTON_BACK,
				button -> {
					ExtendedInventory.open(this.minecraft);
				},
				Component.translatable("container.builders_inventory.extended_inventory.organize.tooltip.button.back").withStyle(ChatFormatting.WHITE)));
		
		this.tiles.clear();
		int scrollRow = this.scrollRow;
		int scrollRowMax = scrollRow + 10;
		for (int i = 0; i < ExtendedInventoryPages.size(); i++) {
			int column = i % 10;
			int row = i / 10;
			var tile = new PageTileWidget(this.leftPos + tileX(column), this.topPos + tileY(row - scrollRow), 18, 18, this, ExtendedInventoryPages.get(i), i);
			tile.visible = row >= scrollRow && row < scrollRowMax;
			this.addRenderableWidget(tile);
			tiles.add(tile);
		}
	}
	
	private static int tileX(int column) {
		return 8 + (column * 18);
	}
	
	private static int tileY(int row) {
		return 17 + (row * 18);
	}
	
	private void scrolledToPos(double mouseY) {
		double scroll = (mouseY - this.topPos - 17 - 7.5) / (180 - 15);
		this.scrolledTo(Mth.clamp(scroll, 0.0, 1.0));
	}
	
	private void scrolledTo(double scroll) {
		this.scrollAmount = scroll;
		int row = (int)((scroll * getScrollRows()) + 0.5);
		if (row != this.scrollRow) this.setScrollRow(row);
	}
	
	private void setScrollRow(int scrollRow) {
		this.scrollRow = scrollRow;
		int scrollRowMax = scrollRow + 10;
		for (int i = 0; i < this.tiles.size(); i++) {
			int column = i % 10;
			int row = i / 10;
			var tile = this.tiles.get(i);
			boolean continueSlowMove = tile.visible;
			tile.visible = row >= scrollRow && row < scrollRowMax;
			tile.snapPosition(this.leftPos + tileX(column), this.topPos + tileY(row - scrollRow), continueSlowMove);
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
			tile.slowMove(this.leftPos + tileX(newInd % 10), this.topPos + tileY(row - this.scrollRow));
			if (tile.slowMoveZ < PageTileWidget.TILE_Z_3) tile.slowMoveZ = (row != i / 10) ? PageTileWidget.TILE_Z_2 : PageTileWidget.TILE_Z_1; // do not transform row for this operation, it's just checking if the tile row changed
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
		x -= 8;
		y -= 17;
		if (x >= 0 && x <= 180 && y >= 0 && y <= 180) {
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
		x -= 8;
		y -= 17;
		if (x < 0) x = 0;
		else if (x > 180) x = 180;
		if (y < 0) y = 0;
		else if (y > 180) y = 180;
		
		int col = x / 18;
		int rx = x % 18;
		int row = y / 18;
		int ry = y % 18;
		
		if (row > tiles.size() / 10) {
			row = (tiles.size() / 10);
			if (col > tiles.size() % 10) col = (tiles.size() % 10);
		}
		
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
		super.clearWidgets();
	}
	
	@Override
	public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		super.render(gui, mouseX, mouseY, partialTick);
		gui.drawString(this.font, this.title, this.leftPos + 8, this.topPos + 6, 0x404040, false);
		
		if (this.dragTile != null) {
			int col = this.lastHoveredIndex % 10;
			int row = (this.lastHoveredIndex / 10) - this.scrollRow;
			gui.blitSprite(SPRITE_TILE_SHADOW, this.leftPos + tileX(col) + 1, this.topPos + tileY(row) + 1, 16, 16);
//			gui.fill(this.leftPos + tileX(col) + 1, this.topPos + tileY(row) + 1, this.leftPos + tileX(col) + 1 + 16, this.topPos + tileY(row) + 1 + 16, 1, 0xBB77BB77); //DEBUG THING
			gui.blitSprite(this.dragTileIndex == ExtendedInventory.getPage() ? SPRITE_TILE_ACTIVE_SELECTED : SPRITE_TILE_SELECTED, mouseX - this.dragOffsetX, mouseY - this.dragOffsetY, PageTileWidget.TILE_Z_4, 16, 16);
			if (this.dragTile.page.icon.isEmpty()) {
				this.renderTileText(this.dragTileIndex + 1, gui, mouseX - this.dragOffsetX + 8, mouseY - this.dragOffsetY + 4, PageTileWidget.TILE_Z_4);
			} else {
				this.renderTileIcon(this.dragTile.page.icon, this.dragTile.page.iconScaleDown, gui, mouseX - this.dragOffsetX, mouseY - this.dragOffsetY, PageTileWidget.TILE_Z_4);
			}
			if (!this.dragPickup) this.renderTileTooltip(this.dragTile, this.dragTileIndex, gui, mouseX, mouseY, PageTileWidget.TILE_Z_4);
		} else {
			this.renderTooltip(gui, mouseX, mouseY, PageTileWidget.TILE_Z_4);
		}
		
	}
	
	protected void renderTooltip(GuiGraphics gui, int mouseX, int mouseY, int z) {
		for (int i = (this.scrollRow * 10); i < this.tiles.size(); i++) {
			var tile = this.tiles.get(i);
			if (tile.isActive() && tile.isHovered()) {
				renderTileTooltip(tile, i, gui, mouseX, mouseY, z);
				return;
			}
		}
		this.exGui.renderTooltip(this.font, gui, mouseX, mouseY);
	}
	
	void renderTileText(int number, GuiGraphics gui, int x, int y, int z) {
		String text = String.valueOf(number);
		gui.pose().pushPose();
		gui.pose().translate(x, y, z);
		if (text.length() > 2) {
			float scale = 2f / text.length();
			gui.pose().translate(0, 4 * (1f - scale), 0); // (8*scale - 8) / 2
			gui.pose().scale(scale, scale, 1f);
		}
		gui.drawCenteredString(this.font, Component.literal(text), 0, 0, 0xFFFFFF);
		gui.pose().popPose();
	}
	
	void renderTileIcon(ItemStack icon, int iconScaleDown, GuiGraphics gui, int x, int y, int z) {
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
	
	private void renderTileTooltip(PageTileWidget tile, int index, GuiGraphics gui, int mouseX, int mouseY, int z) {
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
			this.scrolledToPos(mouseY);
			return true;
		} else {
			boolean consume = false;
			
			long time = Util.getMillis();
			
			var tile = findTile((int)mouseX - this.leftPos, (int)mouseY - this.topPos);
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
			this.scrolledToPos(mouseY);
			return true;
		} else {
			if (this.dragTile != null) {
				this.dragDeltaX += dragX;
				this.dragDeltaY += dragY;
				if (!this.dragPickup) {
					if (Math.abs(this.dragDeltaX) < 1.5f && Math.abs(this.dragDeltaY) < 1.5f) return true;
					else this.dragPickup = true;
				}
				var hovered = this.findNearestTile((int)mouseX - this.leftPos, (int)mouseY - this.topPos);
				if (hovered.index != this.lastHoveredIndex) {
					this.shiftTiles(this.lastHoveredIndex, hovered.index);
					this.lastHoveredIndex = hovered.index;
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
				int col = this.lastHoveredIndex % 10;
				int row = (this.lastHoveredIndex / 10) - this.scrollRow;
				this.endDrag((int)mouseX, (int)mouseY, this.leftPos + tileX(col), this.topPos + tileY(row));
				if (this.dragTileIndex != this.lastHoveredIndex) {
					this.reorderTiles(this.dragTileIndex, this.lastHoveredIndex);
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
	
	private void startDrag(PageTileWidget tile, int index, int rx, int ry) {
		tile.visible = false;
		this.dragTile = tile;
		this.dragTileIndex = index;
		this.dragOffsetX = rx;
		this.dragOffsetY = ry;
		this.dragDeltaX = 0;
		this.dragDeltaY = 0;
		this.dragPickup = false;
		this.lastHoveredIndex = index;
	}
	
	private void endDrag(int mouseX, int mouseY, int targetX, int targetY) {
		this.dragTile.visible = true;
		this.dragTile.slowMoveFrom(mouseX - this.dragOffsetX, mouseY - this.dragOffsetY, targetX, targetY);
		this.dragTile.slowMoveZ = PageTileWidget.TILE_Z_3;
		this.dragTile.slowMoveShadow = true;
		this.dragTile = null;
	}
	
	@Override
	public void renderBackground(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		this.renderTransparentBackground(gui);
		gui.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		
		ResourceLocation scrollSprite = this.canScroll() ? SPRITE_CREATIVE_SCROLLER : SPRITE_CREATIVE_SCROLLER_DISABLED;
		gui.blitSprite(scrollSprite, this.leftPos + 193, this.topPos + 18 + (int)((178 - 15) * this.scrollAmount), 12, 15);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (!this.canScroll() || this.scrolling) {
			return false;
		} else {
			this.scrolledTo(Mth.clamp(this.scrollAmount - (scrollY / this.getScrollRows()), 0.0, 1.0));
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
	public boolean isPauseScreen() {
		return false;
	}
	
	private static class PageTileWidget extends AbstractWidget {
		
		public static final int TILE_Z_1 = 200;
		public static final int TILE_Z_2 = 400;
		public static final int TILE_Z_3 = 600;
		public static final int TILE_Z_4 = 800;
		
		public final ExtendedInventoryOrganizeScreen screen;
		public final ExtendedInventoryPage page;
		public int index = -1;
		private long slowMoveTime = 0L;
		private int slowMoveStartX = 0;
		private int slowMoveStartY = 0;
		private float slowMoveLength = 200f;
		public int slowMoveZ = 0;
		public boolean slowMoveShadow = false;
		private boolean forceVisible = false;
		
		public PageTileWidget(int x, int y, int width, int height, ExtendedInventoryOrganizeScreen screen, ExtendedInventoryPage page, int index) {
			super(x, y, width, height, CommonComponents.EMPTY);
			this.screen = screen;
			this.page = page;
			this.index = index;
		}
		
		public void snapPosition(int x, int y, boolean continueSlowMove) {
			if (continueSlowMove && this.visible && this.slowMoveTime > 0L) {
				this.slowMove(x, y);
			} else {
				this.slowMoveTime = 0L;
				this.setPosition(x, y);
			}
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
			this.forceVisible = false;
			this.setPosition(targetX, targetY);
		}
		
		public void slowMove(int targetX, int targetY) {
			int x = this.getX() + 1;
			int y = this.getY() + 1;
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
		
		@Override
		protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		}
		
		@Override
		public void playDownSound(SoundManager handler) {
		}
		
		@Nullable
		@Override
		public ComponentPath nextFocusPath(FocusNavigationEvent event) {
			return null;
		}
		
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			return false;
		}
		
		@Override
		public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
			return false;
		}
		
		@Override
		public boolean mouseReleased(double mouseX, double mouseY, int button) {
			return false;
		}
		
		@Override
		protected void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
			int x = this.getX() + 1;
			int y = this.getY() + 1;
			boolean moving = this.slowMoveTime > 0L;
			WidgetSprites sprites = this.index == ExtendedInventory.getPage() ? SPRITES_TILE_ACTIVE : SPRITES_TILE;
			ResourceLocation sprite = sprites.get(this.isActive(), this.screen.dragTile == null && this.isHoveredOrFocused());
			if (moving) {
				float progress = (Util.getMillis() - this.slowMoveTime) / this.slowMoveLength;
				if (progress >= 1.0f) {
					progress = 1.0f;
					this.slowMoveTime = 0L;
					if (this.forceVisible) {
						this.forceVisible = false;
						this.visible = false;
					}
				}
				int mx = this.slowMoveStartX + (int)((x - this.slowMoveStartX) * progress);
				int my = this.slowMoveStartY + (int)((y - this.slowMoveStartY) * progress);
				if (this.slowMoveShadow) gui.blitSprite(SPRITE_TILE_SHADOW, x, y, 16, 16);
				gui.blitSprite(sprite, mx, my, this.slowMoveZ, 16, 16);
				if (this.page.icon.isEmpty()) {
					this.screen.renderTileText(this.index + 1, gui, mx + 8, my + 4, this.slowMoveZ);
				} else {
					this.screen.renderTileIcon(this.page.icon, this.page.iconScaleDown, gui, mx, my, this.slowMoveZ);
				}
			} else {
				gui.blitSprite(sprite, x, y, 16, 16);
				if (this.page.icon.isEmpty()) {
					this.screen.renderTileText(this.index + 1, gui, x + 8, y + 4, 0);
				} else {
					this.screen.renderTileIcon(this.page.icon, this.page.iconScaleDown, gui, x, y, 0);
				}
			}
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
		public int getHeight() {
			return 120;
		}
		
		@Override
		public void renderImage(Font font, int x, int y, GuiGraphics gui) {
			ResourceLocation background = this.page.valid ? this.page.isLocked() ? SPRITE_BACKGROUND_LOCKED : SPRITE_BACKGROUND : SPRITE_BACKGROUND_INVALID;
			gui.blitSprite(background, x, y, this.getWidth(font), this.getHeight());
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
		public final PageTileWidget tile;
		public final int rx;
		public final int ry;
		@SuppressWarnings("unused")
		public final int col;
		@SuppressWarnings("unused")
		public final int row;
		
		public TileFindResult(int index, PageTileWidget tile, int rx, int ry, int col, int row) {
			this.index = index;
			this.tile = tile;
			this.rx = rx;
			this.ry = ry;
			this.col = col;
			this.row = row;
		}
		
	}
	
}
