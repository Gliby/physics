package gliby.minecraft.physics.common.physics.mechanics.physicsgun;

import gliby.minecraft.physics.common.physics.engine.IRayResult;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import net.minecraft.entity.Entity;

import javax.vecmath.Vector3f;

/**
 *
 */
public class OwnedPickedObject extends PickedObject {

    private final Entity owner;

    /**
     * @param rayCallback
     * @param rayFromWorld
     * @param rayToWorld
     */
    public OwnedPickedObject(IRigidBody body, Entity owner, IRayResult rayCallback, Vector3f rayFromWorld, Vector3f rayToWorld) {
        super(body, rayCallback, rayFromWorld, rayToWorld);
        this.owner = owner;
    }

    /**
     * @return the owner
     */
    public Entity getOwner() {
        return owner;
    }

}
