package com.monifactory.multiblocks.common.machine.multiblock;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import com.monifactory.multiblocks.api.block.IChillerCasingType;
import com.monifactory.multiblocks.common.block.ChillerCasingBlock;
import com.monifactory.multiblocks.common.machine.multiblock.part.SculkSourceBus;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

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

    protected SculkSourceBus sculkSource;

    protected TickableSubscription passiveSubs;

    @Getter
    @Persisted
    private int sculkGrowthMeter;

    private final ScheduledExecutorService sculkDecayScheduler = Executors.newScheduledThreadPool(1);

    public HypogeanInfuserMachine(IMachineBlockEntity holder) {
        super(holder);
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
                        "Sculk Tier: %s", GTValues.VN[casingType.getTier()]))
                .withStyle(ChatFormatting.LIGHT_PURPLE));

        if (!this.hasSculk) {
            textList.add(Component.translatable("moni_multiblocks.multiblock.hasNoSculk")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        textList.add(Component.literal(LocalizationUtils.format(
                        "Sculk Growth Meter: %d%%", this.sculkGrowthMeter))
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
        passiveSubs = null;
        sculkDecayScheduler.shutdownNow();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.updatePassiveSubscription();
        sculkDecayScheduler.scheduleAtFixedRate(this::doSculkDecay, 0, 5, TimeUnit.SECONDS);
    }

    private void doSculkDecay() {
        if(this.sculkSource == null) {
            decreaseSculkGrowthMeter();
            return;
        }
        if(!this.sculkSource.isValidSculk()) {
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
