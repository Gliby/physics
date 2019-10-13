package gliby.minecraft.physics.common.blocks;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class DefaultBlockGenerator implements IBlockGenerator {

    @Override
    public JsonObject write(ResourceLocation resourceLocation, Block block) {
        JsonObject writable = new JsonObject();
        float hardness = block.getBlockHardness(null, null, null);
        writable.addProperty("mass", MathHelper.clamp(hardness * 20, 1, Float.MAX_VALUE));
        writable.addProperty("friction", (1 - block.slipperiness) * 5);
        /*
         * if (block.getCollisionBoundingBox(null, new BlockPos(0, 0, 0), null)
         * == null) { writable.addProperty("collisionEnabled", false); }
         */
        if (block.getBlockState().getBaseState().getPropertyKeys().contains("explode") || hardness < 0) {
            writable.addProperty("shouldSpawnInExplosion", false);
        }

        JsonArray actions = new JsonArray();
        actions.add(new JsonPrimitive("EnvironmentGravity"));
        actions.add(new JsonPrimitive("EnvironmentResponse"));
        /*
         * hasMethod(block.getClass(), "onEntityCollidedWithBlock")) {
         * System.out.println( "Has special method!"); actions.add(new
         * JsonPrimitive("BlockInheritance")); actions.add(new
         * JsonPrimitive("ClientBlockInheritance")); }
         *
         * writable.add("actions", actions); String fileName =
         */

        return writable;
    }

}
