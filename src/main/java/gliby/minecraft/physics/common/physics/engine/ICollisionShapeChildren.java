package gliby.minecraft.physics.common.physics.engine;

import com.badlogic.gdx.math.Matrix4;
import com.bulletphysicsx.linearmath.Transform;

/**
 *
 */
public interface ICollisionShapeChildren {

	public Matrix4 getTransform();

	public ICollisionShape getCollisionShape();
	
}
