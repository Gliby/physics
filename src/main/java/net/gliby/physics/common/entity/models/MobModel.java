package net.gliby.physics.common.entity.models;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector3f;

import net.minecraft.util.AxisAlignedBB;

/**
 * Most important parts of a Model.
 * 
 */
public class MobModel {

	private final String name;
	private final List<ModelCubeGroup> cubeGroups;

	public List<ModelCubeGroup> getCubeGroups() {
		return cubeGroups;
	}

	public MobModel(String name, List<ModelCubeGroup> cubeGroups) {
		this.name = name;
		this.cubeGroups = cubeGroups;
	}

	public MobModel(String name) {
		this(name, new ArrayList<ModelCubeGroup>());
	}

	public static class ModelCubeGroup {

		private Vector3f rotationPoint, rotateAngle, offset;

		private final List<AxisAlignedBB> cubes;

		public Vector3f getRotationPoint() {
			return rotationPoint;
		}

		public void setRotationPoint(Vector3f rotationPoint) {
			this.rotationPoint = rotationPoint;
		}

		public Vector3f getRotateAngle() {
			return rotateAngle;
		}

		public void setRotateAngle(Vector3f rotateAngle) {
			this.rotateAngle = rotateAngle;
		}

		public Vector3f getOffset() {
			return offset;
		}

		public void setOffset(Vector3f offset) {
			this.offset = offset;
		}

		public List<AxisAlignedBB> getCubes() {
			return cubes;
		}

		public ModelCubeGroup(Vector3f rotationPoint, Vector3f rotateAngle, Vector3f offset,
				List<AxisAlignedBB> cubes) {
			this.rotationPoint = rotationPoint;
			this.rotateAngle = rotateAngle;
			this.offset = offset;
			this.cubes = cubes;
		}

		public ModelCubeGroup(Vector3f rotationPoint, Vector3f rotateAngle, Vector3f offset) {
			this(rotationPoint, rotateAngle, offset, new ArrayList<AxisAlignedBB>());
		}

	}

}
