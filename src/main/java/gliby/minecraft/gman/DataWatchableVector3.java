package gliby.minecraft.gman;

import com.badlogic.gdx.math.Vector3;

import net.minecraft.entity.Entity;
import net.minecraft.util.Rotations;

/**
 *
 */
public class DataWatchableVector3 extends DataWatchableObject {

	public Vector3 lastWrote;
	private int index;
	private Rotations rotation;

	/**
	 * @param entity
	 */
	public DataWatchableVector3(Entity entity, Vector3 vec3) {
		super(entity);
		index = ++dataWatcherIndex;
		entity.getDataWatcher().addObject(index, rotation = new Rotations(vec3.x, vec3.y, vec3.z));
		lastWrote = new Vector3();
	}

	@Override
	public void write(Object... obj) {
		Vector3 vec3 = (Vector3) obj[0];
		lastWrote.set(vec3);
		rotation = new Rotations(vec3.x, vec3.y, vec3.z);
		entity.getDataWatcher().updateObject(index, rotation);
	}

	@Override
	public void read(Object... obj) {
		Vector3 vec3 = (Vector3) obj[0];
		rotation = entity.getDataWatcher().getWatchableObjectRotations(index);
		vec3.set(rotation.getX(), rotation.getY(), rotation.getZ());
	}
}
