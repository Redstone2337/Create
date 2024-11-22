package com.simibubi.create.compat.rei.category;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.Create;
import com.simibubi.create.compat.rei.category.animations.AnimatedItemDrain;
import com.simibubi.create.compat.rei.display.CreateDisplay;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;
import com.simibubi.create.content.fluids.transfer.EmptyingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.item.ItemHelper;

import io.github.fabricators_of_create.porting_lib.transfer.MutableContainerItemContext;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public class ItemDrainCategory extends CreateRecipeCategory<EmptyingRecipe> {

	private final AnimatedItemDrain drain = new AnimatedItemDrain();

	public ItemDrainCategory(Info<EmptyingRecipe> info) {
		super(info);
	}

		public static void consumeRecipes(Consumer<EmptyingRecipe> consumer) {
			EntryRegistry.getInstance().getEntryStacks()
					.filter(stack -> Objects.equals(stack.getType(), VanillaEntryTypes.ITEM))
					.<EntryStack<ItemStack>>map(EntryStack::cast)
					.collect(Collectors.toList())
					.stream()
					.forEach(entryStack -> {
						ItemStack stack = entryStack.getValue();
			Item item = stack.getItem();
			if (item == Items.POTION || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION) {
				FluidStack fluidFromPotionItem = PotionFluidHandler.getFluidFromPotionItem(stack);
				Ingredient potion = Ingredient.of(stack);
				consumer.accept(new ProcessingRecipeBuilder<>(EmptyingRecipe::new, Create.asResource("potions"))
					.withItemIngredients(potion)
					.withFluidOutputs(fluidFromPotionItem)
					.withSingleItemOutput(new ItemStack(Items.GLASS_BOTTLE))
					.build());
				return;
			}

			ItemStack copy = stack.copy();
			MutableContainerItemContext ctx = new MutableContainerItemContext(copy);
			Storage<FluidVariant> handler = ctx.find(FluidStorage.ITEM);

			if (handler == null)
				return;
			FluidStack extracted = TransferUtil.extractAnyFluid(handler, FluidConstants.BUCKET);
			int amount = ctx.getItemVariant().isBlank() ? 0 : (int) ctx.getAmount(); // GH#1622
			ItemStack result = ctx.getItemVariant().toStack(ItemHelper.truncateLong(amount));
			if (extracted.isEmpty())
				return;
			if (result.isEmpty())
				return;

			Ingredient ingredient = Ingredient.of(stack);
			ResourceLocation itemName = BuiltInRegistries.ITEM
					.getKey(stack.getItem());
			ResourceLocation fluidName = BuiltInRegistries.FLUID
					.getKey(extracted.getFluid());

			consumer.accept(new ProcessingRecipeBuilder<>(EmptyingRecipe::new,
				Create.asResource("empty_" + itemName.getNamespace() + "_" + itemName.getPath() + "_of_"
					+ fluidName.getNamespace() + "_" + fluidName.getPath())).withItemIngredients(ingredient)
						.withFluidOutputs(extracted)
						.withSingleItemOutput(result)
						.build());
		});
	}

	@Override
	public void addWidgets(CreateDisplay<EmptyingRecipe> display, List<Widget> ingredients, Point origin) {
		FluidStack fluidOutput = display.getRecipe().getResultingFluid();
		List<ItemStack> matchingIngredients = Arrays.asList(display.getRecipe().getIngredients()
				.get(0)
				.getItems());

		Slot fluidSlot = basicSlot(origin.x + 132, origin.y + 8)
				.markOutput()
				.entries(EntryIngredients.of(CreateRecipeCategory.convertToREIFluid(fluidOutput)));
		CreateRecipeCategory.setFluidRenderRatio(fluidSlot);
		ingredients.add(fluidSlot);
		ingredients.add(basicSlot(origin.x + 27, origin.y + 8)
				.markOutput()
				.entries(EntryIngredients.ofItemStacks(matchingIngredients)));
		ingredients.add(basicSlot(origin.x + 132, origin.y + 27)
				.markInput()
				.entries(display.getOutputEntries().get(0)));

		addFluidTooltip(ingredients, Collections.emptyList(), ImmutableList.of(fluidOutput));
	}

	@Override
	public void draw(EmptyingRecipe recipe, GuiGraphics graphics, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SLOT.render(graphics, 131, 7);
		AllGuiTextures.JEI_SLOT.render(graphics, 26, 7);
		getRenderedSlot(recipe, 0).render(graphics, 131, 26);
		AllGuiTextures.JEI_SHADOW.render(graphics, 62, 37);
		AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 73, 4);
		drain.withFluid(recipe.getResultingFluid())
			.draw(graphics, getDisplayWidth(null) / 2 - 13, 40);
	}

}
