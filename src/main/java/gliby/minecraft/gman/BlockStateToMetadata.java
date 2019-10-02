package gliby.minecraft.gman;

import io.netty.buffer.ByteBuf;

import java.util.*;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.Block;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.IBlockState;

import com.google.common.collect.*;

/**
 * Utilities to convert block states to metadata and the reverse.
 *
 * @author Zaggy1024
 */
public class BlockStateToMetadata
{
    public static final BitMask MAXMETAVALUE = BitMask.forValueCount(16);

    private static final Map<Collection<IProperty<?>>, List<IProperty<?>>> SORTED_PROPERTIES = new HashMap<>();

    private static final Map<Collection<? extends Comparable<?>>, Comparator<?>> VALUES_SORTERS = new HashMap<>();
    private static final Map<Collection<? extends Comparable<?>>, List<? extends Comparable<?>>> VALUES_SORTED = new HashMap<>();

    public static List<IProperty<?>> getSortedProperties(Collection<IProperty<?>> properties)
    {
        List<IProperty<?>> output = SORTED_PROPERTIES.get(properties);

        if (output == null)
        {
            output = new ArrayList<IProperty<?>>(properties);
            Collections.sort(output, new Comparator<IProperty<?>>()
            {
                @Override
                public int compare(IProperty<?> prop1, IProperty<?> prop2)
                {
                    // Special case "variant" properties to always be last so that if we add variants it doesn't mess up loading old worlds.
                    boolean prop1Var = prop1.getName().equals("variant");
                    boolean prop2Var = prop2.getName().equals("variant");

                    if (prop1Var && prop2Var)
                        return 0;
                    else if (prop1Var)
                        return 1;
                    else if (prop2Var)
                        return -1;

                    return prop1.getName().compareTo(prop2.getName());
                }
            });
            output = ImmutableList.copyOf(output);
            SORTED_PROPERTIES.put(ImmutableList.copyOf(properties), output);
        }

        return output;
    }

    public static List<IProperty<?>> getSortedPropertiesDumb(Collection<IProperty<?>> properties)
    {
        return getSortedProperties(properties);
    }

    public static <T extends Comparable<T>> void setSorter(Collection<T> values, Comparator<T> sorter)
    {
        VALUES_SORTERS.put(ImmutableSet.copyOf(values), sorter);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>> List<T> getSortedValues(IProperty<T> property)
    {
        Collection<T> unsortedValues = property.getAllowedValues();
        Collection<T> unsortedValuesKey = ImmutableSet.copyOf(unsortedValues);
        List<T> sortedValues = (List<T>) VALUES_SORTED.get(unsortedValues);

        if (sortedValues == null)
        {
            boolean hasSorter = VALUES_SORTERS.containsKey(unsortedValues);

            if (unsortedValues instanceof List<?> && !hasSorter)
            {	// Values have an order that we can use.
                sortedValues = ImmutableList.copyOf(unsortedValues);
            }
            else
            {	// We have to sort the values.
                sortedValues = new ArrayList<>(unsortedValues);

                if (hasSorter)
                {
                    Collections.sort(sortedValues, (Comparator<T>) VALUES_SORTERS.get(unsortedValuesKey));
                }
                else
                {
                    try
                    {
                        Collections.sort(sortedValues);
                    }
                    catch (ClassCastException e)
                    {
                        throw new RuntimeException("Property " + property + " cannot be sorted with its values' comparators, "
                                + "as they do not all expect the same types. "
                                + "Please set a comparator for getSortedValues to use.", e);
                    }
                }

                sortedValues = ImmutableList.copyOf(sortedValues);
            }

            VALUES_SORTED.put(unsortedValues, sortedValues);
        }

        return sortedValues;
    }

    private static final class MetadataStruct
    {
        int metadata = 0;
        int offset = 0;
    }

    private static <T extends Comparable<T>> void addMetaForProperty(MetadataStruct struct, IBlockState state, IProperty<T> property)
    {
        T value = state.getValue(property);
        List<T> values = getSortedValues(property);
        int index = values.indexOf(value);

        BitMask mask = BitMask.forValueCount(values.size(), struct.offset);
        struct.metadata = mask.encode(struct.metadata, index);

        struct.offset += mask.getBitCount();
    }

    /**
     * Gets the IBlockState represented by the metadata passed to the function, filtered by an array of properties.
     *
     * @param state The state to convert to metadata.
     * @param properties The properties to store in the metadata, in the desired order.
     * @return The metadata to represent the IBlockState.
     */
    public static int getMetaForBlockState(IBlockState state, IProperty<?>... properties)
    {
        MetadataStruct struct = new MetadataStruct();

        for (IProperty<?> property : properties)
        {
            addMetaForProperty(struct, state, property);
        }

        if (struct.offset > MAXMETAVALUE.getBitCount())
        {
            throw new RuntimeException("Attempted to store an IBlockState that requires " + struct.offset + " bits in " + MAXMETAVALUE.getBitCount() + " bits of metadata");
        }

        return struct.metadata;
    }

    /**
     * Gets the IBlockState represented by the metadata passed to the function.
     *
     * @param state The state to convert to metadata.
     * @return The metadata to represent the IBlockState.
     */
    public static int getMetaForBlockState(IBlockState state)
    {
        return getMetaForBlockState(state, getSortedPropertiesDumb(state.getProperties().keySet()).toArray(new IProperty<?>[0]));
    }

    /**
     * @return Pair of the new state and the new offset.
     */
    private static <T extends Comparable<T>> Pair<IBlockState, Integer> decodePropertyFromMetadata(IBlockState state, int metadata, IProperty<T> property, int offset)
    {
        List<T> values = getSortedValues(property);

        BitMask mask = BitMask.forValueCount(values.size(), offset);
        int metaValue = mask.decode(metadata);

        T propValue = values.get(metaValue);

        state = state.withProperty(property, propValue);

        offset += mask.getBitCount();

        return Pair.of(state, offset);
    }

    /**
     * Gets the IBlockState represented by the metadata passed to the function, filtered by an array of properties.
     *
     * @param state The state to base the new state off of (will usually be Block.getDefaultState()).
     * @param metadata The metadata to restore to an IBlockState.
     * @param properties The properties to restore from the metadata, in the order they were passed to getMetaForBlockState.
     * @return The restored IBlockState.
     */
    public static <T extends Comparable<T>> IBlockState getBlockStateFromMeta(IBlockState state, int metadata, IProperty<?>... properties)
    {
        int offset = 0;

        for (IProperty<?> property : properties)
        {
            Pair<IBlockState, Integer> decoded = decodePropertyFromMetadata(state, metadata, property, offset);
            state = decoded.getLeft();
            offset = decoded.getRight();
        }

        if (offset > MAXMETAVALUE.getBitCount())
        {
            throw new RuntimeException("Attempted to retrieve a property from an IBlockState past " + MAXMETAVALUE.getBitCount() + " bits, the maximum metadata bit count.");
        }

        return state;
    }

    /**
     * Gets the IBlockState represented by the metadata passed to the function.
     *
     * @param state The state to base the new state off of (will usually be Block.getDefaultState()).
     * @param metadata The metadata to restore to an IBlockState.
     * @return The restored IBlockState.
     */
    public static IBlockState getBlockStateFromMeta(IBlockState state, int metadata)
    {
        return getBlockStateFromMeta(state, metadata, getSortedPropertiesDumb(state.getProperties().keySet()).toArray(new IProperty<?>[0]));
    }

    /**
     * Gets the number of possible values after the provided properties have been stored in metadata.
     * Used to determine how many variants a block can store after storing other properties (like facing direction).
     *
     * @param properties The properties that must be stored.
     * @return Number of possible values.
     */
    public static int getMetadataLeftAfter(IProperty<?>... properties)
    {
        int bitsLeft = MAXMETAVALUE.getBitCount();

        for (IProperty<?> property : properties)
        {
            BitMask mask = BitMask.forValueCount(property.getAllowedValues().size());
            bitsLeft -= mask.getBitCount();
        }

        if (bitsLeft > 0)
        {
            int vals = (int) Math.pow(2, bitsLeft);
            return vals;
        }

        return 1;
    }

    /**
     * Writes a block state to a {@link ByteBuf}.
     */
    public static void serializeBlockState(IBlockState inputState, ByteBuf buf)
    {
        int stateID = Block.getStateId(inputState);
        buf.writeInt(stateID);
        IBlockState metaState = Block.getStateById(stateID);
        IBlockState defaultState = inputState.getBlock().getDefaultState();

        List<IProperty<?>> propertyList = getSortedPropertiesDumb(defaultState.getProperties().keySet());

        for (IProperty<?> property : propertyList)
        {
            if (metaState.getValue(property).equals(defaultState.getValue(property)))
            {
                buf.writeInt(getSortedValues(property).indexOf(inputState.getValue(property)));
            }
        }
    }

    private static <T extends Comparable<T>> IBlockState decodePropertyFromInteger(IBlockState state, IProperty<T> property, int value)
    {
        return state.withProperty(property, getSortedValues(property).get(value));
    }

    /**
     * Reads a blockstate from a {@link ByteBuf} that has been written to using {@link #serializeBlockState}.
     */
    public static IBlockState deserializeBlockState(ByteBuf buf)
    {
        IBlockState outState = Block.getStateById(buf.readInt());
        IBlockState defaultState = outState.getBlock().getDefaultState();

        for (IProperty<?> property : getSortedPropertiesDumb(outState.getProperties().keySet()))
        {
            if (outState.getValue(property).equals(defaultState.getValue(property)))
            {
                outState = decodePropertyFromInteger(outState, property, buf.readInt());
            }
        }

        return outState;
    }
}


/**
 * @author Zaggy1024
 */
class BitMask
{
    public static BitMask forValueCount(int values, int shift)
    {
        return new BitMask(values == 1 ? 0 : (values - 1) | 1, shift);
    }

    public static BitMask forValueCount(int values)
    {
        return forValueCount(values, 0);
    }

    protected final int mask;
    protected final int shift;
    protected final int bits;

    /**
     * Creates a BitwiseMask from this mask and shift position. This will set all bits between the highest and lowest of the mask input to
     * make sure that the mask is contiguous.
     */
    public BitMask(int maskIn, int shift)
    {
        if (maskIn == 0)
        {
            this.mask = maskIn;
            this.shift = shift;
            this.bits = 0;
        }
        else
        {
            int firstBit = Integer.lowestOneBit(maskIn);
            int lastBit = Integer.highestOneBit(maskIn);
            int firstBitPos = Integer.numberOfTrailingZeros(firstBit);

            this.mask = (((lastBit - 1) | lastBit) & -firstBit) >> firstBitPos;
            this.shift = shift + firstBitPos;
            this.bits = Integer.bitCount(mask);
        }
    }

    public BitMask(int mask)
    {
        this(mask, 0);
    }

    /**
     * Get the bitwise mask.
     */
    public int getMask()
    {
        return mask;
    }

    /**
     * Get the number of bits used by the mask.
     */
    public int getBitCount()
    {
        return bits;
    }

    public int getShifted(int value)
    {
        return value << shift;
    }

    public int getShiftedMask()
    {
        return getShifted(mask);
    }

    /**
     * Returns the baseValue encoded with maskValue at this bit mask's location.
     */
    public int encode(int baseValue, int maskValue)
    {
        int shiftMask = getShiftedMask();
        return (baseValue & ~shiftMask) | (getShifted(maskValue) & shiftMask);
    }

    /**
     * Gets the value encoded by this bitwise mask.
     */
    public int decode(int value)
    {
        return (value >> shift) & mask;
    }

    public BitMask shiftTo(int shift)
    {
        return new BitMask(mask, shift - bits);
    }

	/*// Unit tests
	protected static void throwIf(boolean value)
	{
		if (value)
		{
			throw new RuntimeException("Unit tests failed.");
		}
	}

	static
	{
		int test = 2436;
		int testMask = 1023;
		int encTest = 546;
		BitwiseMask testMaskObj = new BitwiseMask(test);
		throwIf(testMaskObj.getBitCount() != 10);
		throwIf(testMaskObj.getMask() != testMask);
		throwIf(testMaskObj.getMask() != testMask);
		throwIf(testMaskObj.decode(testMaskObj.encode(0, encTest)) != encTest);

		test = 32;
		testMask = 1;
		encTest = 1;
		testMaskObj = new BitwiseMask(test);
		throwIf(testMaskObj.getBitCount() != 1);
		throwIf(testMaskObj.getMask() != testMask);
		throwIf(testMaskObj.getMask() != testMask);
		throwIf(testMaskObj.decode(testMaskObj.encode(0, encTest)) != encTest);
		test = 402980;
		testMask = 131071;
		encTest = 1;
		testMaskObj = new BitwiseMask(test);
		throwIf(testMaskObj.getBitCount() != 17);
		throwIf(testMaskObj.getMask() != testMask);
		throwIf(testMaskObj.getMask() != testMask);
		throwIf(testMaskObj.decode(testMaskObj.encode(0, encTest)) != encTest);
	}*/
}