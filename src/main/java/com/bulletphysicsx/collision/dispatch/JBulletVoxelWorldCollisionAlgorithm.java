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

package com.bulletphysicsx.collision.dispatch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3i;
import javax.vecmath.Tuple3i;
import javax.vecmath.Vector3f;

import com.bulletphysicsx.collision.broadphase.BroadphaseNativeType;
import com.bulletphysicsx.collision.broadphase.CollisionAlgorithm;
import com.bulletphysicsx.collision.broadphase.CollisionAlgorithmConstructionInfo;
import com.bulletphysicsx.collision.broadphase.DispatcherInfo;
import com.bulletphysicsx.collision.narrowphase.PersistentManifold;
import com.bulletphysicsx.collision.shapes.BoxShape;
import com.bulletphysicsx.collision.shapes.CollisionShape;
import com.bulletphysicsx.collision.shapes.voxel.JBulletVoxelWorldShape;
import com.bulletphysicsx.collision.shapes.voxel.VoxelInfo;
import com.bulletphysicsx.linearmath.IntUtil;
import com.bulletphysicsx.linearmath.Transform;
import com.bulletphysicsx.util.ObjectArrayList;
import com.bulletphysicsx.util.ObjectPool;

import gliby.minecraft.physics.common.physics.BlockCollisionInfo;

/**
 * @author Immortius
 */
public class JBulletVoxelWorldCollisionAlgorithm extends CollisionAlgorithm {

	private List<BlockCollisionInfo> blockCollisionInfo = new ArrayList<BlockCollisionInfo>();
	private boolean isSwapped;
	private Tuple3i lastMin = new Point3i(0, 0, 0);
	private Tuple3i lastMax = new Point3i(-1, -1, -1);

	public void init(CollisionAlgorithmConstructionInfo ci, CollisionObject body0, CollisionObject body1, boolean isSwapped) {
		super.init(ci);

		this.isSwapped = isSwapped;
	}

	@Override
	public void destroy() {
		for (BlockCollisionInfo info : blockCollisionInfo) {
			if (info.algorithm != null) {
				dispatcher.freeCollisionAlgorithm((CollisionAlgorithm) info.algorithm);
			}
		}
		blockCollisionInfo.clear();
		lastMin.set(0, 0, 0);
		lastMax.set(-1, -1, -1);
	}

	@Override
	public void processCollision(CollisionObject body0, CollisionObject body1, DispatcherInfo dispatchInfo, ManifoldResult resultOut) {
		CollisionObject colObj = isSwapped ? body1 : body0;
		CollisionObject otherObj = isSwapped ? body0 : body1;
		assert (colObj.getCollisionShape().getShapeType() == BroadphaseNativeType.VOXEL_WORLD_PROXYTYPE);
		if (!(colObj.getCollisionShape() instanceof JBulletVoxelWorldShape)) return;
		JBulletVoxelWorldShape worldShape = (JBulletVoxelWorldShape) colObj.getCollisionShape();

		Transform otherObjTransform = new Transform();
		otherObj.getWorldTransform(otherObjTransform);
		Vector3f aabbMin = new Vector3f();
		Vector3f aabbMax = new Vector3f();
		otherObj.getCollisionShape().getAabb(otherObjTransform, aabbMin, aabbMax);
		Matrix4f otherObjMatrix = new Matrix4f();
		otherObjTransform.getMatrix(otherObjMatrix);
		Vector3f otherObjPos = new Vector3f();
		otherObjMatrix.get(otherObjPos);

		Tuple3i regionMin = new Point3i(IntUtil.floorToInt(aabbMin.x + 0.5f), IntUtil.floorToInt(aabbMin.y + 0.5f), IntUtil.floorToInt(aabbMin.z + 0.5f));
		Tuple3i regionMax = new Point3i(IntUtil.floorToInt(aabbMax.x + 0.5f), IntUtil.floorToInt(aabbMax.y + 0.5f), IntUtil.floorToInt(aabbMax.z + 0.5f));

		Transform orgTrans = new Transform();
		colObj.getWorldTransform(orgTrans);

		Transform newChildWorldTrans = new Transform();
		Matrix4f childMat = new Matrix4f();

		Matrix3f rot = new Matrix3f();
		rot.setIdentity();

		for (int x = regionMin.x; x <= regionMax.x; ++x) {
			for (int y = regionMin.y; y <= regionMax.y; ++y) {
				for (int z = regionMin.z; z <= regionMax.z; ++z) {
					if ((x < lastMin.x || x > lastMax.x) || (y < lastMin.y || y > lastMax.y) || (z < lastMin.z || z > lastMax.z)) {
						blockCollisionInfo.add(new BlockCollisionInfo(x, y, z));
					}
				}
			}
		}

		Iterator<BlockCollisionInfo> iterator = blockCollisionInfo.iterator();
		while (iterator.hasNext()) {
			BlockCollisionInfo info = iterator.next();
			// Check still in bounds
			if (info.position.x < regionMin.x || info.position.x > regionMax.x || info.position.y < regionMin.y || info.position.y > regionMax.y || info.position.z < regionMin.z || info.position.z > regionMax.z) {
				if (info.algorithm != null) {
					dispatcher.freeCollisionAlgorithm((CollisionAlgorithm) info.algorithm);
				}
				iterator.remove();
			} else {
				VoxelInfo childInfo = worldShape.getWorld().getCollisionShapeAt(info.position.x, info.position.y, info.position.z);
				if (childInfo.isBlocking()) {
					if (info.algorithm != null && info.blockShape != ((CollisionShape) childInfo.getCollisionShape()).getShapeType().ordinal()) {
						dispatcher.freeCollisionAlgorithm((CollisionAlgorithm) info.algorithm);
						info.algorithm = null;
					}
					colObj.internalSetTemporaryCollisionShape((CollisionShape)childInfo.getCollisionShape());

					if (info.algorithm == null) {
						info.algorithm = dispatcher.findAlgorithm(colObj, otherObj);
						info.blockShape = ((CollisionShape) childInfo.getCollisionShape()).getShapeType().ordinal();
					}

					if (((CollisionShape) childInfo.getCollisionShape()).getShapeType().isCompound() && info.algorithm instanceof ConvexConvexAlgorithm || (colObj.getCollisionShape() instanceof BoxShape && info.algorithm instanceof CompoundCollisionAlgorithm)) {
						info.algorithm = dispatcher.findAlgorithm(colObj, otherObj);
						info.blockShape = ((CollisionShape) childInfo.getCollisionShape()).getShapeType().ordinal();
					}

					Vector3f offset = (Vector3f)childInfo.getCollisionOffset();
					childMat.set(rot, new Vector3f(info.position.x + offset.x, info.position.y + offset.y, info.position.z + offset.z), 1.0f);
					newChildWorldTrans.set(childMat);
					colObj.setWorldTransform(newChildWorldTrans);
					colObj.setInterpolationWorldTransform(newChildWorldTrans);
					colObj.setUserPointer(childInfo.getUserData());
					colObj.setRestitution(childInfo.getRestitution());
					colObj.setFriction(childInfo.getFriction());

					((CollisionAlgorithm) info.algorithm).processCollision(colObj, otherObj, dispatchInfo, resultOut);

				} else if (info.algorithm != null) {
					dispatcher.freeCollisionAlgorithm((CollisionAlgorithm) info.algorithm);
					info.algorithm = null;
					info.blockShape = BroadphaseNativeType.INVALID_SHAPE_PROXYTYPE.ordinal();
				}
			}
		}

		lastMin.set(regionMin);
		lastMax.set(regionMax);

		colObj.internalSetTemporaryCollisionShape(worldShape);
		colObj.setWorldTransform(orgTrans);
		colObj.setInterpolationWorldTransform(orgTrans);
	}

	@Override
	public float calculateTimeOfImpact(CollisionObject body0, CollisionObject body1, DispatcherInfo dispatchInfo, ManifoldResult resultOut) {
		// TODO: Implement this? Although not used for discrete dynamics
		/*
		 * PerformanceMonitor.startActivity("World Calculate Time Of Impact");
		 * CollisionObject colObj = isSwapped ? body1 : body0; CollisionObject
		 * otherObj = isSwapped ? body0 : body1;
		 * 
		 * assert (colObj.getCollisionShape().getShapeType() ==
		 * BroadphaseNativeType.INVALID_SHAPE_PROXYTYPE);
		 * 
		 * WorldShape worldShape = (WorldShape) colObj.getCollisionShape();
		 * 
		 * Transform otherObjTransform = new Transform(); Vector3f
		 * otherLinearVelocity = new Vector3f(); Vector3f otherAngularVelocity =
		 * new Vector3f();
		 * otherObj.getInterpolationWorldTransform(otherObjTransform);
		 * otherObj.getInterpolationLinearVelocity(otherLinearVelocity);
		 * otherObj.getInterpolationAngularVelocity(otherAngularVelocity);
		 * Vector3f aabbMin = new Vector3f(); Vector3f aabbMax = new Vector3f();
		 * otherObj.getCollisionShape().getAabb(otherObjTransform, aabbMin,
		 * aabbMax);
		 * 
		 * Region3i region = Region3i.createFromMinMax(new Vector3i(aabbMin,
		 * 0.5f), new Vector3i(aabbMax, 0.5f));
		 * 
		 * Transform orgTrans = new Transform(); Transform childTrans = new
		 * Transform(); float hitFraction = 1f;
		 * 
		 * Matrix3f rot = new Matrix3f(); rot.setIdentity();
		 * 
		 * for (Vector3i blockPos : region) { Block block =
		 * worldShape.getWorld().getBlock(blockPos); if (block.isPenetrable())
		 * continue;
		 * 
		 * // recurse, using each shape within the block. CollisionShape
		 * childShape = defaultBox;
		 * 
		 * // backup colObj.getWorldTransform(orgTrans);
		 * 
		 * childTrans.set(new Matrix4f(rot, blockPos.toVector3f(), 1.0f));
		 * colObj.setWorldTransform(childTrans);
		 * 
		 * // the contactpoint is still projected back using the original
		 * inverted worldtrans CollisionShape tmpShape =
		 * colObj.getCollisionShape();
		 * colObj.internalSetTemporaryCollisionShape(childShape);
		 * colObj.setUserPointer(blockPos);
		 * 
		 * CollisionAlgorithm collisionAlg =
		 * collisionAlgorithmFactory.dispatcher1.findAlgorithm(colObj,
		 * otherObj); usedCollisionAlgorithms.add(collisionAlg); float frac =
		 * collisionAlg.calculateTimeOfImpact(colObj, otherObj, dispatchInfo,
		 * resultOut); if (frac < hitFraction) { hitFraction = frac; }
		 * 
		 * // revert back colObj.internalSetTemporaryCollisionShape(tmpShape);
		 * colObj.setWorldTransform(orgTrans); }
		 * PerformanceMonitor.endActivity(); return hitFraction;
		 */
		return 1.0f;
	}

	@Override
	public void getAllContactManifolds(ObjectArrayList<PersistentManifold> manifoldArray) {
		for (BlockCollisionInfo info : blockCollisionInfo) {
			if (info.algorithm != null) {
				((CollisionAlgorithm) info.algorithm).getAllContactManifolds(manifoldArray);
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////

	public static class CreateFunc extends CollisionAlgorithmCreateFunc {
		private final ObjectPool<JBulletVoxelWorldCollisionAlgorithm> pool = ObjectPool.get(JBulletVoxelWorldCollisionAlgorithm.class);

		@Override
		public CollisionAlgorithm createCollisionAlgorithm(CollisionAlgorithmConstructionInfo ci, CollisionObject body0, CollisionObject body1) {
			JBulletVoxelWorldCollisionAlgorithm algo = pool.get();
			algo.init(ci, body0, body1, false);
			return algo;
		}

		@Override
		public void releaseCollisionAlgorithm(CollisionAlgorithm algo) {
			pool.release((JBulletVoxelWorldCollisionAlgorithm) algo);
		}
	}

	;

	public static class SwappedCreateFunc extends CollisionAlgorithmCreateFunc {
		private final ObjectPool<JBulletVoxelWorldCollisionAlgorithm> pool = ObjectPool.get(JBulletVoxelWorldCollisionAlgorithm.class);

		@Override
		public CollisionAlgorithm createCollisionAlgorithm(CollisionAlgorithmConstructionInfo ci, CollisionObject body0, CollisionObject body1) {
			JBulletVoxelWorldCollisionAlgorithm algo = pool.get();
			algo.init(ci, body0, body1, true);
			return algo;
		}

		@Override
		public void releaseCollisionAlgorithm(CollisionAlgorithm algo) {
			pool.release((JBulletVoxelWorldCollisionAlgorithm) algo);
		}
	}

	;
}
