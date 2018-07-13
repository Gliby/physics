package gliby.minecraft.physics.common.physics.engine.javabullet;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector3f;

import com.bulletphysicsx.collision.broadphase.BroadphaseNativeType;
import com.bulletphysicsx.collision.shapes.BoxShape;
import com.bulletphysicsx.collision.shapes.CollisionShape;
import com.bulletphysicsx.collision.shapes.CompoundShape;
import com.bulletphysicsx.collision.shapes.CompoundShapeChild;
import com.bulletphysicsx.linearmath.Transform;

import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.ICollisionShape;
import gliby.minecraft.physics.common.physics.engine.ICollisionShapeChildren;

/**
 *
 */
public class JavaCollisionShape implements ICollisionShape {

	private CollisionShape shape;

	protected PhysicsWorld physicsWorld;

	JavaCollisionShape(PhysicsWorld physicsWorld, CollisionShape shape) {
		this.physicsWorld = physicsWorld;
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
	public void calculateLocalInertia(final float mass, final Object localInertia) {
		/*
		 * this.getPhysicsWorld().physicsTasks.add(new Runnable() {
		 * 
		 * @Override public void run() {
		 */
		synchronized (physicsWorld) {
			shape.calculateLocalInertia(mass, (Vector3f) localInertia);
		}
		/*
		 * } });
		 */
	}

	@Override
	public void getHalfExtentsWithMargin(Vector3f halfExtent) {
		((BoxShape) shape).getHalfExtentsWithMargin(halfExtent);
	}

	@Override
	public List<ICollisionShapeChildren> getChildren() {
		synchronized (physicsWorld) {
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
						return new JavaCollisionShape(physicsWorld, child.childShape);
					}

				});
			}
			return shapeList;
		}
	}

	@Override
	public void setLocalScaling(final Vector3f localScaling) {
		this.getPhysicsWorld().physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				shape.setLocalScaling(localScaling);
			}
		});
	}

	@Override
	public PhysicsWorld getPhysicsWorld() {
		return physicsWorld;
	}

}
