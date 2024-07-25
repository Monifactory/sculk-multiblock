package com.monifactory.multiblocks.common.machine.multiblock;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import com.monifactory.multiblocks.api.block.IChillerCasingType;
import com.monifactory.multiblocks.common.block.ChillerCasingBlock;
import com.monifactory.multiblocks.common.machine.multiblock.part.SculkSourceBus;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class HypogeanInfuserMachine extends WorkableElectricMultiblockMachine
        implements IFancyUIMachine, IDisplayUIMachine, IHypogeanInfuserMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            HypogeanInfuserMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Getter
    private IChillerCasingType casingType = ChillerCasingBlock.ChillerCasingType.MESOL;
    @Persisted
    protected boolean hasSculk;
    @Nullable
    protected EnergyContainerList inputEnergyContainers;
    @Getter
    @Persisted
    private int sculkGrowthMeter;
    protected SculkSourceBus sculkSource;
    protected TickableSubscription passiveSubs;
    private TickableSubscription serverTickEvent;
    @Persisted
    @DescSynced
    private final NotifiableEnergyContainer internalPowerBuffer;

    public HypogeanInfuserMachine(IMachineBlockEntity holder) {
        super(holder);
        long tierVoltage = GTValues.V[casingType.getTier()];
        this.internalPowerBuffer = NotifiableEnergyContainer.receiverContainer(this,
                tierVoltage * 64L, tierVoltage, 1L);
        this.hasSculk = false;
        serverTickEvent = this.subscribeServerTick(this::serverTickEvent);
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        super.addDisplayText(textList);

        textList.add(Component.literal(LocalizationUtils.format(
                        "Power Buffer: %d / %d", internalPowerBuffer.getEnergyStored(), internalPowerBuffer.getEnergyCapacity()))
                .withStyle(ChatFormatting.WHITE));

        if (!this.hasSculk) {
            textList.add(Component.translatable("moni_multiblocks.multiblock.hasNoSculk")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        textList.add(Component.literal(LocalizationUtils.format(
                        "Sculk Growth Meter: %d%%", sculkGrowthMeter))
                .withStyle(ChatFormatting.BLUE));
    }

    @Nullable
    @Override
    public GTRecipe getRealRecipe(@NotNull GTRecipe recipe) {
        var realRecipe = super.getRealRecipe(recipe);
        if (realRecipe != null) realRecipe.duration = 0;
        return realRecipe;
    }

    protected void updatePassiveSubscription() {
        if (!this.hasSculk) {
            if (this.sculkSource != null && this.sculkSource.isValidSculk()) {
                this.hasSculk = true;
                this.passiveSubs = subscribeServerTick(passiveSubs, this::updatePassive);
            } else {
                unsubscribe(passiveSubs);
                this.passiveSubs = null;
            }
        } else {
            unsubscribe(passiveSubs);
            this.passiveSubs = null;
        }
    }

    @SuppressWarnings("unchecked")
    protected void updatePassive() {
        if (this.isFormed()) {
            if ((getOffsetTimer() % casingType.getPassiveConsumptionRate()) == 0) {
                List<IRecipeHandler<?>> inputTanks = new ArrayList<>();
                if (getCapabilitiesProxy().contains(IO.IN, FluidRecipeCapability.CAP)) {
                    inputTanks.addAll(Objects.requireNonNull(
                            getCapabilitiesProxy().get(IO.IN, FluidRecipeCapability.CAP)));
                }
                if (getCapabilitiesProxy().contains(IO.BOTH, FluidRecipeCapability.CAP)) {
                    inputTanks.addAll(Objects.requireNonNull(
                            getCapabilitiesProxy().get(IO.BOTH, FluidRecipeCapability.CAP)));
                }
                List<FluidIngredient> toDrain = List
                        .of(FluidIngredient.of(casingType.getPassiveConsumptionAmount(), Fluids.WATER));
                for (IRecipeHandler<?> tank : inputTanks) {
                    toDrain = (List<FluidIngredient>) tank.handleRecipe(IO.IN, null, toDrain, null,
                            false);
                    if (toDrain == null)
                        break;
                }
                // are we still looking for fluid to consume?
                if (toDrain != null) {
                    this.hasSculk = false;
                    this.updatePassiveSubscription();
                }
            }
        }
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        // capture all energy containers
        List<IEnergyContainer> energyContainers = new ArrayList<>();
        for (IMultiPart part : getParts()) {
            if (part instanceof SculkSourceBus sculkSource) {
                // TODO make this be like the chiller casing with MatchContext or whatever
                sculkSource.addListener(this::updatePassiveSubscription);
                this.sculkSource = sculkSource;
            }
            for (var handler : part.getRecipeHandlers()) {
                IO handlerIO = handler.getHandlerIO();
                if (handlerIO == IO.IN) {
                    if (handler.getCapability() == EURecipeCapability.CAP && handler instanceof IEnergyContainer container) {
                        energyContainers.add(container);
                    }
                }
            }
        }
        this.inputEnergyContainers = new EnergyContainerList(energyContainers);
        Object obj = this.getMultiblockState().getMatchContext().get("ChillerCasing");
        if (obj instanceof IChillerCasingType casing) {
            this.casingType = casing;
        }
        this.updatePassiveSubscription();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.hasSculk = false;
        this.unsubscribe(passiveSubs);
        this.unsubscribe(serverTickEvent);
        passiveSubs = null;
        serverTickEvent = null;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.updatePassiveSubscription();
    }

    private void serverTickEvent() {
        var offsetTimer = getOffsetTimer();
        if (offsetTimer % 100 == 0) doSculkDecay();
        doEnergyUpdate();
    }

    private void doEnergyUpdate() {
        if (!this.isWorkingEnabled() || inputEnergyContainers == null) return;

        var energyStored = inputEnergyContainers.getEnergyStored();
        long consumptionAmount = this.casingType.getPassiveConsumptionAmount() * this.casingType.getPassiveConsumptionRate();

        if (internalPowerBuffer.getEnergyCanBeInserted() >= energyStored) {
            this.inputEnergyContainers.removeEnergy(energyStored);
        }

        internalPowerBuffer.addEnergy(energyStored);
        internalPowerBuffer.removeEnergy(consumptionAmount);
    }

    private void doSculkDecay() {
        if (getOffsetTimer() % 100 != 0) return;

        if (this.sculkSource == null) {
            decreaseSculkGrowthMeter();
            return;
        }
        if (!this.sculkSource.isValidSculk()) {
            decreaseSculkGrowthMeter();
            return;
        }
        this.sculkSource.extractSculk();
        increaseSculkGrowthMeter();
    }

    private void decreaseSculkGrowthMeter() {
        int decreaseAmount = ThreadLocalRandom.current().nextInt(10, 31);
        sculkGrowthMeter = Math.max(0, sculkGrowthMeter - decreaseAmount);
    }

    private void increaseSculkGrowthMeter() {
        int increaseAmount = ThreadLocalRandom.current().nextInt(5, 16);
        sculkGrowthMeter = Math.min(100, sculkGrowthMeter + increaseAmount);
    }
}
