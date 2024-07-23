package com.monifactory.multiblocks.common.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.monifactory.multiblocks.common.recipe.CoilTierCondition;
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
            .inputFluids(GTMaterials.Water.getFluid(1000))
            .outputItems(MMBlocks.MESOL_CASING, 1)
            .addCondition(new InitialEnergyCondition(GTValues.V[GTValues.LuV])) // poc: the machine requires X amount of power to be stored in it, then instantly does the recipe
            .addCondition(new CoilTierCondition(GTValues.EV))
            .addCondition(new SculkGrowthMeterCondition(100))
            .save(reg);
    }
}
