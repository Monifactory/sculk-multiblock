package com.monifactory.multiblocks.common.data;

import static com.gregtechceu.gtceu.api.GTValues.ZPM;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.error.PatternStringError;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.client.renderer.machine.OverlayTieredActiveMachineRenderer;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import com.monifactory.multiblocks.common.CommonProxy;
import com.monifactory.multiblocks.MoniMultiblocks;
import com.monifactory.multiblocks.api.block.IChillerCasingType;
import com.monifactory.multiblocks.common.machine.multiblock.HypogeanInfuserMachine;
import com.monifactory.multiblocks.common.machine.multiblock.part.SculkSourceBus;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Comparator;

public class MMMachines {

    public static final MultiblockMachineDefinition HYPOGEAN_INFUSER = CommonProxy.REGISTRATE
        .multiblock("hypogean_infuser", HypogeanInfuserMachine::new)
        .rotationState(RotationState.ALL)
        .tier(GTValues.LuV)
        .recipeType(MMRecipeTypes.INFUSER_RECIPES)
        .recipeModifier(
            GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK))
        .appearanceBlock(GTBlocks.CASING_ALUMINIUM_FROSTPROOF)
        .pattern(definition -> FactoryBlockPattern.start()
            .aisle("CCCCC", "     ", "     ", "     ", "CCCCC")
            .aisle("CCCCC", " HHH ", " HHH ", " HHH ", "CCCCC")
            .aisle("CCCCC", " HAH ", " HAH ", " HAH ", "CCCCC")
            .aisle("CCCCC", " HHH ", " HHH ", " HHH ", "CCCCC")
            .aisle("CCYCC", "     ", "     ", "     ", "CCCCC").where('H', coolingCoils())
            .where(' ', Predicates.any())
            .where('Y', Predicates.controller(Predicates.blocks(definition.getBlock())))
            .where('C',
                Predicates.blocks(GTBlocks.CASING_ALUMINIUM_FROSTPROOF.get())
                    .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                    .or(Predicates.abilities(MMPartAbilities.SCULK_SEED)))
            .where('A', Predicates.air()).build())
        .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_frost_proof"),
            MoniMultiblocks.id("block/multiblock/hypogean_infuser"), false)
        .register();

    public static final MachineDefinition SCULK_SEED_BUS = CommonProxy.REGISTRATE
        .machine("sculk_seed_bus", SculkSourceBus::new)
        .rotationState(RotationState.ALL)
        .abilities(MMPartAbilities.SCULK_SEED)
        .renderer(() -> new OverlayTieredActiveMachineRenderer(ZPM,
            GTCEu.id("block/machine/part/object_holder"),
            GTCEu.id("block/machine/part/object_holder_active")))
        .register();

    public static void init() {

    }

    public static TraceabilityPredicate coolingCoils() {
        return new TraceabilityPredicate(multiState -> {
            BlockState possibleCasing = multiState.getBlockState();
            for (var entry : MMBlocks.CHILLER_CASINGS.entrySet())
            {
                if (possibleCasing.is(entry.getValue().get()))
                {
                    IChillerCasingType stats = entry.getKey();
                    Object currentCoil = multiState.getMatchContext().getOrPut("ChillerCasing",
                        stats);
                    if (!currentCoil.equals(stats))
                    {
                        multiState.setError(
                            new PatternStringError(
                                "moni_multiblocks.multiblock.pattern.error.coils"));
                        return false;
                    }
                    return true;
                }
            }
            return false;
        }, () -> {
            return MMBlocks.CHILLER_CASINGS.entrySet().stream()
                .sorted(Comparator.comparingDouble(entry -> entry.getKey().getCasingTemperature()))
                .map(entry -> BlockInfo.fromBlockState(entry.getValue().get().defaultBlockState()))
                .toArray(BlockInfo[]::new);
        });
    }
}
