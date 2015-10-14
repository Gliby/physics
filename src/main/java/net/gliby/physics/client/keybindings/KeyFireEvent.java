/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.client.keybindings;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import javax.vecmath.Vector3f;

import com.bulletphysics.linearmath.Transform;

import net.gliby.physics.Physics;
import net.gliby.physics.client.gui.creator.GuiScreenPhysicsCreator;
import net.gliby.physics.common.physics.AttachementPoint;
import net.gliby.physics.common.physics.ModelPart;
import net.gliby.physics.common.physics.PhysicsWorld;
import net.gliby.physics.common.physics.engine.IConstraintGeneric6Dof;
import net.gliby.physics.common.physics.engine.IRigidBody;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.AxisAlignedBB;
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
		World world = null;
		if ((world = Minecraft.getMinecraft().theWorld) != null) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiScreenPhysicsCreator(null));
			// TODO Remove client debug physics.
			// Physics.getInstance().getClientProxy().getPhysicsOverWorld().debugSpawn(world);
			// debugSpawn(Minecraft.getMinecraft().theWorld);
		}
	}

	public void debugSpawn(World world) {
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

	@Override
	public EnumBinding getEnumBinding() {
		return EnumBinding.FIRE;
	}
}
