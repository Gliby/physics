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

package com.bulletphysics.dynamics;

import javax.vecmath.Vector3f;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.broadphase.DispatcherInfo;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.ContactSolverInfo;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;

/**
 * SimpleDynamicsWorld serves as unit-test and to verify more complicated and
 * optimized dynamics worlds. Please use {@link DiscreteDynamicsWorld} instead
 * (or ContinuousDynamicsWorld once it is finished).
 *
 * @author jezek2
 */
public class SimpleDynamicsWorld extends DynamicsWorld {

    protected ConstraintSolver constraintSolver;
    protected boolean ownsConstraintSolver;
    protected final Vector3f gravity = new Vector3f(0f, 0f, -10f);

    public SimpleDynamicsWorld(Dispatcher dispatcher, BroadphaseInterface pairCache, ConstraintSolver constraintSolver, CollisionConfiguration collisionConfiguration) {
        super(dispatcher, pairCache, collisionConfiguration);
        this.constraintSolver = constraintSolver;
        this.ownsConstraintSolver = false;
    }

    protected void predictUnconstraintMotion(float timeStep) {
        Transform tmpTrans = new Transform();

        for (int i = 0; i < collisionObjects.size(); i++) {
            CollisionObject colObj = collisionObjects.getQuick(i);
            RigidBody body = RigidBody.upcast(colObj);
            if (body != null) {
                if (!body.isStaticObject()) {
                    if (body.isActive()) {
                        body.applyGravity();
                        body.integrateVelocities(timeStep);
                        body.applyDamping(timeStep);
                        body.predictIntegratedTransform(timeStep, body.getInterpolationWorldTransform(tmpTrans));
                    }
                }
            }
        }
    }

    protected void integrateTransforms(float timeStep) {
        Transform predictedTrans = new Transform();
        for (int i = 0; i < collisionObjects.size(); i++) {
            CollisionObject colObj = collisionObjects.getQuick(i);
            RigidBody body = RigidBody.upcast(colObj);
            if (body != null) {
                if (body.isActive() && (!body.isStaticObject())) {
                    body.predictIntegratedTransform(timeStep, predictedTrans);
                    body.proceedToTransform(predictedTrans);
                }
            }
        }
    }

    /**
     * maxSubSteps/fixedTimeStep for interpolation is currently ignored for SimpleDynamicsWorld, use DiscreteDynamicsWorld instead.
     */
    @Override
    public int stepSimulation(float timeStep, int maxSubSteps, float fixedTimeStep) {
        // apply gravity, predict motion
        predictUnconstraintMotion(timeStep);

        DispatcherInfo dispatchInfo = getDispatchInfo();
        dispatchInfo.timeStep = timeStep;
        dispatchInfo.stepCount = 0;
        dispatchInfo.debugDraw = getDebugDrawer();

        // perform collision detection
        performDiscreteCollisionDetection();

        // solve contact constraints
        int numManifolds = dispatcher1.getNumManifolds();
        if (numManifolds != 0) {
            ObjectArrayList<PersistentManifold> manifoldPtr = ((CollisionDispatcher) dispatcher1).getInternalManifoldPointer();

            ContactSolverInfo infoGlobal = new ContactSolverInfo();
            infoGlobal.timeStep = timeStep;
            constraintSolver.prepareSolve(0, numManifolds);
            constraintSolver.solveGroup(null, 0, manifoldPtr, 0, numManifolds, null, 0, 0, infoGlobal, debugDrawer/*, m_stackAlloc*/, dispatcher1);
            constraintSolver.allSolved(infoGlobal, debugDrawer/*, m_stackAlloc*/);
        }

        // integrate transforms
        integrateTransforms(timeStep);

        updateAabbs();

        synchronizeMotionStates();

        clearForces();

        return 1;
    }

    @Override
    public void clearForces() {
        // todo: iterate over awake simulation islands!
        for (int i = 0; i < collisionObjects.size(); i++) {
            CollisionObject colObj = collisionObjects.getQuick(i);

            RigidBody body = RigidBody.upcast(colObj);
            if (body != null) {
                body.clearForces();
            }
        }
    }

    @Override
    public void setGravity(Vector3f gravity) {
        this.gravity.set(gravity);
        for (int i = 0; i < collisionObjects.size(); i++) {
            CollisionObject colObj = collisionObjects.getQuick(i);
            RigidBody body = RigidBody.upcast(colObj);
            if (body != null) {
                body.setGravity(gravity);
            }
        }
    }

    @Override
    public Vector3f getGravity(Vector3f out) {
        out.set(gravity);
        return out;
    }

    @Override
    public void addRigidBody(RigidBody body) {
        body.setGravity(gravity);

        if (body.getCollisionShape() != null) {
            addCollisionObject(body);
        }
    }

    @Override
    public void removeRigidBody(RigidBody body) {
        removeCollisionObject(body);
    }

    @Override
    public void updateAabbs() {
        Transform tmpTrans = new Transform();
        Transform predictedTrans = new Transform();
        Vector3f minAabb = new Vector3f(), maxAabb = new Vector3f();

        for (int i = 0; i < collisionObjects.size(); i++) {
            CollisionObject colObj = collisionObjects.getQuick(i);
            RigidBody body = RigidBody.upcast(colObj);
            if (body != null) {
                if (body.isActive() && (!body.isStaticObject())) {
                    colObj.getCollisionShape().getAabb(colObj.getWorldTransform(tmpTrans), minAabb, maxAabb);
                    BroadphaseInterface bp = getBroadphase();
                    bp.setAabb(body.getBroadphaseHandle(), minAabb, maxAabb, dispatcher1);
                }
            }
        }
    }

    public void synchronizeMotionStates() {
        Transform tmpTrans = new Transform();

        // todo: iterate over awake simulation islands!
        for (int i = 0; i < collisionObjects.size(); i++) {
            CollisionObject colObj = collisionObjects.getQuick(i);
            RigidBody body = RigidBody.upcast(colObj);
            if (body != null && body.getMotionState() != null) {
                if (body.getActivationState() != CollisionObject.ISLAND_SLEEPING) {
                    body.getMotionState().setWorldTransform(body.getWorldTransform(tmpTrans));
                }
            }
        }
    }

    @Override
    public void setConstraintSolver(ConstraintSolver solver) {
        if (ownsConstraintSolver) {
            //btAlignedFree(m_constraintSolver);
        }

        ownsConstraintSolver = false;
        constraintSolver = solver;
    }

    @Override
    public ConstraintSolver getConstraintSolver() {
        return constraintSolver;
    }

    @Override
    public void debugDrawWorld() {
        // TODO: throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DynamicsWorldType getWorldType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
