package gliby.minecraft.physics.common.game.events;

import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.common.blocks.PhysicsBlockMetadata;
import gliby.minecraft.physics.common.entity.EntityPhysicsBlock;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.math3.analysis.function.Min;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ExplosionHandler {

    Physics physics;

    public ExplosionHandler(Physics physics) {
        this.physics = physics;
    }

    // TODO bug: fix explosions
    @SubscribeEvent
    public void handleEvent(final ExplosionEvent.Detonate event) {
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
                    if (blockState.getBlock().getMaterial(blockState) != Material.AIR && shouldSpawnInExplosions) {
                        blockState = blockState.getBlock().getActualState(blockState, event.getWorld(), pos);
                        EntityPhysicsBlock analog = new EntityPhysicsBlock(event.getWorld(), physicsWorld, blockState,
                                pos.getX(), pos.getY(), pos.getZ());
                        event.getWorld().spawnEntity(analog);
                        affectedEntities.add(analog);
                    }
                }
                float explosionRadius = physics.getSettings().getFloatSetting("Game.ExplosionImpulseRadius")
                        .getFloatValue();
                float force = Physics.getInstance().getSettings().getFloatSetting("Game.ExplosionImpulseForce")
                        .getFloatValue();
                for (int i = 0; i < affectedEntities.size(); i++) {
                    IRigidBody body = affectedEntities.get(i).getRigidBody();
                    Vector3f centerOfMass = body.getCenterOfMassPosition(new Vector3f());
                    Vector3f direction = new Vector3f();
                    direction.sub(explosion, centerOfMass);
                    float distance = direction.length();
                    if (distance <= explosionRadius && body.isValid()) {
                        direction.normalize();
                        float forceMultiplier = explosionRadius / (1 + explosionRadius - distance);
                        direction.scale(force * Math.abs(forceMultiplier));
                        body.applyCentralImpulse(direction);
                    }
                }

            }
        });
    }
}
