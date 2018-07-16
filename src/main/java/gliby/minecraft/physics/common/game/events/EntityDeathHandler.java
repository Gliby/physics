package gliby.minecraft.physics.common.game.events;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.bulletphysicsx.linearmath.QuaternionUtil;
import com.bulletphysicsx.linearmath.Transform;

import gliby.minecraft.physics.common.physics.ModelPart;
import gliby.minecraft.physics.common.physics.PhysicsOverworld;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.ICollisionShape;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 *
 */
public class EntityDeathHandler {

	PhysicsOverworld physicsOverworld;

	public EntityDeathHandler(PhysicsOverworld overworld) {
		this.physicsOverworld = overworld;
		// TODO improvement: Import from .json instead of this.
		RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
		for (Object obj : renderManager.entityRenderMap.entrySet()) {
			Map.Entry<Class<? extends Entity>, RenderLiving> entry = (Map.Entry<Class<? extends Entity>, RenderLiving>) obj;
			Class<? extends Entity> entityClass = entry.getKey();
			RenderLiving renderLiving = entry.getValue();
			ArrayList<ModelPart> modelParts = generateModelParts(renderLiving.getMainModel());
			modelRegistry.put(entityClass, generateModelParts(renderLiving.getMainModel()));
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
				Vector3 origin = new Vector3();
				Quaternion rotation = new Quaternion();
				rotation.idt();
				
				origin.add(new Vector3(model.getModelBox().posX1 + model.getModelBox().posX2,
						model.getModelBox().posY1 + model.getModelBox().posY2,
						model.getModelBox().posZ1 + model.getModelBox().posZ2));
				origin.scl(0.5f);
				origin.add(model.getPosition());
				origin.scl(-0.0625f);
				origin.add(new Vector3(-0.5f, event.entity.getEyeHeight(), -0.5f));
				// Place in world.
				Vector3 extent = new Vector3(model.getModelBox().posX2 - model.getModelBox().posX1,
						model.getModelBox().posY2 - model.getModelBox().posY1,
						model.getModelBox().posZ2 - model.getModelBox().posZ1);
				// Adjust to minecraft's scale.
				extent.scl(0.0625f);
				extent.scl(0.5f);
				ICollisionShape shape = physicsWorld.createBoxShape(extent);
				if (event.entityLiving != null) {
					float yaw = event.entityLiving.rotationYaw;
					// QuaternionUtil.quatRotate(rotation, transform.origin,
					// transform.origin);
				}

				float width = (float) event.entity.getEntityBoundingBox().maxX
						- (float) event.entity.getEntityBoundingBox().minX;
				float height = (float) event.entity.getEntityBoundingBox().maxY
						- (float) event.entity.getEntityBoundingBox().minY;
				float length = (float) event.entity.getEntityBoundingBox().maxZ
						- (float) event.entity.getEntityBoundingBox().minZ;

				origin.add(
						new Vector3((float) event.entity.posX, (float) event.entity.posY, (float) event.entity.posZ));
				origin.sub(new Vector3(width / 2, height / 2, length / 2));
				Matrix4 transform = new Matrix4();
				transform.idt();
				transform.set(origin, rotation);
				IRigidBody rigidBody = physicsWorld.createRigidBody(null, transform, 10, shape);
				physicsWorld.addRigidBody(rigidBody);
				event.entity.setDead();

			}
		}
	}

	protected final Vector3 getVectorForRotation(float pitch, float yaw) {
		float f2 = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
		float f3 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
		float f4 = -MathHelper.cos(-pitch * 0.017453292F);
		float f5 = MathHelper.sin(-pitch * 0.017453292F);
		return new Vector3((f3 * f4), f5, (f2 * f4));
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
					proxyList.add(new ModelPart(new Vector3(modelRenderer.rotationPointX, modelRenderer.rotationPointY,
							modelRenderer.rotationPointZ), box));
				}
			}
		}
		return proxyList;
	}

}
