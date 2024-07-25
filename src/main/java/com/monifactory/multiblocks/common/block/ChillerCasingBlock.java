package com.monifactory.multiblocks.common.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.client.renderer.block.TextureOverrideRenderer;
import com.lowdragmc.lowdraglib.Platform;
import com.monifactory.multiblocks.MoniMultiblocks;
import com.monifactory.multiblocks.api.block.IChillerCasingType;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ChillerCasingBlock extends ActiveBlock {
    public IChillerCasingType casingType;

    public ChillerCasingBlock(Properties properties, IChillerCasingType casingType) {
        super(properties,
            Platform.isClient()
                ? new TextureOverrideRenderer(new ResourceLocation("block/cube_all"),
                    Map.of("all", casingType.getTexture()))
                : null,
            Platform.isClient()
                ? new TextureOverrideRenderer(GTCEu.id("block/cube_2_layer_all"),
                    Map.of("bot_all", casingType.getTexture(), "top_all",
                        new ResourceLocation(casingType.getTexture() + "_bloom")))
                : null);
    }

    public enum ChillerCasingType implements StringRepresentable, IChillerCasingType {
        MESOL("mesol", 1e-3, 164, 1, GTValues.LuV, null, prependRoot("mesol")),
        BATHYAL("bathyal", 1e-6, 256, 2, GTValues.ZPM, null, prependRoot("bathyal")),
        ABYSSAL("abyssal", 1e-9, 1024, 3, GTValues.UV, null, prependRoot("abyssal")),
        HADAL("hadal", 1e-12, 4096, 4, GTValues.UHV, null, prependRoot("hadal"));

        private final String name;
        private final double minTemp;
        private final int passiveAmount;
        private final int passiveRate;
        private final int usage;
        @Getter
        private final int tier;
        private final Material material;
        private final ResourceLocation texture;

        ChillerCasingType(String name, double minTemp, int passiveAmount, int passiveRate, int tier,
            Material material, ResourceLocation texture) {
            this.name = name;
            this.minTemp = minTemp;
            this.passiveAmount = passiveAmount;
            this.passiveRate = passiveRate;
            this.tier = tier;
            this.usage = GTValues.VA[tier];
            this.material = material;
            this.texture = texture;
        }
        @Override
        public @NotNull String getName() {
            return this.name;
        }

        @Override
        public double getCasingTemperature() {
            return this.minTemp;
        }

        @Override
        public long getPassiveConsumptionAmount() {
            return this.passiveAmount;
        }

        @Override
        public long getPassiveConsumptionRate() {
            return this.passiveRate;
        }

        @Override
        public long getEnergyUsage() {
            return this.usage;
        }

        @Override
        public @Nullable Material getMaterial() {
            return this.material;
        }

        @Override
        public ResourceLocation getTexture() {
            return this.texture;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        private static ResourceLocation prependRoot(String path) {
            return MoniMultiblocks.id("block/casings/coils/" + path);
        }


    }

}
