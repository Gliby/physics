package gliby.minecraft.physics.common.game;

import com.google.common.collect.ImmutableSet;
import gliby.minecraft.gman.RawItem;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.PhysicsConfig;
import gliby.minecraft.physics.common.entity.EntityPhysicsBlock;
import gliby.minecraft.physics.common.entity.EntityToolGunBeam;
import gliby.minecraft.physics.common.game.items.ItemPhysicsGun;
import gliby.minecraft.physics.common.game.items.toolgun.ItemToolGun;
import gliby.minecraft.physics.common.game.items.toolgun.actions.*;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.LinkedList;
import java.util.Set;

public class GameManager {

    protected static int networkId = 0;
    public static final Set<EntityEntry> SET_ENTITIES = ImmutableSet.of(
            // physics block
            EntityEntryBuilder.create()
                    .entity(EntityPhysicsBlock.class)
                    .id(new ResourceLocation(Physics.ID, "physicsblock"), networkId++)
                    .name("physicsblock")
                    .tracker(64, 20, false)
                    .build(),
            // tool gun beam
            EntityEntryBuilder.create()
                    .entity(EntityToolGunBeam.class)
                    .id(new ResourceLocation(Physics.ID, "toolgunbeam"), networkId++)
                    .name("toolgunbeam")
                    .tracker(64, 1, false)
                    .build()
    );
    public RawItem itemPhysicsGun, itemToolgun;
    private Physics physics;
    private ToolGunActionRegistry toolGunRegistry;

    public GameManager(Physics physics) {
        this.physics = physics;
    }

    @SubscribeEvent
    public void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        final IForgeRegistry<EntityEntry> registry = event.getRegistry();
        for (final EntityEntry entityEntry : SET_ENTITIES) {
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

    public void preInit() {
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


    public static LinkedList<EntityPhysicsBlock> getActiveBlocks() {
        return activeBlocks;
    }

    /**
     * Queue used for block culling.
     */
    protected static LinkedList<EntityPhysicsBlock> activeBlocks = new LinkedList<EntityPhysicsBlock>();

    public void onServerStopping() {
        getActiveBlocks().clear();
    }

    public void onPhysicsBlockDied(EntityPhysicsBlock physicsBlock) {
        if (PhysicsConfig.GAME.maxPhysicsBlocks > 0) {
            getActiveBlocks().remove(physicsBlock);
        }
    }

    public void onPhysicsBlockCreated(EntityPhysicsBlock physicsBlock) {
        if (PhysicsConfig.GAME.maxPhysicsBlocks > 0) {
            getActiveBlocks().push(physicsBlock);
            while (getActiveBlocks().size() > PhysicsConfig.GAME.maxPhysicsBlocks) {
                EntityPhysicsBlock block = getActiveBlocks().pollLast();
                if (block != null) {
                    block.setDead();
                }
            }
        }
    }


}
