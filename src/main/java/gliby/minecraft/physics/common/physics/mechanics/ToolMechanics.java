package gliby.minecraft.physics.common.physics.mechanics;

import gliby.minecraft.physics.common.game.items.toolgun.actions.IToolGunAction;
import gliby.minecraft.physics.common.game.items.toolgun.actions.IToolGunTickable;
import gliby.minecraft.physics.common.game.items.toolgun.actions.ToolGunActionRegistry;
import gliby.minecraft.physics.common.physics.PhysicsWorld;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ToolMechanics extends PhysicsMechanic {

    private List<IToolGunTickable> tickables;

    public ToolMechanics(ToolGunActionRegistry registry, PhysicsWorld physicsWorld,
                         int ticksPerSecond) {
        super(physicsWorld, ticksPerSecond);
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
		/*for (IToolGunTickable tickable : tickables) {
		}*/
    }

    @Override
    public void dispose() {
        tickables.clear();
    }

}
