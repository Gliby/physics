package net.gliby.physics.common.physics.jbullet;

import javax.vecmath.Vector3f;

import net.gliby.physics.common.physics.IVector3;

public class JBulletVector3 implements IVector3 {

	private Vector3f vector3;

	public JBulletVector3(Vector3f vector3f) {
		this.vector3 = vector3f;
	}

	@Override
	public float getX() {
		return vector3.x;
	}

	@Override
	public float getY() {
		return vector3.y;
	}

	@Override
	public float getZ() {
		return vector3.z;
	}

	public JBulletVector3 set(Vector3f vec3) {
		this.vector3.x = vec3.x;
		this.vector3.y = vec3.y;
		this.vector3.z = vec3.z;
		return this;
	}
}
