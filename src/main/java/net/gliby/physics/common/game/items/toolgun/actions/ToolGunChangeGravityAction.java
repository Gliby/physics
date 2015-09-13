/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.game.items.toolgun.actions;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Vector3f;

import net.gliby.gman.EntityUtility;
import net.gliby.physics.Physics;
import net.gliby.physics.common.physics.PhysicsWorld;
import net.gliby.physics.common.physics.mechanics.gravitymagnets.GravityModifierMechanic;
import net.gliby.physics.common.physics.mechanics.gravitymagnets.GravityModifierMechanic.GravityMagnet;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 *
 */
public class ToolGunChangeGravityAction implements IToolGunAction {

	private Map<Integer, GravityMagnet> magnetOwners = new HashMap<Integer, GravityMagnet>();

	public boolean use(PhysicsWorld world, EntityPlayerMP player, Vector3f lookAt) {
		GravityModifierMechanic mechanic;
		if ((mechanic = (GravityModifierMechanic) world.getMechanics().get("GravityMagnet")) != null) {
			GravityMagnet gravityMagnet = magnetOwners.get(player.getEntityId());
			if (gravityMagnet != null) {
				mechanic.removeGravityMagnet(gravityMagnet);
				magnetOwners.remove(player.getEntityId());
			}
			Vector3f otherLookAt = new Vector3f(lookAt);
			Vector3f eyes = EntityUtility.getPositionEyes(player);
			otherLookAt.sub(eyes);
			otherLookAt.normalize();
			magnetOwners.put(player.getEntityId(),
					mechanic.addGravityMagnet(new GravityMagnet(lookAt, otherLookAt,
							Physics.getInstance().getSettings().getIntegerSetting("Tools.GravitizerRadius").getIntValue(),
							Physics.getInstance().getSettings().getIntegerSetting("Tools.GravitizerForce").getIntValue())));
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

	@Override
	public String getName() {
		return "Gravitizer";
	}

}
