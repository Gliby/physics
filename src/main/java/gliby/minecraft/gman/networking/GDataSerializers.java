package gliby.minecraft.gman.networking;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.io.IOException;

public class GDataSerializers {
    // Custom Vector serializer.
    public static final DataSerializer<Vector3f> VECTOR3F = new DataSerializer<Vector3f>()
    {
        public void write(PacketBuffer buf, Vector3f value)
        {
            buf.writeFloat(value.getX());
            buf.writeFloat(value.getY());
            buf.writeFloat(value.getZ());
        }
        public Vector3f read(PacketBuffer buf) throws IOException
        {
            return new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat());
        }
        public DataParameter<Vector3f> createKey(int id)
        {
            return new DataParameter<Vector3f>(id, this);
        }
        public Vector3f copyValue(Vector3f value)
        {
            return value;
        }
    };

    // Custom Quat serializer.
    public static final DataSerializer<Quat4f> QUAT4F = new DataSerializer<Quat4f>()
    {
        public void write(PacketBuffer buf, Quat4f value)
        {
            buf.writeFloat(value.getX());
            buf.writeFloat(value.getY());
            buf.writeFloat(value.getZ());
            buf.writeFloat(value.getW());

        }
        public Quat4f read(PacketBuffer buf) throws IOException
        {
            return new Quat4f(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
        }
        public DataParameter<Quat4f> createKey(int id)
        {
            return new DataParameter<Quat4f>(id, this);
        }
        public Quat4f copyValue(Quat4f value)
        {
            return value;
        }
    };

    static {
        DataSerializers.registerSerializer(VECTOR3F);
        DataSerializers.registerSerializer(QUAT4F);
    }
}
