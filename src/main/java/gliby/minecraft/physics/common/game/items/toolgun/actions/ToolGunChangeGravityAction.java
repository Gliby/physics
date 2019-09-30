package gliby.minecraft.physics.common.game.items.toolgun.actions;

import gliby.minecraft.gman.EntityUtility;
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
