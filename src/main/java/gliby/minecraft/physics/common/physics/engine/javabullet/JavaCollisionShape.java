package gliby.minecraft.physics.common.physics.engine.javabullet;

import com.bulletphysicsx.collision.broadphase.BroadphaseNativeType;
import com.bulletphysicsx.collision.shapes.BoxShape;
import com.bulletphysicsx.collision.shapes.CollisionShape;
import com.bulletphysicsx.collision.shapes.CompoundShape;
import com.bulletphysicsx.collision.shapes.CompoundShapeChild;
import com.bulletphysicsx.linearmath.Transform;
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
public class JavaCollisionShape implements ICollisionShape {

    protected SoftReference<PhysicsWorld> physicsWorld;
    private SoftReference<CollisionShape> shape;
    protected float volume;

    JavaCollisionShape(PhysicsWorld physicsWorld, CollisionShape shape, float volume) {
        this.physicsWorld = new SoftReference<PhysicsWorld>(physicsWorld);
        this.shape = new SoftReference<CollisionShape>(shape);
        this.volume = volume;
    }

    @Override
    public Object getCollisionShape() {
        return shape.get();
    }

    @Override
    public int getShapeType() {
        return shape.get().getShapeType().ordinal();
    }

    @Override
    public boolean isBoxShape() {
        return shape.get().getShapeType() == BroadphaseNativeType.BOX_SHAPE_PROXYTYPE;
    }

    @Override
    public boolean isCompoundShape() {
        return shape.get().isCompound();
    }

    @Override
    public void calculateLocalInertia(final float mass, final Object localInertia) {
        shape.get().calculateLocalInertia(mass, (Vector3f) localInertia);
    }

    @Override
    public void getHalfExtentsWithMargin(Vector3f halfExtent) {
        ((BoxShape) shape.get()).getHalfExtentsWithMargin(halfExtent);
    }

    @Override
    public List<ICollisionShapeChildren> getChildren() {
        ArrayList<ICollisionShapeChildren> shapeList = new ArrayList<ICollisionShapeChildren>();
        final CompoundShape compoundShape = (CompoundShape) shape.get();
        for (int i = 0; i < compoundShape.getChildList().size(); i++) {
            final CompoundShapeChild child = compoundShape.getChildList().get(i);
            shapeList.add(new ICollisionShapeChildren() {
                @Override
                public Transform getTransform() {
                    return child.transform;
                }

                @Override
                public ICollisionShape getCollisionShape() {
                    return new JavaCollisionShape(physicsWorld.get(), child.childShape, 0);
                }

            });
        }
        return shapeList;

    }

    @Override
    public void dispose() {
    }

    @Override
    public void setLocalScaling(final Vector3f localScaling) {
        shape.get().setLocalScaling(localScaling);
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
