/*
 * Copyright 2012
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bulletphysics.collision.shapes.voxel;

import com.bulletphysics.collision.shapes.CollisionShape;

import javax.vecmath.Vector3f;

/**
 * The collision data for a single Voxel.
 *
 * @author Immortius
 */
public interface VoxelInfo {

	/**
	 * @return Whether the voxel can be collided with at all.
	 */
	boolean isColliding();

	/**
	 * @return The user data associated with this voxel. I would suggest at
	 *         least the position of the voxel.
	 */
	Object getUserData();

	/**
	 * @return The collision shape for the voxel. Reuse these as much as
	 *         possible.
	 */
	Object getCollisionShape();

	/**
	 * @return The offset of the collision shape from the center of the voxel.
	 */
	Object getCollisionOffset();

	/**
	 * @return Does this voxel block rigid bodies
	 */
	boolean isBlocking();

	/**
	 * @return
	 */
	float getFriction();

	/**
	 * @return
	 */
	float getRestitution();
}
