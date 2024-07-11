package com.monifactory.multiblocks.common;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.monifactory.multiblocks.MoniMultiblocks;
import com.monifactory.multiblocks.client.data.MMCreativeTabs;
import com.monifactory.multiblocks.common.data.MMBlocks;
import com.monifactory.multiblocks.common.data.MMMachines;
import com.monifactory.multiblocks.common.data.MMRecipeTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class CommonProxy {
    public static final GTRegistrate REGISTRATE = GTRegistrate.create(MoniMultiblocks.MOD_ID);


    // public static final GTRegistrate REGISTRATE =
    // GTRegistrate.create(MoniMultiblocks.MOD_ID);
    // public static final Registrate REGISTRATE
    // =Registrate.create(MoniMultiblocks.MOD_ID);
    // public static GTRegistrate REGISTRATE;
    /*
     * public static RegistryEntry<CreativeModeTab> DEFAULT = REGISTRATE
     * .defaultCreativeTab("material_block", builder -> builder .displayItems(new
     * RegistrateDisplayItemsGenerator("material_item", REGISTRATE)) .icon(() ->
     * MMBlocks.MESOL_CASING.asStack()) .build()) .register();
     */
    static
    {
        // REGISTRATE.creativeModeTab(() -> DEFAULT);
    }

    public CommonProxy() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(this);
        bus.addGenericListener(GTRecipeType.class, this::registerRecipeTypes);
        // bus.addGenericListener(Class.class, this::registerRecipeConditions);
        bus.addGenericListener(MachineDefinition.class, this::registerMachines);
    }

    public void init() {
        REGISTRATE.creativeModeTab(() -> MMCreativeTabs.DEFAULT);
        MMBlocks.init();

        REGISTRATE.registerRegistrate();
    }

    @SubscribeEvent
    public void modConstruct(FMLConstructModEvent event) {
        // this is done to delay initialization of content to be after KJS has set up.
        event.enqueueWork(() -> this.init());
    }

    @SubscribeEvent
    public void registerRecipeTypes(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {
        MMRecipeTypes.init();
        System.out.println(MMRecipeTypes.INFUSER_RECIPES);
    }

    @SubscribeEvent
    public void registerMachines(
        GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        MMMachines.init();
    }
}
