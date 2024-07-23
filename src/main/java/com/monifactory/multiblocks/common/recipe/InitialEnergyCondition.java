package com.monifactory.multiblocks.common.recipe;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
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
public class InitialEnergyCondition extends RecipeCondition {
    public static final InitialEnergyCondition INSTANCE = new InitialEnergyCondition();

    private long requiredInitialEnergy;

    public InitialEnergyCondition(long requiredInitialEnergy) {
        this.requiredInitialEnergy = requiredInitialEnergy;
    }

    @Override
    public String getType() {
        return "required_initial_energy";
    }

    @Override
    public Component getTooltips() {
        return Component.literal(LocalizationUtils.format("Required Initial Energy: %d", requiredInitialEnergy));
    }

    @Override
    public boolean test(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        return recipeLogic.machine.self() instanceof IHypogeanInfuserMachine machine
                && machine.getEnergyContainer().getEnergyStored() >= this.requiredInitialEnergy;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new CoilTierCondition();
    }

    @NotNull
    @Override
    public JsonObject serialize() {
        JsonObject config = super.serialize();
        config.addProperty("requiredInitialEnergy", requiredInitialEnergy);
        return config;
    }

    @Override
    public RecipeCondition deserialize(@NotNull JsonObject config) {
        super.deserialize(config);
        requiredInitialEnergy = GsonHelper.getAsLong(config, "requiredInitialEnergy", 0);
        return this;
    }

    @Override
    public RecipeCondition fromNetwork(FriendlyByteBuf buf) {
        super.fromNetwork(buf);
        requiredInitialEnergy = buf.readLong();
        return this;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        super.toNetwork(buf);
        buf.writeLong(requiredInitialEnergy);
    }
}
