package gliby.minecraft.physics.common.physics.engine.concurrent.javabullet;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector3f;

import com.bulletphysicsx.dynamics.constraintsolver.SliderConstraint;
import com.bulletphysicsx.linearmath.Transform;

import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IConstraintSlider;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import gliby.minecraft.physics.common.physics.engine.IRope;

// TODO JavaRope: implement decent rope algorithm
public class JavaRope implements IRope {

	JavaRope(Vector3f startPos, Vector3f endPos, int detail) {
		this.startPos = startPos;
		this.endPos = endPos;
		this.detail = detail;
		rigidBodies = new ArrayList<IRigidBody>();
	}

	private ArrayList<IRigidBody> rigidBodies;

	@Override
	public List<Vector3f> getSpherePositions() {
		List<Vector3f> points = new ArrayList<Vector3f>();
		for (int i = 0; i < rigidBodies.size(); i++) {
			points.add(rigidBodies.get(i).getCenterOfMassPosition(new Vector3f()));
		}
		return points;
	}

	@Override
	public void dispose(PhysicsWorld physicsWorld) {
	}

	private Vector3f startPos, endPos;
	private int detail;

	@Override
	public void create(PhysicsWorld physicsWorld) {
		Vector3f posDiff = new Vector3f();
		posDiff.sub(startPos, endPos);
		float absoluteDiff = posDiff.length() / detail;
		posDiff.normalize();
		IRigidBody lastBody = null;
		Vector3f lastRelativePoint = null;
		for (int i = 1; i <= detail; i++) {
			Vector3f relativePoint = new Vector3f(posDiff);
			relativePoint.scale(i * absoluteDiff);

			float radius = 0.1f;
			float mass = 1;
			Transform transform = new Transform();
			transform.setIdentity();
			transform.origin.set(relativePoint);

			IRigidBody body = physicsWorld.createRigidBody(null, transform, mass,
					physicsWorld.createSphereShape(radius));
			body.setAngularVelocity(new Vector3f());
			body.setLinearVelocity(new Vector3f());
			rigidBodies.add(body);
			if (lastBody != null) {
				Transform lastBodyTransfrom = new Transform();
				lastBodyTransfrom.setIdentity();
				lastBodyTransfrom.origin.set(lastRelativePoint);
				lastBodyTransfrom.origin.scale(0.5f);

				Transform bodyTransform = new Transform();
				bodyTransform.setIdentity();
				bodyTransform.origin.set(relativePoint);
				lastBodyTransfrom.origin.scale(0.5F);
				IConstraintSlider slider = physicsWorld.createSliderConstraint(lastBody, body, lastBodyTransfrom,
						bodyTransform, false);
				SliderConstraint constraint = (SliderConstraint) slider.getConstraint();
				physicsWorld.addConstraint(slider);
			}

			physicsWorld.addRigidBody(body);

			transform.origin.add(endPos);
			body.setWorldTransform(transform);
			lastRelativePoint = relativePoint;
			lastBody = body;
		}

		/*
		 * for (int i = 0; i < rigidBodies.size(); i++) {
		 * physicsWorld.addRigidBody(rigidBodies.get(i)); }
		 */
	}

	@Override
	public Vector3f getStartPosition() {
		return startPos;
	}

	@Override
	public Vector3f getEndPosition() {
		return endPos;
	}

	@Override
	public int getDetail() {
		return detail;
	}

}
