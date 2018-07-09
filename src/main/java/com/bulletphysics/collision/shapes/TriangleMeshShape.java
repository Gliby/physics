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

package com.bulletphysics.collision.shapes;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import com.bulletphysics.linearmath.AabbUtil2;
import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.linearmath.VectorUtil;

/**
 * Concave triangle mesh abstract class. Use {@link BvhTriangleMeshShape} as concrete
 * implementation.
 *
 * @author jezek2
 */
public abstract class TriangleMeshShape extends ConcaveShape {

    protected final Vector3f localAabbMin = new Vector3f();
    protected final Vector3f localAabbMax = new Vector3f();
    protected StridingMeshInterface meshInterface;

    /**
     * TriangleMeshShape constructor has been disabled/protected, so that users will not mistakenly use this class.
     * Don't use btTriangleMeshShape but use btBvhTriangleMeshShape instead!
     */
    protected TriangleMeshShape(StridingMeshInterface meshInterface) {
        this.meshInterface = meshInterface;

        // JAVA NOTE: moved to BvhTriangleMeshShape
        //recalcLocalAabb();
    }

    public Vector3f localGetSupportingVertex(Vector3f vec, Vector3f out) {
        Vector3f tmp = new Vector3f();

        Vector3f supportVertex = out;

        Transform ident = new Transform();
        ident.setIdentity();

        SupportVertexCallback supportCallback = new SupportVertexCallback(vec, ident);

        Vector3f aabbMax = new Vector3f();
        aabbMax.set(1e30f, 1e30f, 1e30f);
        tmp.negate(aabbMax);

        processAllTriangles(supportCallback, tmp, aabbMax);

        supportCallback.getSupportVertexLocal(supportVertex);

        return out;
    }

    public Vector3f localGetSupportingVertexWithoutMargin(Vector3f vec, Vector3f out) {
        assert (false);
        return localGetSupportingVertex(vec, out);
    }

    public void recalcLocalAabb() {
        for (int i = 0; i < 3; i++) {
            Vector3f vec = new Vector3f();
            vec.set(0f, 0f, 0f);
            VectorUtil.setCoord(vec, i, 1f);
            Vector3f tmp = localGetSupportingVertex(vec, new Vector3f());
            VectorUtil.setCoord(localAabbMax, i, VectorUtil.getCoord(tmp, i) + collisionMargin);
            VectorUtil.setCoord(vec, i, -1f);
            localGetSupportingVertex(vec, tmp);
            VectorUtil.setCoord(localAabbMin, i, VectorUtil.getCoord(tmp, i) - collisionMargin);
        }
    }

    @Override
    public void getAabb(Transform trans, Vector3f aabbMin, Vector3f aabbMax) {
        Vector3f tmp = new Vector3f();

        Vector3f localHalfExtents = new Vector3f();
        localHalfExtents.sub(localAabbMax, localAabbMin);
        localHalfExtents.scale(0.5f);

        Vector3f localCenter = new Vector3f();
        localCenter.add(localAabbMax, localAabbMin);
        localCenter.scale(0.5f);

        Matrix3f abs_b = new Matrix3f(trans.basis);
        MatrixUtil.absolute(abs_b);

        Vector3f center = new Vector3f(localCenter);
        trans.transform(center);

        Vector3f extent = new Vector3f();
        abs_b.getRow(0, tmp);
        extent.x = tmp.dot(localHalfExtents);
        abs_b.getRow(1, tmp);
        extent.y = tmp.dot(localHalfExtents);
        abs_b.getRow(2, tmp);
        extent.z = tmp.dot(localHalfExtents);

        Vector3f margin = new Vector3f();
        margin.set(getMargin(), getMargin(), getMargin());
        extent.add(margin);

        aabbMin.sub(center, extent);
        aabbMax.add(center, extent);
    }

    @Override
    public void processAllTriangles(TriangleCallback callback, Vector3f aabbMin, Vector3f aabbMax) {
        FilteredCallback filterCallback = new FilteredCallback(callback, aabbMin, aabbMax);

        meshInterface.internalProcessAllTriangles(filterCallback, aabbMin, aabbMax);
    }

    @Override
    public void calculateLocalInertia(float mass, Vector3f inertia) {
        // moving concave objects not supported
        assert (false);
        inertia.set(0f, 0f, 0f);
    }


    @Override
    public void setLocalScaling(Vector3f scaling) {
        meshInterface.setScaling(scaling);
        recalcLocalAabb();
    }

    @Override
    public Vector3f getLocalScaling(Vector3f out) {
        return meshInterface.getScaling(out);
    }

    public StridingMeshInterface getMeshInterface() {
        return meshInterface;
    }

    public Vector3f getLocalAabbMin(Vector3f out) {
        out.set(localAabbMin);
        return out;
    }

    public Vector3f getLocalAabbMax(Vector3f out) {
        out.set(localAabbMax);
        return out;
    }

    @Override
    public String getName() {
        return "TRIANGLEMESH";
    }

    ////////////////////////////////////////////////////////////////////////////

    private class SupportVertexCallback extends TriangleCallback {
        private final Vector3f supportVertexLocal = new Vector3f(0f, 0f, 0f);
        public final Transform worldTrans = new Transform();
        public float maxDot = -1e30f;
        public final Vector3f supportVecLocal = new Vector3f();

        public SupportVertexCallback(Vector3f supportVecWorld, Transform trans) {
            this.worldTrans.set(trans);
            MatrixUtil.transposeTransform(supportVecLocal, supportVecWorld, worldTrans.basis);
        }

        public void processTriangle(Vector3f[] triangle, int partId, int triangleIndex) {
            for (int i = 0; i < 3; i++) {
                float dot = supportVecLocal.dot(triangle[i]);
                if (dot > maxDot) {
                    maxDot = dot;
                    supportVertexLocal.set(triangle[i]);
                }
            }
        }

        public Vector3f getSupportVertexWorldSpace(Vector3f out) {
            out.set(supportVertexLocal);
            worldTrans.transform(out);
            return out;
        }

        public Vector3f getSupportVertexLocal(Vector3f out) {
            out.set(supportVertexLocal);
            return out;
        }
    }

    private static class FilteredCallback extends InternalTriangleIndexCallback {
        public TriangleCallback callback;
        public final Vector3f aabbMin = new Vector3f();
        public final Vector3f aabbMax = new Vector3f();

        public FilteredCallback(TriangleCallback callback, Vector3f aabbMin, Vector3f aabbMax) {
            this.callback = callback;
            this.aabbMin.set(aabbMin);
            this.aabbMax.set(aabbMax);
        }

        public void internalProcessTriangleIndex(Vector3f[] triangle, int partId, int triangleIndex) {
            if (AabbUtil2.testTriangleAgainstAabb2(triangle, aabbMin, aabbMax)) {
                // check aabb in triangle-space, before doing this
                callback.processTriangle(triangle, partId, triangleIndex);
            }
        }
    }

}
