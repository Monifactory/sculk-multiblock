package monifactory.multiblocks.common.data;

import com.gregtechceu.gtceu.api.item.RendererBlockItem;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import monifactory.multiblocks.api.block.IChillerCasingType;
import monifactory.multiblocks.common.CommonProxy;
import monifactory.multiblocks.common.block.ChillerCasingBlock;
import monifactory.multiblocks.common.block.ChillerCasingBlock.ChillerCasingType;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MMBlocks {


    public static Map<IChillerCasingType, Supplier<ChillerCasingBlock>> CHILLER_CASINGS = new HashMap<>();

    public static BlockEntry<ChillerCasingBlock> MESOL_CASING = createChillerCasingBlock(
        ChillerCasingType.MESOL);
    public static BlockEntry<ChillerCasingBlock> BATHYAL_CASING = createChillerCasingBlock(
        ChillerCasingType.BATHYAL);
    public static BlockEntry<ChillerCasingBlock> ABYSSAL_CASING = createChillerCasingBlock(
        ChillerCasingType.ABYSSAL);
    public static BlockEntry<ChillerCasingBlock> HADAL_CASING = createChillerCasingBlock(
        ChillerCasingType.HADAL);



    public static void init() {

    }

    private static BlockEntry<ChillerCasingBlock> createChillerCasingBlock(
        IChillerCasingType coilType) {
        System.out.println("Creating Chiller Casing");
        BlockEntry<ChillerCasingBlock> coilBlock = CommonProxy.REGISTRATE
            .block("%s_coil_block".formatted(coilType.getName()),
                p -> new ChillerCasingBlock(p, coilType))
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
            .blockstate(NonNullBiConsumer.noop())
            .tag(/* GTToolType.WRENCH.harvestTags.get(0), */BlockTags.MINEABLE_WITH_PICKAXE)
            .item(RendererBlockItem::new).model(NonNullBiConsumer.noop())
            .build()
            .register();
        CHILLER_CASINGS.put(coilType, coilBlock);
        return coilBlock;
    }
}
