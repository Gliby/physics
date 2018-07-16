package gliby.minecraft.physics.common.game.items.toolgun.actions;


import com.badlogic.gdx.math.Vector3;

import gliby.minecraft.gman.EntityUtility;
import gliby.minecraft.physics.common.entity.EntityPhysicsBlock;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;

/**
 *
 */
public class ToolGunReviveAction implements IToolGunAction {

	public boolean use(PhysicsWorld physicsWorld, EntityPlayerMP player, Vector3 lookAt) {
		MovingObjectPosition position = EntityUtility.rayTrace(player, 64);
		if (position.getBlockPos() != null) {
			IBlockState state = player.worldObj.getBlockState(position.getBlockPos());
			state = state.getBlock().getActualState(state, player.worldObj, position.getBlockPos());
			EntityPhysicsBlock block = new EntityPhysicsBlock(player.worldObj, physicsWorld, state,
					position.getBlockPos().getX(), position.getBlockPos().getY(), position.getBlockPos().getZ())
							.setDropItem(new ItemStack(Item.getItemFromBlock(state.getBlock())));
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
		return "Reconstruct";
	}

}
