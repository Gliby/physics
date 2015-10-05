package net.gliby.physics.common.blocks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.minecraft.block.Block;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.common.registry.GameRegistry.UniqueIdentifier;

public class DefaultBlockGenerator implements IBlockGenerator {

	@Override
	public JsonObject write(UniqueIdentifier uniqueIdentifer, Block block) {
		JsonObject writable = new JsonObject();
		float hardness = block.getBlockHardness(null, null);
		writable.addProperty("mass", MathHelper.clamp_float(hardness * 20, 1, Float.MAX_VALUE));
		writable.addProperty("friction", (1 - block.slipperiness) * 5);
		/*
		 * if (block.getCollisionBoundingBox(null, new BlockPos(0, 0, 0), null)
		 * == null) { writable.addProperty("collisionEnabled", false); }
		 */
		if (block.getBlockState().getBaseState().getPropertyNames().contains("explode") || hardness < 0)

		{
			writable.addProperty("shouldSpawnInExplosion", false);
		}

		JsonArray mechanics = new JsonArray();
		mechanics.add(new JsonPrimitive("EnvironmentGravity"));
		mechanics.add(new JsonPrimitive("EnvironmentResponse"));
		/*
		 * hasMethod(block.getClass(), "onEntityCollidedWithBlock")) {
		 * System.out.println( "Has special method!"); mechanics.add(new
		 * JsonPrimitive("BlockInheritance")); mechanics.add(new
		 * JsonPrimitive("ClientBlockInheritance")); }
		 *
		 * writable.add("mechanics", mechanics); String fileName =
		 */

		return writable;
	}

}
