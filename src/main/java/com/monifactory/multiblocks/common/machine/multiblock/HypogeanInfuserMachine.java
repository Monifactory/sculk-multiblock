package com.monifactory.multiblocks.common.machine.multiblock;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
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
    @Getter
    @Persisted
    @DescSynced
    private final NotifiableEnergyContainer internalPowerBuffer;

    public HypogeanInfuserMachine(IMachineBlockEntity holder) {
        super(holder);
        long tierVoltage = GTValues.V[casingType.getTier()];
        this.internalPowerBuffer = NotifiableEnergyContainer.receiverContainer(this,
                tierVoltage * 64L, tierVoltage, 1L);
        this.hasSculk = false;
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        super.addDisplayText(textList);

        textList.add(Component.literal(LocalizationUtils.format(
                        "Power Buffer: %s / %s", prettyFormatNumber(internalPowerBuffer.getEnergyStored()),
                        prettyFormatNumber(internalPowerBuffer.getEnergyCapacity())))
                .withStyle(ChatFormatting.WHITE));

        if (this.sculkSource == null) {
            textList.add(Component.translatable("moni_multiblocks.multiblock.hasNoSculk")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        if (!this.hasSculk) {
            textList.add(Component.literal("Sculk Input Bus is empty")
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

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        List<IEnergyContainer> energyContainers = new ArrayList<>();
        for (IMultiPart part : getParts()) {
            if (part instanceof SculkSourceBus sculkSourceBus) {
                sculkSourceBus.addListener(this::updateServerTickSubscription);
                this.sculkSource = sculkSourceBus;
                continue;
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
        updateServerTickSubscription();
        checkAndPenalizeSculk();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.hasSculk = false;
        this.unsubscribe(passiveSubs);
        this.unsubscribe(serverTickEvent);
        passiveSubs = null;
        serverTickEvent = null;
        sculkSource = null;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if(!isRemote() && isFormed()) {
            updateServerTickSubscription();
        }
    }

    private void updateServerTickSubscription() {
        serverTickEvent = this.subscribeServerTick(serverTickEvent, this::serverTickEvent);
    }

    private void serverTickEvent() {
        var offsetTimer = getOffsetTimer();
        if (offsetTimer % 100 == 0) doSculkDecay();
        doEnergyUpdate();
    }

    private void doEnergyUpdate() {
        if (!this.isWorkingEnabled() || inputEnergyContainers == null) return;

        long consumptionAmount = this.casingType.getPassiveConsumptionAmount() * this.casingType.getPassiveConsumptionRate();
        long energyStored = inputEnergyContainers.getEnergyStored();
        long energyAdded = internalPowerBuffer.addEnergy(energyStored);

        inputEnergyContainers.removeEnergy(energyAdded);
        internalPowerBuffer.removeEnergy(consumptionAmount);
    }

    private void doSculkDecay() {
        if (getOffsetTimer() % 100 != 0) return;
        if(!checkAndPenalizeSculk()) return;

        this.sculkSource.extractSculk();
        increaseSculkGrowthMeter();
    }

    private boolean checkAndPenalizeSculk() {
        if (this.sculkSource == null || !this.sculkSource.isValidSculk()) {
            decreaseSculkGrowthMeter();
            return false;
        }
        hasSculk = true;
        return true;
    }

    private void decreaseSculkGrowthMeter() {
        int decreaseAmount = ThreadLocalRandom.current().nextInt(10, 31);
        sculkGrowthMeter = Math.max(0, sculkGrowthMeter - decreaseAmount);
    }

    private void increaseSculkGrowthMeter() {
        int increaseAmount = ThreadLocalRandom.current().nextInt(5, 16);
        sculkGrowthMeter = Math.min(100, sculkGrowthMeter + increaseAmount);
    }

    public static String prettyFormatNumber(long number) {
        if (number < 1000) {
            return Long.toString(number);
        } else if (number < 1_000_000) {
            return String.format("%.1fk", number / 1_000.0);
        } else if (number < 1_000_000_000) {
            return String.format("%.1fM", number / 1_000_000.0);
        } else {
            return String.format("%.1fB", number / 1_000_000_000.0);
        }
    }
}
