/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.physics.jbullet;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector3f;

import net.gliby.physics.common.physics.ICollisionShape;
import net.gliby.physics.common.physics.ICollisionShapeChildren;

import com.bulletphysics.collision.broadphase.BroadphaseNativeType;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.CompoundShapeChild;
import com.bulletphysics.linearmath.Transform;

/**
 *
 */
public class JavaCollisionShape implements ICollisionShape {

	private CollisionShape shape;

	JavaCollisionShape(CollisionShape shape) {
		this.shape = shape;
	}

	@Override
	public Object getCollisionShape() {
		return shape;
	}

	@Override
	public int getShapeType() {
		return shape.getShapeType().ordinal();
	}

	@Override
	public boolean isBoxShape() {
		return shape.getShapeType() == BroadphaseNativeType.BOX_SHAPE_PROXYTYPE;
	}

	@Override
	public boolean isCompoundShape() {
		return shape.isCompound();
	}

	@Override
	public void calculateLocalInertia(float mass, Object localInertia) {
		shape.calculateLocalInertia(mass, (Vector3f) localInertia);
	}

	@Override
	public void getHalfExtentsWithMargin(Vector3f halfExtent) {
		((BoxShape) shape).getHalfExtentsWithMargin(halfExtent);
	}

	@Override
	public List<ICollisionShapeChildren> getChildren() {
		ArrayList<ICollisionShapeChildren> shapeList = new ArrayList<ICollisionShapeChildren>();
		final CompoundShape compoundShape = (CompoundShape) shape;
		for (int i = 0; i < compoundShape.getChildList().size(); i++) {
			final CompoundShapeChild child = compoundShape.getChildList().get(i);
			shapeList.add(new ICollisionShapeChildren() {
				@Override
				public Transform getTransform() {
					return child.transform;
				}

				@Override
				public ICollisionShape getCollisionShape() {
					return new JavaCollisionShape(child.childShape);
				}
			});
		}
		return shapeList;
	}
}
