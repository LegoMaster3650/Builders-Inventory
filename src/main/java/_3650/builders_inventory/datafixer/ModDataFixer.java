package _3650.builders_inventory.datafixer;

import java.util.Optional;

import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;

import _3650.builders_inventory.datafixer.extended_inventory.ExtendedInventoryPageFixer;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

public class ModDataFixer {
	public static final int VERSION = 2;
	
	public static int getVersion(CompoundTag tag, int defaultVersion) {
		return tag.getIntOr("version", defaultVersion);
	}
	
	public static Dynamic<Tag> tagDynamic(CompoundTag tag) {
		return new Dynamic<>(NbtOps.INSTANCE, tag);
	}
	
	public static int currentVersion() {
		return SharedConstants.getCurrentVersion().getDataVersion().getVersion();
	}
	
	public static CompoundTag updateToCurrentVersion(DataFixer fixer, CompoundTag tag, int version, TypeReference type) {
		return (CompoundTag) fixer.update(type, tagDynamic(tag), version, currentVersion()).getValue();
	}
	
	public static Optional<CompoundTag> extendedInventoryPage(CompoundTag tag, int defaultVersion) {
		if (tag == null) return Optional.empty();
		final int version = getVersion(tag, defaultVersion);
		return ExtendedInventoryPageFixer.update(tag, version);
	}
}
