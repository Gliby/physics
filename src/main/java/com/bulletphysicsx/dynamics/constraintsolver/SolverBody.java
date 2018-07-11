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

package com.bulletphysicsx.dynamics.constraintsolver;

import javax.vecmath.Vector3f;

import com.bulletphysicsx.dynamics.RigidBody;
import com.bulletphysicsx.linearmath.Transform;
import com.bulletphysicsx.linearmath.TransformUtil;

/**
 * SolverBody is an internal data structure for the constraint solver. Only necessary
 * data is packed to increase cache coherence/performance.
 *
 * @author jezek2
 */
public class SolverBody {

    //protected final BulletStack stack = BulletStack.get();

    public final Vector3f angularVelocity = new Vector3f();
    public Vector3f angularFactor = new Vector3f();
    public Vector3f linearFactor = new Vector3f();
    public float invMass;
    public float friction;
    public RigidBody originalBody;
    public final Vector3f linearVelocity = new Vector3f();
    public final Vector3f centerOfMassPosition = new Vector3f();

    public final Vector3f pushVelocity = new Vector3f();
    public final Vector3f turnVelocity = new Vector3f();

    public void getVelocityInLocalPoint(Vector3f rel_pos, Vector3f velocity) {
        Vector3f tmp = new Vector3f();
        tmp.cross(angularVelocity, rel_pos);
        velocity.add(linearVelocity, tmp);
    }

    /**
     * Optimization for the iterative solver: avoid calculating constant terms involving inertia, normal, relative position.
     */
    public void internalApplyImpulse(Vector3f linearComponent, Vector3f angularComponent, float impulseMagnitude) {
        if (invMass != 0f) {
            linearVelocity.scaleAdd(impulseMagnitude,  applyVelocityFactor(linearComponent, linearFactor), applyVelocityFactor(linearVelocity, linearFactor));
            angularVelocity.scaleAdd(impulseMagnitude, applyVelocityFactor(angularComponent, angularFactor), applyVelocityFactor(angularVelocity, angularFactor));
        }
    }

    public void internalApplyPushImpulse(Vector3f linearComponent, Vector3f angularComponent, float impulseMagnitude) {
        if (invMass != 0f) {
            pushVelocity.scaleAdd(impulseMagnitude,  applyVelocityFactor(linearComponent, linearFactor), applyVelocityFactor(linearVelocity, pushVelocity));
            turnVelocity.scaleAdd(impulseMagnitude, applyVelocityFactor(angularComponent, angularFactor), applyVelocityFactor(angularVelocity, turnVelocity));
        }
    }

    public void writebackVelocity() {
        if (invMass != 0f) {
            originalBody.setLinearVelocity(linearVelocity);
            originalBody.setAngularVelocity(angularVelocity);
            //m_originalBody->setCompanionId(-1);
        }
    }

    public void writebackVelocity(float timeStep) {
        if (invMass != 0f) {
            originalBody.setLinearVelocity(linearVelocity);
            originalBody.setAngularVelocity(angularVelocity);

            // correct the position/orientation based on push/turn recovery
            Transform newTransform = new Transform();
            Transform curTrans = originalBody.getWorldTransform(new Transform());
            TransformUtil.integrateTransform(curTrans, pushVelocity, turnVelocity, timeStep, newTransform);
            originalBody.setWorldTransform(newTransform);

            //m_originalBody->setCompanionId(-1);
        }
    }

    public void readVelocity() {
        if (invMass != 0f) {
            originalBody.getLinearVelocity(linearVelocity);
            originalBody.getAngularVelocity(angularVelocity);
        }
    }

    private Vector3f applyVelocityFactor(Vector3f value, Vector3f velocity) {
        Vector3f valueWithLinearFactor = new Vector3f(value);
        valueWithLinearFactor.x *= velocity.x;
        valueWithLinearFactor.y *= velocity.y;
        valueWithLinearFactor.z *= velocity.z;
        return valueWithLinearFactor;
    }


}
