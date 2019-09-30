package gliby.minecraft.physics.common.game.items.toolgun.actions;

import gliby.minecraft.physics.common.physics.engine.IRigidBody;

import javax.vecmath.Vector3f;

/**
 *
 */
public class ToolGunHit {
    private Vector3f lastHitNormal;
    private IRigidBody lastBody;

    /**
     * @param lastHitNormal
     * @param lastBody
     */
    public ToolGunHit(Vector3f lastHitNormal, IRigidBody lastBody) {
        this.lastHitNormal = lastHitNormal;
        this.lastBody = lastBody;
    }

    /**
     * @return the lastHitNormal
     */
    public Vector3f getLastHitNormal() {
        return lastHitNormal;
    }

    /**
     * @param lastHitNormal the lastHitNormal to set
     */
    public void setLastHitNormal(Vector3f lastHitNormal) {
        this.lastHitNormal = lastHitNormal;
    }

    /**
     * @return the lastBody
     */
    public IRigidBody getLastBody() {
        return lastBody;
    }

    /**
     * @param lastBody the lastBody to set
     */
    public void setLastBody(IRigidBody lastBody) {
        this.lastBody = lastBody;
    }
}