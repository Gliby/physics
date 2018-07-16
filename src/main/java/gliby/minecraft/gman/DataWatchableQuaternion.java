package gliby.minecraft.gman;

import com.badlogic.gdx.math.Quaternion;

import net.minecraft.entity.Entity;
import net.minecraft.util.Rotations;

/**
 *
 */
public class DataWatchableQuaternion extends DataWatchableObject {

	public Quaternion lastWrote;
	private int indexRotation, indexW;
	private Rotations rotation;

	/**
	 * @param entity
	 * @param quat
	 */
	public DataWatchableQuaternion(Entity entity, Quaternion quat) {
		super(entity);
		indexRotation = ++dataWatcherIndex;
		indexW = ++dataWatcherIndex;
		entity.getDataWatcher().addObject(indexRotation, rotation = new Rotations(quat.x, quat.y, quat.z));
		entity.getDataWatcher().addObject(indexW, quat.w);
		lastWrote = new Quaternion();
	}

	@Override
	public void write(Object... obj) {
		Quaternion quat4 = (Quaternion) obj[0];
		lastWrote.set(quat4);
		rotation = new Rotations(quat4.x, quat4.y, quat4.z);
		entity.getDataWatcher().updateObject(indexRotation, rotation);
		entity.getDataWatcher().updateObject(indexW, quat4.w);
	}

	@Override
	public void read(Object... obj) {
		Quaternion quat = (Quaternion) obj[0];
		rotation = entity.getDataWatcher().getWatchableObjectRotations(indexRotation);
		quat.set(rotation.getX(), rotation.getY(), rotation.getZ(),
				entity.getDataWatcher().getWatchableObjectFloat(indexW));
	}

}
