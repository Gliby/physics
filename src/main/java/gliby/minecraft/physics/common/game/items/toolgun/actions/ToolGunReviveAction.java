package gliby.minecraft.physics.common.game.items.toolgun.actions;

import gliby.minecraft.gman.EntityUtility;
import gliby.minecraft.physics.common.entity.EntityPhysicsBlock;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;

import javax.vecmath.Vector3f;

/**
 *
 */
public class ToolGunReviveAction implements IToolGunAction {

    public boolean use(PhysicsWorld physicsWorld, EntityPlayerMP player, Vector3f lookAt) {
        RayTraceResult position = EntityUtility.rayTrace(player, 64);
        if (position.getBlockPos() != null) {
            IBlockState state = player.world.getBlockState(position.getBlockPos());
            state = state.getBlock().getActualState(state, player.world, position.getBlockPos());
            EntityPhysicsBlock block = new EntityPhysicsBlock(player.world, physicsWorld, state,
                    position.getBlockPos().getX(), position.getBlockPos().getY(), position.getBlockPos().getZ());
            player.world.setBlockToAir(position.getBlockPos());
            player.world.spawnEntity(block);
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
