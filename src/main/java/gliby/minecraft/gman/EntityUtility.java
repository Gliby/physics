package gliby.minecraft.gman;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.vecmath.Vector3f;

/**
 *
 */
public class EntityUtility {

    public static Vector3f calculateRay(Entity base, float distance, Vector3f offset) {
        Vec3d vec3 = base.getPositionVector();
        Vec3d vec31 = base.getLook(1);
        Vec3d vec32 = vec3.addVector(vec31.x * distance, vec31.y * distance, vec31.z * distance);
        Vector3f lookAt = new Vector3f((float) vec32.x, (float) vec32.y, (float) vec32.z);
        lookAt.sub(offset);
        return lookAt;
    }

    public static ItemStack getItemStackFromEntityItem(EntityItem entityItem) {
        return entityItem.getItem();
    }

    public static Entity spawnEntityScheduled(final World world, final Entity entity) {
        world.getMinecraftServer().addScheduledTask(new Runnable() {

            @Override
            public void run() {
                world.spawnEntity(entity);
            }

        });
        return entity;
    }


    /**
     * @param pos
     * @return
     */
    public static Vector3f toVector3f(Vec3d pos) {
        return new Vector3f((float) pos.x, (float) pos.y, (float) pos.z);
    }

    public static Vector3f getPositionEyes(Entity base) {
        return new Vector3f((float) base.posX, (float) base.posY + base.getEyeHeight(), (float) base.posZ);
    }

    public static RayTraceResult rayTrace(Entity base, double distance) {
        Vec3d vec3 = getPositionEyesMC(base);
        Vec3d vec31 = base.getLook(1);
        Vec3d vec32 = vec3.addVector(vec31.x * distance, vec31.y * distance, vec31.z * distance);
        return base.world.rayTraceBlocks(vec3, vec32, false, false, true);
    }

    private static Vec3d getPositionEyesMC(Entity base) {
        return new Vec3d(base.posX, base.posY + base.getEyeHeight(), base.posZ);
    }

    public static boolean isEntityInChunk(Entity entity, int chunkCoordX, int chunkCoordZ) {
        int chunkX = MathHelper.floor(entity.posX / 16.0D);
        int chunkZ = MathHelper.floor(entity.posZ / 16.0D);
        return chunkX != chunkCoordX || chunkZ != chunkCoordZ;
    }
}
