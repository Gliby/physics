package net.gliby.physics.common.physics.mechanics;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector3f;

import net.gliby.physics.common.physics.PhysicsWorld;
import net.gliby.physics.common.physics.engine.IRigidBody;

public class ToolMechanic extends PhysicsMechanic {

	public ToolMechanic(PhysicsWorld physicsWorld, boolean threaded, int ticksPerSecond) {
		super(physicsWorld, threaded, ticksPerSecond);
		this.bodies = new ArrayList<IRigidBody>();
	}

	private List<IRigidBody> bodies;

	public List<IRigidBody> getBodies() {
		return bodies;
	}

	@Override
	public void update() {
		for (IRigidBody body : bodies) {
			Vector3f direction = new Vector3f((Vector3f) body.getProperties().get("Motorized"));
			direction.scale(500);
			body.applyTorque((Vector3f) body.getProperties().get("Motorized"));
		}
	}

	@Override
	public String getName() {
		return "ToolMechanic";
	}

	@Override
	public void init() {

	}

}
