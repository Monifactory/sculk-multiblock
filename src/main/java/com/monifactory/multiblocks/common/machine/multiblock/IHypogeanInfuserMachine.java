package com.monifactory.multiblocks.common.machine.multiblock;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.monifactory.multiblocks.api.block.IChillerCasingType;

public interface IHypogeanInfuserMachine {
    IChillerCasingType getCasingType();
    EnergyContainerList getEnergyContainer();
    NotifiableEnergyContainer getInternalPowerBuffer();
    int getSculkGrowthMeter();
}
