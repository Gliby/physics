package net.gliby.minecraft.physics.common.physics.engine.nativebullet;

import com.badlogic.gdx.math.Vector3;

import net.gliby.minecraft.physics.common.physics.engine.IVector3;

public class NativeVector3 implements IVector3 {

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

	public IVector3 set(Vector3 vec3) {
		this.vector3.x = vec3.x;
		this.vector3.y = vec3.y;
		this.vector3.z = vec3.z;
		return this;
	}

	Vector3 vector3;

	public NativeVector3(Vector3 vector3) {
		this.vector3 = vector3;
	}
}
