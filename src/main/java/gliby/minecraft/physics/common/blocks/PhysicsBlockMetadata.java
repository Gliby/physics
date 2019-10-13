package gliby.minecraft.physics.common.blocks;

import gliby.minecraft.physics.common.entity.actions.RigidBodyAction;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class PhysicsBlockMetadata {

    /**
     *
     */
    // @SerializedName("shouldSpawnInExplosion")
    public boolean spawnInExplosions = true;

    // @SerializedName("mass")
    public float mass;

    /**
     * value 0 would be friction-less
     */
    // @SerializedName("friction")
    public float friction;

    /**
     * Forces regular cube collision shape.
     */
    // @SerializedName("defaultCollisionShape")
    public boolean defaultCollisionShape;

    /**
     * Bounciness.
     */
    // @SerializedName("restitution")
    public float restitution;

    /**
     * RigidBodyActions!
     */
    // @SerializedName("actions")
    public List<RigidBodyAction> actions = new ArrayList<RigidBodyAction>();

    /**
     * Disables collision with player, used for stuff like web, and flowers.
     */
    // @SerializedName("collisionEnabled")
    public boolean collisionEnabled = true;

}