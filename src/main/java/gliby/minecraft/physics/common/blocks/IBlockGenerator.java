package gliby.minecraft.physics.common.blocks;

import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

public interface IBlockGenerator {

    JsonObject write(ResourceLocation resourceLocation, Block block);

}
