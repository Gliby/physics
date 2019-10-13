package gliby.minecraft.physics.common.physics.engine.nativebullet;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btCompoundShape;
import com.bulletphysicsx.linearmath.Transform;
import gliby.minecraft.physics.client.render.VecUtility;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.ICollisionShape;
import gliby.minecraft.physics.common.physics.engine.ICollisionShapeChildren;

import javax.vecmath.Vector3f;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
class NativeCollisionShape implements ICollisionShape {
    private static final int BOX_SHAPE = 0;


    protected float volume;
    protected SoftReference<PhysicsWorld> physicsWorld;
    private SoftReference<btCollisionShape> shape;

    NativeCollisionShape(PhysicsWorld physicsWorld, btCollisionShape shape, float volume) {
        this.physicsWorld = new SoftReference<PhysicsWorld>(physicsWorld);
        this.shape = new SoftReference<btCollisionShape>(shape);
        this.volume = volume;
    }

    @Override
    public Object getCollisionShape() {
        return shape.get();
    }

    @Override
    public int getShapeType() {
        return shape.get().getShapeType();
    }

    @Override
    public boolean isBoxShape() {
        return shape.get().getShapeType() == BOX_SHAPE;
    }

    @Override
    public boolean isCompoundShape() {
        return shape.get().isCompound();
    }

    @Override
    public void calculateLocalInertia(final float mass, final Object localInertia) {
        /*
         * getPhysicsWorld().physicsTasks.add(new Runnable() {
         *
         * @Override public void run() {
         */
        shape.get().calculateLocalInertia(mass, (Vector3) localInertia);

        /*
         * System.out.println("calculated inertia"); } });
         */
    }

    @Override
    public void getHalfExtentsWithMargin(Vector3f halfExtent) {
        halfExtent.set(VecUtility.toVector3f(((btBoxShape) shape.get()).getHalfExtentsWithMargin()));
    }

    @Override
    public List<ICollisionShapeChildren> getChildren() {
        ArrayList<ICollisionShapeChildren> shapeList = new ArrayList<ICollisionShapeChildren>();
        final btCompoundShape compoundShape = (btCompoundShape) shape.get();
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
                    return new NativeCollisionShape(physicsWorld.get(), compoundShape.getChildShape(index), 0);
                }

            });
        }
        return shapeList;

    }

    @Override
    public void dispose() {
        if (shape.get() != null && !shape.get().isDisposed()) shape.get().dispose();
    }

    @Override
    public void setLocalScaling(final Vector3f localScaling) {
        shape.get().setLocalScaling(VecUtility.toVector3(localScaling));
    }

    @Override
    public float getVolume() {
        return volume;
    }

    @Override
    public PhysicsWorld getPhysicsWorld() {
        return physicsWorld.get();
    }

}
