package gliby.minecraft.physics.common.physics.engine.javabullet;

import com.bulletphysicsx.dynamics.constraintsolver.Point2PointConstraint;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IConstraintPoint2Point;

import javax.vecmath.Vector3f;
import java.lang.ref.WeakReference;

/**
 *
 */
public class JavaConstraintPoint2Point implements IConstraintPoint2Point {

    protected WeakReference<PhysicsWorld> physicsWorld;
    private WeakReference<Point2PointConstraint> constraint;

    JavaConstraintPoint2Point(PhysicsWorld physicsWorld, Point2PointConstraint constraint) {
        this.physicsWorld = new WeakReference<PhysicsWorld>(physicsWorld);
        this.constraint = new WeakReference<Point2PointConstraint>(constraint);
    }

    @Override
    public void setImpulseClamp(final float f) {
        constraint.get().setting.impulseClamp = f;
    }

    @Override
    public void setTau(final float f) {
        constraint.get().setting.tau = f;
    }

    @Override
    public void setPivotB(final Vector3f newPos) {
        constraint.get().setPivotB(newPos);
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
        return constraint.get().getPivotInA(new Vector3f());
    }

    @Override
    public Vector3f getPivotInB() {
        return constraint.get().getPivotInB(new Vector3f());
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
