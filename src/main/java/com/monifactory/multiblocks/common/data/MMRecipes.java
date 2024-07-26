package com.monifactory.multiblocks.common.data;

import com.enderio.base.common.init.EIOFluids;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.monifactory.multiblocks.common.recipe.InitialEnergyCondition;
import com.monifactory.multiblocks.common.recipe.SculkGrowthMeterCondition;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class MMRecipes {
    public static void init(Consumer<FinishedRecipe> reg) {
        MMRecipeTypes.INFUSER_RECIPES.recipeBuilder("mesol")
                .inputItems(Items.SCULK_CATALYST, 1)
                .inputItems(TagPrefix.ingot, GTMaterials.Neutronium, 8)
                .inputFluids(FluidIngredient.of(1000, EIOFluids.XP_JUICE.get()))
                .outputItems(MMBlocks.MESOL_CASING, 1)
                .inputEU(2077152)
                .addCondition(new InitialEnergyCondition(2077152)) // poc: the machine requires X amount of power to be stored in it, then instantly does the recipe
                .addCondition(new SculkGrowthMeterCondition(100))
                .save(reg);
    }
}
