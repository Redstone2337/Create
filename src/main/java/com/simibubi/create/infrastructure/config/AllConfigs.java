package com.simibubi.create.infrastructure.config;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.Create;
import com.simibubi.create.content.kinetics.BlockStressValues;
import com.simibubi.create.foundation.config.ConfigBase;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public class AllConfigs {

	private static final Map<ModConfig.Type, ConfigBase> CONFIGS = new EnumMap<>(ModConfig.Type.class);

	private static CClient client;
	private static CCommon common;
	private static CServer server;

	public static CClient client() {
		return client;
	}

	public static CCommon common() {
		return common;
	}

	public static CServer server() {
		return server;
	}

	public static ConfigBase byType(ModConfig.Type type) {
		return CONFIGS.get(type);
	}

	private static <T extends ConfigBase> T register(Supplier<T> factory, ModConfig.Type side) {
		Pair<T, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(builder -> {
			T config = factory.get();
			config.registerAll(builder);
			return config;
		});

		T config = specPair.getLeft();
		config.specification = specPair.getRight();
		CONFIGS.put(side, config);
		return config;
	}

	public static void register() {
		client = register(CClient::new, ModConfig.Type.CLIENT);
		common = register(CCommon::new, ModConfig.Type.COMMON);
		server = register(CServer::new, ModConfig.Type.SERVER);

		for (Entry<ModConfig.Type, ConfigBase> pair : CONFIGS.entrySet())
			ForgeConfigRegistry.INSTANCE.register(Create.ID, pair.getKey(), pair.getValue().specification);

		BlockStressValues.registerProvider(Create.ID, server().kinetics.stressValues);

		ModConfigEvents.loading(Create.ID).register(AllConfigs::onLoad);
		ModConfigEvents.reloading(Create.ID).register(AllConfigs::onReload);
	}

	public static void onLoad(ModConfig modConfig) {
		for (ConfigBase config : CONFIGS.values())
			if (config.specification == modConfig
				.getSpec())
				config.onLoad();
	}

	public static void onReload(ModConfig modConfig) {
		for (ConfigBase config : CONFIGS.values())
			if (config.specification == modConfig
				.getSpec())
				config.onReload();
	}

}
