package gliby.minecraft.physics.common.game.items.toolgun.actions;

import gliby.minecraft.physics.common.physics.PhysicsWorld;
import net.minecraft.entity.player.EntityPlayerMP;

import javax.vecmath.Vector3f;

/**
 *
 */
public interface IToolGunAction {

    String getName();

    boolean use(PhysicsWorld world, EntityPlayerMP player, Vector3f lookAt);

    void stoppedUsing(PhysicsWorld world, EntityPlayerMP player);
}
