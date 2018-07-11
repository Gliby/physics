package gliby.minecraft.physics.common.game.items.toolgun.actions;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Vector3f;

import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.mechanics.gravitymagnets.GravityModifierMechanic;
import gliby.minecraft.physics.common.physics.mechanics.gravitymagnets.GravityModifierMechanic.GravityMagnet;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 *
 */
public class ToolGunAttractAction implements IToolGunAction {

	@Override
	public String getName() {
		return "Attract";
	}

	private Map<Integer, GravityMagnet> attractionHandlers = new HashMap<Integer, GravityMagnet>();

	@Override
	public boolean use(PhysicsWorld world, EntityPlayerMP player, Vector3f lookAt) {
		GravityModifierMechanic mechanic;
		if ((mechanic = (GravityModifierMechanic) world.getMechanics().get("GravityMagnet")) != null) {
			GravityMagnet gravityMagnet = attractionHandlers.get(player.getEntityId());
			if (gravityMagnet != null) {
				mechanic.removeGravityMagnet(gravityMagnet);
				attractionHandlers.remove(player.getEntityId());
			}
			attractionHandlers.put(player.getEntityId(), mechanic.addGravityMagnet(new GravityMagnet(lookAt, Physics.getInstance().getSettings().getIntegerSetting("Tools.AttractRadius").getIntValue(), Physics.getInstance().getSettings().getIntegerSetting("Tools.AttractForce").getIntValue())));
		}
		return true;
	}

	@Override
	public void stoppedUsing(PhysicsWorld world, EntityPlayerMP player) {
		GravityModifierMechanic mechanic;
		if ((mechanic = (GravityModifierMechanic) world.getMechanics().get("GravityMagnet")) != null) {
			GravityMagnet magnet = attractionHandlers.get(player.getEntityId());
			if (magnet != null) {
				mechanic.removeGravityMagnet(magnet);
				attractionHandlers.remove(player.getEntityId());
			}
		}
	}
}
