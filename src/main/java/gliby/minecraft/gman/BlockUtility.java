package gliby.minecraft.gman;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 */
public class BlockUtility {

    /**
     * @param inputState
     * @param buf
     * @author Zaggy1024
     */
    public static void serializeBlockState(IBlockState inputState, ByteBuf buf) {
        int stateID = Block.getStateId(inputState);
        buf.writeInt(stateID);
        IBlockState metaState = Block.getStateById(stateID);
        IBlockState defaultState = inputState.getBlock().getDefaultState();

        for (IProperty property : defaultState.getProperties().keySet()) {
            Comparable<?> metaValue = metaState.getValue(property);
            Comparable<?> defaultValue = defaultState.getValue(property);

            if (metaValue == defaultValue) {
                int valueID = 0;
                Comparable<?> inputValue = inputState.getValue(property);

                for (Comparable<?> checkValue : (Collection<Comparable<?>>) property.getAllowedValues()) {
                    if (inputValue.equals(checkValue)) {
                        break;
                    }

                    valueID++;
                }

                buf.writeInt(valueID);
            }
        }
    }

    /**
     * @param buf
     * @return
     * @author Zaggy1024
     */
    public static IBlockState deserializeBlockState(ByteBuf buf) {
        IBlockState metaState = Block.getStateById(buf.readInt());
        IBlockState outState = metaState;
        IBlockState defaultState = metaState.getBlock().getDefaultState();

        new ArrayList<IProperty>();

        for (IProperty property :  metaState.getProperties().keySet()) {
            Comparable<?> metaValue = metaState.getValue(property);
            Comparable<?> defaultValue = defaultState.getValue(property);

            if (metaValue == defaultValue) {
                int valueID = buf.readInt();
                int i = 0;
                // todo 1.12.2 port VERY IMPORTANT
//                for (Comparable<?> checkValue : (Collection<Comparable<?>>) property.getAllowedValues()) {
//                    if (i == valueID) {
//                        outState = outState.withProperty(property, (Comparable<?>) checkValue);
//                        break;
//                    }
//
//                    i++;
//                }
            }
        }

        return outState;
    }
}
