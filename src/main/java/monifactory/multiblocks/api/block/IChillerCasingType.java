package monifactory.multiblocks.api.block;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IChillerCasingType {
    /**
     * @return The Unique Name of this casing (sans `casing`, just the name)
     */
    @NotNull
    String getName();

    /**
     * @return the minimum temperature this casing can withstand in K
     */
    double getCasingTemperature();

    /**
     * @return how much of the passive fluid is used each time in mB
     */
    long getPassiveConsumptionAmount();

    /**
     * @return ticks between passive consumptions + 1
     */
    long getPassiveConsumptionRate();

    /**
     * 
     * @return the tier of energy it passively consumes
     */
    long getEnergyUsage();

    /**
     * @return the {@link Material} of the casing if it has one, otherwise
     *         {@code null}
     */
    @Nullable
    Material getMaterial();

    /**
     * @return the {@link ResourceLocation} defining the base texture of the casing
     */
    ResourceLocation getTexture();
}
