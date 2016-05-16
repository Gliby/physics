package net.gliby.minecraft.physics.common.physics.mechanics;

import net.gliby.minecraft.physics.common.physics.engine.IGhostObject;
import net.gliby.minecraft.physics.common.physics.engine.IRigidBody;

//TODO Finish
public class PhysicsMechanicEvent {

	public static class TouchedEntity {
		IRigidBody bodyA;
		IRigidBody bodyB;
		
		public TouchedEntity(IRigidBody bodyA, IGhostObject ghostObject) {
			
		}
	}
	
}
