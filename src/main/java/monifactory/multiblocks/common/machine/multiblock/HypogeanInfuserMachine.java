package monifactory.multiblocks.common.machine.multiblock;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import monifactory.multiblocks.api.block.IChillerCasingType;
import monifactory.multiblocks.common.block.ChillerCasingBlock.ChillerCasingType;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HypogeanInfuserMachine extends WorkableElectricMultiblockMachine {

    public static final double ROOM_TEMP = 293.15;

    private IChillerCasingType casingType = ChillerCasingType.MESOL;
    /**
     * Temperature of the infuser. in K
     * 
     * Lower temperature means it can cool down faster and reach a recipe's temp
     * faster (or just be able to reach the recipe in the first place
     */
    @Persisted
    private double temp = ROOM_TEMP;
    /**
     * If the infuser has sculk in it ready to run recipes.
     * 
     * Getting sculk requires paying some expensive item, but can be kept as long as
     * its fed xp (or whatever)
     */
    @Persisted
    protected boolean hasSculk;

    @Persisted
    protected double recipeTemp;

    protected TickableSubscription passiveSubs;
    protected TickableSubscription temperatureSubs;

    public HypogeanInfuserMachine(IMachineBlockEntity holder, Object[] args) {
        super(holder, args);
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean onWorking() {
        return super.onWorking() ? hasSculk && temp <= recipeTemp : false;

    }

    public static GTRecipe recipeModifer(MetaMachine machine, @NotNull GTRecipe recipe) {
        if (machine instanceof HypogeanInfuserMachine infuserMachine)
        {
            if (!recipe.data.contains("sculk_temp") || recipe.data
                .getDouble("sculk_temp") < infuserMachine.getCasingType()
                    .getCasingTemperature())
            {
                return null;
            } else
            {
                infuserMachine.setRecipeTemp(recipe.data.getDouble("sculk_temp"));
                return recipe;
            }
        } else
        {
            return null;
        }
    }

    private void setRecipeTemp(double recipeTemp) {
        this.recipeTemp = recipeTemp;
    }

    @SuppressWarnings("unchecked")
    protected void updatePassive() {
        if (getOffsetTimer() % casingType.getPassiveConsumptionRate() == 0)
        {
            List<IRecipeHandler<?>> inputTanks = new ArrayList<>();
            if (getCapabilitiesProxy().contains(IO.IN, FluidRecipeCapability.CAP))
            {
                inputTanks.addAll(Objects
                    .requireNonNull(getCapabilitiesProxy().get(IO.IN, FluidRecipeCapability.CAP)));
            }
            if (getCapabilitiesProxy().contains(IO.BOTH, FluidRecipeCapability.CAP))
            {
                inputTanks.addAll(Objects.requireNonNull(
                    getCapabilitiesProxy().get(IO.BOTH, FluidRecipeCapability.CAP)));
            }
            List<FluidIngredient> toDrain = List
                .of(FluidIngredient.of(casingType.getPassiveConsumptionAmount(), Fluids.WATER));
            for (IRecipeHandler<?> tank : inputTanks)
            {
                toDrain = (List<FluidIngredient>) tank.handleRecipe(IO.IN, null, toDrain, null,
                    false);
                if (toDrain == null)
                    break;
            }

            // are we still looking for fluid to consume?
            if (toDrain != null)
            {
                this.hasSculk = false;
                this.passiveSubs.unsubscribe();
                this.passiveSubs = null;
            }
        }
    }

    protected void updateTemperature() {
        if (this.isWorkingEnabled())
        {

            if (this.energyContainer
                .removeEnergy(this.casingType.getEnergyUsage()) >= this.casingType.getEnergyUsage())
            {
                this.temp = equalizeTemp(this.temp, this.casingType.getCasingTemperature());
                return;
            }
        } else
        {
            // TODO use biome temp?
            this.temp = equalizeTemp(this.temp, ROOM_TEMP);
        }

    }

    // loosely based off Newton's law of cooling
    protected static double equalizeTemp(double temp, double targetTemp) {
        return .1 * 1 * temp - targetTemp;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        Object obj = this.getMultiblockState().getMatchContext().get("ChillerCasing");
        if (obj instanceof IChillerCasingType casing)
        {
            this.casingType = casing;
        }
    }

    @Override
    public void onLoad() {
        this.passiveSubs = this.subscribeServerTick(this::updatePassive);
        this.temperatureSubs = this.subscribeServerTick(this::updateTemperature);
    }

    public IChillerCasingType getCasingType() {
        return this.casingType;
    }
}
