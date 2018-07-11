/*
 * Voxel world extension (c) 2012 Steven Brooker <immortius@gmail.com>
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

package com.bulletphysicsx.collision.shapes.voxel;

import javax.vecmath.Vector3f;

import com.bulletphysicsx.collision.broadphase.BroadphaseNativeType;
import com.bulletphysicsx.collision.shapes.CollisionShape;
import com.bulletphysicsx.linearmath.Transform;

/**
 * @author Immortius
 */
public class JBulletVoxelWorldShape extends CollisionShape {
    public static final int AABB_SIZE = Integer.MAX_VALUE;
    private VoxelPhysicsWorld world;

    private float collisionMargin = 0f;
    protected final Vector3f localScaling = new Vector3f(1f, 1f, 1f);

    public JBulletVoxelWorldShape(VoxelPhysicsWorld world) {
        this.world = world;
    }

    /**
     * getAabb's default implementation is brute force, expected derived classes to implement a fast dedicated version.
     */
    @Override
    public void getAabb(Transform trans, Vector3f aabbMin, Vector3f aabbMax) {
        aabbMin.set(-AABB_SIZE, -AABB_SIZE, -AABB_SIZE);
        aabbMax.set(AABB_SIZE, AABB_SIZE, AABB_SIZE);
    }

    @Override
    public void setLocalScaling(Vector3f scaling) {
        localScaling.set(scaling);
    }

    @Override
    public Vector3f getLocalScaling(Vector3f out) {
        out.set(localScaling);
        return out;
    }

    @Override
    public void calculateLocalInertia(float mass, Vector3f inertia) {
        inertia.set(0, 0, 0);
    }

    @Override
    public BroadphaseNativeType getShapeType() {
        return BroadphaseNativeType.VOXEL_WORLD_PROXYTYPE;
    }

    @Override
    public void setMargin(float margin) {
        collisionMargin = margin;
    }

    @Override
    public float getMargin() {
        return collisionMargin;
    }

    @Override
    public String getName() {
        return "World";
    }

    public VoxelPhysicsWorld getWorld() {
        return world;
    }
}
