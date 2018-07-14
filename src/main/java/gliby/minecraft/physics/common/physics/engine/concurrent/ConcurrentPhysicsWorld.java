package gliby.minecraft.physics.common.physics.engine.concurrent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.vecmath.Vector3f;

import com.bulletphysicsx.linearmath.Transform;
import com.google.common.collect.Queues;
import com.google.gson.annotations.SerializedName;

import gliby.minecraft.gman.WorldUtility;
import gliby.minecraft.physics.common.physics.PhysicsOverworld.IPhysicsWorldConfiguration;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.ICollisionObject;
import gliby.minecraft.physics.common.physics.engine.ICollisionShape;
import gliby.minecraft.physics.common.physics.engine.IConstraint;
import gliby.minecraft.physics.common.physics.engine.IConstraintGeneric6Dof;
import gliby.minecraft.physics.common.physics.engine.IConstraintPoint2Point;
import gliby.minecraft.physics.common.physics.engine.IConstraintSlider;
import gliby.minecraft.physics.common.physics.engine.IGhostObject;
import gliby.minecraft.physics.common.physics.engine.IRayResult;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import gliby.minecraft.physics.common.physics.engine.IRope;
import gliby.minecraft.physics.common.physics.mechanics.PhysicsMechanic;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 *
 */
public abstract class ConcurrentPhysicsWorld extends PhysicsWorld {

	public ConcurrentPhysicsWorld(IPhysicsWorldConfiguration physicsConfiguration) {
		super(physicsConfiguration);
	}

	public final ConcurrentLinkedQueue<Runnable> physicsTasks = Queues.newConcurrentLinkedQueue();

	@Override
	protected void simulate() {
		super.simulate();
		Queue queue = this.physicsTasks;
		synchronized (this.physicsTasks) {
			while (!this.physicsTasks.isEmpty()) {
				this.physicsTasks.poll().run();
			}
		}
	}
}
