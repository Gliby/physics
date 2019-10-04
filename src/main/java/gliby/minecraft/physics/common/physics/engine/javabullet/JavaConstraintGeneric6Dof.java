package gliby.minecraft.physics.common.physics.engine.javabullet;

import com.bulletphysicsx.dynamics.constraintsolver.Generic6DofConstraint;
import com.bulletphysicsx.linearmath.Transform;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IConstraintGeneric6Dof;

import java.lang.ref.WeakReference;

/**
 *
 */
public class JavaConstraintGeneric6Dof implements IConstraintGeneric6Dof {

    protected WeakReference<PhysicsWorld> physicsWorld;
    private WeakReference<Generic6DofConstraint> constraint;

    JavaConstraintGeneric6Dof(PhysicsWorld physicsWorld, Generic6DofConstraint constraint) {
        this.physicsWorld = new WeakReference<PhysicsWorld>(physicsWorld);
        this.constraint = new WeakReference<Generic6DofConstraint>(constraint);
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
