package gliby.minecraft.physics.common.physics.engine.nativebullet;

import com.badlogic.gdx.physics.bullet.dynamics.btPoint2PointConstraint;
import gliby.minecraft.physics.client.render.VecUtility;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IConstraintPoint2Point;

import javax.vecmath.Vector3f;
import java.lang.ref.SoftReference;

/**
 *
 */
class NativePoint2PointConstraint implements IConstraintPoint2Point {

    protected SoftReference<PhysicsWorld> physicsWorld;
    private SoftReference<btPoint2PointConstraint> constraint;

    NativePoint2PointConstraint(PhysicsWorld physicsWorld, btPoint2PointConstraint constraint) {
        this.physicsWorld = new SoftReference<PhysicsWorld>(physicsWorld);
        this.constraint = new SoftReference<btPoint2PointConstraint>(constraint);
    }

    @Override
    public void setImpulseClamp(final float f) {
        constraint.get().getSetting().setImpulseClamp(f);
    }

    @Override
    public void setTau(final float f) {
        constraint.get().getSetting().setTau(f);
    }

    @Override
    public void setPivotB(final Vector3f newPos) {
        constraint.get().setPivotB(VecUtility.toVector3fTera(newPos));
    }

    @Override
    public Object getConstraint() {
        return constraint.get();
    }

    @Override
    public boolean isPoint2Point() {
        return true;
    }

    @Override
    public Vector3f getPivotInA() {
        return VecUtility.toVector3f(constraint.get().getPivotInA());
    }

    @Override
    public Vector3f getPivotInB() {
        return VecUtility.toVector3f(constraint.get().getPivotInB());
    }

    @Override
    public boolean isGeneric6Dof() {
        return false;
    }

    @Override
    public boolean isSlider() {
        return false;
    }

    @Override
    public PhysicsWorld getPhysicsWorld() {
        return physicsWorld.get();
    }
}
