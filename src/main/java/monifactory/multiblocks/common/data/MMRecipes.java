package monifactory.multiblocks.common.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import monifactory.multiblocks.common.recipe.SculkTemperatureCondition;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class MMRecipes {
    public static void init(Consumer<FinishedRecipe> reg) {
        MMRecipeTypes.INFUSER_RECIPES.recipeBuilder("mesol")
            .inputItems(Items.SCULK_CATALYST, 1)
            .inputItems(TagPrefix.ingot, GTMaterials.Neutronium, 8)
            .inputFluids(GTMaterials.Water.getFluid(1000))
            .EUt(GTValues.VA[GTValues.LuV])
            .outputItems(MMBlocks.MESOL_CASING, 1)
            .addCondition(new SculkTemperatureCondition(0.1))
            .duration(5 * 20)
            .save(reg);
    }
}
