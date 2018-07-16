package gliby.minecraft.physics.common.game.items.toolgun.actions;


import com.badlogic.gdx.math.Vector3;

import gliby.minecraft.physics.common.physics.engine.IRigidBody;

/**
 *
 */
public class ToolGunHit {
	/**
	 * @param lastHitNormal
	 * @param lastBody
	 */
	public ToolGunHit(Vector3 lastHitNormal, IRigidBody lastBody) {
		this.lastHitNormal = lastHitNormal;
		this.lastBody = lastBody;
	}

	private Vector3 lastHitNormal;

	/**
	 * @return the lastHitNormal
	 */
	public Vector3 getLastHitNormal() {
		return lastHitNormal;
	}

	/**
	 * @param lastHitNormal
	 *            the lastHitNormal to set
	 */
	public void setLastHitNormal(Vector3 lastHitNormal) {
		this.lastHitNormal = lastHitNormal;
	}

	/**
	 * @return the lastBody
	 */
	public IRigidBody getLastBody() {
		return lastBody;
	}

	/**
	 * @param lastBody
	 *            the lastBody to set
	 */
	public void setLastBody(IRigidBody lastBody) {
		this.lastBody = lastBody;
	}

	private IRigidBody lastBody;
}