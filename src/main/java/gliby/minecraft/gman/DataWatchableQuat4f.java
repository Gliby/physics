package gliby.minecraft.gman;

import javax.vecmath.Quat4f;

import net.minecraft.entity.Entity;
import net.minecraft.util.Rotations;

/**
 *
 */
public class DataWatchableQuat4f extends DataWatchableObject {

	public Quat4f lastWrote;
	private int indexRotation, indexW;
	private Rotations rotation;

	/**
	 * @param entity
	 * @param quat4
	 */
	public DataWatchableQuat4f(Entity entity, Quat4f quat4) {
		super(entity);
		indexRotation = ++dataWatcherIndex;
		indexW = ++dataWatcherIndex;
		entity.getDataWatcher().addObject(indexRotation, rotation = new Rotations(quat4.x, quat4.y, quat4.z));
		entity.getDataWatcher().addObject(indexW, quat4.w);
		lastWrote = new Quat4f();
	}

	@Override
	public void write(Object... obj) {
		Quat4f quat4 = (Quat4f) obj[0];
		lastWrote.set(quat4);
		rotation = new Rotations(quat4.x, quat4.y, quat4.z);
		entity.getDataWatcher().updateObject(indexRotation, rotation);
		entity.getDataWatcher().updateObject(indexW, quat4.w);
	}

	@Override
	public void read(Object... obj) {
		Quat4f quat = (Quat4f) obj[0];
		rotation = entity.getDataWatcher().getWatchableObjectRotations(indexRotation);
		quat.set(rotation.getX(), rotation.getY(), rotation.getZ(),
				entity.getDataWatcher().getWatchableObjectFloat(indexW));
	}

}
