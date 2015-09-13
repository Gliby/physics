/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.game.items.toolgun.actions;

import javax.vecmath.Vector3f;

import net.gliby.gman.EntityUtility;
import net.gliby.physics.common.entity.EntityPhysicsBlock;
import net.gliby.physics.common.physics.PhysicsWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;

/**
 *
 */
public class ToolGunReviveAction implements IToolGunAction {

	public boolean use(PhysicsWorld world, EntityPlayerMP player, Vector3f lookAt) {
		MovingObjectPosition position = EntityUtility.rayTrace(player, 64);
		if (position.getBlockPos() != null) {
			IBlockState state = player.worldObj.getBlockState(position.getBlockPos());
			state = state.getBlock().getActualState(state, player.worldObj, position.getBlockPos());
			EntityPhysicsBlock block = new EntityPhysicsBlock(player.worldObj, world, state,
					position.getBlockPos().getX(), position.getBlockPos().getY(), position.getBlockPos().getZ())
							.setDropItem(new ItemStack(state.getBlock().getItem(player.worldObj, position.getBlockPos())));
			player.worldObj.setBlockToAir(position.getBlockPos());
			player.worldObj.spawnEntityInWorld(block);
			return true;
		}
		return false;
	}

	@Override
	public void stoppedUsing(PhysicsWorld world, EntityPlayerMP player) {
	}

	@Override
	public String getName() {
		return "Revive";
	}

}
