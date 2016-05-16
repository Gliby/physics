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

package com.bulletphysics.dynamics.constraintsolver;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectPool;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import org.apache.commons.math3.util.FastMath;

/**
 * Functions for resolving contacts.
 *
 * @author jezek2
 */
public class ContactConstraint {

    public static final ContactSolverFunc resolveSingleCollision = new ContactSolverFunc() {
        public float resolveContact(RigidBody body1, RigidBody body2, ManifoldPoint contactPoint, ContactSolverInfo info) {
            return resolveSingleCollision(body1, body2, contactPoint, info);
        }
    };

    public static final ContactSolverFunc resolveSingleFriction = new ContactSolverFunc() {
        public float resolveContact(RigidBody body1, RigidBody body2, ManifoldPoint contactPoint, ContactSolverInfo info) {
            return resolveSingleFriction(body1, body2, contactPoint, info);
        }
    };

    public static final ContactSolverFunc resolveSingleCollisionCombined = new ContactSolverFunc() {
        public float resolveContact(RigidBody body1, RigidBody body2, ManifoldPoint contactPoint, ContactSolverInfo info) {
            return resolveSingleCollisionCombined(body1, body2, contactPoint, info);
        }
    };

    /**
     * Bilateral constraint between two dynamic objects.
     */
    public static void resolveSingleBilateral(RigidBody body1, Vector3f pos1,
                                              RigidBody body2, Vector3f pos2,
                                              float distance, Vector3f normal, float[] impulse, float timeStep) {
        float normalLenSqr = normal.lengthSquared();
        assert (Math.abs(normalLenSqr) < 1.1f);
        if (normalLenSqr > 1.1f) {
            impulse[0] = 0f;
            return;
        }

        ObjectPool<JacobianEntry> jacobiansPool = ObjectPool.get(JacobianEntry.class);
        Vector3f tmp = new Vector3f();

        Vector3f rel_pos1 = new Vector3f();
        rel_pos1.sub(pos1, body1.getCenterOfMassPosition(tmp));

        Vector3f rel_pos2 = new Vector3f();
        rel_pos2.sub(pos2, body2.getCenterOfMassPosition(tmp));

        //this jacobian entry could be re-used for all iterations

        Vector3f vel1 = new Vector3f();
        body1.getVelocityInLocalPoint(rel_pos1, vel1);

        Vector3f vel2 = new Vector3f();
        body2.getVelocityInLocalPoint(rel_pos2, vel2);

        Vector3f vel = new Vector3f();
        vel.sub(vel1, vel2);

        Matrix3f mat1 = body1.getCenterOfMassTransform(new Transform()).basis;
        mat1.transpose();

        Matrix3f mat2 = body2.getCenterOfMassTransform(new Transform()).basis;
        mat2.transpose();

        JacobianEntry jac = jacobiansPool.get();
        jac.init(mat1, mat2,
                rel_pos1, rel_pos2, normal,
                body1.getInvInertiaDiagLocal(new Vector3f()), body1.getInvMass(),
                body2.getInvInertiaDiagLocal(new Vector3f()), body2.getInvMass());

        float jacDiagAB = jac.getDiagonal();
        float jacDiagABInv = 1f / jacDiagAB;

        Vector3f tmp1 = body1.getAngularVelocity(new Vector3f());
        mat1.transform(tmp1);

        Vector3f tmp2 = body2.getAngularVelocity(new Vector3f());
        mat2.transform(tmp2);

        float rel_vel = jac.getRelativeVelocity(
                body1.getLinearVelocity(new Vector3f()),
                tmp1,
                body2.getLinearVelocity(new Vector3f()),
                tmp2);

        jacobiansPool.release(jac);

        float a;
        a = jacDiagABInv;


        rel_vel = normal.dot(vel);

        // todo: move this into proper structure
        float contactDamping = 0.2f;

        //#ifdef ONLY_USE_LINEAR_MASS
        //	btScalar massTerm = btScalar(1.) / (body1.getInvMass() + body2.getInvMass());
        //	impulse = - contactDamping * rel_vel * massTerm;
        //#else
        float velocityImpulse = -contactDamping * rel_vel * jacDiagABInv;
        impulse[0] = velocityImpulse;
        //#endif
    }

    /**
     * Response between two dynamic objects with friction.
     */
    public static float resolveSingleCollision(
            RigidBody body1,
            RigidBody body2,
            ManifoldPoint contactPoint,
            ContactSolverInfo solverInfo) {

        Vector3f tmpVec = new Vector3f();

        Vector3f pos1_ = contactPoint.getPositionWorldOnA(new Vector3f());
        Vector3f pos2_ = contactPoint.getPositionWorldOnB(new Vector3f());
        Vector3f normal = contactPoint.normalWorldOnB;

        // constant over all iterations
        Vector3f rel_pos1 = new Vector3f();
        rel_pos1.sub(pos1_, body1.getCenterOfMassPosition(tmpVec));

        Vector3f rel_pos2 = new Vector3f();
        rel_pos2.sub(pos2_, body2.getCenterOfMassPosition(tmpVec));

        Vector3f vel1 = body1.getVelocityInLocalPoint(rel_pos1, new Vector3f());
        Vector3f vel2 = body2.getVelocityInLocalPoint(rel_pos2, new Vector3f());
        Vector3f vel = new Vector3f();
        vel.sub(vel1, vel2);

        float rel_vel;
        rel_vel = normal.dot(vel);

        float Kfps = 1f / solverInfo.timeStep;

        // btScalar damping = solverInfo.m_damping ;
        float Kerp = solverInfo.erp;
        float Kcor = Kerp * Kfps;

        ConstraintPersistentData cpd = (ConstraintPersistentData) contactPoint.userPersistentData;
        assert (cpd != null);
        float distance = cpd.penetration;
        float positionalError = Kcor * -distance;
        float velocityError = cpd.restitution - rel_vel; // * damping;

        float penetrationImpulse = positionalError * cpd.jacDiagABInv;

        float velocityImpulse = velocityError * cpd.jacDiagABInv;

        float normalImpulse = penetrationImpulse + velocityImpulse;

        // See Erin Catto's GDC 2006 paper: Clamp the accumulated impulse
        float oldNormalImpulse = cpd.appliedImpulse;
        float sum = oldNormalImpulse + normalImpulse;
        cpd.appliedImpulse = 0f > sum ? 0f : sum;

        normalImpulse = cpd.appliedImpulse - oldNormalImpulse;

        //#ifdef USE_INTERNAL_APPLY_IMPULSE
        Vector3f tmp = new Vector3f();
        if (body1.getInvMass() != 0f) {
            tmp.scale(body1.getInvMass(), contactPoint.normalWorldOnB);
            body1.internalApplyImpulse(tmp, cpd.angularComponentA, normalImpulse);
        }
        if (body2.getInvMass() != 0f) {
            tmp.scale(body2.getInvMass(), contactPoint.normalWorldOnB);
            body2.internalApplyImpulse(tmp, cpd.angularComponentB, -normalImpulse);
        }
        //#else //USE_INTERNAL_APPLY_IMPULSE
        //	body1.applyImpulse(normal*(normalImpulse), rel_pos1);
        //	body2.applyImpulse(-normal*(normalImpulse), rel_pos2);
        //#endif //USE_INTERNAL_APPLY_IMPULSE

        return normalImpulse;
    }

    public static float resolveSingleFriction(
            RigidBody body1,
            RigidBody body2,
            ManifoldPoint contactPoint,
            ContactSolverInfo solverInfo) {

        Vector3f tmpVec = new Vector3f();

        Vector3f pos1 = contactPoint.getPositionWorldOnA(new Vector3f());
        Vector3f pos2 = contactPoint.getPositionWorldOnB(new Vector3f());

        Vector3f rel_pos1 = new Vector3f();
        rel_pos1.sub(pos1, body1.getCenterOfMassPosition(tmpVec));

        Vector3f rel_pos2 = new Vector3f();
        rel_pos2.sub(pos2, body2.getCenterOfMassPosition(tmpVec));

        ConstraintPersistentData cpd = (ConstraintPersistentData) contactPoint.userPersistentData;
        assert (cpd != null);

        float combinedFriction = cpd.friction;

        float limit = cpd.appliedImpulse * combinedFriction;

        if (cpd.appliedImpulse > 0f) //friction
        {
            //apply friction in the 2 tangential directions

            // 1st tangent
            Vector3f vel1 = new Vector3f();
            body1.getVelocityInLocalPoint(rel_pos1, vel1);

            Vector3f vel2 = new Vector3f();
            body2.getVelocityInLocalPoint(rel_pos2, vel2);

            Vector3f vel = new Vector3f();
            vel.sub(vel1, vel2);

            float j1, j2;

            {
                float vrel = cpd.frictionWorldTangential0.dot(vel);

                // calculate j that moves us to zero relative velocity
                j1 = -vrel * cpd.jacDiagABInvTangent0;
                float oldTangentImpulse = cpd.accumulatedTangentImpulse0;
                cpd.accumulatedTangentImpulse0 = oldTangentImpulse + j1;

                cpd.accumulatedTangentImpulse0 = FastMath.min(cpd.accumulatedTangentImpulse0, limit);
                cpd.accumulatedTangentImpulse0 = FastMath.max(cpd.accumulatedTangentImpulse0, -limit);
                j1 = cpd.accumulatedTangentImpulse0 - oldTangentImpulse;
            }
            {
                // 2nd tangent

                float vrel = cpd.frictionWorldTangential1.dot(vel);

                // calculate j that moves us to zero relative velocity
                j2 = -vrel * cpd.jacDiagABInvTangent1;
                float oldTangentImpulse = cpd.accumulatedTangentImpulse1;
                cpd.accumulatedTangentImpulse1 = oldTangentImpulse + j2;

                cpd.accumulatedTangentImpulse1 = FastMath.min(cpd.accumulatedTangentImpulse1, limit);
                cpd.accumulatedTangentImpulse1 = FastMath.max(cpd.accumulatedTangentImpulse1, -limit);
                j2 = cpd.accumulatedTangentImpulse1 - oldTangentImpulse;
            }

            //#ifdef USE_INTERNAL_APPLY_IMPULSE
            Vector3f tmp = new Vector3f();

            if (body1.getInvMass() != 0f) {
                tmp.scale(body1.getInvMass(), cpd.frictionWorldTangential0);
                body1.internalApplyImpulse(tmp, cpd.frictionAngularComponent0A, j1);

                tmp.scale(body1.getInvMass(), cpd.frictionWorldTangential1);
                body1.internalApplyImpulse(tmp, cpd.frictionAngularComponent1A, j2);
            }
            if (body2.getInvMass() != 0f) {
                tmp.scale(body2.getInvMass(), cpd.frictionWorldTangential0);
                body2.internalApplyImpulse(tmp, cpd.frictionAngularComponent0B, -j1);

                tmp.scale(body2.getInvMass(), cpd.frictionWorldTangential1);
                body2.internalApplyImpulse(tmp, cpd.frictionAngularComponent1B, -j2);
            }
            //#else //USE_INTERNAL_APPLY_IMPULSE
            //	body1.applyImpulse((j1 * cpd->m_frictionWorldTangential0)+(j2 * cpd->m_frictionWorldTangential1), rel_pos1);
            //	body2.applyImpulse((j1 * -cpd->m_frictionWorldTangential0)+(j2 * -cpd->m_frictionWorldTangential1), rel_pos2);
            //#endif //USE_INTERNAL_APPLY_IMPULSE
        }
        return cpd.appliedImpulse;
    }

    /**
     * velocity + friction<br>
     * response between two dynamic objects with friction
     */
    public static float resolveSingleCollisionCombined(
            RigidBody body1,
            RigidBody body2,
            ManifoldPoint contactPoint,
            ContactSolverInfo solverInfo) {

        Vector3f tmpVec = new Vector3f();

        Vector3f pos1 = contactPoint.getPositionWorldOnA(new Vector3f());
        Vector3f pos2 = contactPoint.getPositionWorldOnB(new Vector3f());
        Vector3f normal = contactPoint.normalWorldOnB;

        Vector3f rel_pos1 = new Vector3f();
        rel_pos1.sub(pos1, body1.getCenterOfMassPosition(tmpVec));

        Vector3f rel_pos2 = new Vector3f();
        rel_pos2.sub(pos2, body2.getCenterOfMassPosition(tmpVec));

        Vector3f vel1 = body1.getVelocityInLocalPoint(rel_pos1, new Vector3f());
        Vector3f vel2 = body2.getVelocityInLocalPoint(rel_pos2, new Vector3f());
        Vector3f vel = new Vector3f();
        vel.sub(vel1, vel2);

        float rel_vel;
        rel_vel = normal.dot(vel);

        float Kfps = 1f / solverInfo.timeStep;

        //btScalar damping = solverInfo.m_damping ;
        float Kerp = solverInfo.erp;
        float Kcor = Kerp * Kfps;

        ConstraintPersistentData cpd = (ConstraintPersistentData) contactPoint.userPersistentData;
        assert (cpd != null);
        float distance = cpd.penetration;
        float positionalError = Kcor * -distance;
        float velocityError = cpd.restitution - rel_vel;// * damping;

        float penetrationImpulse = positionalError * cpd.jacDiagABInv;

        float velocityImpulse = velocityError * cpd.jacDiagABInv;

        float normalImpulse = penetrationImpulse + velocityImpulse;

        // See Erin Catto's GDC 2006 paper: Clamp the accumulated impulse
        float oldNormalImpulse = cpd.appliedImpulse;
        float sum = oldNormalImpulse + normalImpulse;
        cpd.appliedImpulse = 0f > sum ? 0f : sum;

        normalImpulse = cpd.appliedImpulse - oldNormalImpulse;


        //#ifdef USE_INTERNAL_APPLY_IMPULSE
        Vector3f tmp = new Vector3f();
        if (body1.getInvMass() != 0f) {
            tmp.scale(body1.getInvMass(), contactPoint.normalWorldOnB);
            body1.internalApplyImpulse(tmp, cpd.angularComponentA, normalImpulse);
        }
        if (body2.getInvMass() != 0f) {
            tmp.scale(body2.getInvMass(), contactPoint.normalWorldOnB);
            body2.internalApplyImpulse(tmp, cpd.angularComponentB, -normalImpulse);
        }
        //#else //USE_INTERNAL_APPLY_IMPULSE
        //	body1.applyImpulse(normal*(normalImpulse), rel_pos1);
        //	body2.applyImpulse(-normal*(normalImpulse), rel_pos2);
        //#endif //USE_INTERNAL_APPLY_IMPULSE

        {
            //friction
            body1.getVelocityInLocalPoint(rel_pos1, vel1);
            body2.getVelocityInLocalPoint(rel_pos2, vel2);
            vel.sub(vel1, vel2);

            rel_vel = normal.dot(vel);

            tmp.scale(rel_vel, normal);
            Vector3f lat_vel = new Vector3f();
            lat_vel.sub(vel, tmp);
            float lat_rel_vel = lat_vel.length();

            float combinedFriction = cpd.friction;

            if (cpd.appliedImpulse > 0) {
                if (lat_rel_vel > BulletGlobals.FLT_EPSILON) {
                    lat_vel.scale(1f / lat_rel_vel);

                    Vector3f temp1 = new Vector3f();
                    temp1.cross(rel_pos1, lat_vel);
                    body1.getInvInertiaTensorWorld(new Matrix3f()).transform(temp1);

                    Vector3f temp2 = new Vector3f();
                    temp2.cross(rel_pos2, lat_vel);
                    body2.getInvInertiaTensorWorld(new Matrix3f()).transform(temp2);

                    Vector3f java_tmp1 = new Vector3f();
                    java_tmp1.cross(temp1, rel_pos1);

                    Vector3f java_tmp2 = new Vector3f();
                    java_tmp2.cross(temp2, rel_pos2);

                    tmp.add(java_tmp1, java_tmp2);

                    float friction_impulse = lat_rel_vel /
                            (body1.getInvMass() + body2.getInvMass() + lat_vel.dot(tmp));
                    float normal_impulse = cpd.appliedImpulse * combinedFriction;

                    friction_impulse = FastMath.min(friction_impulse, normal_impulse);
                    friction_impulse = FastMath.max(friction_impulse, -normal_impulse);

                    tmp.scale(-friction_impulse, lat_vel);
                    body1.applyImpulse(tmp, rel_pos1);

                    tmp.scale(friction_impulse, lat_vel);
                    body2.applyImpulse(tmp, rel_pos2);
                }
            }
        }

        return normalImpulse;
    }

    public static float resolveSingleFrictionEmpty(
            RigidBody body1,
            RigidBody body2,
            ManifoldPoint contactPoint,
            ContactSolverInfo solverInfo) {
        return 0f;
    }

}
