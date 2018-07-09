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

package com.bulletphysics.collision.dispatch;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3i;
import javax.vecmath.Quat4f;
import javax.vecmath.Tuple3i;
import javax.vecmath.Vector3f;

import org.apache.commons.math3.util.FastMath;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.BulletStats;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.BroadphaseNativeType;
import com.bulletphysics.collision.broadphase.BroadphaseProxy;
import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.broadphase.DispatcherInfo;
import com.bulletphysics.collision.broadphase.OverlappingPairCache;
import com.bulletphysics.collision.narrowphase.ConvexCast;
import com.bulletphysics.collision.narrowphase.GjkConvexCast;
import com.bulletphysics.collision.narrowphase.GjkEpaPenetrationDepthSolver;
import com.bulletphysics.collision.narrowphase.SubsimplexConvexCast;
import com.bulletphysics.collision.narrowphase.TriangleConvexcastCallback;
import com.bulletphysics.collision.narrowphase.TriangleRaycastCallback;
import com.bulletphysics.collision.narrowphase.VoronoiSimplexSolver;
import com.bulletphysics.collision.narrowphase.ConvexCast.CastResult;
import com.bulletphysics.collision.shapes.BvhTriangleMeshShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.ConcaveShape;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.collision.shapes.TriangleMeshShape;
import com.bulletphysics.collision.shapes.voxel.JBulletVoxelWorldShape;
import com.bulletphysics.collision.shapes.voxel.VoxelInfo;
import com.bulletphysics.collision.shapes.voxel.VoxelPhysicsWorld;
import com.bulletphysics.linearmath.AabbUtil2;
import com.bulletphysics.linearmath.IDebugDraw;
import com.bulletphysics.linearmath.IntUtil;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.linearmath.TransformUtil;
import com.bulletphysics.linearmath.VectorUtil;
import com.bulletphysics.util.ObjectArrayList;

/**
 * CollisionWorld is interface and container for the collision detection.
 *
 * @author jezek2
 */
public class CollisionWorld {

    private static final Matrix3f IDENTITY_MAT3F = new Matrix3f();

    static {
        IDENTITY_MAT3F.setIdentity();
    }

    //protected final BulletStack stack = BulletStack.get();

    protected ObjectArrayList<CollisionObject> collisionObjects = new ObjectArrayList<CollisionObject>();
    protected Dispatcher dispatcher1;
    protected DispatcherInfo dispatchInfo = new DispatcherInfo();
    //protected btStackAlloc*	m_stackAlloc;
    protected BroadphaseInterface broadphasePairCache;
    protected IDebugDraw debugDrawer;

    /**
     * This constructor doesn't own the dispatcher and paircache/broadphase.
     */
    public CollisionWorld(Dispatcher dispatcher, BroadphaseInterface broadphasePairCache, CollisionConfiguration collisionConfiguration) {
        this.dispatcher1 = dispatcher;
        this.broadphasePairCache = broadphasePairCache;
    }

    public void destroy() {
        // clean up remaining objects
        for (int i = 0; i < collisionObjects.size(); i++) {
            CollisionObject collisionObject = collisionObjects.getQuick(i);

            BroadphaseProxy bp = collisionObject.getBroadphaseHandle();
            if (bp != null) {
                //
                // only clear the cached algorithms
                //
                getBroadphase().getOverlappingPairCache().cleanProxyFromPairs(bp, dispatcher1);
                getBroadphase().destroyProxy(bp, dispatcher1);
            }
        }
    }

    public void addCollisionObject(CollisionObject collisionObject) {
        addCollisionObject(collisionObject, CollisionFilterGroups.DEFAULT_FILTER, CollisionFilterGroups.ALL_FILTER);
    }

    public void addCollisionObject(CollisionObject collisionObject, short collisionFilterGroup, short collisionFilterMask) {
        // check that the object isn't already added
        assert (!collisionObjects.contains(collisionObject));

        collisionObjects.add(collisionObject);

        // calculate new AABB
        // TODO: check if it's overwritten or not
        Transform trans = collisionObject.getWorldTransform(new Transform());

        Vector3f minAabb = new Vector3f();
        Vector3f maxAabb = new Vector3f();
        collisionObject.getCollisionShape().getAabb(trans, minAabb, maxAabb);

        BroadphaseNativeType type = collisionObject.getCollisionShape().getShapeType();
        collisionObject.setBroadphaseHandle(getBroadphase().createProxy(
                minAabb,
                maxAabb,
                type,
                collisionObject,
                collisionFilterGroup,
                collisionFilterMask,
                dispatcher1, null));
    }

    public void performDiscreteCollisionDetection() {
        BulletStats.pushProfile("performDiscreteCollisionDetection");
        try {
            //DispatcherInfo dispatchInfo = getDispatchInfo();

            updateAabbs();

            BulletStats.pushProfile("calculateOverlappingPairs");
            try {
                broadphasePairCache.calculateOverlappingPairs(dispatcher1);
            } finally {
                BulletStats.popProfile();
            }

            Dispatcher dispatcher = getDispatcher();
            {
                BulletStats.pushProfile("dispatchAllCollisionPairs");
                try {
                    if (dispatcher != null) {
                        dispatcher.dispatchAllCollisionPairs(broadphasePairCache.getOverlappingPairCache(), dispatchInfo, dispatcher1);
                    }
                } finally {
                    BulletStats.popProfile();
                }
            }
        } finally {
            BulletStats.popProfile();
        }
    }

    public void removeCollisionObject(CollisionObject collisionObject) {
        //bool removeFromBroadphase = false;

        {
            BroadphaseProxy bp = collisionObject.getBroadphaseHandle();
            if (bp != null) {
                //
                // only clear the cached algorithms
                //
                getBroadphase().getOverlappingPairCache().cleanProxyFromPairs(bp, dispatcher1);
                getBroadphase().destroyProxy(bp, dispatcher1);
                collisionObject.setBroadphaseHandle(null);
            }
        }

        //swapremove
        collisionObjects.remove(collisionObject);
    }

    public void setBroadphase(BroadphaseInterface pairCache) {
        broadphasePairCache = pairCache;
    }

    public BroadphaseInterface getBroadphase() {
        return broadphasePairCache;
    }

    public OverlappingPairCache getPairCache() {
        return broadphasePairCache.getOverlappingPairCache();
    }

    public Dispatcher getDispatcher() {
        return dispatcher1;
    }

    public DispatcherInfo getDispatchInfo() {
        return dispatchInfo;
    }

    private static boolean updateAabbs_reportMe = true;

    // JAVA NOTE: ported from 2.74, missing contact threshold stuff
    public void updateSingleAabb(CollisionObject colObj) {
        Vector3f minAabb = new Vector3f(), maxAabb = new Vector3f();
        Vector3f tmp = new Vector3f();
        Transform tmpTrans = new Transform();

        colObj.getCollisionShape().getAabb(colObj.getWorldTransform(tmpTrans), minAabb, maxAabb);
        // need to increase the aabb for contact thresholds
        Vector3f contactThreshold = new Vector3f();
        contactThreshold.set(BulletGlobals.getContactBreakingThreshold(), BulletGlobals.getContactBreakingThreshold(), BulletGlobals.getContactBreakingThreshold());
        minAabb.sub(contactThreshold);
        maxAabb.add(contactThreshold);

        BroadphaseInterface bp = broadphasePairCache;

        // moving objects should be moderately sized, probably something wrong if not
        tmp.sub(maxAabb, minAabb); // TODO: optimize
        if (colObj.isStaticObject() || (tmp.lengthSquared() < 1e12f)) {
            bp.setAabb(colObj.getBroadphaseHandle(), minAabb, maxAabb, dispatcher1);
        } else {
            // something went wrong, investigate
            // this assert is unwanted in 3D modelers (danger of loosing work)
            colObj.setActivationState(CollisionObject.DISABLE_SIMULATION);

            if (updateAabbs_reportMe && debugDrawer != null) {
                updateAabbs_reportMe = false;
                debugDrawer.reportErrorWarning("Overflow in AABB, object removed from simulation");
                debugDrawer.reportErrorWarning("If you can reproduce this, please email bugs@continuousphysics.com\n");
                debugDrawer.reportErrorWarning("Please include above information, your Platform, version of OS.\n");
                debugDrawer.reportErrorWarning("Thanks.\n");
            }
        }
    }

    public void updateAabbs() {
        BulletStats.pushProfile("updateAabbs");
        try {
            for (int i = 0; i < collisionObjects.size(); i++) {
                CollisionObject colObj = collisionObjects.getQuick(i);

                // only update aabb of active objects
                if (colObj.isActive()) {
                    updateSingleAabb(colObj);
                }
            }
        } finally {
            BulletStats.popProfile();
        }
    }

    public IDebugDraw getDebugDrawer() {
        return debugDrawer;
    }

    public void setDebugDrawer(IDebugDraw debugDrawer) {
        this.debugDrawer = debugDrawer;
    }

    public int getNumCollisionObjects() {
        return collisionObjects.size();
    }

    // TODO
    public static void rayTestSingle(Transform rayFromTrans, Transform rayToTrans,
                                     CollisionObject collisionObject,
                                     CollisionShape collisionShape,
                                     Transform colObjWorldTransform,
                                     RayResultCallback resultCallback) {
        SphereShape pointShape = new SphereShape(0f);
        pointShape.setMargin(0f);
        ConvexShape castShape = pointShape;

        if (collisionShape.isConvex()) {
            CastResult castResult = new CastResult();
            castResult.fraction = resultCallback.closestHitFraction;

            ConvexShape convexShape = (ConvexShape) collisionShape;
            VoronoiSimplexSolver simplexSolver = new VoronoiSimplexSolver();

            //#define USE_SUBSIMPLEX_CONVEX_CAST 1
            //#ifdef USE_SUBSIMPLEX_CONVEX_CAST
            SubsimplexConvexCast convexCaster = new SubsimplexConvexCast(castShape, convexShape, simplexSolver);
            //#else
            //btGjkConvexCast	convexCaster(castShape,convexShape,&simplexSolver);
            //btContinuousConvexCollision convexCaster(castShape,convexShape,&simplexSolver,0);
            //#endif //#USE_SUBSIMPLEX_CONVEX_CAST

            if (convexCaster.calcTimeOfImpact(rayFromTrans, rayToTrans, colObjWorldTransform, colObjWorldTransform, castResult)) {
                //add hit
                if (castResult.normal.lengthSquared() > 0.0001f) {
                    if (castResult.fraction < resultCallback.closestHitFraction) {
                        //#ifdef USE_SUBSIMPLEX_CONVEX_CAST
                        //rotate normal into worldspace
                        rayFromTrans.basis.transform(castResult.normal);
                        //#endif //USE_SUBSIMPLEX_CONVEX_CAST

                        castResult.normal.normalize();
                        LocalRayResult localRayResult = new LocalRayResult(
                                collisionObject,
                                null,
                                castResult.normal,
                                castResult.fraction);

                        boolean normalInWorldSpace = true;
                        resultCallback.addSingleResult(localRayResult, normalInWorldSpace);
                    }
                }
            }
        } else if (collisionShape.isConcave()) {
            if (collisionShape.getShapeType() == BroadphaseNativeType.TRIANGLE_MESH_SHAPE_PROXYTYPE) {
                // optimized version for BvhTriangleMeshShape
                BvhTriangleMeshShape triangleMesh = (BvhTriangleMeshShape) collisionShape;
                Transform worldTocollisionObject = new Transform();
                worldTocollisionObject.inverse(colObjWorldTransform);
                Vector3f rayFromLocal = new Vector3f(rayFromTrans.origin);
                worldTocollisionObject.transform(rayFromLocal);
                Vector3f rayToLocal = new Vector3f(rayToTrans.origin);
                worldTocollisionObject.transform(rayToLocal);

                BridgeTriangleRaycastCallback rcb = new BridgeTriangleRaycastCallback(rayFromLocal, rayToLocal, resultCallback, collisionObject, triangleMesh);
                rcb.hitFraction = resultCallback.closestHitFraction;
                triangleMesh.performRaycast(rcb, rayFromLocal, rayToLocal);
            } else {
                ConcaveShape triangleMesh = (ConcaveShape) collisionShape;

                Transform worldTocollisionObject = new Transform();
                worldTocollisionObject.inverse(colObjWorldTransform);

                Vector3f rayFromLocal = new Vector3f(rayFromTrans.origin);
                worldTocollisionObject.transform(rayFromLocal);
                Vector3f rayToLocal = new Vector3f(rayToTrans.origin);
                worldTocollisionObject.transform(rayToLocal);

                BridgeTriangleRaycastCallback rcb = new BridgeTriangleRaycastCallback(rayFromLocal, rayToLocal, resultCallback, collisionObject, triangleMesh);
                rcb.hitFraction = resultCallback.closestHitFraction;

                Vector3f rayAabbMinLocal = new Vector3f(rayFromLocal);
                VectorUtil.setMin(rayAabbMinLocal, rayToLocal);
                Vector3f rayAabbMaxLocal = new Vector3f(rayFromLocal);
                VectorUtil.setMax(rayAabbMaxLocal, rayToLocal);

                triangleMesh.processAllTriangles(rcb, rayAabbMinLocal, rayAabbMaxLocal);
            }
        } else if (collisionShape.isVoxelWorld()) {

            JBulletVoxelWorldShape voxelShape = (JBulletVoxelWorldShape) collisionShape;
            VoxelPhysicsWorld world = voxelShape.getWorld();

            int currentVoxX = IntUtil.floorToInt(rayFromTrans.origin.x + 0.5f);
            int currentVoxY = IntUtil.floorToInt(rayFromTrans.origin.y + 0.5f);
            int currentVoxZ = IntUtil.floorToInt(rayFromTrans.origin.z + 0.5f);
            float dx = Math.abs(rayToTrans.origin.x - rayFromTrans.origin.x);
            float dy = Math.abs(rayToTrans.origin.y - rayFromTrans.origin.y);
            float dz = Math.abs(rayToTrans.origin.z - rayFromTrans.origin.z);
            float invDx = 1.0f / dx;
            float invDy = 1.0f / dy;
            float invDz = 1.0f / dz;
            float tNextX = invDx;
            float tNextY = invDy;
            float tNextZ = invDz;

            float t = 0;
            int number = 1;
            int xIncrement = 0;
            if (rayToTrans.origin.x > rayFromTrans.origin.x) {
                xIncrement = 1;
                number += IntUtil.floorToInt(rayToTrans.origin.x + 0.5f) - currentVoxX;
                tNextX = (currentVoxX + 0.5f - rayFromTrans.origin.x) * invDx;
            } else if (rayToTrans.origin.x < rayFromTrans.origin.x) {
                xIncrement = -1;
                number += currentVoxX - IntUtil.floorToInt(rayToTrans.origin.x + 0.5f);
                tNextX = (rayFromTrans.origin.x - currentVoxX + 0.5f) * invDx;
            }
            int yIncrement = 0;
            if (rayToTrans.origin.y > rayFromTrans.origin.y) {
                yIncrement = 1;
                number += IntUtil.floorToInt(rayToTrans.origin.y + 0.5f) - currentVoxY;
                tNextY = (currentVoxY + 0.5f - rayFromTrans.origin.y) * invDy;
            } else if (rayToTrans.origin.y < rayFromTrans.origin.y) {
                yIncrement = -1;
                number += currentVoxY - IntUtil.floorToInt(rayToTrans.origin.y + 0.5f);
                tNextY = (rayFromTrans.origin.y - currentVoxY + 0.5f) * invDy;
            }
            int zIncrement = 0;
            if (rayToTrans.origin.z > rayFromTrans.origin.z) {
                zIncrement = 1;
                number += IntUtil.floorToInt(rayToTrans.origin.z + 0.5f) - currentVoxZ;
                tNextZ = (currentVoxZ + 0.5f - rayFromTrans.origin.z) * invDz;
            } else if (rayToTrans.origin.z < rayFromTrans.origin.z) {
                zIncrement = -1;
                number += currentVoxZ - IntUtil.floorToInt(rayToTrans.origin.z + 0.5f);
                tNextZ = (rayFromTrans.origin.z - currentVoxZ + 0.5f) * invDz;
            }

            for (; number > 0; --number) {
                VoxelInfo childInfo = world.getCollisionShapeAt(currentVoxX, currentVoxY, currentVoxZ);
                if (childInfo.isColliding()) {
                    Vector3f pos = new Vector3f();
                    pos.set(currentVoxX, currentVoxY, currentVoxZ);
                    pos.add((Vector3f)childInfo.getCollisionOffset());
                    Matrix4f transformMat = new Matrix4f();
                    transformMat.set(IDENTITY_MAT3F, pos, 1.0f);
                    Transform childTransform = new Transform();
                    childTransform.set(transformMat);
                    // replace collision shape so that callback can determine the triangle
                    CollisionShape saveCollisionShape = collisionObject.getCollisionShape();
                    collisionObject.internalSetTemporaryCollisionShape((CollisionShape)childInfo.getCollisionShape());
                    collisionObject.setUserPointer(childInfo.getUserData());
                    rayTestSingle(rayFromTrans, rayToTrans,
                            collisionObject,
                            (CollisionShape) childInfo.getCollisionShape(),
                            childTransform,
                            resultCallback);
                    // restore
                    collisionObject.internalSetTemporaryCollisionShape(saveCollisionShape);
                    // TODO: Need an early out if hit
                }

                if (tNextX < tNextY) {
                    if (tNextX < tNextZ) {
                        currentVoxX += xIncrement;
                        t = tNextX;
                        tNextX += invDx;
                    } else {
                        currentVoxZ += zIncrement;
                        t = tNextZ;
                        tNextZ += invDz;
                    }
                } else {
                    if (tNextY < tNextZ) {
                        currentVoxY += yIncrement;
                        t = tNextY;
                        tNextY += invDy;
                    } else {
                        currentVoxZ += zIncrement;
                        t = tNextZ;
                        tNextZ += invDz;
                    }
                }
            }
        } else if (collisionShape.isCompound()) {
            // todo: use AABB tree or other BVH acceleration structure!
            CompoundShape compoundShape = (CompoundShape) collisionShape;
            int i = 0;
            Transform childTrans = new Transform();
            for (i = 0; i < compoundShape.getNumChildShapes(); i++) {
                compoundShape.getChildTransform(i, childTrans);
                CollisionShape childCollisionShape = compoundShape.getChildShape(i);
                Transform childWorldTrans = new Transform(colObjWorldTransform);
                childWorldTrans.mul(childTrans);
                // replace collision shape so that callback can determine the triangle
                CollisionShape saveCollisionShape = collisionObject.getCollisionShape();
                collisionObject.internalSetTemporaryCollisionShape(childCollisionShape);
                rayTestSingle(rayFromTrans, rayToTrans,
                        collisionObject,
                        childCollisionShape,
                        childWorldTrans,
                        resultCallback);
                // restore
                collisionObject.internalSetTemporaryCollisionShape(saveCollisionShape);
            }
        }
    }

    private static class BridgeTriangleConvexcastCallback extends TriangleConvexcastCallback {
        public ConvexResultCallback resultCallback;
        public CollisionObject collisionObject;
        public TriangleMeshShape triangleMesh;
        public boolean normalInWorldSpace;

        public BridgeTriangleConvexcastCallback(ConvexShape castShape, Transform from, Transform to, ConvexResultCallback resultCallback, CollisionObject collisionObject, TriangleMeshShape triangleMesh, Transform triangleToWorld) {
            super(castShape, from, to, triangleToWorld, triangleMesh.getMargin());
            this.resultCallback = resultCallback;
            this.collisionObject = collisionObject;
            this.triangleMesh = triangleMesh;
        }

        @Override
        public float reportHit(Vector3f hitNormalLocal, Vector3f hitPointLocal, float hitFraction, int partId, int triangleIndex) {
            LocalShapeInfo shapeInfo = new LocalShapeInfo();
            shapeInfo.shapePart = partId;
            shapeInfo.triangleIndex = triangleIndex;
            if (hitFraction <= resultCallback.closestHitFraction) {
                LocalConvexResult convexResult = new LocalConvexResult(collisionObject, shapeInfo, hitNormalLocal, hitPointLocal, hitFraction);
                return resultCallback.addSingleResult(convexResult, normalInWorldSpace);
            }
            return hitFraction;
        }
    }

    /**
     * objectQuerySingle performs a collision detection query and calls the resultCallback. It is used internally by rayTest.
     */
    public static void objectQuerySingle(ConvexShape castShape, Transform convexFromTrans, Transform convexToTrans, CollisionObject collisionObject, CollisionShape collisionShape, Transform colObjWorldTransform, ConvexResultCallback resultCallback, float allowedPenetration) {
        if (collisionShape.isConvex()) {
            CastResult castResult = new CastResult();
            castResult.allowedPenetration = allowedPenetration;
            castResult.fraction = 1f; // ??

            ConvexShape convexShape = (ConvexShape) collisionShape;
            VoronoiSimplexSolver simplexSolver = new VoronoiSimplexSolver();
            GjkEpaPenetrationDepthSolver gjkEpaPenetrationSolver = new GjkEpaPenetrationDepthSolver();

            // JAVA TODO: should be convexCaster1
            //ContinuousConvexCollision convexCaster1(castShape,convexShape,&simplexSolver,&gjkEpaPenetrationSolver);
            GjkConvexCast convexCaster2 = new GjkConvexCast(castShape, convexShape, simplexSolver);
            //btSubsimplexConvexCast convexCaster3(castShape,convexShape,&simplexSolver);

            ConvexCast castPtr = convexCaster2;

            if (castPtr.calcTimeOfImpact(convexFromTrans, convexToTrans, colObjWorldTransform, colObjWorldTransform, castResult)) {
                // add hit
                if (castResult.normal.lengthSquared() > 0.0001f) {
                    if (castResult.fraction < resultCallback.closestHitFraction) {
                        castResult.normal.normalize();
                        LocalConvexResult localConvexResult = new LocalConvexResult(collisionObject, null, castResult.normal, castResult.hitPoint, castResult.fraction);

                        boolean normalInWorldSpace = true;
                        resultCallback.addSingleResult(localConvexResult, normalInWorldSpace);
                    }
                }
            }
        } else if (collisionShape.isConcave()) {
            if (collisionShape.getShapeType() == BroadphaseNativeType.TRIANGLE_MESH_SHAPE_PROXYTYPE) {
                BvhTriangleMeshShape triangleMesh = (BvhTriangleMeshShape) collisionShape;
                Transform worldTocollisionObject = new Transform();
                worldTocollisionObject.inverse(colObjWorldTransform);

                Vector3f convexFromLocal = new Vector3f();
                convexFromLocal.set(convexFromTrans.origin);
                worldTocollisionObject.transform(convexFromLocal);

                Vector3f convexToLocal = new Vector3f();
                convexToLocal.set(convexToTrans.origin);
                worldTocollisionObject.transform(convexToLocal);

                // rotation of box in local mesh space = MeshRotation^-1 * ConvexToRotation
                Transform rotationXform = new Transform();
                Matrix3f tmpMat = new Matrix3f();
                tmpMat.mul(worldTocollisionObject.basis, convexToTrans.basis);
                rotationXform.set(tmpMat);

                BridgeTriangleConvexcastCallback tccb = new BridgeTriangleConvexcastCallback(castShape, convexFromTrans, convexToTrans, resultCallback, collisionObject, triangleMesh, colObjWorldTransform);
                tccb.hitFraction = resultCallback.closestHitFraction;
                tccb.normalInWorldSpace = true;

                Vector3f boxMinLocal = new Vector3f();
                Vector3f boxMaxLocal = new Vector3f();
                castShape.getAabb(rotationXform, boxMinLocal, boxMaxLocal);
                triangleMesh.performConvexcast(tccb, convexFromLocal, convexToLocal, boxMinLocal, boxMaxLocal);
            } else {
                BvhTriangleMeshShape triangleMesh = (BvhTriangleMeshShape) collisionShape;
                Transform worldTocollisionObject = new Transform();
                worldTocollisionObject.inverse(colObjWorldTransform);

                Vector3f convexFromLocal = new Vector3f();
                convexFromLocal.set(convexFromTrans.origin);
                worldTocollisionObject.transform(convexFromLocal);

                Vector3f convexToLocal = new Vector3f();
                convexToLocal.set(convexToTrans.origin);
                worldTocollisionObject.transform(convexToLocal);

                // rotation of box in local mesh space = MeshRotation^-1 * ConvexToRotation
                Transform rotationXform = new Transform();
                Matrix3f tmpMat = new Matrix3f();
                tmpMat.mul(worldTocollisionObject.basis, convexToTrans.basis);
                rotationXform.set(tmpMat);

                BridgeTriangleConvexcastCallback tccb = new BridgeTriangleConvexcastCallback(castShape, convexFromTrans, convexToTrans, resultCallback, collisionObject, triangleMesh, colObjWorldTransform);
                tccb.hitFraction = resultCallback.closestHitFraction;
                tccb.normalInWorldSpace = false;
                Vector3f boxMinLocal = new Vector3f();
                Vector3f boxMaxLocal = new Vector3f();
                castShape.getAabb(rotationXform, boxMinLocal, boxMaxLocal);

                Vector3f rayAabbMinLocal = new Vector3f(convexFromLocal);
                VectorUtil.setMin(rayAabbMinLocal, convexToLocal);
                Vector3f rayAabbMaxLocal = new Vector3f(convexFromLocal);
                VectorUtil.setMax(rayAabbMaxLocal, convexToLocal);
                rayAabbMinLocal.add(boxMinLocal);
                rayAabbMaxLocal.add(boxMaxLocal);
                triangleMesh.processAllTriangles(tccb, rayAabbMinLocal, rayAabbMaxLocal);
            }
        } else if (collisionShape.isVoxelWorld()) {
            JBulletVoxelWorldShape worldShape = (JBulletVoxelWorldShape) collisionShape;
            // TODO: Replace with AABB sweep.
            Vector3f minAABB1 = new Vector3f();
            Vector3f maxAABB1 = new Vector3f();
            Vector3f minAABB2 = new Vector3f();
            Vector3f maxAABB2 = new Vector3f();
            castShape.getAabb(convexFromTrans, minAABB1, maxAABB1);
            castShape.getAabb(convexToTrans, minAABB2, maxAABB2);
            Tuple3i min = new Point3i();
            min.x = IntUtil.floorToInt(FastMath.min(minAABB1.x, minAABB2.x) + 0.5f);
            min.y = IntUtil.floorToInt(FastMath.min(minAABB1.y, minAABB2.y) + 0.5f);
            min.z = IntUtil.floorToInt(FastMath.min(minAABB1.z, minAABB2.z) + 0.5f);
            Tuple3i max = new Point3i();
            max.x = IntUtil.floorToInt(FastMath.max(maxAABB1.x, maxAABB2.x) + 0.5f);
            max.y = IntUtil.floorToInt(FastMath.max(maxAABB1.y, maxAABB2.y) + 0.5f);
            max.z = IntUtil.floorToInt(FastMath.max(maxAABB1.z, maxAABB2.z) + 0.5f);

            for (int x = min.x; x <= max.x; ++x) {
                for (int y = min.y; y <= max.y; ++y) {
                    for (int z = min.z; z <= max.z; ++z) {
                        VoxelInfo childInfo = worldShape.getWorld().getCollisionShapeAt(x, y, z);
                        if (!childInfo.isBlocking()) {
                            continue;
                        }
                        Vector3f pos = new Vector3f();
                        pos.set(x, y, z);
                        pos.add((Vector3f)childInfo.getCollisionOffset());
                        Transform childTrans = new Transform(new Matrix4f(IDENTITY_MAT3F, pos, 1.0f));
                        // replace collision shape so that callback can determine the triangle
                        CollisionShape saveCollisionShape = collisionObject.getCollisionShape();
                        collisionObject.internalSetTemporaryCollisionShape((CollisionShape)childInfo.getCollisionShape());
                        collisionObject.setUserPointer(childInfo.getUserData());
                        objectQuerySingle(castShape, convexFromTrans, convexToTrans,
                                collisionObject,
                                (CollisionShape)childInfo.getCollisionShape(),
                                childTrans,
                                resultCallback, allowedPenetration);
                        // restore
                        collisionObject.internalSetTemporaryCollisionShape(saveCollisionShape);
                    }
                }
            }
        } else if (collisionShape.isCompound()) {
            // todo: use AABB tree or other BVH acceleration structure!
            CompoundShape compoundShape = (CompoundShape) collisionShape;
            for (int i = 0; i < compoundShape.getNumChildShapes(); i++) {
                Transform childTrans = compoundShape.getChildTransform(i, new Transform());
                CollisionShape childCollisionShape = compoundShape.getChildShape(i);
                Transform childWorldTrans = new Transform();
                childWorldTrans.mul(colObjWorldTransform, childTrans);
                // replace collision shape so that callback can determine the triangle
                CollisionShape saveCollisionShape = collisionObject.getCollisionShape();
                collisionObject.internalSetTemporaryCollisionShape(childCollisionShape);
                objectQuerySingle(castShape, convexFromTrans, convexToTrans,
                        collisionObject,
                        childCollisionShape,
                        childWorldTrans,
                        resultCallback, allowedPenetration);
                // restore
                collisionObject.internalSetTemporaryCollisionShape(saveCollisionShape);
            }
        }
    }

    /**
     * rayTest performs a raycast on all objects in the CollisionWorld, and calls the resultCallback.
     * This allows for several queries: first hit, all hits, any hit, dependent on the value returned by the callback.
     */
    public void rayTest(Vector3f rayFromWorld, Vector3f rayToWorld, RayResultCallback resultCallback) {
        Transform rayFromTrans = new Transform(), rayToTrans = new Transform();
        rayFromTrans.setIdentity();
        rayFromTrans.origin.set(rayFromWorld);
        rayToTrans.setIdentity();

        rayToTrans.origin.set(rayToWorld);

        // go over all objects, and if the ray intersects their aabb, do a ray-shape query using convexCaster (CCD)
        Vector3f collisionObjectAabbMin = new Vector3f(), collisionObjectAabbMax = new Vector3f();
        float[] hitLambda = new float[1];

        Transform tmpTrans = new Transform();

        for (int i = 0; i < collisionObjects.size(); i++) {
            // terminate further ray tests, once the closestHitFraction reached zero
            if (resultCallback.closestHitFraction == 0f) {
                break;
            }

            CollisionObject collisionObject = collisionObjects.getQuick(i);
            // only perform raycast if filterMask matches
            if (resultCallback.needsCollision(collisionObject.getBroadphaseHandle())) {
                //RigidcollisionObject* collisionObject = ctrl->GetRigidcollisionObject();
                collisionObject.getCollisionShape().getAabb(collisionObject.getWorldTransform(tmpTrans), collisionObjectAabbMin, collisionObjectAabbMax);

                hitLambda[0] = resultCallback.closestHitFraction;
                Vector3f hitNormal = new Vector3f();
                if (AabbUtil2.rayAabb(rayFromWorld, rayToWorld, collisionObjectAabbMin, collisionObjectAabbMax, hitLambda, hitNormal)) {
                    rayTestSingle(rayFromTrans, rayToTrans,
                            collisionObject,
                            collisionObject.getCollisionShape(),
                            collisionObject.getWorldTransform(tmpTrans),
                            resultCallback);
                }
            }

        }
    }

    /**
     * convexTest performs a swept convex cast on all objects in the {@link CollisionWorld}, and calls the resultCallback
     * This allows for several queries: first hit, all hits, any hit, dependent on the value return by the callback.
     */
    public void convexSweepTest(ConvexShape castShape, Transform convexFromWorld, Transform convexToWorld, ConvexResultCallback resultCallback) {
        Transform convexFromTrans = new Transform();
        Transform convexToTrans = new Transform();

        convexFromTrans.set(convexFromWorld);
        convexToTrans.set(convexToWorld);

        Vector3f castShapeAabbMin = new Vector3f();
        Vector3f castShapeAabbMax = new Vector3f();

        // Compute AABB that encompasses angular movement
        {
            Vector3f linVel = new Vector3f();
            Vector3f angVel = new Vector3f();
            TransformUtil.calculateVelocity(convexFromTrans, convexToTrans, 1f, linVel, angVel);
            Transform R = new Transform();
            R.setIdentity();
            R.setRotation(convexFromTrans.getRotation(new Quat4f()));
            castShape.calculateTemporalAabb(R, linVel, angVel, 1f, castShapeAabbMin, castShapeAabbMax);
        }

        Transform tmpTrans = new Transform();
        Vector3f collisionObjectAabbMin = new Vector3f();
        Vector3f collisionObjectAabbMax = new Vector3f();
        float[] hitLambda = new float[1];

        // go over all objects, and if the ray intersects their aabb + cast shape aabb,
        // do a ray-shape query using convexCaster (CCD)
        for (int i = 0; i < collisionObjects.size(); i++) {
            CollisionObject collisionObject = collisionObjects.getQuick(i);

            // only perform raycast if filterMask matches
            if (resultCallback.needsCollision(collisionObject.getBroadphaseHandle())) {
                //RigidcollisionObject* collisionObject = ctrl->GetRigidcollisionObject();
                collisionObject.getWorldTransform(tmpTrans);
                collisionObject.getCollisionShape().getAabb(tmpTrans, collisionObjectAabbMin, collisionObjectAabbMax);
                AabbUtil2.aabbExpand(collisionObjectAabbMin, collisionObjectAabbMax, castShapeAabbMin, castShapeAabbMax);
                hitLambda[0] = 1f; // could use resultCallback.closestHitFraction, but needs testing
                Vector3f hitNormal = new Vector3f();
                if (AabbUtil2.rayAabb(convexFromWorld.origin, convexToWorld.origin, collisionObjectAabbMin, collisionObjectAabbMax, hitLambda, hitNormal)) {
                    objectQuerySingle(castShape, convexFromTrans, convexToTrans,
                            collisionObject,
                            collisionObject.getCollisionShape(),
                            tmpTrans,
                            resultCallback,
                            getDispatchInfo().allowedCcdPenetration);
                }
            }
        }
    }

    public ObjectArrayList<CollisionObject> getCollisionObjectArray() {
        return collisionObjects;
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * LocalShapeInfo gives extra information for complex shapes.
     * Currently, only btTriangleMeshShape is available, so it just contains triangleIndex and subpart.
     */
    public static class LocalShapeInfo {
        public int shapePart;
        public int triangleIndex;
        //const btCollisionShape*	m_shapeTemp;
        //const btTransform*	m_shapeLocalTransform;
    }

    public static class LocalRayResult {
        public CollisionObject collisionObject;
        public LocalShapeInfo localShapeInfo;
        public final Vector3f hitNormalLocal = new Vector3f();
        public float hitFraction;

        public LocalRayResult(CollisionObject collisionObject, LocalShapeInfo localShapeInfo, Vector3f hitNormalLocal, float hitFraction) {
            this.collisionObject = collisionObject;
            this.localShapeInfo = localShapeInfo;
            this.hitNormalLocal.set(hitNormalLocal);
            this.hitFraction = hitFraction;
        }
    }

    /**
     * RayResultCallback is used to report new raycast results.
     */
    public static abstract class RayResultCallback {
        public float closestHitFraction = 1f;
        public CollisionObject collisionObject;
        public short collisionFilterGroup = CollisionFilterGroups.DEFAULT_FILTER;
        public short collisionFilterMask = CollisionFilterGroups.ALL_FILTER;

        public boolean hasHit() {
            return (collisionObject != null);
        }

        public boolean needsCollision(BroadphaseProxy proxy0) {
            boolean collides = ((proxy0.collisionFilterGroup & collisionFilterMask) & 0xFFFF) != 0;
            collides = collides && ((collisionFilterGroup & proxy0.collisionFilterMask) & 0xFFFF) != 0;
            return collides;
        }

        public abstract float addSingleResult(LocalRayResult rayResult, boolean normalInWorldSpace);
    }

    public static class ClosestRayResultCallback extends RayResultCallback {
        public final Vector3f rayFromWorld = new Vector3f(); //used to calculate hitPointWorld from hitFraction
        public final Vector3f rayToWorld = new Vector3f();

        public final Vector3f hitNormalWorld = new Vector3f();
        public final Vector3f hitPointWorld = new Vector3f();

        public ClosestRayResultCallback(Vector3f rayFromWorld, Vector3f rayToWorld) {
            this.rayFromWorld.set(rayFromWorld);
            this.rayToWorld.set(rayToWorld);
        }

        @Override
        public float addSingleResult(LocalRayResult rayResult, boolean normalInWorldSpace) {
            // caller already does the filter on the closestHitFraction
            assert (rayResult.hitFraction <= closestHitFraction);

            closestHitFraction = rayResult.hitFraction;
            collisionObject = rayResult.collisionObject;
            if (normalInWorldSpace) {
                hitNormalWorld.set(rayResult.hitNormalLocal);
            } else {
                // need to transform normal into worldspace
                hitNormalWorld.set(rayResult.hitNormalLocal);
                collisionObject.getWorldTransform(new Transform()).basis.transform(hitNormalWorld);
            }

            VectorUtil.setInterpolate3(hitPointWorld, rayFromWorld, rayToWorld, rayResult.hitFraction);
            return rayResult.hitFraction;
        }
    }

    public static class ClosestRayResultWithUserDataCallback extends RayResultCallback {
        public final Vector3f rayFromWorld = new Vector3f(); //used to calculate hitPointWorld from hitFraction
        public final Vector3f rayToWorld = new Vector3f();

        public final Vector3f hitNormalWorld = new Vector3f();
        public final Vector3f hitPointWorld = new Vector3f();

        public Object userData = null;

        public ClosestRayResultWithUserDataCallback(Vector3f rayFromWorld, Vector3f rayToWorld) {
            this.rayFromWorld.set(rayFromWorld);
            this.rayToWorld.set(rayToWorld);
        }

        @Override
        public float addSingleResult(LocalRayResult rayResult, boolean normalInWorldSpace) {
            // caller already does the filter on the closestHitFraction
            assert (rayResult.hitFraction <= closestHitFraction);

            closestHitFraction = rayResult.hitFraction;
            collisionObject = rayResult.collisionObject;
            userData = collisionObject.getUserPointer();
            if (normalInWorldSpace) {
                hitNormalWorld.set(rayResult.hitNormalLocal);
            } else {
                // need to transform normal into worldspace
                hitNormalWorld.set(rayResult.hitNormalLocal);
                collisionObject.getWorldTransform(new Transform()).basis.transform(hitNormalWorld);
            }

            VectorUtil.setInterpolate3(hitPointWorld, rayFromWorld, rayToWorld, rayResult.hitFraction);
            return rayResult.hitFraction;
        }
    }

    public static class LocalConvexResult {
        public CollisionObject hitCollisionObject;
        public LocalShapeInfo localShapeInfo;
        public final Vector3f hitNormalLocal = new Vector3f();
        public final Vector3f hitPointLocal = new Vector3f();
        public float hitFraction;

        public LocalConvexResult(CollisionObject hitCollisionObject, LocalShapeInfo localShapeInfo, Vector3f hitNormalLocal, Vector3f hitPointLocal, float hitFraction) {
            this.hitCollisionObject = hitCollisionObject;
            this.localShapeInfo = localShapeInfo;
            this.hitNormalLocal.set(hitNormalLocal);
            this.hitPointLocal.set(hitPointLocal);
            this.hitFraction = hitFraction;
        }
    }

    public static abstract class ConvexResultCallback {
        public float closestHitFraction = 1f;
        public short collisionFilterGroup = CollisionFilterGroups.DEFAULT_FILTER;
        public short collisionFilterMask = CollisionFilterGroups.ALL_FILTER;

        public boolean hasHit() {
            return (closestHitFraction < 1f);
        }

        public boolean needsCollision(BroadphaseProxy proxy0) {
            boolean collides = ((proxy0.collisionFilterGroup & collisionFilterMask) & 0xFFFF) != 0;
            collides = collides && ((collisionFilterGroup & proxy0.collisionFilterMask) & 0xFFFF) != 0;
            return collides;
        }

        public abstract float addSingleResult(LocalConvexResult convexResult, boolean normalInWorldSpace);
    }

    public static class ClosestConvexResultCallback extends ConvexResultCallback {
        public final Vector3f convexFromWorld = new Vector3f(); // used to calculate hitPointWorld from hitFraction
        public final Vector3f convexToWorld = new Vector3f();
        public final Vector3f hitNormalWorld = new Vector3f();
        public final Vector3f hitPointWorld = new Vector3f();
        public CollisionObject hitCollisionObject;

        public ClosestConvexResultCallback(Vector3f convexFromWorld, Vector3f convexToWorld) {
            this.convexFromWorld.set(convexFromWorld);
            this.convexToWorld.set(convexToWorld);
            this.hitCollisionObject = null;
        }

        @Override
        public float addSingleResult(LocalConvexResult convexResult, boolean normalInWorldSpace) {
            // caller already does the filter on the m_closestHitFraction
            assert (convexResult.hitFraction <= closestHitFraction);

            closestHitFraction = convexResult.hitFraction;
            hitCollisionObject = convexResult.hitCollisionObject;
            if (normalInWorldSpace) {
                hitNormalWorld.set(convexResult.hitNormalLocal);
                if (hitNormalWorld.length() > 2) {
                    System.out.println("CollisionWorld.addSingleResult world " + hitNormalWorld);
                }
            } else {
                // need to transform normal into worldspace
                hitNormalWorld.set(convexResult.hitNormalLocal);
                hitCollisionObject.getWorldTransform(new Transform()).basis.transform(hitNormalWorld);
                if (hitNormalWorld.length() > 2) {
                    System.out.println("CollisionWorld.addSingleResult world " + hitNormalWorld);
                }
            }

            hitPointWorld.set(convexResult.hitPointLocal);
            return convexResult.hitFraction;
        }
    }

    private static class BridgeTriangleRaycastCallback extends TriangleRaycastCallback {
        public RayResultCallback resultCallback;
        public CollisionObject collisionObject;
        public ConcaveShape triangleMesh;

        public BridgeTriangleRaycastCallback(Vector3f from, Vector3f to, RayResultCallback resultCallback, CollisionObject collisionObject, ConcaveShape triangleMesh) {
            super(from, to);
            this.resultCallback = resultCallback;
            this.collisionObject = collisionObject;
            this.triangleMesh = triangleMesh;
        }

        public float reportHit(Vector3f hitNormalLocal, float hitFraction, int partId, int triangleIndex) {
            LocalShapeInfo shapeInfo = new LocalShapeInfo();
            shapeInfo.shapePart = partId;
            shapeInfo.triangleIndex = triangleIndex;

            LocalRayResult rayResult = new LocalRayResult(collisionObject, shapeInfo, hitNormalLocal, hitFraction);

            boolean normalInWorldSpace = false;
            return resultCallback.addSingleResult(rayResult, normalInWorldSpace);
        }
    }

}
