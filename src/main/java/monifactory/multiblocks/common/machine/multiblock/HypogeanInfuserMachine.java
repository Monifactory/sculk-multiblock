package monifactory.multiblocks.common.machine.multiblock;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import monifactory.multiblocks.api.block.IChillerCasingType;
import monifactory.multiblocks.common.block.ChillerCasingBlock.ChillerCasingType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HypogeanInfuserMachine extends WorkableMultiblockMachine
    implements IFancyUIMachine, IDisplayUIMachine {

    public static final double ROOM_TEMP = 293.15;

    private IChillerCasingType casingType = ChillerCasingType.MESOL;

    /**
     * Current Temperature of the infuser. in K
     * 
     * Lower min temperature means it can cool down faster and reach a recipe's temp
     * faster (or just be able to reach the recipe in the first place
     */

    private double maxTemp;
    private double minTemp;

    @Persisted
    private double temp;

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

    protected TickableSubscription passiveSubs;
    protected TickableSubscription temperatureSubs;

    public HypogeanInfuserMachine(IMachineBlockEntity holder) {
        super(holder);
        // this.temp = ROOM_TEMP;
        this.hasSculk = true;
    }

    public void notifyStatusChanged(RecipeLogic.Status oldStatus, RecipeLogic.Status newStatus) {
        if (oldStatus == RecipeLogic.Status.SUSPEND)
        {
            this.updateTemperatureSubscription(newStatus);
        }
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(198, 208, this, entityPlayer)
            .widget(new FancyMachineUIWidget(this, 198, 208));
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        textList.add(Component.literal(Double.toString(this.temp) + 'K'));
        textList.add(Component.literal("Has Sculk: " + this.hasSculk));
        IDisplayUIMachine.super.addDisplayText(textList);
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 182 + 8, 117 + 8);
        group.addWidget(new DraggableScrollableWidgetGroup(4, 4, 182, 117)
            .setBackground(getScreenTexture())
            .addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()))
            .addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText)
                .textSupplier(this.getLevel().isClientSide ? null : this::addDisplayText)
                .setMaxWidthLimit(150).clickHandler(this::handleDisplayClick)));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
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

    @SuppressWarnings("unchecked")
    protected void updatePassive() {
        if (this.isFormed()) {
        if ((getOffsetTimer() % casingType.getPassiveConsumptionRate()) == 0)
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
                this.unsubscribe(passiveSubs);
                this.passiveSubs = null;
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
            { //enabled and has energy, start cooling
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
        if (this.isWorkingEnabled())
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
            IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.BOTH);
            if (io == IO.NONE || io == IO.OUT)
                continue;
            for (var handler : part.getRecipeHandlers())
            {
                // If IO not compatible
                if (io != IO.BOTH && handler.getHandlerIO() != IO.BOTH
                    && io != handler.getHandlerIO())
                    continue;
                if (handler.getCapability() == EURecipeCapability.CAP
                    && handler instanceof IEnergyContainer container)
                {
                    energyContainers.add(container);
                    traitSubscriptions
                        .add(handler.addChangedListener(this::updateTemperatureSubscription));
                }
            }
        }
        this.inputEnergyContainers = new EnergyContainerList(energyContainers);
        Object obj = this.getMultiblockState().getMatchContext().get("ChillerCasing");
        if (obj instanceof IChillerCasingType casing)
        {
            this.casingType = casing;
        }


    }


    @Override
    public void onLoad() {
        super.onLoad();
        this.updateTemperatureSubscription();
        this.passiveSubs = this.subscribeServerTick(this::updatePassive);
    }

    public IChillerCasingType getCasingType() {
        return this.casingType;
    }

    /**
     * Slightly randomize value to make it look more realistic
     * 
     * @param value
     * @return value randomized by a guassian
     */
    private double fuzz(double value) {
        return value - value * this.getLevel().getRandom().nextGaussian();
    }

    public double getTemp() {
        return this.temp;
    }

    public boolean getSculk() {
        return this.hasSculk;
    }
}
