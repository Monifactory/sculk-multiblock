package com.monifactory.multiblocks.common.recipe;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.monifactory.multiblocks.common.machine.multiblock.HypogeanInfuserMachine;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class SculkTemperatureCondition extends RecipeCondition {

    double maxTemp;

    public SculkTemperatureCondition(double maxTemp) {
        this.maxTemp = maxTemp;
    }

    public SculkTemperatureCondition() {
        this.maxTemp = -1;
    }
    @Override
    public String getType() {
        return "sculk_temperature";
    }

    @Override
    public Component getTooltips() {
        return Component.literal("temp: " + maxTemp + 'K');
    }

    @Override
    public boolean test(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        if (recipeLogic.getMachine() instanceof HypogeanInfuserMachine machine)
        {
            return machine.getSculk() && machine.getTemp() < maxTemp;
        } else
        {
            return false;
        }
    }

    @Override
    public RecipeCondition createTemplate() {
        return new SculkTemperatureCondition();
    }

    @Override
    public @NotNull JsonObject serialize() {
        JsonObject value = super.serialize();
        value.addProperty("sculk_temperature", maxTemp);
        return value;
    }

    @Override
    public RecipeCondition deserialize(@NotNull JsonObject config) {
        super.deserialize(config);
        this.maxTemp = config.get("sculk_temperature").getAsDouble();
        return this;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        super.toNetwork(buf);
        buf.writeDouble(maxTemp);
    }

    @Override
    public RecipeCondition fromNetwork(FriendlyByteBuf buf) {
        super.fromNetwork(buf);
        buf.readDouble();
        return this;
    }

}
