package gliby.minecraft.physics.client.render.world;

import static org.lwjgl.opengl.GL11.*;

import java.nio.FloatBuffer;
import java.util.List;

import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.bulletphysicsx.linearmath.Transform;

import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.client.render.RenderUtilities;
import gliby.minecraft.physics.common.physics.PhysicsOverworld;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.ICollisionShapeChildren;
import gliby.minecraft.physics.common.physics.engine.IConstraint;
import gliby.minecraft.physics.common.physics.engine.IConstraintGeneric6Dof;
import gliby.minecraft.physics.common.physics.engine.IConstraintPoint2Point;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import gliby.minecraft.physics.common.physics.engine.IRope;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 *
 */
public class RenderDebugAdditionalWorld {

	private Physics physics;
	private Minecraft mc;

	public RenderDebugAdditionalWorld(Physics physics) {
		this.mc = Minecraft.getMinecraft();
		this.physics = physics;
	}


	@SubscribeEvent
	public void postRender(RenderWorldLastEvent event) {
		// if (MineFortress.DEBUG_PHYSICS_RENDER)
//		if (Physics.getInstance().getPhysicsOverworld() != null)
//			renderDebugPhysics(physics.getPhysicsOverworld(), event);
		// event);
	}

	private static Transform physicsTransform = new Transform();
	private static FloatBuffer physicsFloatBufferMatrix4x4 = BufferUtils.createFloatBuffer(16);
	private static float[] physicsFloatMatrix4x4 = new float[16];

	/**
	 * Draws debug physics world with inefficient rendering.
	 */
	private void renderDebugPhysics(PhysicsOverworld overworld, RenderWorldLastEvent event) {
		boolean renderConstraint = false;
		boolean renderRopeJoints = true;

		PhysicsWorld physicsWorld = overworld.getPhysicsByWorld(mc.theWorld);
		if (physicsWorld != null) {

			Vector3f world = RenderUtilities.getWorldTranslation(mc, event.partialTicks);
			glEnable(GL_LINE_SMOOTH);
			glDisable(GL_TEXTURE_2D);
			glEnable(GL_BLEND);
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			glPushMatrix();
			// Transform into world + physics offset because Minecraft is
			// special.
			glTranslatef(-world.x + 0.5F, -world.y + 0.5F, -world.z + 0.5F);
			if (renderConstraint) {
				for (int i = 0; i < physicsWorld.getConstraints().size(); i++) {
					IConstraint constraint = physicsWorld.getConstraints().get(i);
					if (constraint.isPoint2Point()) {
						IConstraintPoint2Point point2point = (IConstraintPoint2Point) constraint;
						Vector3f pointA = point2point.getPivotInA(new Vector3f());
						Vector3f pointB = point2point.getPivotInB(new Vector3f());
						glPushMatrix();
						GL11.glBegin(GL11.GL_LINES);
						GL11.glVertex3f(pointA.x, pointA.y, pointA.z);
						GL11.glVertex3f(pointB.x, pointB.y, pointB.z);
						GL11.glEnd();
						glPopMatrix();
					} else if (constraint.isGeneric6Dof()) {
						IConstraintGeneric6Dof generic6Dof = (IConstraintGeneric6Dof) constraint;
						Transform pointA = generic6Dof.getGlobalFrameOffsetA(new Transform());
						Transform pointB = generic6Dof.getGlobalFrameOffsetB(new Transform());
						glPushMatrix();
						glColor4f(1.0f, 0.2f, 0.2f, 1.0f);
						glDisable(GL_DEPTH_TEST);
						glLineWidth(999.0F);
						glBegin(GL11.GL_LINES);
						float size = 0;
						glVertex3f(pointA.origin.x - size, pointA.origin.y - size, pointA.origin.z - size);
						glVertex3f(pointB.origin.x + size, pointB.origin.y + size, pointB.origin.z + size);
						glEnd();
						glEnable(GL_DEPTH_TEST);
						glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
						glLineWidth(1.0F);
						glPopMatrix();
					}
				}
			}

			// for (int i = 0; i < physicsWorld.getRopes().size(); i++) {
			// IRope rope = physicsWorld.getRopes().get(i);
			// glPushMatrix();
			// glColor4f(0.0F, 0.0F, 0.0F, 1.0F);
			//
			// if (renderRopeJoints) {
			// glEnable(GL11.GL_POINT_SMOOTH);
			// glPointSize(20);
			// glBegin(GL11.GL_POINTS);
			// for (Vector3f pos : rope.getSpherePositions()) {
			// GL11.glVertex3f(pos.x, pos.y, pos.z);
			// }
			// glEnd();
			//
			// glPointSize(15);
			// glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			// glBegin(GL11.GL_POINTS);
			// for (Vector3f pos : rope.getSpherePositions()) {
			// GL11.glVertex3f(pos.x, pos.y, pos.z);
			// }
			// glEnd();
			// glDisable(GL11.GL_POINT_SMOOTH);
			// }
			//
			// glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			// glBegin(GL11.GL_LINE_STRIP);
			// for (Vector3f pos : rope.getSpherePositions()) {
			// GL11.glVertex3f(pos.x, pos.y, pos.z);
			// }
			// glEnd();
			// glPopMatrix();
			// }

			for (int i = 0; i < physicsWorld.getRigidBodies().size(); i++) {
				glPushMatrix();
				IRigidBody rigidBody = physicsWorld.getRigidBodies().get(i);
				// Get's world Transform.
				rigidBody.getWorldTransform(physicsTransform); //
				// Converts transform.matrix into a matrix4x4 float array.
				physicsTransform.getOpenGLMatrix(physicsFloatMatrix4x4);
				physicsFloatBufferMatrix4x4.clear(); // Insert float matrix4x4
														// into
				// FloatBuffer because LWJGL takes // FloatBuffer, not float
				// array.
				physicsFloatBufferMatrix4x4.put(physicsFloatMatrix4x4);
				physicsFloatBufferMatrix4x4.flip(); // Apply transformation.
				glMultMatrix(physicsFloatBufferMatrix4x4);
				if (rigidBody.getCollisionShape().isBoxShape()) {
					Vector3f halfExtent = new Vector3f();
					rigidBody.getCollisionShape().getHalfExtentsWithMargin(halfExtent);
					glScalef(2f * halfExtent.x, 2f * halfExtent.y, 2f * halfExtent.z);
					glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
					RenderUtilities.drawCube(1.0f, true);
					glLineWidth(1.5f);
					glColor4f(1.0f, 0.2f, 0.2f, 0.6f);
					RenderUtilities.drawCube(1.0f, false);
				} else if (rigidBody.getCollisionShape().isCompoundShape()) {
					List<ICollisionShapeChildren> children = rigidBody.getCollisionShape().getChildren();
					for (int j = 0; j < children.size(); j++) {
						ICollisionShapeChildren shape = children.get(j);
						if (shape.getCollisionShape().isBoxShape()) {
							Transform transform = shape.getTransform();
							shape.getTransform().getOpenGLMatrix(physicsFloatMatrix4x4);
							physicsFloatBufferMatrix4x4.clear();
							physicsFloatBufferMatrix4x4.put(physicsFloatMatrix4x4);
							physicsFloatBufferMatrix4x4.flip();

							glPushMatrix();
							glMultMatrix(physicsFloatBufferMatrix4x4);
							Vector3f halfExtent = new Vector3f();
							shape.getCollisionShape().getHalfExtentsWithMargin(halfExtent);
							glScalef(2f * halfExtent.x, 2f * halfExtent.y, 2f * halfExtent.z);
							glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
							RenderUtilities.drawCube(1.0f, true);
							glLineWidth(1.5f);
							glColor4f(1.0f, 0.2f, 0.2f, 0.6f);
							RenderUtilities.drawCube(1.0f, false);
							glPopMatrix();
						}
					}
				}
				glPopMatrix();
			}
			glPopMatrix();
			glDisable(GL_LINE_SMOOTH);
			glEnable(GL_TEXTURE_2D);
		}

	}

	public static void renderOffsetAABB(AxisAlignedBB bb) {
		GlStateManager.disableTexture2D();
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		worldrenderer.startDrawingQuads();
		worldrenderer.setNormal(0.0F, 0.0F, -1.0F);
		worldrenderer.addVertex(bb.minX, bb.maxY, bb.minZ);
		worldrenderer.addVertex(bb.maxX, bb.maxY, bb.minZ);
		worldrenderer.addVertex(bb.maxX, bb.minY, bb.minZ);
		worldrenderer.addVertex(bb.minX, bb.minY, bb.minZ);
		worldrenderer.setNormal(0.0F, 0.0F, 1.0F);
		worldrenderer.addVertex(bb.minX, bb.minY, bb.maxZ);
		worldrenderer.addVertex(bb.maxX, bb.minY, bb.maxZ);
		worldrenderer.addVertex(bb.maxX, bb.maxY, bb.maxZ);
		worldrenderer.addVertex(bb.minX, bb.maxY, bb.maxZ);
		worldrenderer.setNormal(0.0F, -1.0F, 0.0F);
		worldrenderer.addVertex(bb.minX, bb.minY, bb.minZ);
		worldrenderer.addVertex(bb.maxX, bb.minY, bb.minZ);
		worldrenderer.addVertex(bb.maxX, bb.minY, bb.maxZ);
		worldrenderer.addVertex(bb.minX, bb.minY, bb.maxZ);
		worldrenderer.setNormal(0.0F, 1.0F, 0.0F);
		worldrenderer.addVertex(bb.minX, bb.maxY, bb.maxZ);
		worldrenderer.addVertex(bb.maxX, bb.maxY, bb.maxZ);
		worldrenderer.addVertex(bb.maxX, bb.maxY, bb.minZ);
		worldrenderer.addVertex(bb.minX, bb.maxY, bb.minZ);
		worldrenderer.setNormal(-1.0F, 0.0F, 0.0F);
		worldrenderer.addVertex(bb.minX, bb.minY, bb.maxZ);
		worldrenderer.addVertex(bb.minX, bb.maxY, bb.maxZ);
		worldrenderer.addVertex(bb.minX, bb.maxY, bb.minZ);
		worldrenderer.addVertex(bb.minX, bb.minY, bb.minZ);
		worldrenderer.setNormal(1.0F, 0.0F, 0.0F);
		worldrenderer.addVertex(bb.maxX, bb.minY, bb.minZ);
		worldrenderer.addVertex(bb.maxX, bb.maxY, bb.minZ);
		worldrenderer.addVertex(bb.maxX, bb.maxY, bb.maxZ);
		worldrenderer.addVertex(bb.maxX, bb.minY, bb.maxZ);
		worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();
		GlStateManager.enableTexture2D();
	}

}
