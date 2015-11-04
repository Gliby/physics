package net.gliby.physics.common.physics.mechanics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.gliby.physics.common.game.items.toolgun.actions.IToolGunAction;
import net.gliby.physics.common.game.items.toolgun.actions.IToolGunTickable;
import net.gliby.physics.common.game.items.toolgun.actions.ToolGunActionRegistry;
import net.gliby.physics.common.physics.PhysicsWorld;
import net.gliby.physics.common.physics.engine.IRigidBody;

public class ToolMechanics extends PhysicsMechanic {

	private List<IToolGunTickable> tickables;

	public ToolMechanics(ToolGunActionRegistry registry, PhysicsWorld physicsWorld, boolean threaded,
			int ticksPerSecond) {
		super(physicsWorld, threaded, ticksPerSecond);
		this.tickables = new ArrayList<IToolGunTickable>();
		Iterator<Entry<Integer, IToolGunAction>> it = registry.getActions().entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Integer, IToolGunAction> entry = it.next();
			if (entry.getValue() instanceof IToolGunTickable) {
				tickables.add((IToolGunTickable) entry.getValue());
			}
		}
	}

	public List<IToolGunTickable> getTickables() {
		return tickables;
	}

	@Override
	public void update() {
		for (IToolGunTickable tickable : tickables) {
			tickable.update(physicsWorld);
			// Vector3f direction = new Vector3f((Vector3f)
			// body.getProperties().get("Motorized"));
			// direction.scale(500);
			// body.applyTorque((Vector3f)
			// body.getProperties().get("Motorized"));
		}
	}

	@Override
	public String getName() {
		return "ToolMechanic";
	}

	@Override
	public void init() {
		for (IToolGunTickable tickable : tickables) {
		}
	}

}
