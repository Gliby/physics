package net.gliby.physics.common.physics.jbullet;

import javax.vecmath.Quat4f;

import net.gliby.physics.common.physics.transform.IQuaternion;

public class JavaQuaternion implements IQuaternion {

	private Quat4f quaternion;

	public JavaQuaternion(Quat4f quaternion) {
		this.quaternion = quaternion;
	}

	@Override
	public float getX() {
		return quaternion.x;
	}

	@Override
	public float getY() {
		return quaternion.y;
	}

	@Override
	public float getZ() {
		return quaternion.z;
	}

	@Override
	public float getW() {
		return quaternion.w;
	}

	public IQuaternion set(Quat4f quaternion) {
		this.quaternion.x = quaternion.x;
		this.quaternion.y = quaternion.y;
		this.quaternion.z = quaternion.z;
		this.quaternion.w = quaternion.w;
		return this;
	}

}
