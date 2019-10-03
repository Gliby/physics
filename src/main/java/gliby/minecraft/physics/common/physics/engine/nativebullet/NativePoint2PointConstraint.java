package gliby.minecraft.physics.common.physics.engine.nativebullet;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btPoint2PointConstraint;
import gliby.minecraft.physics.client.render.ConversionUtility;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IConstraintPoint2Point;

import javax.vecmath.Vector3f;

/**
 *
 */
class NativePoint2PointConstraint implements IConstraintPoint2Point {

    protected PhysicsWorld physicsWorld;
    private btPoint2PointConstraint constraint;

    NativePoint2PointConstraint(PhysicsWorld physicsWorld, btPoint2PointConstraint constraint) {
        this.physicsWorld = physicsWorld;
        this.constraint = constraint;
    }

    @Override
    public void setImpulseClamp(final float f) {

        constraint.getSetting().setImpulseClamp(f);

    }

    @Override
    public void setTau(final float f) {

        constraint.getSetting().setTau(f);

    }

    @Override
    public void setPivotB(final Vector3f newPos) {
        constraint.setPivotB(ConversionUtility.toVector3(newPos));
    }

    @Override
    public Object getConstraint() {
        return constraint;
    }

    @Override
    public boolean isPoint2Point() {
        return true;
    }

    @Override
    public Vector3f getPivotInA() {
        return ConversionUtility.toVector3f(constraint.getPivotInA());
    }

    @Override
    public Vector3f getPivotInB() {
        return ConversionUtility.toVector3f(constraint.getPivotInB());
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
        return physicsWorld;
    }
}
