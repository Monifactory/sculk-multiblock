package monifactory.multiblocks.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import monifactory.multiblocks.common.CommonProxy;
import monifactory.multiblocks.common.machine.multiblock.HypogeanInfuserMachine;
import net.minecraft.world.level.block.Blocks;

public class MMMachines {

    public static final MultiblockMachineDefinition HYPOGEAN_INFUSER = CommonProxy.REGISTRATE
        .multiblock("hypogean_infuser", HypogeanInfuserMachine::new)
        .rotationState(RotationState.NON_Y_AXIS)
        .recipeType(GTRecipeTypes.VACUUM_RECIPES)
        .appearanceBlock(GTBlocks.CASING_ALUMINIUM_FROSTPROOF)
        .pattern(definition -> FactoryBlockPattern.start()
            .aisle("CCCCC", "#####", "#####", "#####", "CCCCC")
            .aisle("CCCCC", "#HHH#", "#HHH#", "#HHH#", "CCCCC")
            .aisle("CCCCC", "#HAH#", "#HAH#", "#HAH#", "CCCCC")
            .aisle("CCCCC", "#HHH#", "#HHH#", "#HHH#", "CCCCC")
            .aisle("CCCCC", "#####", "#####", "#####", "CCYCC").where('H',
                Predicates.blocks(MMBlocks.MESOL_CASING.get(), MMBlocks.BATHYAL_CASING.get(),
                    MMBlocks.ABYSSAL_CASING.get(), MMBlocks.HADAL_CASING.get()))
            .where('#', Predicates.any())
            .where('Y', Predicates.controller(Predicates.blocks(definition.getBlock())))
            .where('C', Predicates.blocks(GTBlocks.CASING_ALUMINIUM_FROSTPROOF.get())).build())
        .shapeInfo(definition -> MultiblockShapeInfo.builder()
            .aisle("CCCCC", "#####", "#####", "#####", "CCCCC")
            .aisle("CCCCC", "#HHH#", "#HHH#", "#HHH#", "CCCCC")
            .aisle("CCCCC", "#HAH#", "#HAH#", "#HAH#", "CCCCC")
            .aisle("CCCCC", "#HHH#", "#HHH#", "#HHH#", "CCCCC")
            .aisle("CCCCC", "#####", "#####", "#####", "CCYCC")
            .where('H', MMBlocks.HADAL_CASING.get())
            .where('#', Blocks.AIR)
            .where('Y', definition.getBlock())
            .where('C', GTBlocks.CASING_ALUMINIUM_FROSTPROOF.get()).build())
        .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_frost_proof"),
            GTCEu.id("block/multiblock/vacuum_freezer"), false)
        .register();
    public static void init() {

    }
}
