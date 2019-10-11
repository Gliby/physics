package gliby.minecraft.physics.common.entity.mechanics;

import gliby.minecraft.physics.common.entity.EntityPhysicsBlock;
import gliby.minecraft.physics.common.entity.IEntityPhysics;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;

// Slated to be removed.
public class ClientBlockInheritanceMechanic extends RigidBodyMechanic {

    @SuppressWarnings("unchecked")
    @Override
    public void update(IRigidBody rigidBody, PhysicsWorld physicsWorld, Entity entity, Side side) {
//        if (side.isClient()) {
//            if (entity instanceof EntityPhysicsBlock) {
//                EntityPhysicsBlock blockEntity = (EntityPhysicsBlock) entity;
//                AxisAlignedBB bb = blockEntity.getRenderBoundingBox();
//                List<Entity> entitesWithin = entity.getEntityWorld().getEntitiesWithinAABB(Entity.class, bb,
//                        IEntityPhysics.NOT_PHYSICS_OBJECT);
//                for (int i = 0; i < entitesWithin.size(); i++) {
//                    Entity collidedEntity = entitesWithin.get(i);
//                    IBlockState blockState = blockEntity.getBlockState();
//                    Block block = blockState.getBlock();
//                    // collidedEntity.attackEntityFrom(DamageSource.cactus,
//                    // 1F);
//                    BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(entity.posX, entity.posY, entity.posZ);
//                    block.onEntityCollidedWithBlock(entity.getEntityWorld(), pos, blockState, collidedEntity);
//                    pos.release();
////                    block.onEntityCollidedWithBlock(entity.getEntityWorld(), pos, collidedEntity);
//                }
//            }
//        }
    }

}
