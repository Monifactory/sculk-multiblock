package com.monifactory.multiblocks.integration.kjs.recipe;

import com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema;
import com.monifactory.multiblocks.common.recipe.CoilTierCondition;
import com.monifactory.multiblocks.common.recipe.InitialEnergyCondition;
import com.monifactory.multiblocks.common.recipe.SculkGrowthMeterCondition;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import lombok.experimental.Accessors;
import static com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema.*;


public interface MMRecipeSchema {

    @SuppressWarnings({"unused", "UnusedReturnValue"})
    @Accessors(chain = true, fluent = true)
    class MMRecipeJS extends GTRecipeSchema.GTRecipeJS {
        public GTRecipeSchema.GTRecipeJS CoilTierCondition(int tier) {
            return addCondition(new CoilTierCondition(tier));
        }
        public GTRecipeSchema.GTRecipeJS InitialEnergyCondition(int energy) {
            return addCondition(new InitialEnergyCondition(energy));
        }
        public GTRecipeSchema.GTRecipeJS SculkGrowthMeterCondition(int minGrowthMeter) {
            return addCondition(new SculkGrowthMeterCondition(minGrowthMeter));
        }
    }

    RecipeSchema SCHEMA = new RecipeSchema(MMRecipeJS.class, MMRecipeJS::new, DURATION, DATA, CONDITIONS, ALL_INPUTS, ALL_TICK_INPUTS, ALL_OUTPUTS, ALL_TICK_OUTPUTS, IS_FUEL)
            .constructor((recipe, schemaType, keys, from) -> recipe.id(from.getValue(recipe, ID)), ID)
            .constructor(DURATION, CONDITIONS, ALL_INPUTS, ALL_OUTPUTS, ALL_TICK_INPUTS, ALL_TICK_OUTPUTS);

}