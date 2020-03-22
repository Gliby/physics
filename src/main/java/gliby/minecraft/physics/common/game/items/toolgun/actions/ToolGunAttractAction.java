package gliby.minecraft.physics.common.game.items.toolgun.actions;

import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.mechanics.gravitymagnets.GravityModifierMechanic;
import gliby.minecraft.physics.common.physics.mechanics.gravitymagnets.GravityModifierMechanic.GravityMagnet;
import net.minecraft.entity.player.EntityPlayerMP;

import javax.vecmath.Vector3f;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ToolGunAttractAction implements IToolGunAction {

    private Map<Integer, GravityMagnet> attractionHandlers = new HashMap<Integer, GravityMagnet>();

    @Override
    public String getName() {
        return "Attract";
    }

    @Override
    public boolean use(PhysicsWorld world, EntityPlayerMP player, Vector3f lookAt) {
        GravityModifierMechanic mechanic;
        if ((mechanic = (GravityModifierMechanic) world.getMechanics().get("GravityMagnet")) != null) {
            GravityMagnet gravityMagnet = attractionHandlers.get(player.getEntityId());
            if (gravityMagnet != null) {
                mechanic.removeGravityMagnet(gravityMagnet);
                attractionHandlers.remove(player.getEntityId());
            }
            attractionHandlers.put(player.getEntityId(),
                    mechanic.addGravityMagnet(
                            new GravityMagnet(lookAt,
                                    Physics.getConfig().getTools().getAttractRadius(),
                                    Physics.getConfig().getTools().getAttractForce())));
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
