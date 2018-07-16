package gliby.minecraft.physics.common.game.items.toolgun.actions;

import javax.vecmath.Vector3f;

import com.badlogic.gdx.math.Vector3;

import gliby.minecraft.physics.common.physics.PhysicsWorld;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 *
 */
public interface IToolGunAction {

	public String getName();

	public boolean use(PhysicsWorld world, EntityPlayerMP player, Vector3 lookAt);
	
	public void stoppedUsing(PhysicsWorld world, EntityPlayerMP player);
}
