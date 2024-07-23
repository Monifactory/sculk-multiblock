package com.monifactory.multiblocks.integration.kjs;


import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.monifactory.multiblocks.MoniMultiblocks;
import com.monifactory.multiblocks.common.data.MMBlocks;
import com.monifactory.multiblocks.common.data.MMMachines;
import com.monifactory.multiblocks.common.data.MMRecipeTypes;
import com.monifactory.multiblocks.integration.kjs.recipe.MMRecipeSchema;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ClassFilter;

public class MMKubeJSPlugin extends KubeJSPlugin {

    @Override
    public void initStartup() {
        super.initStartup();
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void registerClasses(ScriptType type, ClassFilter filter) {
        super.registerClasses(type, filter);
        // allow user to access all gtceu classes by importing them.
        filter.allow("com.monifactory.multiblocks");
    }

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        for (var entry : GTRegistries.RECIPE_TYPES.entries()) {
            event.register(entry.getKey(), MMRecipeSchema.SCHEMA);
        }
    }


    @Override
    public void registerBindings(BindingsEvent event) {
        super.registerBindings(event);
        event.add("MMBlocks", MMBlocks.class);
        event.add("MMMachines", MMMachines.class);
        event.add("MMRecipeTypes", MMRecipeTypes.class);
        event.add("MoniMultiblocks", MoniMultiblocks.class);
    }
}