package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllTags.AllFluidTags;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;

import io.github.fabricators_of_create.porting_lib.tags.Tags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluids;

public class FillingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	HONEY_BOTTLE = create("honey_bottle", b -> b.require(AllFluidTags.HONEY.tag, AllFluids.HONEY_BOTTLE_AMOUNT)
		.require(Items.GLASS_BOTTLE)
		.output(Items.HONEY_BOTTLE)),

		BUILDERS_TEA = create("builders_tea", b -> b.require(AllFluids.TEA.get(), FluidConstants.BOTTLE)
			.require(Items.GLASS_BOTTLE)
			.output(AllItems.BUILDERS_TEA.get())),

		FD_MILK = create(Mods.FD.recipeId("milk_bottle"), b -> b.require(Tags.Fluids.MILK, FluidConstants.BOTTLE)
			.require(Items.GLASS_BOTTLE)
			.output(1, Mods.FD, "milk_bottle", 1)
			.whenModLoaded(Mods.FD.getId())),

		BLAZE_CAKE = create("blaze_cake", b -> b.require(Fluids.LAVA, FluidConstants.BOTTLE)
			.require(AllItems.BLAZE_CAKE_BASE.get())
			.output(AllItems.BLAZE_CAKE.get())),

		HONEYED_APPLE = create("honeyed_apple", b -> b.require(AllFluidTags.HONEY.tag, AllFluids.HONEY_BOTTLE_AMOUNT)
			.require(Items.APPLE)
			.output(AllItems.HONEYED_APPLE.get())),

		SWEET_ROLL = create("sweet_roll", b -> b.require(Tags.Fluids.MILK, FluidConstants.BOTTLE)
			.require(Items.BREAD)
			.output(AllItems.SWEET_ROLL.get())),

		CHOCOLATE_BERRIES = create("chocolate_glazed_berries", b -> b.require(AllFluids.CHOCOLATE.get(), FluidConstants.BOTTLE)
			.require(Items.SWEET_BERRIES)
			.output(AllItems.CHOCOLATE_BERRIES.get())),

		GRASS_BLOCK = create("grass_block", b -> b.require(Fluids.WATER, FluidConstants.BUCKET / 2)
			.require(Items.DIRT)
			.output(Items.GRASS_BLOCK)),

		GUNPOWDER = create("gunpowder", b -> b.require(PotionFluidHandler.potionIngredient(Potions.HARMING, FluidConstants.BUCKET / 40))
			.require(AllItems.CINDER_FLOUR.get())
			.output(Items.GUNPOWDER)),

		REDSTONE = create("redstone", b -> b.require(PotionFluidHandler.potionIngredient(Potions.STRENGTH, FluidConstants.BUCKET / 40))
			.require(AllItems.CINDER_FLOUR.get())
			.output(Items.REDSTONE)),

		GLOWSTONE = create("glowstone", b -> b.require(PotionFluidHandler.potionIngredient(Potions.NIGHT_VISION, FluidConstants.BUCKET / 40))
			.require(AllItems.CINDER_FLOUR.get())
			.output(Items.GLOWSTONE_DUST)),


		AM_LAVA = create(Mods.AM.recipeId("lava_bottle"), b -> b.require(Fluids.LAVA, FluidConstants.BOTTLE)
			.require(Items.GLASS_BOTTLE)
			.output(1, Mods.AM, "lava_bottle", 1)
			.whenModLoaded(Mods.AM.getId())),

		BYG_LUSH_GRASS = create(Mods.BYG.recipeId("lush_grass_block"), b -> b.require(Mods.BYG, "lush_dirt")
			.require(Fluids.WATER, FluidConstants.BUCKET / 2)
			.output(Mods.BYG, "lush_grass_block")
			.whenModLoaded(Mods.BYG.getId())),

		NEA_MILK = create(Mods.NEA.recipeId("milk_bottle"), b -> b.require(Tags.Fluids.MILK, FluidConstants.BOTTLE)
			.require(Items.GLASS_BOTTLE)
			.output(1, Mods.NEA, "milk_bottle", 1)
			.whenModLoaded(Mods.NEA.getId())),

		AET_GRASS = moddedGrass(Mods.AET, "aether"),

		RU_PEAT_GRAS = moddedGrass(Mods.RU, "peat"),

		RU_SILT_GRAS = moddedGrass(Mods.RU, "silt"),

		// Vampirism

		VMP_CURSED_GRASS = create(Mods.VMP.recipeId("cursed_grass"), b -> b.require(Fluids.WATER, FluidConstants.BUCKET / 2)
			.require(Mods.VMP, "cursed_earth")
			.output(Mods.VMP, "cursed_grass")
			.whenModLoaded(Mods.VMP.getId()));

	;

	public GeneratedRecipe moddedGrass(Mods mod, String name) {
		String grass = name + "_grass_block";
		return create(mod.recipeId(grass), b -> b.require(Fluids.WATER, FluidConstants.BUCKET / 2)
				.require(mod, name + "_dirt")
				.output(mod, grass)
				.whenModLoaded(mod.getId()));
	}

	public FillingRecipeGen(FabricDataOutput output) {
		super(output);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.FILLING;
	}

}
