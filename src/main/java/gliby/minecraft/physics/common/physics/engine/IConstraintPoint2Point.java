package gliby.minecraft.physics.common.physics.engine;

import gliby.minecraft.physics.common.physics.PhysicsWorld;

import javax.vecmath.Vector3f;

/**
 *
 */
public interface IConstraintPoint2Point extends IConstraint {

    PhysicsWorld getPhysicsWorld();

    void setImpulseClamp(float f);

    void setTau(float f);

    void setPivotB(Vector3f newPos);

    Vector3f getPivotInA();

    Vector3f getPivotInB();
}
