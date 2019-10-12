package gliby.minecraft.physics.common.entity.mechanics;

import gliby.minecraft.physics.common.entity.EntityPhysicsBlock;
import gliby.minecraft.physics.common.entity.EnumRigidBodyProperty;
import gliby.minecraft.physics.common.entity.IEntityPhysics;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Block Inheritance is a mechanic that aims to replicate the original blocks principles.
 * For example the Cactus Block deals damage when you collide with it,
 * let's try to inherit that mechanic from the game and apply to our Physics Blocks.
 */
public class BlockInheritanceMechanic extends RigidBodyMechanic {

    @SuppressWarnings("unchecked")
    @Override
    public void update(IRigidBody rigidBody, @Nullable PhysicsWorld physicsWorld, Entity entity, Side side) {
        IBlockState blockState = null;
        // We don't have access to rigid body properties on the client.
        if (entity instanceof EntityPhysicsBlock)
            blockState = ((EntityPhysicsBlock) entity).getBlockState();
        else if (side.isServer())
            blockState = (IBlockState) rigidBody.getProperties().get(EnumRigidBodyProperty.BLOCKSTATE.getName());

        // Matches blocks actual tick rate.
        boolean shouldTick = entity.ticksExisted % blockState.getBlock().tickRate(entity.getEntityWorld()) == 0;

        if (blockState != null && shouldTick) {
            List<Entity> entitesWithin = entity.getEntityWorld().getEntitiesWithinAABB(Entity.class,
                    entity.getCollisionBoundingBox().grow(0.1f), IEntityPhysics.NOT_BLACKLISTED);
            BlockPos pos = entity.getPosition();
            for (int i = 0; i < entitesWithin.size(); i++) {
                Entity collidedEntity = entitesWithin.get(i);
                Block block = blockState.getBlock();
                // simulate block touch
                try {
                    block.onEntityCollidedWithBlock(entity.getEntityWorld(), pos, blockState,
                            collidedEntity);
                } catch (IllegalArgumentException e) {

                }
            }
        }
    }

}
