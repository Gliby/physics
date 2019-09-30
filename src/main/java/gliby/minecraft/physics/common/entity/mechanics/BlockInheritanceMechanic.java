package gliby.minecraft.physics.common.entity.mechanics;

import gliby.minecraft.physics.common.entity.EnumRigidBodyProperty;
import gliby.minecraft.physics.common.entity.IEntityPhysics;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.relauncher.Side;

import javax.vecmath.Vector3f;
import java.util.List;

public class BlockInheritanceMechanic extends RigidBodyMechanic {

    @Override
    public void update(IRigidBody rigidBody, PhysicsWorld physicsWorld, Entity entity, Side side) {
        IBlockState blockState;
        if (side.isServer()) {
            if ((blockState = (IBlockState) rigidBody.getProperties().get(EnumRigidBodyProperty.BLOCKSTATE.getName())) != null) {
                Vector3f bbMin = new Vector3f(), bbMax = new Vector3f();
                rigidBody.getAabb(bbMin, bbMax);
                AxisAlignedBB bb = AxisAlignedBB.fromBounds(bbMin.x, bbMin.y, bbMin.z, bbMax.x, bbMax.y, bbMax.z)
                        .offset(0.5f, 0.5f, 0.5f);
                List<Entity> entitesWithin = rigidBody.getOwner().getEntityWorld().getEntitiesWithinAABB(Entity.class,
                        bb, IEntityPhysics.NOT_PHYSICS_OBJECT);
                for (int i = 0; i < entitesWithin.size(); i++) {
                    Entity collidedEntity = entitesWithin.get(i);
                    Block block = blockState.getBlock();
                    // collidedEntity.attackEntityFrom(DamageSource.cactus, 1F);
                    BlockPos pos = new BlockPos(rigidBody.getOwner());
                    block.onEntityCollidedWithBlock(rigidBody.getOwner().getEntityWorld(), pos, blockState,
                            collidedEntity);
                    block.onEntityCollidedWithBlock(rigidBody.getOwner().getEntityWorld(), pos, collidedEntity);
                }
            }
        } else {
        }
    }

}
