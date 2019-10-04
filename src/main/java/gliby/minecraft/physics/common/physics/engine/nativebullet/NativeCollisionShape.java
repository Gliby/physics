package gliby.minecraft.physics.common.physics.engine.nativebullet;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btCompoundShape;
import com.bulletphysicsx.linearmath.Transform;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.client.render.VecUtility;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.ICollisionShape;
import gliby.minecraft.physics.common.physics.engine.ICollisionShapeChildren;

import javax.vecmath.Vector3f;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
class NativeCollisionShape implements ICollisionShape {
    private static final int BOX_SHAPE = 0;

    protected SoftReference<PhysicsWorld> physicsWorld;
    private btCollisionShape shape;

    NativeCollisionShape(PhysicsWorld physicsWorld, btCollisionShape shape) {
        this.physicsWorld = new SoftReference<PhysicsWorld>(physicsWorld);
        this.shape = (shape);
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
    public void calculateLocalInertia(final float mass, final Object localInertia) {
        /*
         * getPhysicsWorld().physicsTasks.add(new Runnable() {
         *
         * @Override public void run() {
         */
        shape.calculateLocalInertia(mass, (Vector3) localInertia);

        /*
         * System.out.println("calculated inertia"); } });
         */
    }

    @Override
    public void getHalfExtentsWithMargin(Vector3f halfExtent) {
        halfExtent.set(VecUtility.toVector3f(((btBoxShape) shape).getHalfExtentsWithMargin()));
    }

    @Override
    public List<ICollisionShapeChildren> getChildren() {
        ArrayList<ICollisionShapeChildren> shapeList = new ArrayList<ICollisionShapeChildren>();
        final btCompoundShape compoundShape = (btCompoundShape) shape;
        for (int i = 0; i < compoundShape.getNumChildShapes(); i++) {
            final int index = i;
            final Transform transform = new Transform();
            transform.setIdentity();
            transform.set(VecUtility.toMatrix4f(compoundShape.getChildTransform(index)));
            shapeList.add(new ICollisionShapeChildren() {
                @Override
                public Transform getTransform() {
                    return transform;
                }

                @Override
                public ICollisionShape getCollisionShape() {
                    return new NativeCollisionShape(getPhysicsWorld(), compoundShape.getChildShape(index));
                }

            });
        }
        return shapeList;

    }

    @Override
    public void dispose() {
        if (shape != null && !shape.isDisposed()) shape.dispose();
    }

    @Override
    public void setLocalScaling(final Vector3f localScaling) {
        shape.setLocalScaling(VecUtility.toVector3(localScaling));
    }

    @Override
    public PhysicsWorld getPhysicsWorld() {
        return physicsWorld.get();
    }

}
