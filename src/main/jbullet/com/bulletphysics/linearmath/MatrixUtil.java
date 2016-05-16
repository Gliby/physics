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

package com.bulletphysics.linearmath;

import javax.vecmath.Matrix3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.apache.commons.math3.util.FastMath;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.util.ArrayPool;

/**
 * Utility functions for matrices.
 *
 * @author jezek2
 */
public class MatrixUtil {

	public static void scale(Matrix3f dest, Matrix3f mat, Vector3f s) {
		dest.m00 = mat.m00 * s.x;
		dest.m01 = mat.m01 * s.y;
		dest.m02 = mat.m02 * s.z;
		dest.m10 = mat.m10 * s.x;
		dest.m11 = mat.m11 * s.y;
		dest.m12 = mat.m12 * s.z;
		dest.m20 = mat.m20 * s.x;
		dest.m21 = mat.m21 * s.y;
		dest.m22 = mat.m22 * s.z;
	}

	public static void absolute(Matrix3f mat) {
		mat.m00 = Math.abs(mat.m00);
		mat.m01 = Math.abs(mat.m01);
		mat.m02 = Math.abs(mat.m02);
		mat.m10 = Math.abs(mat.m10);
		mat.m11 = Math.abs(mat.m11);
		mat.m12 = Math.abs(mat.m12);
		mat.m20 = Math.abs(mat.m20);
		mat.m21 = Math.abs(mat.m21);
		mat.m22 = Math.abs(mat.m22);
	}

	public static void setFromOpenGLSubMatrix(Matrix3f mat, float[] m) {
		mat.m00 = m[0];
		mat.m01 = m[4];
		mat.m02 = m[8];
		mat.m10 = m[1];
		mat.m11 = m[5];
		mat.m12 = m[9];
		mat.m20 = m[2];
		mat.m21 = m[6];
		mat.m22 = m[10];
	}

	public static void getOpenGLSubMatrix(Matrix3f mat, float[] m) {
		m[0] = mat.m00;
		m[1] = mat.m10;
		m[2] = mat.m20;
		m[3] = 0f;
		m[4] = mat.m01;
		m[5] = mat.m11;
		m[6] = mat.m21;
		m[7] = 0f;
		m[8] = mat.m02;
		m[9] = mat.m12;
		m[10] = mat.m22;
		m[11] = 0f;
	}

	/**
	 * Sets rotation matrix from euler angles. The euler angles are applied in
	 * ZYX order. This means a vector is first rotated about X then Y and then Z
	 * axis.
	 */
	public static void setEulerZYX(Matrix3f mat, float eulerX, float eulerY, float eulerZ) {
		float ci = (float) Math.cos(eulerX);
		float cj = (float) Math.cos(eulerY);
		float ch = (float) Math.cos(eulerZ);
		float si = (float) Math.sin(eulerX);
		float sj = (float) Math.sin(eulerY);
		float sh = (float) Math.sin(eulerZ);
		float cc = ci * ch;
		float cs = ci * sh;
		float sc = si * ch;
		float ss = si * sh;

		mat.setRow(0, cj * ch, sj * sc - cs, sj * cc + ss);
		mat.setRow(1, cj * sh, sj * ss + cc, sj * cs - sc);
		mat.setRow(2, -sj, cj * si, cj * ci);
	}

	private static float tdotx(Matrix3f mat, Vector3f vec) {
		return mat.m00 * vec.x + mat.m10 * vec.y + mat.m20 * vec.z;
	}

	private static float tdoty(Matrix3f mat, Vector3f vec) {
		return mat.m01 * vec.x + mat.m11 * vec.y + mat.m21 * vec.z;
	}

	private static float tdotz(Matrix3f mat, Vector3f vec) {
		return mat.m02 * vec.x + mat.m12 * vec.y + mat.m22 * vec.z;
	}

	public static void transposeTransform(Vector3f dest, Vector3f vec, Matrix3f mat) {
		float x = tdotx(mat, vec);
		float y = tdoty(mat, vec);
		float z = tdotz(mat, vec);
		dest.x = x;
		dest.y = y;
		dest.z = z;
	}

	public static void setRotation(Matrix3f dest, Quat4f q) {
		float d = q.x * q.x + q.y * q.y + q.z * q.z + q.w * q.w;
		assert (d != 0f);
		float s = 2f / d;
		float xs = q.x * s, ys = q.y * s, zs = q.z * s;
		float wx = q.w * xs, wy = q.w * ys, wz = q.w * zs;
		float xx = q.x * xs, xy = q.x * ys, xz = q.x * zs;
		float yy = q.y * ys, yz = q.y * zs, zz = q.z * zs;
		dest.m00 = 1f - (yy + zz);
		dest.m01 = xy - wz;
		dest.m02 = xz + wy;
		dest.m10 = xy + wz;
		dest.m11 = 1f - (xx + zz);
		dest.m12 = yz - wx;
		dest.m20 = xz - wy;
		dest.m21 = yz + wx;
		dest.m22 = 1f - (xx + yy);
	}

	public static void getRotation(Matrix3f mat, Quat4f dest) {
		float trace = mat.m00 + mat.m11 + mat.m22;
		float[] temp = new float[4];
		if (trace > 0f) {
			float s = (float) FastMath.sqrt(trace + 1f);
			temp[3] = (s * 0.5f);
			s = 0.5f / s;

			temp[0] = ((mat.m21 - mat.m12) * s);
			temp[1] = ((mat.m02 - mat.m20) * s);
			temp[2] = ((mat.m10 - mat.m01) * s);
		} else {
			int i = mat.m00 < mat.m11 ? (mat.m11 < mat.m22 ? 2 : 1) : (mat.m00 < mat.m22 ? 2 : 0);
			int j = (i + 1) % 3;
			int k = (i + 2) % 3;

			float s = (float) FastMath.sqrt(mat.getElement(i, i) - mat.getElement(j, j) - mat.getElement(k, k) + 1f);
			temp[i] = s * 0.5f;
			s = 0.5f / s;

			temp[3] = (mat.getElement(k, j) - mat.getElement(j, k)) * s;
			temp[j] = (mat.getElement(j, i) + mat.getElement(i, j)) * s;
			temp[k] = (mat.getElement(k, i) + mat.getElement(i, k)) * s;
		}
		dest.set(temp[0], temp[1], temp[2], temp[3]);

		// floatArrays.release(temp);
	}

	private static float cofac(Matrix3f mat, int r1, int c1, int r2, int c2) {
		return mat.getElement(r1, c1) * mat.getElement(r2, c2) - mat.getElement(r1, c2) * mat.getElement(r2, c1);
	}

	public static void invert(Matrix3f mat) {
		float co_x = cofac(mat, 1, 1, 2, 2);
		float co_y = cofac(mat, 1, 2, 2, 0);
		float co_z = cofac(mat, 1, 0, 2, 1);

		float det = mat.m00 * co_x + mat.m01 * co_y + mat.m02 * co_z;
		assert (det != 0f);

		float s = 1f / det;
		float m00 = co_x * s;
		float m01 = cofac(mat, 0, 2, 2, 1) * s;
		float m02 = cofac(mat, 0, 1, 1, 2) * s;
		float m10 = co_y * s;
		float m11 = cofac(mat, 0, 0, 2, 2) * s;
		float m12 = cofac(mat, 0, 2, 1, 0) * s;
		float m20 = co_z * s;
		float m21 = cofac(mat, 0, 1, 2, 0) * s;
		float m22 = cofac(mat, 0, 0, 1, 1) * s;

		mat.m00 = m00;
		mat.m01 = m01;
		mat.m02 = m02;
		mat.m10 = m10;
		mat.m11 = m11;
		mat.m12 = m12;
		mat.m20 = m20;
		mat.m21 = m21;
		mat.m22 = m22;
	}

	/**
	 * Diagonalizes this matrix by the Jacobi method. rot stores the rotation
	 * from the coordinate system in which the matrix is diagonal to the
	 * original coordinate system, i.e., old_this = rot * new_this * rot^T. The
	 * iteration stops when all off-diagonal elements are less than the
	 * threshold multiplied by the sum of the absolute values of the diagonal,
	 * or when maxSteps have been executed. Note that this matrix is assumed to
	 * be symmetric.
	 */
	// JAVA NOTE: diagonalize method from 2.71
	public static void diagonalize(Matrix3f mat, Matrix3f rot, float threshold, int maxSteps) {
		Vector3f row = new Vector3f();

		rot.setIdentity();
		for (int step = maxSteps; step > 0; step--) {
			// find off-diagonal element [p][q] with largest magnitude
			int p = 0;
			int q = 1;
			int r = 2;
			float max = Math.abs(mat.m01);
			float v = Math.abs(mat.m02);
			if (v > max) {
				q = 2;
				r = 1;
				max = v;
			}
			v = Math.abs(mat.m12);
			if (v > max) {
				p = 1;
				q = 2;
				r = 0;
				max = v;
			}

			float t = threshold * (Math.abs(mat.m00) + Math.abs(mat.m11) + Math.abs(mat.m22));
			if (max <= t) {
				if (max <= BulletGlobals.SIMD_EPSILON * t) {
					return;
				}
				step = 1;
			}

			// compute Jacobi rotation J which leads to a zero for element
			// [p][q]
			float mpq = mat.getElement(p, q);
			float theta = (mat.getElement(q, q) - mat.getElement(p, p)) / (2 * mpq);
			float theta2 = theta * theta;
			float cos;
			float sin;
			if ((theta2 * theta2) < (10f / BulletGlobals.SIMD_EPSILON)) {
				t = (theta >= 0f) ? 1f / (theta + (float) FastMath.sqrt(1f + theta2)) : 1f / (theta - (float) FastMath.sqrt(1f + theta2));
				cos = 1f / (float) FastMath.sqrt(1f + t * t);
				sin = cos * t;
			} else {
				// approximation for large theta-value, i.e., a nearly diagonal
				// matrix
				t = 1 / (theta * (2 + 0.5f / theta2));
				cos = 1 - 0.5f * t * t;
				sin = cos * t;
			}

			// apply rotation to matrix (this = J^T * this * J)
			mat.setElement(p, q, 0f);
			mat.setElement(q, p, 0f);
			mat.setElement(p, p, mat.getElement(p, p) - t * mpq);
			mat.setElement(q, q, mat.getElement(q, q) + t * mpq);
			float mrp = mat.getElement(r, p);
			float mrq = mat.getElement(r, q);
			mat.setElement(r, p, cos * mrp - sin * mrq);
			mat.setElement(p, r, cos * mrp - sin * mrq);
			mat.setElement(r, q, cos * mrq + sin * mrp);
			mat.setElement(q, r, cos * mrq + sin * mrp);

			// apply rotation to rot (rot = rot * J)
			for (int i = 0; i < 3; i++) {
				rot.getRow(i, row);

				mrp = VectorUtil.getCoord(row, p);
				mrq = VectorUtil.getCoord(row, q);
				VectorUtil.setCoord(row, p, cos * mrp - sin * mrq);
				VectorUtil.setCoord(row, q, cos * mrq + sin * mrp);
				rot.setRow(i, row);
			}
		}
	}

}
