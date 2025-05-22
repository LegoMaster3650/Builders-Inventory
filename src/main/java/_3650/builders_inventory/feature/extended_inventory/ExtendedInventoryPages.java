package _3650.builders_inventory.feature.extended_inventory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import _3650.builders_inventory.BuildersInventory;
import _3650.builders_inventory.config.Config;
import _3650.builders_inventory.datafixer.ModDataFixer;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;

public class ExtendedInventoryPages {
	
	private static final String FILE_PREFIX = "inventory-";
	private static final String FILE_SUFFIX = "-page.nbt";
	
	private static final ArrayBlockingQueue<Component> PLAYER_MESSAGE_QUEUE = new ArrayBlockingQueue<>(50);
	private static final ArrayList<ExtendedInventoryPage> PAGES = new ArrayList<>();
	private static int deleted = 0;
	private static boolean loaded = false;
	private static boolean valid = false;
	private static int timeToSave = 0;
	private static boolean hasChanged = false;
	private static boolean forceUpdate = false;
	
	static void tick(Minecraft mc) {
		if (hasChanged && --timeToSave <= 0) save();
		
		if (!PLAYER_MESSAGE_QUEUE.isEmpty() && mc.player != null) {
			ArrayList<Component> messages = new ArrayList<>();
			PLAYER_MESSAGE_QUEUE.drainTo(messages);
			for (var msg : messages) mc.player.sendSystemMessage(msg);
		}
	}
	
	public static void setChanged() {
		if (!loaded) load();
		if (!valid) return;
		
		hasChanged = true;
		timeToSave = Config.instance().extended_inventory_save_delay * 20;
	}
	
	static void forceUpdate() {
		forceUpdate = true;
	}
	
	public static void load() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level != null) load(mc.level.registryAccess().createSerializationContext(NbtOps.INSTANCE));
		else if (mc.getConnection() != null) load(mc.getConnection().registryAccess().createSerializationContext(NbtOps.INSTANCE));
		else {
			BuildersInventory.LOGGER.error("Error loading extended inventory saved data: Not in-game!");
			PLAYER_MESSAGE_QUEUE.add(Component.translatable("error.builders_inventory.extended_inventory.load_failed"));
		}
	}
	
	public static void load(RegistryOps<Tag> registryOps) {
		BuildersInventory.LOGGER.info("Loading Extended Inventory...");
		BuildersInventory.LOGGER.info("Hey Log Readers: LOGS ARE ZERO-INDEXED");
		
		if (loaded && valid && hasChanged) {
			BuildersInventory.LOGGER.info("Must save extended inventory before reloading");
			if (save(registryOps)) {
				BuildersInventory.LOGGER.info("Saved");
			} else {
				BuildersInventory.LOGGER.error("Error loading extended inventory saved data: Could not save first!");
				PLAYER_MESSAGE_QUEUE.add(Component.translatable("error.builders_inventory.extended_inventory.load_failed"));
				return;
			}
		}
		
		loaded = true;
		valid = false;
		
		PAGES.clear();
		ExtendedInventory.PAGE_CONTAINER.reset();
		
		Path root = FabricLoader.getInstance().getGameDir().resolve(BuildersInventory.MOD_ID).resolve("extended_inventory");
		try {
			if (!Files.isDirectory(root)) {
				Path oldRoot = FabricLoader.getInstance().getConfigDir().resolve(BuildersInventory.MOD_ID);
				if (Files.isDirectory(oldRoot)) {
					root = oldRoot;
					forceUpdate = true;
				}
			}
			
			// Get possible filenames
			String[] filenames = root.toFile().list((dir, name) -> name.startsWith(FILE_PREFIX) && name.endsWith(FILE_SUFFIX));
			if (filenames == null) filenames = new String[0];
			
			// order and number validity NOT guaranteed, construct array of files and save max id to fill in blanks
			int substrStart = FILE_PREFIX.length();
			int substrEnd = FILE_SUFFIX.length();
			int max = -1;
			var pageMap = new Int2ObjectOpenHashMap<ExtendedInventoryPage>(filenames.length);
			for (var name : filenames) {
				String idStr = name.substring(substrStart, name.length() - substrEnd);
				try {
					int id = Integer.parseInt(idStr) - 1;
					
					var optPage = loadPage(registryOps, root.resolve(name), id);
					if (optPage.isPresent()) {
						if (id > max) max = id;
						pageMap.put(id, optPage.get());
					}
					
				} catch (NumberFormatException e) {
					BuildersInventory.LOGGER.error("Error loading extended inventory page " + idStr + ": Invalid Integer!", e);
					continue;
				}
			}
			
			// Load items in, filling in the blanks
			for (int i = 0; i < max + 1; i++) {
				if (pageMap.containsKey(i)) PAGES.add(pageMap.get(i));
				else {
					BuildersInventory.LOGGER.warn("Could not find extended inventory page {}", i);
					PAGES.add(ExtendedInventoryPage.INVALID);
				}
			}
			BuildersInventory.LOGGER.info("Loaded {} extended inventory pages...", PAGES.size());
			
			// Set valid, it loaded at least and everything after this fails if invalid
			valid = true;
			
			// If no pages loaded, make a blank page
			if (PAGES.isEmpty()) create();
			
			// Load Saved Data
			if (Files.isRegularFile(root.resolve("inventory-data.nbt"))) {
				CompoundTag tag = NbtIo.read(root.resolve("inventory-data.nbt"));
				if (tag == null) {
					BuildersInventory.LOGGER.error("Error loading extended inventory saved data: Invalid Data!");
				}
				
				if (tag.contains("page", Tag.TAG_INT)) {
					int savedPage = tag.getInt("page");
					if (savedPage >= 0 && savedPage < PAGES.size()) {
						ExtendedInventory.PAGE_CONTAINER.setPage(savedPage);
						BuildersInventory.LOGGER.info("Loaded selected page as {}...", savedPage);
					} else if (savedPage >= 0 && PAGES.size() > 0) {
						int newPage = Math.max(0, PAGES.size() - 1);
						ExtendedInventory.PAGE_CONTAINER.setPage(newPage);
						BuildersInventory.LOGGER.warn("Loaded page out of bounds {}, switched to page {}", savedPage, newPage);
					} else {
						BuildersInventory.LOGGER.error("Failed to load invalid selected page {}...", savedPage);
					}
				}
				
			}
			
			// Select page 0 if none selected
			if (ExtendedInventory.getPage() < 0) {
				BuildersInventory.LOGGER.info("No page selected! Selecting first page...");
				ExtendedInventory.PAGE_CONTAINER.setPage(0);
			}
			
			// Save if forced update (after datafixer run or folder switch)
			if (forceUpdate) {
				BuildersInventory.LOGGER.info("Pages have been migrated from an older version, saving...");
				hasChanged = true;
				for (var page : PAGES) page.discreteChange();
				deleted = 0;
				if (!save(registryOps)) {
					BuildersInventory.LOGGER.error("Could not save migrated pages!");
				}
				BuildersInventory.LOGGER.info("Saved migrated data!");
				forceUpdate = false;
			}
			
		} catch (Exception e) {
			BuildersInventory.LOGGER.error("Error loading extended inventory pages!", e);
			PLAYER_MESSAGE_QUEUE.add(Component.translatable("error.builders_inventory.extended_inventory.load_failed").withStyle(ChatFormatting.RED));
		}
	}
	
	public static Optional<ExtendedInventoryPage> loadPage(RegistryOps<Tag> registryOps, Path path, int id) throws Exception {
		
		CompoundTag tag = NbtIo.read(path);
		
		var datafix = ModDataFixer.extendedInventoryPage(tag, 1);
		if (datafix.isPresent()) tag = datafix.get();
		
		if (tag == null || !tag.contains("items", Tag.TAG_LIST)) {
			BuildersInventory.LOGGER.error("Error loading extended inventory page {} tag {}: Invalid Data!", id, tag);
			return Optional.empty();
		}
		
		ListTag itemTags = tag.getList("items", Tag.TAG_COMPOUND);
		if (itemTags == null || itemTags.isEmpty()) {
			BuildersInventory.LOGGER.error("Error loading extended inventory page {} items {}: Invalid Data!", id, itemTags);
			return Optional.empty();
		}
		
		List<ItemStack> items = itemTags.stream()
				.map(itemTag -> ItemStack.OPTIONAL_CODEC
						.parse(registryOps, itemTag)
						.resultOrPartial(err -> BuildersInventory.LOGGER.error("Could not parse extended inventory item: '{}'", err))
						.orElse(ItemStack.EMPTY))
				.collect(Collectors.toList());
		
		boolean locked = false;
		if (tag.contains("locked", Tag.TAG_BYTE)) {
			locked = tag.getBoolean("locked");
		}
		
		String name = "";
		if (tag.contains("name", Tag.TAG_STRING)) {
			name = tag.getString("name");
		}
		
		ItemStack icon = ItemStack.EMPTY;
		if (tag.contains("icon", Tag.TAG_COMPOUND)) {
			CompoundTag iconTag = tag.getCompound("icon");
			icon = ItemStack.OPTIONAL_CODEC
					.parse(registryOps, iconTag)
					.resultOrPartial(err -> BuildersInventory.LOGGER.error("Could not parse extended inventory icon {}: '{}'", iconTag, err))
					.orElse(ItemStack.EMPTY);
		}
		
		ItemStack originalIcon = ItemStack.EMPTY;
		if (tag.contains("original_icon", Tag.TAG_COMPOUND)) {
			CompoundTag iconTag = tag.getCompound("original_icon");
			icon = ItemStack.OPTIONAL_CODEC
					.parse(registryOps, iconTag)
					.resultOrPartial(err -> BuildersInventory.LOGGER.error("Could not parse extended inventory original icon {}: '{}'", iconTag, err))
					.orElse(ItemStack.EMPTY);
		}
		
		boolean iconDataActive = false;
		if (tag.contains("icon_data", Tag.TAG_BYTE)) {
			iconDataActive = tag.getBoolean("icon_data");
		}
		
		int iconScaleDown = 0;
		if (tag.contains("icon_scale_down", Tag.TAG_INT)) {
			iconScaleDown = tag.getInt("icon_scale_down");
		}
		
		var page = ExtendedInventoryPage.of(items, locked, name, icon, originalIcon, iconDataActive, iconScaleDown);
		if (datafix.isPresent()) {
			forceUpdate = true;
			page.discreteChange();
		}
		return Optional.of(page);
	}
	
	public static boolean save() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level != null) return save(mc.level.registryAccess().createSerializationContext(NbtOps.INSTANCE));
		else if (mc.getConnection() != null) return save(mc.getConnection().registryAccess().createSerializationContext(NbtOps.INSTANCE));
		else {
			BuildersInventory.LOGGER.error("Error saving extended inventory saved data: Not in-game!");
			PLAYER_MESSAGE_QUEUE.add(Component.translatable("error.builders_inventory.extended_inventory.save_failed"));
			return false;
		}
	}
	
	public static boolean save(RegistryOps<Tag> registryOps) {
		BuildersInventory.LOGGER.info("Saving Extended Inventory...");
		if (!hasChanged) {
			BuildersInventory.LOGGER.info("Nothing to save.");
			return false;
		}
		BuildersInventory.LOGGER.info("Hey Log Readers: LOGS ARE ZERO-INDEXED");
		
		timeToSave = 0;
		hasChanged = false;
		
		if (!loaded) return false;
		if (!valid) {
			BuildersInventory.LOGGER.error("Refusing to save pages; pages failed to load!");
			return false;
		}
		
		Path root = FabricLoader.getInstance().getGameDir().resolve(BuildersInventory.MOD_ID).resolve("extended_inventory");
		try {
			
			final ArrayList<Pair<CompoundTag, Path>> storeFiles = new ArrayList<>(PAGES.size());
			final ArrayList<Path> deleteFiles = new ArrayList<>();
			
			// Prepare Page Data
			for (int i = 0; i < PAGES.size(); i++) {
				var page = PAGES.get(i);
				if (!page.valid) continue;
				if (!page.resetChanged()) continue; // resetChanged returns true if changed
				
				CompoundTag pageTag = writeTag(registryOps, page);
				
				storeFiles.add(Pair.of(pageTag, root.resolve(FILE_PREFIX + (i + 1) + FILE_SUFFIX)));
			}
			
			// Prepare deletions
			if (deleted > 0) {
				int deleteMax = PAGES.size() + deleted;
				for (int i = PAGES.size(); i < deleteMax; i++) {
					deleteFiles.add(root.resolve(FILE_PREFIX + (i + 1) + FILE_SUFFIX));
				}
				deleted = 0;
			}
			
			
			// Create directory (doing this outside of thread to make sure it gets made before anything else)
			if (!Files.isDirectory(root)) {
				try {
					Files.createDirectories(root);
				} catch (IOException e) {
					BuildersInventory.LOGGER.error("Error saving extended inventory pages!", e);
					PLAYER_MESSAGE_QUEUE.add(Component.translatable("error.builders_inventory.extended_inventory.save_failed").withStyle(ChatFormatting.RED));
					return false;
				}
			}
			
			// Make another thread save the stuff instead of the main one :troll:
			Util.ioPool().execute(() -> {
				int counter = 0;
				int failCounter = 0;
				
				for (int i = 0; i < storeFiles.size(); i++) {
					var pair = storeFiles.get(i);
					try {
						NbtIo.write(pair.getLeft(), pair.getRight());
						++counter;
					} catch (Exception e) {
						++failCounter;
						BuildersInventory.LOGGER.error("Error saving extended inventory page " + i + "!", e);
						PLAYER_MESSAGE_QUEUE.add(Component.translatable("error.builders_inventory.extended_inventory.save_failed.page", i + 1).withStyle(ChatFormatting.RED));
					}
				}
				
				BuildersInventory.LOGGER.info("Saved {} modified pages!", counter);
				if (failCounter > 0) BuildersInventory.LOGGER.error("Also failed to save {} modified pages...", counter);
				
				counter = 0;
				failCounter = 0;
				
				for (int i = 0; i < deleteFiles.size(); i++) {
					var path = deleteFiles.get(i);
					try {
						if (Files.deleteIfExists(path)) ++counter;
					} catch (Exception e) {
						++failCounter;
						BuildersInventory.LOGGER.error("Error deleting extended inventory file " + path.toString() + "!", e);
						PLAYER_MESSAGE_QUEUE.add(Component.translatable("error.builders_inventory.extended_inventory.delete_failed", i + 1).withStyle(ChatFormatting.RED));
					}
				}
				
				BuildersInventory.LOGGER.info("Deleted {} old pages!", counter);
				if (failCounter > 0) BuildersInventory.LOGGER.error("Also failed to delete {} old pages...", counter);
			});
			
			// While that goes on, save the things
			if (ExtendedInventory.getPage() >= 0) {
				CompoundTag tag = new CompoundTag();
				tag.putInt("version", ModDataFixer.VERSION);
				tag.putInt("page", ExtendedInventory.getPage());
				NbtIo.write(tag, root.resolve("inventory-data.nbt"));
				
				BuildersInventory.LOGGER.info("Saved extended inventory extra data!");
			}
		} catch (Exception e) {
			BuildersInventory.LOGGER.error("Error saving extended inventory pages!", e);
			PLAYER_MESSAGE_QUEUE.add(Component.translatable("error.builders_inventory.extended_inventory.save_failed").withStyle(ChatFormatting.RED));
			return false;
		}
		return true;
	}
	
	public static CompoundTag writeTag(RegistryOps<Tag> registryOps, ExtendedInventoryPage page) {
		
		CompoundTag tag = new CompoundTag();
		
		NbtUtils.addCurrentDataVersion(tag);
		tag.putInt("version", ModDataFixer.VERSION);
		tag.put("items", page.streamItems()
				.map(stack -> ItemStack.OPTIONAL_CODEC.encodeStart(registryOps, stack)
						.resultOrPartial()
						.orElse(new CompoundTag()))
				.collect(Collectors.toCollection(ListTag::new)));
		tag.putBoolean("locked", page.isLocked());
		if (!page.getName().isBlank()) tag.putString("name", page.getName());
		if (!page.icon.isEmpty()) {
			var result = ItemStack.OPTIONAL_CODEC.encodeStart(NbtOps.INSTANCE, page.icon).resultOrPartial();
			if (result.isPresent()) {
				tag.put("icon", result.get());
				tag.putBoolean("icon_data", page.iconDataActive);
				tag.putInt("icon_scale_down", page.iconScaleDown);
			}
		}
		if (!page.originalIcon.isEmpty()) {
			var result = ItemStack.OPTIONAL_CODEC.encodeStart(NbtOps.INSTANCE, page.icon).resultOrPartial();
			if (result.isPresent()) tag.put("original_icon", result.get());
		}
		
		return tag;
	}
	
	public static ExtendedInventoryPage get(int i) {
		if (!loaded) load();
		if (!valid) return ExtendedInventoryPage.INVALID;
		
		var page = PAGES.get(i);
		return page != null ? page : ExtendedInventoryPage.INVALID;
	}
	
	public static ExtendedInventoryPage create() {
		if (!loaded) load();
		if (!valid) return ExtendedInventoryPage.INVALID;
		
		var page = new ExtendedInventoryPage();
		page.setChanged();
		page.setLocked(false);
		PAGES.add(page);
		if (deleted > 0) --deleted;
		return page;
	}
	
	public static ExtendedInventoryPage reset(int n) {
		if (!loaded) {
			BuildersInventory.LOGGER.warn("Resetting page {} before pages were even loaded... I don't think that's meant to happen.", n);
			load();
		}
		if (!valid) return ExtendedInventoryPage.INVALID;
		if (n >= PAGES.size()) {
			BuildersInventory.LOGGER.error("Tried to reset page {}, but the index was out of bounds!", n);
			return ExtendedInventoryPage.INVALID;
		}
		
		var page = new ExtendedInventoryPage();
		page.setChanged();
		page.setLocked(false);
		PAGES.set(n, page);
		return page;
	}
	
	public static void delete(int n) {
		if (!loaded) {
			BuildersInventory.LOGGER.warn("Deleting page {} before pages were even loaded... I don't think that's meant to happen.", n);
			load();
		}
		if (!valid) return;
		if (n > PAGES.size()) {
			BuildersInventory.LOGGER.error("Tried to delete page {}, but the index was out of bounds!", n);
			return;
		}
		
		PAGES.remove(n);
		++deleted;
		if (PAGES.size() <= 0) create();
		for (int i = n; i < PAGES.size(); i++) PAGES.get(i).setChanged();
		setChanged();
	}
	
	public static boolean contains(int i) {
		if (!loaded) load();
		if (!valid) return false;
		return i < PAGES.size();
	}
	
	public static int size() {
		if (!loaded) load();
		if (!valid) return 1;
		return PAGES.size();
	}
	
	public static boolean isLoaded() {
		return loaded;
	}
	
	public static boolean isValid() {
		if (!loaded) load();
		return valid;
	}
	
	public static void rotatePages(int from, int to, int dist) {
		Collections.rotate(PAGES.subList(from, to), dist);
	}
	
}
