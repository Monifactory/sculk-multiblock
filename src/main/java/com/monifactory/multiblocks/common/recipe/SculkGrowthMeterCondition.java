package com.monifactory.multiblocks.common.recipe;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import com.monifactory.multiblocks.common.machine.multiblock.IHypogeanInfuserMachine;
import lombok.NoArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class SculkGrowthMeterCondition extends RecipeCondition {
    public static final SculkGrowthMeterCondition INSTANCE = new SculkGrowthMeterCondition();

    private int minGrowthMeter = 0;

    public SculkGrowthMeterCondition(int minGrowthMeter) {
        this.minGrowthMeter = minGrowthMeter;
    }

    @Override
    public String getType() {
        return "required_growth_meter";
    }

    @Override
    public Component getTooltips() {
        return Component.literal("Growth Meter: %d".formatted(minGrowthMeter));
    }

    @Override
    public boolean test(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        if (recipeLogic.machine.self() instanceof IHypogeanInfuserMachine hypogeanInfuserMachine) {
            return hypogeanInfuserMachine.getSculkGrowthMeter() >= this.minGrowthMeter;
        }
        return false;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new CoilTierCondition();
    }

    @NotNull
    @Override
    public JsonObject serialize() {
        JsonObject config = super.serialize();
        config.addProperty("required_growth_meter", minGrowthMeter);
        return config;
    }

    @Override
    public RecipeCondition deserialize(@NotNull JsonObject config) {
        super.deserialize(config);
        minGrowthMeter = GsonHelper.getAsInt(config, "required_growth_meter", 0);
        return this;
    }

    @Override
    public RecipeCondition fromNetwork(FriendlyByteBuf buf) {
        super.fromNetwork(buf);
        minGrowthMeter = buf.readInt();
        return this;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        super.toNetwork(buf);
        buf.writeInt(minGrowthMeter);
    }
}
