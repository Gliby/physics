package net.gliby.physics.common.physics.swig;

import com.badlogic.gdx.math.Quaternion;

import net.gliby.physics.common.physics.transform.IQuaternion;

public class BulletQuaternion implements IQuaternion {

	private Quaternion quaternion;

	public BulletQuaternion(Quaternion quaternion) {
		this.quaternion = quaternion;
	}
	
	public IQuaternion set(Quaternion quaternion) {
		this.quaternion.x = quaternion.x;
		this.quaternion.y = quaternion.y;
		this.quaternion.z = quaternion.z;
		this.quaternion.w = quaternion.w;
		return this;
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
	
}
