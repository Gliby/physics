/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.client;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import javax.vecmath.Vector3f;

import com.bulletphysics.linearmath.Transform;

import net.gliby.physics.Physics;
import net.gliby.physics.common.physics.AttachementPoint;
import net.gliby.physics.common.physics.IRigidBody;
import net.gliby.physics.common.physics.ModelPart;
import net.gliby.physics.common.physics.PhysicsOverworld;
import net.gliby.physics.common.physics.PhysicsWorld;
import net.gliby.physics.common.physics.nativebullet.NativePhysicsWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 *
 */
public class ClientPhysicsOverworld extends PhysicsOverworld {

	// TODO Re-enable
	@SubscribeEvent
	public void onLoad(WorldEvent.Load event) {
		World world = event.world;
		PhysicsWorld worldStepSimulator;
		if ((worldStepSimulator = getPhysicsWorldMap().get(event.world)) == null) {
			worldStepSimulator = new NativePhysicsWorld(world, 60, new Vector3f(0, -9.8F, 0)) {

				@Override
				public boolean shouldSimulate() {
					Minecraft mc = Minecraft.getMinecraft();
					return !mc.isGamePaused();
				}
			};

			// TODO Re-enable.
			// worldStepSimulator.getMechanics().put("EntityCollision",
			// new EntityCollisionResponseMechanic(world, worldStepSimulator,
			// false, 20));
			worldStepSimulator.create();

			Thread thread = new Thread(worldStepSimulator,
					event.world.getWorldInfo().getWorldName() + " Physics Simulator");
			thread.start();
			getPhysicsWorldMap().put(event.world, worldStepSimulator);
			Physics.getLogger().info("Started running " + thread.getName() + ".");
		}
	}

	@SubscribeEvent
	public void onUnload(WorldEvent.Unload event) {
		PhysicsWorld stepSimulator;
		if ((stepSimulator = getPhysicsWorldMap().get(event.world)) != null) {
			stepSimulator.dispose();
			getPhysicsWorldMap().remove(event.world);
			Physics.getLogger().info(
					"Stopped and disposed of " + event.world.getWorldInfo().getWorldName() + " physics simulator.");
		}
	}

	@SubscribeEvent
	public void onBlockEvent(BlockEvent event) {
		PhysicsWorld stepSimulator;
		if ((stepSimulator = getPhysicsWorldMap().get(event.world)) != null) {
			// 1.75f is completely arbitrary. It works quite well for the time
			// being.
			float size = 1.75f;
			final AxisAlignedBB bb = new AxisAlignedBB(-size, -size, -size, size, size, size).offset(event.pos.getX(),
					event.pos.getY(), event.pos.getZ());
			stepSimulator.awakenArea(new Vector3f((float) bb.minX, (float) bb.minY, (float) bb.minZ),
					new Vector3f((float) bb.maxX, (float) bb.maxY, (float) bb.maxZ));
		}
	}

	public void debugSpawn(World world) {

		PhysicsWorld physicsWorld = getPhysicsByWorld(world);
		Minecraft mc = Minecraft.getMinecraft();
		/*
		 * PhysicsWorld physicsWorld = getPhysicsByWorld(world); Minecraft mc =
		 * Minecraft.getMinecraft(); Vector3f basePos =
		 * EntityUtility.toVector3f(mc.thePlayer.getPositionVector());
		 * basePos.sub(new Vector3f(0.5F, 0.5F, 0.5F)); IRope rope =
		 * physicsWorld.createRope(new Vector3f(basePos), new
		 * Vector3f(basePos.x, basePos.y + 2, basePos.z), 4);
		 * physicsWorld.addRope(rope);
		 */
		/*
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
		 */
		double posX = mc.thePlayer.posX;
		double posY = mc.thePlayer.posY;
		double posZ = mc.thePlayer.posZ;

		ModelBiped modelBiped = new ModelBiped();
		ArrayList<ModelPart> models = generateModelProxies(modelBiped);
		ArrayList<AttachementPoint> points = generateAttachementPoints(modelBiped);
		IRigidBody[] rigidBodies = new IRigidBody[models.size()];
		HashMap<ModelBox, IRigidBody> rigidBodyMap = new HashMap<ModelBox, IRigidBody>();

		for (int i = 0; i < rigidBodies.length; i++) {
			ModelPart model = models.get(i);
			Transform transform = new Transform();
			transform.setIdentity();
			transform.origin.add(new Vector3f(model.getModelBox().posX1 + model.getModelBox().posX2,
					model.getModelBox().posY1 + model.getModelBox().posY2,
					model.getModelBox().posZ1 + model.getModelBox().posZ2));
			transform.origin.scale(0.5f);
			transform.origin.add(model.getPosition());
			transform.origin.scale(-0.0625f); // Place in world.
			transform.origin.add(new Vector3f((float) posX, (float) posY, (float) posZ));
			Vector3f extent = new Vector3f(model.getModelBox().posX2 - model.getModelBox().posX1,
					model.getModelBox().posY2 - model.getModelBox().posY1,
					model.getModelBox().posZ2 - model.getModelBox().posZ1); // Adjust
																			// to
																			// minecraft's
																			// scale.
			extent.scale(0.0625f);
			extent.scale(0.5f);

			IRigidBody body = physicsWorld.createRigidBody(null, transform, 1, physicsWorld.createBoxShape(extent));
			physicsWorld.addRigidBody(body);

			rigidBodyMap.put(model.getModelBox(), body);
			physicsWorld.addRigidBody(body);
			rigidBodies[i] = body;
		}

		/*
		 * System.out.println("Body count: " + rigidBodies.length); // Attach
		 * for (AttachementPoint point : points) { if (point.getBodyA() != null
		 * && point.getBodyB() != null) { IRigidBody bodyA =
		 * rigidBodyMap.get(point.getBodyA().getModelBox()); IRigidBody bodyB =
		 * rigidBodyMap.get(point.getBodyB().getModelBox()); System.out.println(
		 * "Created: " + bodyA + ", " + bodyB); Transform transformA = new
		 * Transform(); transformA.setIdentity(); transformA.origin.set(new
		 * Vector3f(0, 20000, 0)); // transformA.origin.scale(-0.0625f);
		 * Transform transformB = new Transform(); transformB.setIdentity();
		 * transformB.origin.set(new Vector3f(0, 0, 0)); //
		 * transformB.origin.set(point.getPosition()); //
		 * transformB.origin.scale(-0.0625f); //
		 * transformB.origin.scale(0.0001f); IConstraintGeneric6Dof joint =
		 * physicsWorld.createGeneric6DofConstraint(bodyA, bodyA, transformA,
		 * transformB, false); physicsWorld.addConstraint(joint); } }
		 */
	}

	public ArrayList<ModelPart> generateModelProxies(ModelBase modelBase) {
		ArrayList<ModelPart> proxyList = new ArrayList<ModelPart>();
		for (int i = 0; i < modelBase.getClass().getDeclaredFields().length; i++) {
			Field field = modelBase.getClass().getDeclaredFields()[i];
			field.setAccessible(true);
			Object obj = null;
			try {
				obj = field.get(modelBase);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (obj instanceof ModelRenderer) {
				ModelRenderer modelRenderer = (ModelRenderer) obj;
				for (int i1 = 0; i1 < modelRenderer.cubeList.size(); i1++) {
					ModelBox box = (ModelBox) modelRenderer.cubeList.get(i1);
					Vector3f rotationPoint = new Vector3f(modelRenderer.rotationPointX, modelRenderer.rotationPointY,
							modelRenderer.rotationPointZ);
					proxyList.add(new ModelPart(rotationPoint, box));
				}
			}
		}

		return proxyList;
	}

	// Attachement points should be generated from ModelProxies.
	public ArrayList<AttachementPoint> generateAttachementPoints(ModelBase modelBase) {
		ArrayList<ModelPart> proxyList = new ArrayList<ModelPart>();
		ArrayList<AttachementPoint> points = new ArrayList<AttachementPoint>();
		for (int i = 0; i < modelBase.getClass().getDeclaredFields().length; i++) {
			Field field = modelBase.getClass().getDeclaredFields()[i];
			field.setAccessible(true);
			Object obj = null;
			try {
				obj = field.get(modelBase);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (obj instanceof ModelRenderer) {
				ModelRenderer modelRenderer = (ModelRenderer) obj;
				for (int i1 = 0; i1 < modelRenderer.cubeList.size(); i1++) {
					ModelBox box = (ModelBox) modelRenderer.cubeList.get(i1);
					Vector3f rotationPoint = new Vector3f(modelRenderer.rotationPointX, modelRenderer.rotationPointY,
							modelRenderer.rotationPointZ);
					proxyList.add(new ModelPart(rotationPoint, box));
					points.add(new AttachementPoint(rotationPoint));
				}
			}
		}
		// Should points be the same model proxy in size?

		for (int i = 0; i < points.size(); i++) {
			AttachementPoint point = points.get(i);
			for (int j = 0; j < proxyList.size(); j++) {
				ModelPart model = proxyList.get(j);
				float size = 0.07f;
				AxisAlignedBB pointBB = AxisAlignedBB
						.fromBounds(point.getPosition().x, point.getPosition().y, point.getPosition().z,
								point.getPosition().x, point.getPosition().y, point.getPosition().z)
						.expand(size, size, size);
				AxisAlignedBB modelBB = AxisAlignedBB
						.fromBounds(model.getModelBox().posX1, model.getModelBox().posY1, model.getModelBox().posZ1,
								model.getModelBox().posX2, model.getModelBox().posY2, model.getModelBox().posZ2)
						.offset(model.getPosition().x, model.getPosition().y, model.getPosition().z);
				if (pointBB.intersectsWith(modelBB)) {
					if (point.bodyA == null)
						point.setBodyA(model);
					else if (point.bodyB == null) {
						point.setBodyB(model);
					}
				}
			}
		}
		return points;
	}
}
