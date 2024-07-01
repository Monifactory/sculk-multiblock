package monifactory.multiblocks.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.ItemHandlerProxyTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

/**
 * Special bus to put sculk catalysts or whatever in to seed the Hypogean
 * Infuser with sculk
 */
public class SculkSourceBus extends MultiblockPartMachine {

    private static final int INVENTORY_SIZE = 1;

    private ItemHandlerProxyTrait inputInventoryProxy;
    @Persisted
    private NotifiableItemStackHandler inputHandler;

    public SculkSourceBus(IMachineBlockEntity holder) {
        super(holder);
        this.inputInventoryProxy = new ItemHandlerProxyTrait(this, IO.IN);
        this.inputHandler = new NotifiableItemStackHandler(this, INVENTORY_SIZE, IO.IN);
        this.inputInventoryProxy.setProxy(inputHandler);
    }

    public void addListener(Runnable runnable) {
        this.inputHandler.addChangedListener(runnable);
    }

    public boolean getSculk() {
        return inputInventoryProxy.isEmpty();
    }

    @Override
    public Widget createUIWidget() {
        int rowSize = (int) Math.sqrt(INVENTORY_SIZE);
        int colSize = rowSize;
        var group = new WidgetGroup(0, 0, 18 * rowSize + 16, 18 * colSize + 16);
        var container = new WidgetGroup(4, 4, 18 * rowSize + 8, 18 * colSize + 8);
        int index = 0;
        for (int y = 0; y < colSize; y++)
        {
            for (int x = 0; x < rowSize; x++)
            {
                container.addWidget(new SlotWidget(inputInventoryProxy, index++, 4 + x * 18,
                    4 + y * 18, true, true).setBackgroundTexture(GuiTextures.SLOT));
                // .setIngredientIO(IngredientIO.INPUT));
            }
        }

        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);

        return group;
    }
}
