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

import static com.gregtechceu.gtceu.api.GTValues.VN;

@NoArgsConstructor
public class CoilTierCondition extends RecipeCondition {
    public static final CoilTierCondition INSTANCE = new CoilTierCondition();

    private int requiredTier;

    public CoilTierCondition(int requiredTier) {
        this.requiredTier = requiredTier;
    }

    @Override
    public String getType() {
        return "required_tier";
    }

    @Override
    public Component getTooltips() {
        return Component.literal(LocalizationUtils.format("Required Coil Tier: %s", VN[requiredTier]));
    }

    @Override
    public boolean test(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        if (recipeLogic.machine.self() instanceof IHypogeanInfuserMachine hypogeanInfuserMachine) {
            return hypogeanInfuserMachine.getCasingType().getTier() >= this.requiredTier;
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
        config.addProperty("requiredTier", requiredTier);
        return config;
    }

    @Override
    public RecipeCondition deserialize(@NotNull JsonObject config) {
        super.deserialize(config);
        requiredTier = GsonHelper.getAsInt(config, "requiredTier", 0);
        return this;
    }

    @Override
    public RecipeCondition fromNetwork(FriendlyByteBuf buf) {
        super.fromNetwork(buf);
        requiredTier = buf.readInt();
        return this;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        super.toNetwork(buf);
        buf.writeInt(requiredTier);
    }
}
