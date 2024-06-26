package monifactory.multiblocks.common.data;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class MMRecipes {
    public static void init(Consumer<FinishedRecipe> reg) {
        MMRecipeTypes.INFUSER_RECIPES.recipeBuilder("mesol").inputItems(Items.SCULK_CATALYST, 1)
            .inputItems(TagPrefix.ingot, GTMaterials.Neutronium, 8)
            .outputItems(MMBlocks.MESOL_CASING, 1).save(reg);
    }
}
