/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.client.keybindings;

import java.util.List;

import javax.vecmath.Vector3f;

import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.linearmath.VectorUtil;

import net.gliby.gman.EntityUtility;
import net.gliby.physics.Physics;
import net.gliby.physics.common.entity.models.MobModel.ModelCubeGroup;
import net.gliby.physics.common.physics.PhysicsWorld;
import net.gliby.physics.common.physics.engine.ICollisionShape;
import net.gliby.physics.common.physics.engine.IRigidBody;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.world.World;

/**
 *
 */
public class KeyFireEvent extends KeyEvent {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.minefortress.client.keybindings.KeyEvent#keyDown(net.minecraft.client
	 * .settings.KeyBinding, boolean, boolean)
	 */
	@Override
	public void keyDown(KeyBinding kb, boolean tickEnd, boolean isRepeat) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.minefortress.client.keybindings.KeyEvent#keyUp(net.minecraft.client
	 * .settings.KeyBinding, boolean)
	 */
	@Override
	public void keyUp(KeyBinding kb, boolean tickEnd) {
		Minecraft mc = Minecraft.getMinecraft();
		World world = null;
		if ((world = mc.theWorld) != null) {
			if (mc.currentScreen == null) {
				// Minecraft.getMinecraft().displayGuiScreen(new
				// GuiScreenPhysicsCreator(null));
				debugSpawn(world);

			}
			// TODO Remove client debug physics.
			// Physics.getInstance().getClientProxy().getPhysicsOverWorld().debugSpawn(world);
			// debugSpawn(Minecraft.getMinecraft().theWorld);
		}
	}

	public void debugSpawn(World world) {
		Minecraft mc = Minecraft.getMinecraft();
		Physics physics = Physics.getInstance();
		PhysicsWorld physicsWorld = physics.getPhysicsOverworld().getPhysicsByWorld(world);
		Vector3f playerPosition = EntityUtility.toVector3f(mc.thePlayer.getPositionVector());
		List<ModelCubeGroup> cubeGroups = physics.getMobModelManager().getModelRegistry().get(EntityZombie.class)
				.getCubeGroups();
		float scale = 1.0F / 16.0F;
		//TODO Finish
		System.out.println("Scale is: " + scale);
		for (ModelCubeGroup group : cubeGroups) {
			Transform transform = new Transform();
			transform.setIdentity();
			transform.origin.add(group.getOffset());
			Vector3f translation = new Vector3f(group.getRotationPoint());
			transform.origin.add(translation);
			transform.origin.scale(-scale);
			transform.origin.add(playerPosition);
			ICollisionShape shape = physicsWorld.buildCollisionShape(group.getCubes(), VectorUtil.IDENTITY);
			shape.setLocalScaling(new Vector3f(scale, scale, scale));
			IRigidBody body = physicsWorld.createRigidBody(null, transform, 0, shape);
			physicsWorld.addRigidBody(body);
		}

		/*
		 * if (true) { PhysicsWorld physicsWorld =
		 * Physics.getInstance().getPhysicsOverworld().getPhysicsByWorld(world);
		 * Minecraft mc = Minecraft.getMinecraft();
		 * 
		 * PhysicsWorld physicsWorld = getPhysicsByWorld(world); Minecraft mc =
		 * Minecraft.getMinecraft(); Vector3f basePos =
		 * EntityUtility.toVector3f(mc.thePlayer.getPositionVector());
		 * basePos.sub(new Vector3f(0.5F, 0.5F, 0.5F)); IRope rope =
		 * physicsWorld.createRope(new Vector3f(basePos), new
		 * Vector3f(basePos.x, basePos.y + 2, basePos.z), 4);
		 * physicsWorld.addRope(rope);
		 * 
		 * 
		 * BlockPos pos = mc.objectMouseOver.getBlockPos(); IBlockState state;
		 * if(pos != null && (state =
		 * world.getBlockState(pos)).getBlock().getMaterial() != Material.air) {
		 * PhysicsWorld physicsWorld = getStepSimulatorByWorld(world);
		 * ICollisionShape shape = physicsWorld.createBlockShape(world, pos,
		 * state); Transform location = new Transform(); location.setIdentity();
		 * location.origin.set(new Vector3f((float)mc.thePlayer.posX,
		 * (float)mc.thePlayer.posY, (float)mc.thePlayer.posZ)); IRigidBody body
		 * = physicsWorld.createRigidBody(null, location, new
		 * Random().nextInt(100) + 1, shape); physicsWorld.addRigidBody(body);
		 * if(lastBody != null) { Transform transformA = new Transform();
		 * transformA.setIdentity(); transformA.origin.set(0, 0, 0); Transform
		 * transformB = new Transform(); transformB.setIdentity();
		 * transformB.origin.set(0, 1, 0); IConstraintGeneric6Dof constraint =
		 * physicsWorld.createGeneric6DofConstraint(lastBody, body, transformA,
		 * transformB, true); physicsWorld.addConstraint(constraint);
		 * 
		 * } this.lastBody = body; }
		 * 
		 * double posX = mc.thePlayer.posX; double posY = mc.thePlayer.posY;
		 * double posZ = mc.thePlayer.posZ;
		 * 
		 * ModelBiped modelBiped = new ModelBiped(); ArrayList<ModelPart> models
		 * = generateModelProxies(modelBiped); ArrayList<AttachementPoint>
		 * points = generateAttachementPoints(modelBiped); IRigidBody[]
		 * rigidBodies = new IRigidBody[models.size()]; HashMap<ModelBox,
		 * IRigidBody> rigidBodyMap = new HashMap<ModelBox, IRigidBody>();
		 * 
		 * for (int i = 0; i < rigidBodies.length; i++) { ModelPart model =
		 * models.get(i); Transform transform = new Transform();
		 * transform.setIdentity(); transform.origin.add(new
		 * Vector3f(model.getModelBox().posX1 + model.getModelBox().posX2,
		 * model.getModelBox().posY1 + model.getModelBox().posY2,
		 * model.getModelBox().posZ1 + model.getModelBox().posZ2));
		 * transform.origin.scale(0.5f);
		 * transform.origin.add(model.getPosition());
		 * transform.origin.scale(-0.0625f); // Place in world.
		 * transform.origin.add(new Vector3f((float) posX, (float) posY, (float)
		 * posZ)); Vector3f extent = new Vector3f(model.getModelBox().posX2 -
		 * model.getModelBox().posX1, model.getModelBox().posY2 -
		 * model.getModelBox().posY1, model.getModelBox().posZ2 -
		 * model.getModelBox().posZ1); // Adjust // to // minecraft's // scale.
		 * extent.scale(0.0625f); extent.scale(0.5f);
		 * 
		 * IRigidBody body = physicsWorld.createRigidBody(null, transform, 1,
		 * physicsWorld.createBoxShape(extent));
		 * rigidBodyMap.put(model.getModelBox(), body); rigidBodies[i] = body; }
		 * 
		 * for (AttachementPoint point : points) { if (point.getBodyA() != null
		 * && point.getBodyB() != null) { IRigidBody bodyA =
		 * rigidBodyMap.get(point.getBodyA().getModelBox()); IRigidBody bodyB =
		 * rigidBodyMap.get(point.getBodyB().getModelBox()); System.out.println(
		 * "Created: " + bodyA + ", " + bodyB);
		 * 
		 * Vector3f rotationPivot = new Vector3f();
		 * rotationPivot.set(point.getPosition());
		 * rotationPivot.scale(-0.0625f);
		 * 
		 * Transform centerA = bodyA.getCenterOfMassTransform(new Transform());
		 * centerA.inverse(); centerA.transform(new Vector3f(rotationPivot));
		 * 
		 * Transform centerB = bodyA.getCenterOfMassTransform(new Transform());
		 * centerB.inverse(); centerB.transform(new Vector3f(rotationPivot));
		 * 
		 * IConstraintGeneric6Dof joint =
		 * physicsWorld.createGeneric6DofConstraint(bodyA, bodyA, centerA,
		 * centerB, true); physicsWorld.addConstraint(joint);
		 * physicsWorld.addRigidBody(bodyA); physicsWorld.addRigidBody(bodyB); }
		 * 
		 * } }
		 */

	}

	@Override
	public EnumBinding getEnumBinding() {
		return EnumBinding.FIRE;
	}
}
