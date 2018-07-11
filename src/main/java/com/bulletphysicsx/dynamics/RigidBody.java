/*
 * Java port of Bullet (c) 2008 Martin Dvorak <jezek2@advel.cz>
 *
 * Bullet Continuous Collision Detection and Physics Library
 * Copyright (c) 2003-2008 Erwin Coumans  http://www.bulletphysics.com/
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from
 * the use of this software.
 * 
 * Permission is granted to anyone to use this software for any purpose, 
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 
 * 1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

package com.bulletphysicsx.dynamics;

import javax.vecmath.Matrix3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.apache.commons.math3.util.FastMath;

import com.bulletphysicsx.BulletGlobals;
import com.bulletphysicsx.collision.broadphase.BroadphaseProxy;
import com.bulletphysicsx.collision.dispatch.CollisionFlags;
import com.bulletphysicsx.collision.dispatch.CollisionObject;
import com.bulletphysicsx.collision.dispatch.CollisionObjectType;
import com.bulletphysicsx.collision.shapes.CollisionShape;
import com.bulletphysicsx.dynamics.constraintsolver.TypedConstraint;
import com.bulletphysicsx.linearmath.MatrixUtil;
import com.bulletphysicsx.linearmath.MiscUtil;
import com.bulletphysicsx.linearmath.MotionState;
import com.bulletphysicsx.linearmath.Transform;
import com.bulletphysicsx.linearmath.TransformUtil;
import com.bulletphysicsx.util.ObjectArrayList;

/**
 * RigidBody is the main class for rigid body objects. It is derived from
 * {@link CollisionObject}, so it keeps reference to {@link CollisionShape}.<p>
 * <p/>
 * It is recommended for performance and memory use to share {@link CollisionShape}
 * objects whenever possible.<p>
 * <p/>
 * There are 3 types of rigid bodies:<br>
 * <ol>
 * <li>Dynamic rigid bodies, with positive mass. Motion is controlled by rigid body dynamics.</li>
 * <li>Fixed objects with zero mass. They are not moving (basically collision objects).</li>
 * <li>Kinematic objects, which are objects without mass, but the user can move them. There
 * is on-way interaction, and Bullet calculates a velocity based on the timestep and
 * previous and current world transform.</li>
 * </ol>
 * <p/>
 * Bullet automatically deactivates dynamic rigid bodies, when the velocity is below
 * a threshold for a given time.<p>
 * <p/>
 * Deactivated (sleeping) rigid bodies don't take any processing time, except a minor
 * broadphase collision detection impact (to allow active objects to activate/wake up
 * sleeping objects).
 *
 * @author jezek2
 */
public class RigidBody extends CollisionObject {

    private static final float MAX_ANGVEL = BulletGlobals.SIMD_HALF_PI;

    private final Matrix3f invInertiaTensorWorld = new Matrix3f();
    private final Vector3f linearVelocity = new Vector3f();
    private final Vector3f angularVelocity = new Vector3f();
    private final Vector3f linearFactor = new Vector3f();
    private final Vector3f angularFactor = new Vector3f();
    private float inverseMass;

    private final Vector3f gravity = new Vector3f();
    private final Vector3f invInertiaLocal = new Vector3f();
    private final Vector3f totalForce = new Vector3f();
    private final Vector3f totalTorque = new Vector3f();

    private float linearDamping;
    private float angularDamping;

    private boolean additionalDamping;
    private float additionalDampingFactor;
    private float additionalLinearDampingThresholdSqr;
    private float additionalAngularDampingThresholdSqr;
    private float additionalAngularDampingFactor;

    private float linearSleepingThreshold;
    private float angularSleepingThreshold;

    // optionalMotionState allows to automatic synchronize the world transform for active objects
    private MotionState optionalMotionState;

    // keep track of typed constraints referencing this rigid body
    private final ObjectArrayList<TypedConstraint> constraintRefs = new ObjectArrayList<TypedConstraint>();

    // for experimental overriding of friction/contact solver func
    public int contactSolverType;
    public int frictionSolverType;

    private static int uniqueId = 0;
    public int debugBodyId;

    public RigidBody(RigidBodyConstructionInfo constructionInfo) {
        setupRigidBody(constructionInfo);
    }

    public RigidBody(float mass, MotionState motionState, CollisionShape collisionShape) {
        this(mass, motionState, collisionShape, new Vector3f(0f, 0f, 0f));
    }

    public RigidBody(float mass, MotionState motionState, CollisionShape collisionShape, Vector3f localInertia) {
        RigidBodyConstructionInfo cinfo = new RigidBodyConstructionInfo(mass, motionState, collisionShape, localInertia);
        setupRigidBody(cinfo);
    }

    private void setupRigidBody(RigidBodyConstructionInfo constructionInfo) {
        internalType = CollisionObjectType.RIGID_BODY;

        linearVelocity.set(0f, 0f, 0f);
        angularVelocity.set(0f, 0f, 0f);
        angularFactor.set(1f,1f,1f);
        linearFactor.set(1f, 1f, 1f);
        gravity.set(0f, 0f, 0f);
        totalForce.set(0f, 0f, 0f);
        totalTorque.set(0f, 0f, 0f);
        linearDamping = 0f;
        angularDamping = 0.5f;
        linearSleepingThreshold = constructionInfo.linearSleepingThreshold;
        angularSleepingThreshold = constructionInfo.angularSleepingThreshold;
        optionalMotionState = constructionInfo.motionState;
        contactSolverType = 0;
        frictionSolverType = 0;
        additionalDamping = constructionInfo.additionalDamping;
        additionalDampingFactor = constructionInfo.additionalDampingFactor;
        additionalLinearDampingThresholdSqr = constructionInfo.additionalLinearDampingThresholdSqr;
        additionalAngularDampingThresholdSqr = constructionInfo.additionalAngularDampingThresholdSqr;
        additionalAngularDampingFactor = constructionInfo.additionalAngularDampingFactor;

        if (optionalMotionState != null) {
            optionalMotionState.getWorldTransform(worldTransform);
        } else {
            worldTransform.set(constructionInfo.startWorldTransform);
        }

        interpolationWorldTransform.set(worldTransform);
        interpolationLinearVelocity.set(0f, 0f, 0f);
        interpolationAngularVelocity.set(0f, 0f, 0f);

        // moved to CollisionObject
        friction = constructionInfo.friction;
        restitution = constructionInfo.restitution;

        setCollisionShape(constructionInfo.collisionShape);
        debugBodyId = uniqueId++;

        setMassProps(constructionInfo.mass, constructionInfo.localInertia);
        setDamping(constructionInfo.linearDamping, constructionInfo.angularDamping);
        updateInertiaTensor();
    }

    private Vector3f applyVelocityFactor(Vector3f value, Vector3f velocityFactor) {
        Vector3f valueWithLinearFactor = new Vector3f(value);
        valueWithLinearFactor.x *= velocityFactor.x;
        valueWithLinearFactor.y *= velocityFactor.y;
        valueWithLinearFactor.z *= velocityFactor.z;
        return valueWithLinearFactor;
    }

    public void destroy() {
        // No constraints should point to this rigidbody
        // Remove constraints from the dynamics world before you delete the related rigidbodies.
        assert (constraintRefs.size() == 0);
    }

    public void proceedToTransform(Transform newTrans) {
        setCenterOfMassTransform(newTrans);
    }

    /**
     * To keep collision detection and dynamics separate we don't store a rigidbody pointer,
     * but a rigidbody is derived from CollisionObject, so we can safely perform an upcast.
     */
    public static RigidBody upcast(CollisionObject colObj) {
        if (colObj.getInternalType() == CollisionObjectType.RIGID_BODY) {
            return (RigidBody) colObj;
        }
        return null;
    }

    /**
     * Continuous collision detection needs prediction.
     */
    public void predictIntegratedTransform(float timeStep, Transform predictedTransform) {
        TransformUtil.integrateTransform(worldTransform, linearVelocity, angularVelocity, timeStep, predictedTransform);
    }

    public void saveKinematicState(float timeStep) {
        //todo: clamp to some (user definable) safe minimum timestep, to limit maximum angular/linear velocities
        if (timeStep != 0f) {
            //if we use motionstate to synchronize world transforms, get the new kinematic/animated world transform
            if (getMotionState() != null) {
                getMotionState().getWorldTransform(worldTransform);
            }
            //Vector3f linVel = new Vector3f(), angVel = new Vector3f();

            TransformUtil.calculateVelocity(interpolationWorldTransform, worldTransform, timeStep, linearVelocity, angularVelocity);
            interpolationLinearVelocity.set(linearVelocity);
            interpolationAngularVelocity.set(angularVelocity);
            interpolationWorldTransform.set(worldTransform);
            //printf("angular = %f %f %f\n",m_angularVelocity.getX(),m_angularVelocity.getY(),m_angularVelocity.getZ());
        }
    }

    public void applyGravity() {
        if (isStaticOrKinematicObject())
            return;

        applyCentralForce(gravity);
    }

    public void setGravity(Vector3f acceleration) {
        if (inverseMass != 0f) {
            gravity.scale(1f / inverseMass, acceleration);
        }
    }

    public Vector3f getGravity(Vector3f out) {
        out.set(gravity);
        return out;
    }

    public void setDamping(float lin_damping, float ang_damping) {
        linearDamping = MiscUtil.GEN_clamped(lin_damping, 0f, 1f);
        angularDamping = MiscUtil.GEN_clamped(ang_damping, 0f, 1f);
    }

    public float getLinearDamping() {
        return linearDamping;
    }

    public float getAngularDamping() {
        return angularDamping;
    }

    public float getLinearSleepingThreshold() {
        return linearSleepingThreshold;
    }

    public float getAngularSleepingThreshold() {
        return angularSleepingThreshold;
    }

    public Vector3f getLinearFactor() {
        return linearFactor;
    }

    public Vector3f getAngularFactor() {
        return angularFactor;
    }

    /**
     * Damps the velocity, using the given linearDamping and angularDamping.
     */
    public void applyDamping(float timeStep) {
        // On new damping: see discussion/issue report here: http://code.google.com/p/bullet/issues/detail?id=74
        // todo: do some performance comparisons (but other parts of the engine are probably bottleneck anyway

        //#define USE_OLD_DAMPING_METHOD 1
        //#ifdef USE_OLD_DAMPING_METHOD
        //linearVelocity.scale(MiscUtil.GEN_clamped((1f - timeStep * linearDamping), 0f, 1f));
        //angularVelocity.scale(MiscUtil.GEN_clamped((1f - timeStep * angularDamping), 0f, 1f));
        //#else
        linearVelocity.scale((float) FastMath.pow(1f - linearDamping, timeStep));
        angularVelocity.scale((float) FastMath.pow(1f - angularDamping, timeStep));
        //#endif

        if (additionalDamping) {
            // Additional damping can help avoiding lowpass jitter motion, help stability for ragdolls etc.
            // Such damping is undesirable, so once the overall simulation quality of the rigid body dynamics system has improved, this should become obsolete
            if ((angularVelocity.lengthSquared() < additionalAngularDampingThresholdSqr) &&
                    (linearVelocity.lengthSquared() < additionalLinearDampingThresholdSqr)) {
                angularVelocity.scale(additionalDampingFactor);
                linearVelocity.scale(additionalDampingFactor);
            }

            float speed = linearVelocity.length();
            if (speed < linearDamping) {
                float dampVel = 0.005f;
                if (speed > dampVel) {
                    Vector3f dir = new Vector3f(linearVelocity);
                    dir.normalize();
                    dir.scale(dampVel);
                    linearVelocity.sub(dir);
                } else {
                    linearVelocity.set(0f, 0f, 0f);
                }
            }

            float angSpeed = angularVelocity.length();
            if (angSpeed < angularDamping) {
                float angDampVel = 0.005f;
                if (angSpeed > angDampVel) {
                    Vector3f dir = new Vector3f(angularVelocity);
                    dir.normalize();
                    dir.scale(angDampVel);
                    angularVelocity.sub(dir);
                } else {
                    angularVelocity.set(0f, 0f, 0f);
                }
            }
        }
    }

    public void setMassProps(float mass, Vector3f inertia) {
        if (mass == 0f) {
            collisionFlags |= CollisionFlags.STATIC_OBJECT;
            inverseMass = 0f;
        } else {
            collisionFlags &= (~CollisionFlags.STATIC_OBJECT);
            inverseMass = 1f / mass;
        }

        invInertiaLocal.set(inertia.x != 0f ? 1f / inertia.x : 0f,
                inertia.y != 0f ? 1f / inertia.y : 0f,
                inertia.z != 0f ? 1f / inertia.z : 0f);
    }

    public float getInvMass() {
        return inverseMass;
    }

    public Matrix3f getInvInertiaTensorWorld(Matrix3f out) {
        out.set(invInertiaTensorWorld);
        return out;
    }

    public void integrateVelocities(float step) {
        if (isStaticOrKinematicObject()) {
            return;
        }

        linearVelocity.scaleAdd(inverseMass * step, totalForce, linearVelocity);
        Vector3f tmp = new Vector3f(totalTorque);
        invInertiaTensorWorld.transform(tmp);
        angularVelocity.scaleAdd(step, tmp, angularVelocity);

        // clamp angular velocity. collision calculations will fail on higher angular velocities
        float angvel = angularVelocity.length();
        if (angvel * step > MAX_ANGVEL) {
            angularVelocity.scale((MAX_ANGVEL / step) / angvel);
        }
    }

    public void setCenterOfMassTransform(Transform xform) {
        if (isStaticOrKinematicObject()) {
            interpolationWorldTransform.set(worldTransform);
        } else {
            interpolationWorldTransform.set(xform);
        }
        getLinearVelocity(interpolationLinearVelocity);
        getAngularVelocity(interpolationAngularVelocity);
        worldTransform.set(xform);
        updateInertiaTensor();
    }

    public void applyCentralForce(Vector3f force) {
        totalForce.add(applyVelocityFactor(force, linearFactor));
    }

    public Vector3f getInvInertiaDiagLocal(Vector3f out) {
        out.set(invInertiaLocal);
        return out;
    }

    public void setInvInertiaDiagLocal(Vector3f diagInvInertia) {
        invInertiaLocal.set(diagInvInertia);
    }

    public void setSleepingThresholds(float linear, float angular) {
        linearSleepingThreshold = linear;
        angularSleepingThreshold = angular;
    }

    public void applyTorque(Vector3f torque) {
        totalTorque.add(applyVelocityFactor(torque, angularFactor));
    }

    public void applyForce(Vector3f force, Vector3f rel_pos) {
        applyCentralForce(force);

        Vector3f tmp = new Vector3f();
        tmp.cross(rel_pos, applyVelocityFactor(force, linearFactor));
        applyTorque(tmp);
    }

    public void applyCentralImpulse(Vector3f impulse) {
        linearVelocity.scaleAdd(inverseMass, applyVelocityFactor(impulse, linearFactor), linearVelocity);
    }

    public void applyTorqueImpulse(Vector3f torque) {
        Vector3f tmp = new Vector3f(torque);
        invInertiaTensorWorld.transform(tmp);
        angularVelocity.add(applyVelocityFactor(tmp,angularFactor));
    }

    public void applyImpulse(Vector3f impulse, Vector3f rel_pos) {
        if (inverseMass != 0f) {
            applyCentralImpulse(impulse);
            Vector3f tmp = new Vector3f();
            tmp.cross(rel_pos, applyVelocityFactor(impulse, linearFactor));
            applyTorqueImpulse(tmp);
        }
    }

    /**
     * Optimization for the iterative solver: avoid calculating constant terms involving inertia, normal, relative position.
     */
    public void internalApplyImpulse(Vector3f linearComponent, Vector3f angularComponent, float impulseMagnitude) {
        if (inverseMass != 0f) {
            linearVelocity.scaleAdd(impulseMagnitude,  applyVelocityFactor(linearComponent, linearFactor), applyVelocityFactor(linearVelocity, linearFactor));
            angularVelocity.scaleAdd(impulseMagnitude, applyVelocityFactor(angularComponent, angularFactor), applyVelocityFactor(angularVelocity, angularFactor));
        }
    }

    public void clearForces() {
        totalForce.set(0f, 0f, 0f);
        totalTorque.set(0f, 0f, 0f);
    }

    public void updateInertiaTensor() {
        Matrix3f mat1 = new Matrix3f();
        MatrixUtil.scale(mat1, worldTransform.basis, invInertiaLocal);

        Matrix3f mat2 = new Matrix3f(worldTransform.basis);
        mat2.transpose();

        invInertiaTensorWorld.mul(mat1, mat2);
    }

    public Vector3f getCenterOfMassPosition(Vector3f out) {
        out.set(worldTransform.origin);
        return out;
    }

    public Quat4f getOrientation(Quat4f out) {
        MatrixUtil.getRotation(worldTransform.basis, out);
        return out;
    }

    public Transform getCenterOfMassTransform(Transform out) {
        out.set(worldTransform);
        return out;
    }

    public Vector3f getLinearVelocity(Vector3f out) {
        out.set(linearVelocity);
        return out;
    }

    public Vector3f getAngularVelocity(Vector3f out) {
        out.set(angularVelocity);
        return out;
    }

    public void setLinearVelocity(Vector3f lin_vel) {
        assert (collisionFlags != CollisionFlags.STATIC_OBJECT);
        linearVelocity.set(lin_vel);
    }

    public void setAngularVelocity(Vector3f ang_vel) {
        assert (collisionFlags != CollisionFlags.STATIC_OBJECT);
        angularVelocity.set(ang_vel);
    }

    public Vector3f getVelocityInLocalPoint(Vector3f rel_pos, Vector3f out) {
        // we also calculate lin/ang velocity for kinematic objects
        Vector3f vec = out;
        vec.cross(angularVelocity, rel_pos);
        vec.add(linearVelocity);
        return out;

        //for kinematic objects, we could also use use:
        //		return 	(m_worldTransform(rel_pos) - m_interpolationWorldTransform(rel_pos)) / m_kinematicTimeStep;
    }

    public void translate(Vector3f v) {
        worldTransform.origin.add(v);
    }

    public void getAabb(Vector3f aabbMin, Vector3f aabbMax) {
        getCollisionShape().getAabb(worldTransform, aabbMin, aabbMax);
    }

    public float computeImpulseDenominator(Vector3f pos, Vector3f normal) {
        Vector3f r0 = new Vector3f();
        r0.sub(pos, getCenterOfMassPosition(new Vector3f()));

        Vector3f c0 = new Vector3f();
        c0.cross(r0, normal);

        Vector3f tmp = new Vector3f();
        MatrixUtil.transposeTransform(tmp, c0, getInvInertiaTensorWorld(new Matrix3f()));

        Vector3f vec = new Vector3f();
        vec.cross(tmp, r0);

        return inverseMass + normal.dot(vec);
    }

    public float computeAngularImpulseDenominator(Vector3f axis) {
        Vector3f vec = new Vector3f();
        MatrixUtil.transposeTransform(vec, axis, getInvInertiaTensorWorld(new Matrix3f()));
        return axis.dot(vec);
    }

    public void updateDeactivation(float timeStep) {
        if ((getActivationState() == ISLAND_SLEEPING) || (getActivationState() == DISABLE_DEACTIVATION)) {
            return;
        }

        if ((getLinearVelocity(new Vector3f()).lengthSquared() < linearSleepingThreshold * linearSleepingThreshold) &&
                (getAngularVelocity(new Vector3f()).lengthSquared() < angularSleepingThreshold * angularSleepingThreshold)) {
            deactivationTime += timeStep;
        } else {
            deactivationTime = 0f;
            setActivationState(0);
        }
    }

    public boolean wantsSleeping() {
        if (getActivationState() == DISABLE_DEACTIVATION) {
            return false;
        }

        // disable deactivation
        if (BulletGlobals.isDeactivationDisabled() || (BulletGlobals.getDeactivationTime() == 0f)) {
            return false;
        }

        if ((getActivationState() == ISLAND_SLEEPING) || (getActivationState() == WANTS_DEACTIVATION)) {
            return true;
        }

        if (deactivationTime > BulletGlobals.getDeactivationTime()) {
            return true;
        }
        return false;
    }

    public BroadphaseProxy getBroadphaseProxy() {
        return broadphaseHandle;
    }

    public void setNewBroadphaseProxy(BroadphaseProxy broadphaseProxy) {
        this.broadphaseHandle = broadphaseProxy;
    }

    public MotionState getMotionState() {
        return optionalMotionState;
    }

    public void setMotionState(MotionState motionState) {
        this.optionalMotionState = motionState;
        if (optionalMotionState != null) {
            motionState.getWorldTransform(worldTransform);
        }
    }

    public void setAngularFactor(Vector3f angFac) {
        angularFactor.set(angFac);
    }

    public void setAngularFactor(float angFac) {
        angularFactor.set(angFac, angFac, angFac);
    }

    public void setLinearFactor(Vector3f linFac) {
        linearFactor.set(linFac);
    }

    /**
     * Is this rigidbody added to a CollisionWorld/DynamicsWorld/Broadphase?
     */
    public boolean isInWorld() {
        return (getBroadphaseProxy() != null);
    }

    @Override
    public boolean checkCollideWithOverride(CollisionObject co) {
        // TODO: change to cast
        RigidBody otherRb = RigidBody.upcast(co);
        if (otherRb == null) {
            return true;
        }

        for (int i = 0; i < constraintRefs.size(); ++i) {
            TypedConstraint c = constraintRefs.getQuick(i);
            if (c.getRigidBodyA() == otherRb || c.getRigidBodyB() == otherRb) {
                return false;
            }
        }

        return true;
    }

    public void addConstraintRef(TypedConstraint c) {
        int index = constraintRefs.indexOf(c);
        if (index == -1) {
            constraintRefs.add(c);
        }

        checkCollideWith = true;
    }

    public void removeConstraintRef(TypedConstraint c) {
        constraintRefs.remove(c);
        checkCollideWith = (constraintRefs.size() > 0);
    }

    public TypedConstraint getConstraintRef(int index) {
        return constraintRefs.getQuick(index);
    }

    public int getNumConstraintRefs() {
        return constraintRefs.size();
    }

}
