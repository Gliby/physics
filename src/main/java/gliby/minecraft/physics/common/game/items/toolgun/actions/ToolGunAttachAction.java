package gliby.minecraft.physics.common.game.items.toolgun.actions;

import com.bulletphysicsx.linearmath.Transform;
import gliby.minecraft.gman.EntityUtility;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IConstraintGeneric6Dof;
import gliby.minecraft.physics.common.physics.engine.IRayResult;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import net.minecraft.entity.player.EntityPlayerMP;

import javax.vecmath.Vector3f;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ToolGunAttachAction implements IToolGunAction {

    private Map<Integer, ToolGunHit> hits = new HashMap<Integer, ToolGunHit>();

    @Override
    public boolean use(PhysicsWorld physicsWorld, EntityPlayerMP player, Vector3f blockLookAt) {
        Vector3f offset = new Vector3f(0.5f, 0.5f, 0.5f);
        Vector3f eyePos = EntityUtility.getPositionEyes(player);
        Vector3f eyeLook = EntityUtility.toVector3f(player.getLook(1));
        Vector3f lookAt = new Vector3f(eyePos);
        eyeLook.scale(64);
        lookAt.add(eyeLook);
        eyePos.sub(offset);
        lookAt.sub(offset);

        IRayResult ray = physicsWorld.createClosestRayResultCallback(eyePos, lookAt);
        physicsWorld.rayTest(eyePos, lookAt, ray);
        if (ray.getCollisionObject() != null && ray.hasHit()) {
            IRigidBody body = physicsWorld.upCastRigidBody(ray.getCollisionObject());
            if (body != null) {
                Transform centerOfMassTransform = body.getCenterOfMassTransform();
                centerOfMassTransform.inverse();
                Vector3f relativePivot = new Vector3f(ray.getHitPointWorld());
                centerOfMassTransform.transform(relativePivot);


                ToolGunHit hit;
                if ((hit = hits.get(player.getEntityId())) != null) {
                    Transform transformA = new Transform();
                    transformA.setIdentity();
                    transformA.origin.set(relativePivot);
                    Transform transformB = new Transform();
                    transformB.setIdentity();
                    transformB.origin.set(hit.getLastHitNormal());

                    body.activate();
                    hit.getLastBody().activate();
                    IConstraintGeneric6Dof generic6Dof = physicsWorld.createGeneric6DofConstraint(body,
                            hit.getLastBody(), transformA, transformB, true);
                    physicsWorld.addConstraint(generic6Dof);
                    hits.remove(player.getEntityId());
                } else
                    hits.put(player.getEntityId(), new ToolGunHit(relativePivot, body));
            }
        }
        physicsWorld.clearRayTest(ray);
        return true;
    }

    @Override
    public void stoppedUsing(PhysicsWorld world, EntityPlayerMP player) {
        hits.remove(player.getEntityId());
    }

    @Override
    public String getName() {
        return "Attach";
    }
}
