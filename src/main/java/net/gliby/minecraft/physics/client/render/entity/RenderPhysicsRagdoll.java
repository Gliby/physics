/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.minecraft.physics.client.render.entity;

import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.vecmath.Vector3f;

import net.gliby.minecraft.physics.client.render.RenderHandler;
import net.gliby.minecraft.physics.common.entity.EntityPhysicsBase;
import net.gliby.minecraft.physics.common.physics.AttachementPoint;
import net.gliby.minecraft.physics.common.physics.ModelPart;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;

/**
 *
 */
public class RenderPhysicsRagdoll extends RenderPhysics {

	/**
	 * @param renderManager
	 */
	public RenderPhysicsRagdoll(RenderHandler renderHandler, RenderManager renderManager) {
		super(renderHandler, renderManager);
	}

	public void draw(Entity uncast, double worldX, double worldY, double worldZ, float partialTick, int color, boolean outline) {
		/*
		 * EntityPhysicsRagdoll entity = (EntityPhysicsRagdoll) uncast; Vector3f
		 * world = RenderUtilities.getWorldTranslation(Minecraft.getMinecraft(),
		 * partialTick); glPushMatrix(); glTranslatef(-world.x + 0.5f, -world.y
		 * + 0.5f, -world.z + 0.5f); glDisable(GL_TEXTURE_2D); for (int i = 0; i
		 * < entity.networkBounds.length; i++) { Vector3f position = new
		 * Vector3f(entity.renderPositions[i]); Quat4f rotation =
		 * entity.renderRotations[i]; Vector3f halfExtent = new
		 * Vector3f(entity.networkBounds[i]); Transform transform = new
		 * Transform(); transform.setIdentity(); transform.origin.set(position);
		 * transform.setRotation(rotation);
		 * 
		 * FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
		 * RenderUtilities.setBufferFromTransform(buffer, transform);
		 * 
		 * glPushMatrix();
		 * 
		 * glMultMatrix(buffer); glScalef(2f * halfExtent.x, 2f * halfExtent.y,
		 * 2f * halfExtent.z); RenderUtilities.drawCube(1.0f, true);
		 * glLineWidth(1.5f); glColor4f(1.0f, 0.2f, 0.2f, 0.6f);
		 * RenderUtilities.drawCube(1.0f, false);
		 * 
		 * glPopMatrix(); } glEnable(GL_TEXTURE_2D); glPopMatrix();
		 */
	}

	private ArrayList<AttachementPoint> generateAttachementPoints(ModelBase modelBase) {
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
					if (point.bodyA == null) {
						System.out.println("Body A set");
						point.setBodyA(model);
					} else if (point.bodyB == null) {
						System.out.println("Body B set");
						point.setBodyB(model);
					}
				}
			}
		}
		return points;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.gliby.physics.client.render.entity.RenderPhysics#getRenderHitPoint(
	 * net.gliby.physics.common.entity.EntityPhysicsBase, float)
	 */
	@Override
	public Vector3f getRenderHitPoint(EntityPhysicsBase entity, float partialTick) {
		return null;
	}
}