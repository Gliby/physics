package gliby.minecraft.gman;

import javax.vecmath.Vector3f;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 *
 */
public class EntityUtility {

	public static Vector3f calculateRay(Entity base, float distance, Vector3f offset) {
		Vec3 vec3 = base.getPositionVector();
		Vec3 vec31 = base.getLook(1);
		Vec3 vec32 = vec3.addVector(vec31.xCoord * distance, vec31.yCoord * distance, vec31.zCoord * distance);
		Vector3f lookAt = new Vector3f((float) vec32.xCoord, (float) vec32.yCoord, (float) vec32.zCoord);
		lookAt.sub(offset);
		return lookAt;
	}

	public static ItemStack getItemStackFromEntityItem(EntityItem entityItem) {
		return entityItem.getDataWatcher().getWatchableObjectItemStack(10);
	}

	public static Entity spawnEntityScheduled(final World world, final Entity entity) {
		MinecraftServer.getServer().addScheduledTask(new Runnable() {

			@Override
			public void run() {
				world.spawnEntityInWorld(entity);
			}

		});
		return entity;
	}

	/**
	 * @param pos
	 * @return
	 */
	public static Vector3f toVector3f(Vec3 pos) {
		return new Vector3f((float) pos.xCoord, (float) pos.yCoord, (float) pos.zCoord);
	}

	public static Vector3f getPositionEyes(Entity base) {
		return new Vector3f((float) base.posX, (float) base.posY + base.getEyeHeight(), (float) base.posZ);
	}

	public static MovingObjectPosition rayTrace(Entity base, double distance) {
		Vec3 vec3 = getPositionEyesMC(base);
		Vec3 vec31 = base.getLook(1);
		Vec3 vec32 = vec3.addVector(vec31.xCoord * distance, vec31.yCoord * distance, vec31.zCoord * distance);
		return base.worldObj.rayTraceBlocks(vec3, vec32, false, false, true);
	}

	private static Vec3 getPositionEyesMC(Entity base) {
		return new Vec3(base.posX, base.posY + base.getEyeHeight(), base.posZ);
	}

	public static boolean isEntityInChunk(Entity entity, int chunkCoordX, int chunkCoordZ) {
		int chunkX = MathHelper.floor_double(entity.posX / 16.0D);
		int chunkZ = MathHelper.floor_double(entity.posZ / 16.0D);
		return chunkX != chunkCoordX || chunkZ != chunkCoordZ;
	}
}
