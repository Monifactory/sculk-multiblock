package com.monifactory.multiblocks.integration.kjs.recipe;

import com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import lombok.experimental.Accessors;
import com.monifactory.multiblocks.common.recipe.SculkTemperatureCondition;
import static com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema.*;


public interface MMRecipeSchema {

    @SuppressWarnings({"unused", "UnusedReturnValue"})
    @Accessors(chain = true, fluent = true)
    class MMRecipeJS extends GTRecipeSchema.GTRecipeJS {

        public GTRecipeSchema.GTRecipeJS sculk_temperature(int maxTemp) {
            return addCondition(new SculkTemperatureCondition(maxTemp));
        }
    }

    RecipeSchema SCHEMA = new RecipeSchema(MMRecipeJS.class, MMRecipeJS::new, DURATION, DATA, CONDITIONS, ALL_INPUTS, ALL_TICK_INPUTS, ALL_OUTPUTS, ALL_TICK_OUTPUTS, IS_FUEL)
            .constructor((recipe, schemaType, keys, from) -> recipe.id(from.getValue(recipe, ID)), ID)
            .constructor(DURATION, CONDITIONS, ALL_INPUTS, ALL_OUTPUTS, ALL_TICK_INPUTS, ALL_TICK_OUTPUTS);

}