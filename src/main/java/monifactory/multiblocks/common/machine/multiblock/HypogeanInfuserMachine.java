package monifactory.multiblocks.common.machine.multiblock;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import monifactory.multiblocks.api.block.IChillerCasingType;
import monifactory.multiblocks.common.block.ChillerCasingBlock.ChillerCasingType;
import monifactory.multiblocks.common.machine.multiblock.part.SculkSourceBus;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HypogeanInfuserMachine extends WorkableElectricMultiblockMachine
    implements IFancyUIMachine, IDisplayUIMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
        HypogeanInfuserMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    public static final double ROOM_TEMP = 293.15;

    private IChillerCasingType casingType = ChillerCasingType.MESOL;

    /**
     * Current Temperature of the infuser. in K
     * 
     * Lower min temperature means it can cool down faster and reach a recipe's temp
     * faster (or just be able to reach the recipe in the first place
     */

    @Persisted
    protected double temp;

    /**
     * If the infuser has sculk in it ready to run recipes.
     * 
     * Getting sculk requires paying some expensive item, but can be kept as long as
     * its fed xp (or whatever)
     */
    @Persisted
    protected boolean hasSculk;

    @Nullable
    protected EnergyContainerList inputEnergyContainers;

    protected SculkSourceBus sculkSource;

    protected TickableSubscription passiveSubs;
    protected TickableSubscription temperatureSubs;

    public HypogeanInfuserMachine(IMachineBlockEntity holder) {
        super(holder);
        this.temp = ROOM_TEMP;
        // this.hasSculk = true;
    }

    @Override
    public void notifyStatusChanged(RecipeLogic.Status oldStatus, RecipeLogic.Status newStatus) {
        super.notifyStatusChanged(oldStatus, newStatus);
        if (oldStatus == RecipeLogic.Status.SUSPEND)
        {
            this.updateTemperatureSubscription(newStatus);
        }
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        textList.add(Component.literal(getMetricUnit(this.temp, 'K')));
        if (this.hasSculk)
        {
            textList.add(Component.translatable("moni_multiblocks.multiblock.hasSculk")
                .withStyle(ChatFormatting.GREEN));
        } else {
            textList.add(Component.translatable("moni_multiblocks.multiblock.hasNoSculk")
                .withStyle(ChatFormatting.RED));
        }

    }

    public static GTRecipe recipeModifer(MetaMachine machine, @NotNull GTRecipe recipe) {

        if (machine instanceof HypogeanInfuserMachine infuserMachine)
        {
            // if (!recipe.condition <
            // infuserMachine.getCasingType().getCasingTemperature())
            // {
            // return null;
            // } else
            // {
            return recipe;
            // }
        } else
        {
            return null;
        }

    }

    protected void updatePassiveSubscription() {
        if (!this.hasSculk) {
            if (this.sculkSource != null && this.sculkSource.getSculk()) {
                this.hasSculk = true;
                this.passiveSubs = subscribeServerTick(passiveSubs, this::updatePassive);
            } else {
                unsubscribe(passiveSubs);
                this.passiveSubs = null;
            }
        } else {
            
        }
    }

    @SuppressWarnings("unchecked")
    protected void updatePassive() {
        if (this.isFormed())
        {
            if ((getOffsetTimer() % casingType.getPassiveConsumptionRate()) == 0)
            {
                List<IRecipeHandler<?>> inputTanks = new ArrayList<>();
                if (getCapabilitiesProxy().contains(IO.IN, FluidRecipeCapability.CAP))
                {
                    inputTanks.addAll(Objects.requireNonNull(
                        getCapabilitiesProxy().get(IO.IN, FluidRecipeCapability.CAP)));
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
                    this.updatePassiveSubscription();
                }
            }
        }
    }

    protected void updateTemperatureSubscription() {
        this.updateTemperatureSubscription(this.recipeLogic.getStatus());
    }

    protected void updateTemperatureSubscription(RecipeLogic.Status status) {
        if (!(RecipeLogic.Status.SUSPEND == status) && this.inputEnergyContainers != null)
        { // working so we can rty to chill
            if (this.inputEnergyContainers.getEnergyStored() > 0)
            { // enabled and has energy, start cooling
                this.temperatureSubs = this.subscribeServerTick(this.temperatureSubs,
                    this::updateTemperature);
            } else
            {// no energy, only go if still cold
                if (temp < ROOM_TEMP)
                {// no energy, but still heating up
                    this.temperatureSubs = this.subscribeServerTick(this.temperatureSubs,
                        this::updateTemperature);
                } else
                {// no energy, no cold, do nothing
                    this.unsubscribe(this.temperatureSubs);
                    this.temperatureSubs = null;
                }
            }
        } else
        {// not working can only warm up
            if (temp >= ROOM_TEMP)
            {// at room temperature, nothing to do
                this.unsubscribe(this.temperatureSubs);
                this.temperatureSubs = null;
            } else
            {// still cold, continue warming
                this.temperatureSubs = this.subscribeServerTick(this.temperatureSubs,
                    this::updateTemperature);
            }
        }
    }

    protected void updateTemperature() {
        // System.out.println(this.temp);
        if (this.isWorkingEnabled() && this.isFormed())
        {
            if (this.inputEnergyContainers != null && this.inputEnergyContainers
                .removeEnergy(this.casingType.getEnergyUsage()) >= this.casingType.getEnergyUsage())
            {
                if (this.temp >= this.casingType.getCasingTemperature())
                    this.recipeLogic.updateTickSubscription();
                this.temp = equalizeTemp(this.temp, this.casingType.getCasingTemperature() * 0.99);

                return;
            }
        }
        // TODO use biome temp?
        this.temp = equalizeTemp(this.temp, ROOM_TEMP * 1.01);
        if (this.temp >= ROOM_TEMP)
        {
            this.unsubscribe(temperatureSubs);
            this.temperatureSubs = null;
        }
    }

    // loosely based off Newton's law of cooling
    protected static double equalizeTemp(double temp, double targetTemp) {
        return temp - .01 * 1 * (temp - targetTemp);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        // capture all energy containers
        List<IEnergyContainer> energyContainers = new ArrayList<>();
        Map<Long, IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap",
            Long2ObjectMaps::emptyMap);
        for (IMultiPart part : getParts())
        {
            if (part instanceof SculkSourceBus sculkSource)
            {
                // TODO make this be like the chiller casing with MatchContext or whatever
                sculkSource.addListener(this::updatePassiveSubscription);
                this.sculkSource = sculkSource;
            } else {
                IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.BOTH);
                if (io == IO.NONE || io == IO.OUT)
                    continue;
                for (var handler : part.getRecipeHandlers()) {
                    // If IO not compatible
                    if (io != IO.BOTH && handler.getHandlerIO() != IO.BOTH
                            && io != handler.getHandlerIO())
                        continue;
                    if (handler.getCapability() == EURecipeCapability.CAP
                            && handler instanceof IEnergyContainer container) {
                        energyContainers.add(container);
                        traitSubscriptions
                                .add(handler.addChangedListener(this::updateTemperatureSubscription));
                    }
                }
            }
        }
        this.inputEnergyContainers = new EnergyContainerList(energyContainers);
        Object obj = this.getMultiblockState().getMatchContext().get("ChillerCasing");
        if (obj instanceof IChillerCasingType casing)
        {
            this.casingType = casing;
        }
        this.updateTemperatureSubscription();
        this.updatePassiveSubscription();

    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.hasSculk = false;
        this.unsubscribe(passiveSubs);
        passiveSubs = null;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.updateTemperatureSubscription();
        this.updatePassiveSubscription();
    }

    /**
     * a prefix to be applied to a metric unit, index isthe order of magnitude of
     * the number. In other words the base-10 logarithm divided by 3 minus 1 because
     * 0 is no prefix
     */
    public static final char[] LARGE_METRIC_PREFIXES = new char[] { 'k', 'M', 'G', 'T', 'P', 'E',
        'Z', 'Y', 'R', 'Q' };
    /**
     * a prefix to be applied to a metric unit, index is the absolute value of the
     * order of magnitude of the number. In other words the absolute value of the
     * base-10 logarithm divided by 3 minus 1 because 0 is no prefix
     */
    public static final char[] SMALL_METRIC_PREFIXES = new char[] { 'm', 'μ', 'n', 'p', 'f', 'a',
        'z', 'y', 'r', 'q' };

    public static String getMetricUnit(double number, char unit) {
        double OoM = Math.log10(Math.abs(number));
        char[] prefix = OoM >= 0 ? LARGE_METRIC_PREFIXES : SMALL_METRIC_PREFIXES;
        int index = (Math.abs((int) Math.floor(OoM / 3))) - 1;
        if (index == -1)
        {
            return String.valueOf(Math.round(number * 10000d) / 10000d) + ' ' + unit;
        } else
        {
            double shifted = number / Math.pow(10, Math.floor(OoM / 3) * 3);
            return String.valueOf(Math.round(shifted * 100d) / 100d) + ' '
                + prefix[index]
                + unit;
        }
    }

    public IChillerCasingType getCasingType() {
        return this.casingType;
    }

    public double getTemp() {
        return this.temp;
    }

    public boolean getSculk() {
        return this.hasSculk;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
