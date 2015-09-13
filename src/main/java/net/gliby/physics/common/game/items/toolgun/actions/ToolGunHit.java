/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.game.items.toolgun.actions;

import javax.vecmath.Vector3f;

import net.gliby.physics.common.physics.engine.IRigidBody;

/**
 *
 */
public class ToolGunHit {
	/**
	 * @param lastHitNormal
	 * @param lastBody
	 */
	public ToolGunHit(Vector3f lastHitNormal, IRigidBody lastBody) {
		this.lastHitNormal = lastHitNormal;
		this.lastBody = lastBody;
	}

	private Vector3f lastHitNormal;

	/**
	 * @return the lastHitNormal
	 */
	public Vector3f getLastHitNormal() {
		return lastHitNormal;
	}

	/**
	 * @param lastHitNormal
	 *            the lastHitNormal to set
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
	 * @param lastBody
	 *            the lastBody to set
	 */
	public void setLastBody(IRigidBody lastBody) {
		this.lastBody = lastBody;
	}

	private IRigidBody lastBody;
}