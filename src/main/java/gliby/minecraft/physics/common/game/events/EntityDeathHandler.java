package gliby.minecraft.physics.common.game.events;

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
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class EntityDeathHandler {

    PhysicsOverworld physicsOverworld;
    Map<Class<? extends Entity>, ArrayList<ModelPart>> modelRegistry = new HashMap<Class<? extends Entity>, ArrayList<ModelPart>>();

    public EntityDeathHandler(PhysicsOverworld overworld) {
        this.physicsOverworld = overworld;
        // TODO (0.7.0) improvement: Import from disk instead of using Reflection.
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        for (Object obj : renderManager.entityRenderMap.entrySet()) {
            Map.Entry<Class<? extends Entity>, RenderLiving> entry = (Map.Entry<Class<? extends Entity>, RenderLiving>) obj;
            Class<? extends Entity> entityClass = entry.getKey();
            RenderLiving renderLiving = entry.getValue();
            ArrayList<ModelPart> modelParts = generateModelParts(renderLiving.getMainModel());
            modelRegistry.put(entityClass, generateModelParts(renderLiving.getMainModel()));
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void handleEvent(LivingDeathEvent event) {
        PhysicsWorld physicsWorld = physicsOverworld.getPhysicsByWorld(event.getEntity().world);
        ArrayList<ModelPart> modelParts = modelRegistry.get(event.getEntity().getClass());
        if (modelParts != null) {
            for (int i = 0; i < modelParts.size(); i++) {
                ModelPart model = modelParts.get(i);
                Transform transform = new Transform();
                transform.setIdentity();
                transform.origin.add(new Vector3f(model.getModelBox().posX1 + model.getModelBox().posX2,
                        model.getModelBox().posY1 + model.getModelBox().posY2,
                        model.getModelBox().posZ1 + model.getModelBox().posZ2));
                transform.origin.scale(0.5f);
                transform.origin.add(model.getPosition());
                transform.origin.scale(-0.0625f);
                // Place in world.
                Vector3f extent = new Vector3f(model.getModelBox().posX2 - model.getModelBox().posX1,
                        model.getModelBox().posY2 - model.getModelBox().posY1,
                        model.getModelBox().posZ2 - model.getModelBox().posZ1);
                // Adjust to minecraft's scale.
                extent.scale(0.0625f);
                extent.scale(0.5f);
                transform.origin.add(new Vector3f(-0.5f, event.getEntity().getEyeHeight(), -0.5f));
                ICollisionShape shape = physicsWorld.createBoxShape(extent);
                if (event.getEntity() != null) {
                    float yaw = event.getEntity().rotationYaw;
                    Quat4f rotation = new Quat4f();
                    QuaternionUtil.setEuler(rotation, yaw, 0, 0);
                    // QuaternionUtil.quatRotate(rotation, transform.origin,
                    // transform.origin);
                    transform.setRotation(rotation);
                }

                float width = (float) event.getEntity().getEntityBoundingBox().maxX
                        - (float) event.getEntity().getEntityBoundingBox().minX;
                float height = (float) event.getEntity().getEntityBoundingBox().maxY
                        - (float) event.getEntity().getEntityBoundingBox().minY;
                float length = (float) event.getEntity().getEntityBoundingBox().maxZ
                        - (float) event.getEntity().getEntityBoundingBox().minZ;

                transform.origin.add(
                        new Vector3f((float) event.getEntity().posX, (float) event.getEntity().posY, (float) event.getEntity().posZ));
                transform.origin.sub(new Vector3f(width / 2, height / 2, length / 2));
                IRigidBody rigidBody = physicsWorld.createRigidBody(null, transform, 10, shape);
                physicsWorld.addRigidBody(rigidBody);
                event.getEntity().setDead();

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
                    ModelBox box = modelRenderer.cubeList.get(i1);
                    proxyList.add(new ModelPart(new Vector3f(modelRenderer.rotationPointX, modelRenderer.rotationPointY,
                            modelRenderer.rotationPointZ), box));
                }
            }
        }
        return proxyList;
    }

}
