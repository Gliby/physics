/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.minecraft.gman;

import java.util.ArrayList;
import java.util.Collection;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

/**
 *
 */
public class BlockUtility {

	/**
	 * @author Zaggy1024
	 * @param inputState
	 * @param buf
	 */
	public static void serializeBlockState(IBlockState inputState, ByteBuf buf) {
		int stateID = Block.getStateId(inputState);
		buf.writeInt(stateID);
		IBlockState metaState = Block.getStateById(stateID);
		IBlockState defaultState = inputState.getBlock().getDefaultState();

		for (IProperty property : (Collection<IProperty>) defaultState.getProperties().keySet()) {
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
	 * @author Zaggy1024
	 * @param buf
	 * @return
	 */
	public static IBlockState deserializeBlockState(ByteBuf buf) {
		IBlockState metaState = Block.getStateById(buf.readInt());
		IBlockState outState = metaState;
		IBlockState defaultState = metaState.getBlock().getDefaultState();

		new ArrayList<IProperty>();

		for (IProperty property : (Collection<IProperty>) metaState.getProperties().keySet()) {
			Comparable<?> metaValue = metaState.getValue(property);
			Comparable<?> defaultValue = defaultState.getValue(property);

			if (metaValue == defaultValue) {
				int valueID = buf.readInt();
				int i = 0;

				for (Comparable<?> checkValue : (Collection<Comparable<?>>) property.getAllowedValues()) {
					if (i == valueID) {
						outState = outState.withProperty(property, checkValue);
						break;
					}

					i++;
				}
			}
		}

		return outState;
	}
}
