package _3650.builders_inventory.mixin.feature.minimessage.resource;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import _3650.builders_inventory.api.minimessage.autocomplete.ReloadableResourceArg;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;

@Mixin(ClientConfigurationPacketListenerImpl.class)
public abstract class ClientConfigurationPacketListenerImplMixin {
	
	@Shadow
	private RegistryAccess.Frozen receivedRegistries;
	@Shadow
	private FeatureFlagSet enabledFeatures;
	
	@Inject(method = "handleConfigurationFinished", at = @At("TAIL"))
	private void builders_inventory_captureItemsAndEntities(ClientboundFinishConfigurationPacket packet, CallbackInfo ci) {
		HolderLookup<Item> itemReg = this.receivedRegistries.lookupOrThrow(Registries.ITEM).filterFeatures(this.enabledFeatures);
		ArrayList<String> items = itemReg.listElementIds()
				.map(ResourceKey::location)
				.map(ResourceLocation::getPath)
				.sorted()
				.collect(Collectors.toCollection(() -> new ArrayList<>()));
		ReloadableResourceArg.ITEMS.loadStr(items);
		
		HolderLookup<EntityType<?>> entityReg = this.receivedRegistries.lookupOrThrow(Registries.ENTITY_TYPE).filterFeatures(this.enabledFeatures);
		ArrayList<String> entities = entityReg.listElementIds()
				.map(ResourceKey::location)
				.map(ResourceLocation::getPath)
				.sorted()
				.collect(Collectors.toCollection(() -> new ArrayList<>()));
		ReloadableResourceArg.ENTITIES.loadStr(entities);
	}
	
}
