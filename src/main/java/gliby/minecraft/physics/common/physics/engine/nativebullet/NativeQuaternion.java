package gliby.minecraft.physics.common.physics.engine.nativebullet;

import com.badlogic.gdx.math.Quaternion;

import gliby.minecraft.physics.common.physics.engine.IQuaternion;

public class NativeQuaternion implements IQuaternion {

	private Quaternion quaternion;

	public NativeQuaternion(Quaternion quaternion) {
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
