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

package com.bulletphysics.collision.narrowphase;

import javax.vecmath.Vector3f;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.collision.narrowphase.DiscreteCollisionDetectorInterface.ClosestPointInput;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.linearmath.VectorUtil;
import com.bulletphysics.util.ObjectPool;

/**
 * GjkConvexCast performs a raycast on a convex object using support mapping.
 *
 * @author jezek2
 */
public class GjkConvexCast extends ConvexCast {

    //protected final BulletStack stack = BulletStack.get();
    protected final ObjectPool<ClosestPointInput> pointInputsPool = ObjectPool.get(ClosestPointInput.class);

    //#ifdef BT_USE_DOUBLE_PRECISION
//	private static final int MAX_ITERATIONS = 64;
//#else
    private static final int MAX_ITERATIONS = 32;
//#endif

    private SimplexSolverInterface simplexSolver;
    private ConvexShape convexA;
    private ConvexShape convexB;

    private GjkPairDetector gjk = new GjkPairDetector();

    public GjkConvexCast(ConvexShape convexA, ConvexShape convexB, SimplexSolverInterface simplexSolver) {
        this.simplexSolver = simplexSolver;
        this.convexA = convexA;
        this.convexB = convexB;
    }

    // Note: Incorporates this fix http://code.google.com/p/bullet/source/detail?r=2362
    // But doesn't add in angular velocity
    public boolean calcTimeOfImpact(Transform fromA, Transform toA, Transform fromB, Transform toB, CastResult result) {
        simplexSolver.reset();

        // compute linear velocity for this interval, to interpolate
        // assume no rotation/angular velocity, assert here?
        Vector3f linVelA = new Vector3f();
        Vector3f linVelB = new Vector3f();

        linVelA.sub(toA.origin, fromA.origin);
        linVelB.sub(toB.origin, fromB.origin);

        Vector3f relLinVel = new Vector3f();
        relLinVel.sub(linVelB, linVelA);
        float relLinVelocLength = relLinVel.length();
        if (relLinVelocLength == 0) {
            return false;
        }

        float lambda = 0f;
        Vector3f v = new Vector3f();
        v.set(1f, 0f, 0f);

        int maxIter = MAX_ITERATIONS;

        Vector3f n = new Vector3f();
        n.set(0f, 0f, 0f);
        boolean hasResult = false;
        Vector3f c = new Vector3f();

        float lastLambda = lambda;
        //btScalar epsilon = btScalar(0.001);

        int numIter = 0;
        // first solution, using GJK

        Transform identityTrans = new Transform();
        identityTrans.setIdentity();

        //result.drawCoordSystem(sphereTr);

        PointCollector pointCollector = new PointCollector();

        gjk.init(convexA, convexB, simplexSolver, null); // penetrationDepthSolver);
        ClosestPointInput input = pointInputsPool.get();
        input.init();
        try {

            input.transformA.set(fromA);
            input.transformB.set(fromB);
            gjk.getClosestPoints(input, pointCollector, null);

            hasResult = pointCollector.hasResult;
            c.set(pointCollector.pointInWorld);

            if (hasResult) {
                float dist;
                dist = pointCollector.distance + result.allowedPenetration;
                n.set(pointCollector.normalOnBInWorld);

                float projectedLinearVelocity = relLinVel.dot(n);
                if ((projectedLinearVelocity) <= BulletGlobals.SIMD_EPSILON) {
                    return false;
                }

                // not close enough
                while (dist > BulletGlobals.SIMD_EPSILON) {
                    /*numIter++;
                    if (numIter > maxIter) {
						return false; // todo: report a failure
					} */
                    float dLambda = 0f;

                    projectedLinearVelocity = relLinVel.dot(n);

                    if ((projectedLinearVelocity) <= BulletGlobals.SIMD_EPSILON) {
                        return false;
                    }

                    dLambda = dist / (projectedLinearVelocity);

                    lambda = lambda + dLambda;

                    if (lambda > 1f) {
                        return false;
                    }
                    if (lambda < 0f) {
                        return false;                    // todo: next check with relative epsilon
                    }

                    if (lambda <= lastLambda) {
                        return false;
                    }
                    lastLambda = lambda;

                    // interpolate to next lambda
                    result.debugDraw(lambda);
                    VectorUtil.setInterpolate3(input.transformA.origin, fromA.origin, toA.origin, lambda);
                    VectorUtil.setInterpolate3(input.transformB.origin, fromB.origin, toB.origin, lambda);

                    gjk.getClosestPoints(input, pointCollector, null);
                    if (pointCollector.hasResult) {
                        dist = pointCollector.distance + result.allowedPenetration;
                        c.set(pointCollector.pointInWorld);
                        n.set(pointCollector.normalOnBInWorld);
                    } else {
                        // ??
                        return false;
                    }
                    numIter++;
                    if (numIter > maxIter) {
                        return false;
                    }

                }

                result.fraction = lambda;
                result.normal.set(n);
                result.hitPoint.set(c);
                return true;
            }

            return false;
        } finally {
            pointInputsPool.release(input);
        }
    }

}
