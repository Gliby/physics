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

package com.bulletphysicsx;

import com.bulletphysicsx.util.ArrayPool;
import com.bulletphysicsx.util.ObjectPool;

/**
 * Bullet global settings and constants.
 *
 * @author jezek2
 */
public class BulletGlobals {

    public static final boolean DEBUG = false;

    public static final float CONVEX_DISTANCE_MARGIN = 0.04f;
    public static final float FLT_EPSILON = 1.19209290e-07f;
    public static final float SIMD_EPSILON = FLT_EPSILON;

    public static final float SIMD_2_PI = 6.283185307179586232f;
    public static final float SIMD_PI = SIMD_2_PI * 0.5f;
    public static final float SIMD_HALF_PI = SIMD_2_PI * 0.25f;
    public static final float SIMD_RADS_PER_DEG = SIMD_2_PI / 360f;
    public static final float SIMD_DEGS_PER_RAD = 360f / SIMD_2_PI;
    public static final float SIMD_INFINITY = Float.MAX_VALUE;

    ////////////////////////////////////////////////////////////////////////////

    private static ThreadLocal<BulletGlobals> threadLocal = new ThreadLocal<BulletGlobals>() {
        @Override
        protected BulletGlobals initialValue() {
            return new BulletGlobals();
        }
    };

    private ContactDestroyedCallback gContactDestroyedCallback;
    private ContactAddedCallback gContactAddedCallback;
    private ContactProcessedCallback gContactProcessedCallback;

    private float contactBreakingThreshold = 0.02f;
    // RigidBody
    private float deactivationTime = 2f;
    private boolean disableDeactivation = false;

    public static ContactAddedCallback getContactAddedCallback() {
        return threadLocal.get().gContactAddedCallback;
    }

    public static void setContactAddedCallback(ContactAddedCallback callback) {
        threadLocal.get().gContactAddedCallback = callback;
    }

    public static ContactDestroyedCallback getContactDestroyedCallback() {
        return threadLocal.get().gContactDestroyedCallback;
    }

    public static void setContactDestroyedCallback(ContactDestroyedCallback callback) {
        threadLocal.get().gContactDestroyedCallback = callback;
    }

    public static ContactProcessedCallback getContactProcessedCallback() {
        return threadLocal.get().gContactProcessedCallback;
    }

    public static void setContactProcessedCallback(ContactProcessedCallback callback) {
        threadLocal.get().gContactProcessedCallback = callback;
    }

    ////////////////////////////////////////////////////////////////////////////

    public static float getContactBreakingThreshold() {
        return threadLocal.get().contactBreakingThreshold;
    }

    public static void setContactBreakingThreshold(float threshold) {
        threadLocal.get().contactBreakingThreshold = threshold;
    }

    public static float getDeactivationTime() {
        return threadLocal.get().deactivationTime;
    }

    public static void setDeactivationTime(float time) {
        threadLocal.get().deactivationTime = time;
    }

    public static boolean isDeactivationDisabled() {
        return threadLocal.get().disableDeactivation;
    }

    public static void setDeactivationDisabled(boolean disable) {
        threadLocal.get().disableDeactivation = disable;
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * Cleans all current thread specific settings and caches.
     */
    public static void cleanCurrentThread() {
        threadLocal.remove();
        ObjectPool.cleanCurrentThread();
        ArrayPool.cleanCurrentThread();
    }

}
