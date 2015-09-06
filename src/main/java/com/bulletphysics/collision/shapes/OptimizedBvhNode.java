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

import javax.vecmath.Vector3f;
import java.io.Serializable;

/**
 * OptimizedBvhNode contains both internal and leaf node information.
 *
 * @author jezek2
 */
public class OptimizedBvhNode implements Serializable {

    private static final long serialVersionUID = 1L;

    public final Vector3f aabbMinOrg = new Vector3f();
    public final Vector3f aabbMaxOrg = new Vector3f();

    public int escapeIndex;

    // for child nodes
    public int subPart;
    public int triangleIndex;

    public void set(OptimizedBvhNode n) {
        aabbMinOrg.set(n.aabbMinOrg);
        aabbMaxOrg.set(n.aabbMaxOrg);
        escapeIndex = n.escapeIndex;
        subPart = n.subPart;
        triangleIndex = n.triangleIndex;
    }

}
