package gliby.minecraft.physics.common.physics.engine.javabullet;

import com.bulletphysicsx.dynamics.constraintsolver.Generic6DofConstraint;
import com.bulletphysicsx.linearmath.Transform;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IConstraintGeneric6Dof;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
 *
 */
public class JavaConstraintGeneric6Dof implements IConstraintGeneric6Dof {

    protected SoftReference<PhysicsWorld> physicsWorld;
    private SoftReference<Generic6DofConstraint> constraint;

    JavaConstraintGeneric6Dof(PhysicsWorld physicsWorld, Generic6DofConstraint constraint) {
        this.physicsWorld = new SoftReference<PhysicsWorld>(physicsWorld);
        this.constraint = new SoftReference<Generic6DofConstraint>(constraint);
    }

    @Override
    public Object getConstraint() {
        return constraint.get();
    }

    @Override
    public boolean isPoint2Point() {
        return false;
    }

    @Override
    public boolean isGeneric6Dof() {
        return true;
    }

    @Override
    public Transform getGlobalFrameOffsetA(Transform transform) {
        return constraint.get().getCalculatedTransformA(transform);
    }

    @Override
    public Transform getGlobalFrameOffsetB(Transform transform) {
        return constraint.get().getCalculatedTransformB(transform);
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
