/*
 * Java port of Bullet (c) 2008 Martin Dvorak <jezek2@advel.cz>
 *
 * This source file is part of GIMPACT Library.
 *
 * For the latest info, see http://gimpact.sourceforge.net/
 *
 * Copyright (c) 2007 Francisco Leon Najera. C.C. 80087371.
 * email: projectileman@yahoo.com
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

package com.bulletphysicsx.extras.gimpact;

import javax.vecmath.Vector3f;

import com.bulletphysicsx.linearmath.VectorUtil;

/**
 * @author jezek2
 */
class Quantization {

    public static void bt_calc_quantization_parameters(Vector3f outMinBound, Vector3f outMaxBound, Vector3f bvhQuantization, Vector3f srcMinBound, Vector3f srcMaxBound, float quantizationMargin) {
        // enlarge the AABB to avoid division by zero when initializing the quantization values
        Vector3f clampValue = new Vector3f();
        clampValue.set(quantizationMargin, quantizationMargin, quantizationMargin);
        outMinBound.sub(srcMinBound, clampValue);
        outMaxBound.add(srcMaxBound, clampValue);
        Vector3f aabbSize = new Vector3f();
        aabbSize.sub(outMaxBound, outMinBound);
        bvhQuantization.set(65535.0f, 65535.0f, 65535.0f);
        VectorUtil.div(bvhQuantization, bvhQuantization, aabbSize);
    }

    public static void bt_quantize_clamp(short[] out, Vector3f point, Vector3f min_bound, Vector3f max_bound, Vector3f bvhQuantization) {
        Vector3f clampedPoint = new Vector3f(point);
        VectorUtil.setMax(clampedPoint, min_bound);
        VectorUtil.setMin(clampedPoint, max_bound);

        Vector3f v = new Vector3f();
        v.sub(clampedPoint, min_bound);
        VectorUtil.mul(v, v, bvhQuantization);

        out[0] = (short) (v.x + 0.5f);
        out[1] = (short) (v.y + 0.5f);
        out[2] = (short) (v.z + 0.5f);
    }

    public static Vector3f bt_unquantize(short[] vecIn, Vector3f offset, Vector3f bvhQuantization, Vector3f out) {
        out.set((float) (vecIn[0] & 0xFFFF) / (bvhQuantization.x),
                (float) (vecIn[1] & 0xFFFF) / (bvhQuantization.y),
                (float) (vecIn[2] & 0xFFFF) / (bvhQuantization.z));
        out.add(offset);
        return out;
    }

}
