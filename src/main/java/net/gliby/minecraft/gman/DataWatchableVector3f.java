/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.minecraft.gman;

import javax.vecmath.Vector3f;

import net.minecraft.entity.Entity;
import net.minecraft.util.Rotations;

/**
 *
 */
public class DataWatchableVector3f extends DataWatchableObject {

	public Vector3f lastWrote;
	private int index;
	private Rotations rotation;

	/**
	 * @param entity
	 */
	public DataWatchableVector3f(Entity entity, Vector3f vec3) {
		super(entity);
		index = ++dataWatcherIndex;
		entity.getDataWatcher().addObject(index, rotation = new Rotations(vec3.x, vec3.y, vec3.z));
		lastWrote = new Vector3f();
	}

	@Override
	public void write(Object... obj) {
		Vector3f vec3 = (Vector3f) obj[0];
		lastWrote.set(vec3);
		rotation = new Rotations(vec3.x, vec3.y, vec3.z);
		entity.getDataWatcher().updateObject(index, rotation);
	}

	@Override
	public void read(Object... obj) {
		Vector3f vec3 = (Vector3f) obj[0];
		rotation = entity.getDataWatcher().getWatchableObjectRotations(index);
		vec3.set(rotation.getX(), rotation.getY(), rotation.getZ());
	}
}
