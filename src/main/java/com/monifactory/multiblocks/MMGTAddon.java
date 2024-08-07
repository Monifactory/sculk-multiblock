package com.monifactory.multiblocks;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.monifactory.multiblocks.common.CommonProxy;
import com.monifactory.multiblocks.common.data.MMRecipes;
import com.monifactory.multiblocks.common.recipe.CoilTierCondition;
import com.monifactory.multiblocks.common.recipe.InitialEnergyCondition;
import com.monifactory.multiblocks.common.recipe.SculkGrowthMeterCondition;
import net.minecraft.data.recipes.FinishedRecipe;


import java.util.function.Consumer;

@GTAddon
public class MMGTAddon implements IGTAddon {

    @Override
    public GTRegistrate getRegistrate() {
        return CommonProxy.REGISTRATE;
    }

    @Override
    public void initializeAddon() {
        // TODO Auto-generated method stub

    }

    @Override
    public String addonModId() {
        return MoniMultiblocks.MOD_ID;
    }

    @Override
    public void addRecipes(Consumer<FinishedRecipe> reg) {
        MMRecipes.init(reg);
    }

    @Override
    public void registerRecipeConditions() {
        GTRegistries.RECIPE_CONDITIONS.register("required_coil_tier", CoilTierCondition.class);
        GTRegistries.RECIPE_CONDITIONS.register("initial_energy_condition", InitialEnergyCondition.class);
        GTRegistries.RECIPE_CONDITIONS.register("sculk_growth_meter_condition", SculkGrowthMeterCondition.class);
    }

}
