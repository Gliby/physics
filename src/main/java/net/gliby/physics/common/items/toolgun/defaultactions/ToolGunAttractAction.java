/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.items.toolgun.defaultactions;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Vector3f;

import net.gliby.physics.Physics;
import net.gliby.physics.common.items.toolgun.IToolGunAction;
import net.gliby.physics.common.physics.PhysicsWorld;
import net.gliby.physics.common.physics.worldmechanics.gravitymagnets.GravityModifierMechanic;
import net.gliby.physics.common.physics.worldmechanics.gravitymagnets.GravityModifierMechanic.GravityMagnet;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 *
 */
public class ToolGunAttractAction implements IToolGunAction {

	@Override
	public String getName() {
		return "Attract";
	}

	private Map<Integer, GravityMagnet> magnetOwners = new HashMap<Integer, GravityMagnet>();

	@Override
	public boolean use(PhysicsWorld world, EntityPlayerMP player, Vector3f lookAt) {
		GravityModifierMechanic mechanic;
		if ((mechanic = (GravityModifierMechanic) world.getMechanics().get("GravityMagnet")) != null) {
			GravityMagnet gravityMagnet = magnetOwners.get(player.getEntityId());
			if (gravityMagnet != null) {
				mechanic.removeGravityMagnet(gravityMagnet);
				magnetOwners.remove(player.getEntityId());
			}
			magnetOwners.put(player.getEntityId(), mechanic.addGravityMagnet(new GravityMagnet(lookAt, Physics.getInstance().getSettings().getIntegerSetting("Tools.AttractRadius").getIntValue(), Physics.getInstance().getSettings().getIntegerSetting("Tools.AttractForce").getIntValue())));
		}
		return true;
	}

	@Override
	public void stoppedUsing(PhysicsWorld world, EntityPlayerMP player) {
		GravityModifierMechanic mechanic;
		if ((mechanic = (GravityModifierMechanic) world.getMechanics().get("GravityMagnet")) != null) {
			GravityMagnet magnet = magnetOwners.get(player.getEntityId());
			if (magnet != null) {
				mechanic.removeGravityMagnet(magnet);
				magnetOwners.remove(player.getEntityId());
			}
		}
	}
}
