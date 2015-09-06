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

package com.bulletphysics.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Object pool.
 *
 * @author jezek2
 */
public class ObjectPool<T> {

    private Class<T> cls;
    private ObjectArrayList<T> list = new ObjectArrayList<T>();

    public ObjectPool(Class<T> cls) {
        this.cls = cls;
    }

    private T create() {
        try {
            return cls.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns instance from pool, or create one if pool is empty.
     *
     * @return instance
     */
    public T get() {
        if (list.size() > 0) {
            return list.remove(list.size() - 1);
        } else {
            return create();
        }
    }

    /**
     * Release instance into pool.
     *
     * @param obj previously obtained instance from pool
     */
    public void release(T obj) {
        list.add(obj);
    }

    ////////////////////////////////////////////////////////////////////////////

    private static ThreadLocal<Map> threadLocal = new ThreadLocal<Map>() {
        @Override
        protected Map initialValue() {
            return new HashMap();
        }
    };

    /**
     * Returns per-thread object pool for given type, or create one if it doesn't exist.
     *
     * @param cls type
     * @return object pool
     */
    @SuppressWarnings("unchecked")
    public static <T> ObjectPool<T> get(Class<T> cls) {
        Map map = threadLocal.get();

        ObjectPool<T> pool = (ObjectPool<T>) map.get(cls);
        if (pool == null) {
            pool = new ObjectPool<T>(cls);
            map.put(cls, pool);
        }

        return pool;
    }

    public static void cleanCurrentThread() {
        threadLocal.remove();
    }

}
