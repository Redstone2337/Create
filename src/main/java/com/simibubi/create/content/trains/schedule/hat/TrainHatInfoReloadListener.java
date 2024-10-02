package com.simibubi.create.content.trains.schedule.hat;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.simibubi.create.Create;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

public class TrainHatInfoReloadListener {

	private static final Map<EntityType<?>, TrainHatInfo> ENTITY_INFO_MAP = new HashMap<>();
	public static final String HAT_INFO_DIRECTORY = "train_hat_info";
	public static final IdentifiableResourceReloadListener LISTENER = new SimpleSynchronousResourceReloadListener() {
		private static final ResourceLocation ID = Create.asResource("train_hat_info_reload_listener");

		@Override
		public void onResourceManagerReload(ResourceManager resourceManager) {
			registerOffsetOverrides(resourceManager);
		}

		@Override
		public ResourceLocation getFabricId() {
			return ID;
		}
	};
	private static final TrainHatInfo DEFAULT = new TrainHatInfo("", 0, Vec3.ZERO, 1.0F);

	private static void registerOffsetOverrides(ResourceManager manager) {
		ENTITY_INFO_MAP.clear();

		FileToIdConverter converter = FileToIdConverter.json(HAT_INFO_DIRECTORY);
		converter.listMatchingResources(manager).forEach((location, resource) -> {
			String[] splitPath = location.getPath().split("/");
			ResourceLocation entityName = new ResourceLocation(location.getNamespace(), splitPath[splitPath.length - 1].replace(".json", ""));
			if (!BuiltInRegistries.ENTITY_TYPE.containsKey(entityName)) {
				Create.LOGGER.error("Failed to load train hat info for entity {} as it does not exist.", entityName);
				return;
			}

			try (BufferedReader reader = resource.openAsReader()) {
				JsonObject json = GsonHelper.parse(reader);
				ENTITY_INFO_MAP.put(BuiltInRegistries.ENTITY_TYPE.get(entityName), TrainHatInfo.CODEC.parse(JsonOps.INSTANCE, json).resultOrPartial(Create.LOGGER::error).orElseThrow());
			} catch (Exception e) {
				Create.LOGGER.error("Failed to read train hat info for entity {}!", entityName, e);
			}
		});
		Create.LOGGER.info("Loaded {} train hat configurations.", ENTITY_INFO_MAP.size());
	}

	public static TrainHatInfo getHatInfoFor(EntityType<?> type) {
		return ENTITY_INFO_MAP.getOrDefault(type, DEFAULT);
	}
}
