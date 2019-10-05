package gliby.minecraft.physics.common.game.events;

import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.common.blocks.PhysicsBlockMetadata;
import gliby.minecraft.physics.common.entity.EntityPhysicsBlock;
import gliby.minecraft.physics.common.physics.PhysicsOverworld;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.math3.analysis.function.Min;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class GameEventHandler {

    Physics physics;

    public GameEventHandler(Physics physics) {
        this.physics = physics;
    }

    @SubscribeEvent
    public void handleExplosion(final EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote)
            return;
        if (event.getEntity() instanceof EntityFallingBlock) {
            boolean replaceFallingBlocks = physics.getSettings().getBooleanSetting("Game.ReplaceFallingBlocks").getBooleanValue();
            if (replaceFallingBlocks) {
                final EntityFallingBlock entityFallingBlock = (EntityFallingBlock) event.getEntity();
                event.setCanceled(true);
                event.getWorld().getMinecraftServer().addScheduledTask(new Runnable() {
                    @Override
                    public void run() {
                        final World world = event.getWorld();
                        Physics physics = Physics.getInstance();
                        PhysicsWorld physicsWorld = physics.getPhysicsOverworld().getPhysicsByWorld(event.getWorld());

                        // Remove block.
                        BlockPos pos = entityFallingBlock.getOrigin();
                        world.setBlockToAir(pos);

                        // Spawn phyics block.
                        IBlockState blockState = entityFallingBlock.getBlock();
                        EntityPhysicsBlock analog = new EntityPhysicsBlock(event.getWorld(), physicsWorld, blockState,
                                entityFallingBlock.posX - PhysicsOverworld.OFFSET, entityFallingBlock.posY - PhysicsOverworld.OFFSET, entityFallingBlock.posZ - PhysicsOverworld.OFFSET);
                        // Disable collision, because playing survival with Physics Falling blocks is annoying.
                        // TODO (0.7.0) FEATURE Survival collision issues, could be fixed if Physics Block were diggable.
                        event.getWorld().spawnEntity(analog.setCollisionEnabled(false).setGameSpawned(true));
                    }
                });
            }
        }
    }

    // TODO explosion tuning: some blocks are too heavy for explosions. while other are too light.
    @SubscribeEvent
    public void handleExplosion(final ExplosionEvent.Detonate event) {
        if (event.getWorld().isRemote)
            return;

        event.getWorld().getMinecraftServer().addScheduledTask(new Runnable() {

            @Override
            public void run() {
                Physics physics = Physics.getInstance();
                PhysicsWorld physicsWorld = physics.getPhysicsOverworld().getPhysicsByWorld(event.getWorld());
                Vector3f explosion = new Vector3f((float) event.getExplosion().getPosition().x,
                        (float) event.getExplosion().getPosition().y, (float) event.getExplosion().getPosition().z);

                List<EntityPhysicsBlock> affectedEntities = new ArrayList<EntityPhysicsBlock>();
                for (int i = 0; i < event.getAffectedBlocks().size(); i++) {
                    BlockPos pos = event.getAffectedBlocks().get(i);
                    IBlockState blockState = event.getWorld().getBlockState(pos);
                    PhysicsBlockMetadata metadata = physics.getBlockManager().getPhysicsBlockMetadata()
                            .get(physics.getBlockManager().getBlockIdentity(blockState.getBlock()));
                    boolean shouldSpawnInExplosions = metadata == null || metadata.spawnInExplosions;
                    if (blockState.getMaterial() != Material.AIR && shouldSpawnInExplosions) {
                        blockState = blockState.getActualState(event.getWorld(), pos);
                        EntityPhysicsBlock analog = new EntityPhysicsBlock(event.getWorld(), physicsWorld, blockState,
                                pos.getX(), pos.getY(), pos.getZ());
                        event.getWorld().spawnEntity(analog.setGameSpawned(true));
                        affectedEntities.add(analog);
                    }
                }
                float explosionRadius = physics.getSettings().getFloatSetting("Game.ExplosionImpulseRadius")
                        .getFloatValue();
                float force = Physics.getInstance().getSettings().getFloatSetting("Game.ExplosionImpulseForce")
                        .getFloatValue();
                for (int i = 0; i < affectedEntities.size(); i++) {
                    IRigidBody body = affectedEntities.get(i).getRigidBody();
                    Vector3f centerOfMass = body.getCenterOfMassPosition();
                    Vector3f direction = new Vector3f();
                    direction.sub(centerOfMass, explosion);
                    float distance = direction.length();
                    if (distance <= explosionRadius && body.isValid()) {
                        direction.normalize();
//                        direction.scale(-1);
                        // blocks closer to the explosion will receive more force.
//                        float forceMultiplier = explosionRadius / (1 + explosionRadius - distance);
                        direction.scale(force);
                        body.applyCentralImpulse(direction);
                    }
                }

            }
        });
    }
}
