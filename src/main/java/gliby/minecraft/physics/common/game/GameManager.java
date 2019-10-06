package gliby.minecraft.physics.common.game;

import com.google.common.collect.ImmutableSet;
import gliby.minecraft.gman.RawItem;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.common.entity.EntityPhysicsBlock;
import gliby.minecraft.physics.common.entity.EntityToolGunBeam;
import gliby.minecraft.physics.common.game.items.ItemPhysicsGun;
import gliby.minecraft.physics.common.game.items.toolgun.ItemToolGun;
import gliby.minecraft.physics.common.game.items.toolgun.actions.*;
import net.minecraft.block.material.MapColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.*;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Set;

public class GameManager {

    public RawItem itemPhysicsGun, itemToolgun;
    private Physics physics;
    private ToolGunActionRegistry toolGunRegistry;

    public GameManager(Physics physics) {
        this.physics = physics;
    }

    public static int networkId = 0;

    public static final Set<EntityEntry> SET_ENTITIES = ImmutableSet.of(
            // physics block
            EntityEntryBuilder.create()
                    .entity(EntityPhysicsBlock.class)
                    .id(new ResourceLocation(Physics.ID, "physicsblock"), networkId++)
                    .name("physicsblock")
                    .tracker(64, 20, true)
                    .build(),
            // tool gun beam
            EntityEntryBuilder.create()
                    .entity(EntityToolGunBeam.class)
                    .id(new ResourceLocation(Physics.ID, "toolgunbeam"), networkId++)
                    .name("toolgunbeam")
                    .tracker(64, 1, false)
                    .build()
            );

    @SubscribeEvent
    public void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        final IForgeRegistry<EntityEntry> registry = event.getRegistry();
        for (final EntityEntry entityEntry : SET_ENTITIES)
        {
            registry.register(entityEntry);
        }
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        final IForgeRegistry<Item> registry = event.getRegistry();

        registry.register(itemToolgun);
        registry.register(itemPhysicsGun);

        MinecraftForge.EVENT_BUS.register(itemPhysicsGun);
        MinecraftForge.EVENT_BUS.register(itemToolgun);

    }

    @SubscribeEvent
    public void registerRecipe(RegistryEvent.Register<IRecipe> event) {
        final IForgeRegistry<IRecipe> registry = event.getRegistry();


    }


    public void preInit() {

//        GameRegistry.addRecipe(new ItemStack(itemPhysicsGun), "AAA", "BCB", "AAA", 'A', Blocks.obsidian, 'B',
//                Items.redstone, 'C', Items.diamond);
//
//        GameRegistry.addRecipe(new ItemStack(itemToolgun), "AAA", "CBC", "AAA", 'A', Items.redstone, 'B',
//                itemPhysicsGun, 'C', Items.diamond);
        /*
         * GameRegistry.addRecipe(new ItemStack(Items.dye, 2, 15), "AB ", "AAC", "A  ",
         * 'A', Items.cookie, 'B', Blocks.dirt, 'C', new ItemStack(Items.dye, 1, 1));
         */

        itemToolgun = new ItemToolGun(physics);
        itemToolgun.setRegistryName(itemToolgun.getUnlocalizedName());

        itemPhysicsGun = new ItemPhysicsGun(physics);
        itemPhysicsGun.setRegistryName(itemPhysicsGun.getUnlocalizedName());

        toolGunRegistry = new ToolGunActionRegistry();
        toolGunRegistry.registerAction(new ToolGunReviveAction(), Physics.ID);
        toolGunRegistry.registerAction(new ToolGunAttachAction(), Physics.ID);
        toolGunRegistry.registerAction(new ToolGunAlignAction(), Physics.ID);
        toolGunRegistry.registerAction(new ToolGunAttractAction(), Physics.ID);
        toolGunRegistry.registerAction(new ToolGunChangeGravityAction(), Physics.ID);
//        toolGunRegistry.registerAction(new ToolGunRemoveAction(), Physics.ID);
        // toolGunRegistry.registerAction(new ToolGunMotorAction(),
        // Physics.MOD_ID);

    }

    public ToolGunActionRegistry getToolGunRegistry() {
        return toolGunRegistry;
    }

}
