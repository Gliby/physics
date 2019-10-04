package gliby.minecraft.physics.common.physics.engine.nativebullet;

import com.badlogic.gdx.physics.bullet.dynamics.btGeneric6DofConstraint;
import com.bulletphysicsx.linearmath.Transform;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.client.render.VecUtility;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IConstraintGeneric6Dof;

import java.lang.ref.WeakReference;

/**
 *
 */
class NativeConstraintGeneric6Dof implements IConstraintGeneric6Dof {

    protected WeakReference<PhysicsWorld> physicsWorld;
    private WeakReference<btGeneric6DofConstraint> constraint;

    NativeConstraintGeneric6Dof(PhysicsWorld physicsWorld, btGeneric6DofConstraint constraint) {
        this.constraint = new WeakReference<btGeneric6DofConstraint>(constraint);
        this.physicsWorld = new WeakReference<PhysicsWorld>(physicsWorld);
    }

    @Override
    public Object getConstraint() {
        return constraint;
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
        transform.set(VecUtility.toMatrix4f(constraint.get().getCalculatedTransformA()));
        return transform;

    }

    @Override
    public Transform getGlobalFrameOffsetB(Transform transform) {
        transform.set(VecUtility.toMatrix4f(constraint.get().getCalculatedTransformB()));
        return transform;
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
