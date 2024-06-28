package monifactory.multiblocks.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;

public class SculkHatchPartMachine extends FluidHatchPartMachine {
	public static final long INITIAL_CAPACITY = 256 * FluidHelper.getBucket();
	
	public SculkHatchPartMachine(IMachineBlockEntity holder, Object[] args) {
		super(holder, GTValues.LuV, IO.IN, INITIAL_CAPACITY, 1, args);
	}
	
	 @Override
	    protected NotifiableFluidTank createTank(long initialCapacity, int slots, Object... args) {
	        return super.createTank(initialCapacity, slots).setFilter(fluidStack -> fluidStack.getFluid().equals(GTMaterials.Water.getFluidTag()));
	    }

}
