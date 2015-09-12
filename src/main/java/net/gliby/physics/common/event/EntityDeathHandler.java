/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.event;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.lwjgl.input.Keyboard;

import net.gliby.physics.client.ClientPhysicsOverworld;
import net.gliby.physics.common.physics.ICollisionShape;
import net.gliby.physics.common.physics.IRigidBody;
import net.gliby.physics.common.physics.ModelPart;
import net.gliby.physics.common.physics.PhysicsWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.bulletphysics.linearmath.QuaternionUtil;
import com.bulletphysics.linearmath.Transform;

/**
 *
 */
public class EntityDeathHandler {

	ClientPhysicsOverworld physicsOverworld;

	public EntityDeathHandler(ClientPhysicsOverworld overworld) {
		this.physicsOverworld = overworld;
		// TODO Import from .json instead of this.
		RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
		for (Object obj : renderManager.entityRenderMap.entrySet()) {
			Map.Entry<Class<? extends Entity>, RenderLiving> entry = (Map.Entry<Class<? extends Entity>, RenderLiving>) obj;
			Class<? extends Entity> entityClass = entry.getKey();
			if (entry.getValue() instanceof RenderLiving) {
				RenderLiving renderLiving = entry.getValue();
				ArrayList<ModelPart> modelParts = generateModelParts(renderLiving.getMainModel());
				modelRegistry.put(entityClass, generateModelParts(renderLiving.getMainModel()));
			}
		}
	}

	Map<Class<? extends Entity>, ArrayList<ModelPart>> modelRegistry = new HashMap<Class<? extends Entity>, ArrayList<ModelPart>>();

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void handleEvent(LivingDeathEvent event) {
		PhysicsWorld physicsWorld = physicsOverworld.getPhysicsByWorld(event.entity.worldObj);
		ArrayList<ModelPart> modelParts = modelRegistry.get(event.entity.getClass());
		if (modelParts != null) {
			for (int i = 0; i < modelParts.size(); i++) {
				ModelPart model = modelParts.get(i);
				Transform transform = new Transform();
				transform.setIdentity();
				transform.origin.add(new Vector3f(model.getModelBox().posX1 + model.getModelBox().posX2, model.getModelBox().posY1 + model.getModelBox().posY2, model.getModelBox().posZ1 + model.getModelBox().posZ2));
				transform.origin.scale(0.5f);
				transform.origin.add(model.getPosition());
				transform.origin.scale(-0.0625f);
				// Place in world.
				Vector3f extent = new Vector3f(model.getModelBox().posX2 - model.getModelBox().posX1, model.getModelBox().posY2 - model.getModelBox().posY1, model.getModelBox().posZ2 - model.getModelBox().posZ1);
				// Adjust to minecraft's scale.
				extent.scale(0.0625f);
				extent.scale(0.5f);
				transform.origin.add(new Vector3f(-0.5f, event.entity.getEyeHeight(), -0.5f));
				ICollisionShape shape = physicsWorld.createBoxShape(extent);
				if (event.entityLiving != null) {
					float yaw = event.entityLiving.rotationYaw;
					Quat4f rotation = new Quat4f();
					QuaternionUtil.setEuler(rotation, yaw, 0, 0);
//					QuaternionUtil.quatRotate(rotation, transform.origin, transform.origin);
					transform.setRotation(rotation);
				}

				float width = (float) event.entity.getEntityBoundingBox().maxX - (float) event.entity.getEntityBoundingBox().minX;
				float height = (float) event.entity.getEntityBoundingBox().maxY - (float) event.entity.getEntityBoundingBox().minY;
				float length = (float) event.entity.getEntityBoundingBox().maxZ - (float) event.entity.getEntityBoundingBox().minZ;

				transform.origin.add(new Vector3f((float) event.entity.posX, (float) event.entity.posY, (float) event.entity.posZ));
				transform.origin.sub(new Vector3f(width / 2, height / 2, length / 2));
				IRigidBody rigidBody = physicsWorld.createRigidBody(null, transform, 10, shape);
				physicsWorld.addRigidBody(rigidBody);
				event.entity.setDead();

			}
		}
	}

	protected final Vector3f getVectorForRotation(float pitch, float yaw) {
		float f2 = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
		float f3 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
		float f4 = -MathHelper.cos(-pitch * 0.017453292F);
		float f5 = MathHelper.sin(-pitch * 0.017453292F);
		return new Vector3f((f3 * f4), f5, (f2 * f4));
	}

	public ArrayList<ModelPart> generateModelParts(Object modelBase) {
		ArrayList<ModelPart> proxyList = new ArrayList<ModelPart>();
		for (int i = 0; i < modelBase.getClass().getFields().length; i++) {
			Field field = modelBase.getClass().getFields()[i];
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
					proxyList.add(new ModelPart(new Vector3f(modelRenderer.rotationPointX, modelRenderer.rotationPointY, modelRenderer.rotationPointZ), box));
				}
			}
		}
		return proxyList;
	}

	private float correctRotation(float offset, float original, float scale) {
		float f3;

		for (f3 = original - offset; f3 < -180.0F; f3 += 360.0F) {
			;
		}

		while (f3 >= 180.0F) {
			f3 -= 360.0F;
		}

		return offset + scale * f3;
	}

}
