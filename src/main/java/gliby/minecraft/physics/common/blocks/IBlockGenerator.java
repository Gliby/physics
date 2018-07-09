package gliby.minecraft.physics.common.blocks;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry.UniqueIdentifier;

public interface IBlockGenerator {

	public JsonObject write(UniqueIdentifier uniqueIdentifer, Block block);

}
