package net.gliby.physics.common.entity.mechanics;

import java.util.List;

import net.gliby.physics.common.entity.EntityPhysicsBlock;
import net.gliby.physics.common.entity.IEntityPhysics;
import net.gliby.physics.common.physics.PhysicsWorld;
import net.gliby.physics.common.physics.engine.IRigidBody;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.relauncher.Side;

public class ClientBlockInheritanceMechanic extends RigidBodyMechanic {

	@Override
	public void update(IRigidBody rigidBody, PhysicsWorld physicsWorld, Entity entity, Side side) {
		if (side.isClient()) {
			if (entity instanceof EntityPhysicsBlock) {
				EntityPhysicsBlock blockEntity = (EntityPhysicsBlock) entity;
				AxisAlignedBB bb = blockEntity.getRenderBoundingBox();
				List<Entity> entitesWithin = entity.getEntityWorld().getEntitiesWithinAABB(Entity.class, bb,
						IEntityPhysics.NOT_PHYSICS_OBJECT);
				for (int i = 0; i < entitesWithin.size(); i++) {
					Entity collidedEntity = entitesWithin.get(i);
					IBlockState blockState = blockEntity.getBlockState();
					Block block = blockState.getBlock();
					// collidedEntity.attackEntityFrom(DamageSource.cactus,
					// 1F);
					BlockPos pos = new BlockPos(entity);
					block.onEntityCollidedWithBlock(entity.getEntityWorld(), pos, blockState, collidedEntity);
					block.onEntityCollidedWithBlock(entity.getEntityWorld(), pos, collidedEntity);
				}
			}
		}
	}

}
