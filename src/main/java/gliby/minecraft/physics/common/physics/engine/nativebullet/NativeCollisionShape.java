/**
 * Copyright (c) 2015, Mine Fortress.
 */
package gliby.minecraft.physics.common.physics.engine.nativebullet;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector3f;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btCompoundShape;
import com.bulletphysicsx.linearmath.Transform;

import gliby.minecraft.physics.common.physics.engine.ICollisionShape;
import gliby.minecraft.physics.common.physics.engine.ICollisionShapeChildren;

/**
 *
 */
class NativeCollisionShape implements ICollisionShape {
	private static final int BOX_SHAPE = 0;

	private btCollisionShape shape;

	NativeCollisionShape(btCollisionShape shape) {
		this.shape = shape;
	}

	@Override
	public Object getCollisionShape() {
		return shape;
	}

	@Override
	public int getShapeType() {
		return shape.getShapeType();
	}

	@Override
	public boolean isBoxShape() {
		return shape.getShapeType() == BOX_SHAPE;
	}

	@Override
	public boolean isCompoundShape() {
		return shape.isCompound();
	}

	@Override
	public void calculateLocalInertia(float mass, Object localInertia) {
		shape.calculateLocalInertia(mass, (Vector3) localInertia);
	}

	@Override
	public void getHalfExtentsWithMargin(Vector3f halfExtent) {
		halfExtent.set(NativePhysicsWorld.toVector3f(((btBoxShape) shape).getHalfExtentsWithMargin()));
	}

	@Override
	public List<ICollisionShapeChildren> getChildren() {
		ArrayList<ICollisionShapeChildren> shapeList = new ArrayList<ICollisionShapeChildren>();
		final btCompoundShape compoundShape = (btCompoundShape) shape;
		for (int i = 0; i < compoundShape.getNumChildShapes(); i++) {
			final int index = i;
			final Transform transform = new Transform();
			transform.setIdentity();
			transform.set(NativePhysicsWorld.toMatrix4f(compoundShape.getChildTransform(index)));
			shapeList.add(new ICollisionShapeChildren() {
				@Override
				public Transform getTransform() {
					return transform;
				}

				@Override
				public ICollisionShape getCollisionShape() {
					return new NativeCollisionShape(compoundShape.getChildShape(index));
				}

			});
		}
		return shapeList;
	}

	@Override
	public void setLocalScaling(Vector3f localScaling) {
		this.shape.setLocalScaling(NativePhysicsWorld.toVector3(localScaling));
	}

}
