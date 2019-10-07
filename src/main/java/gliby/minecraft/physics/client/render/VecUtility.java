package gliby.minecraft.physics.client.render;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.bulletphysicsx.linearmath.Transform;
import com.bulletphysicsx.util.ObjectPool;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;

/**
 *
 */
public class VecUtility {

    public static final AxisAlignedBB ZERO_BB = new AxisAlignedBB(Vec3d.ZERO, Vec3d.ZERO);
    public static ObjectPool<Matrix4f> MAT4F_POOL = ObjectPool.get(Matrix4f.class);
    public static ObjectPool<Transform> TRANS_POOL = ObjectPool.get(Transform.class);
    public static ObjectPool<Vector3f> VEC3F_POOL = ObjectPool.get(Vector3f.class);
    public static ObjectPool<Vector3> VEC3_POOL = ObjectPool.get(Vector3.class);
    public static ObjectPool<Quaternion> QUAT_POOL = ObjectPool.get(Quaternion.class);
    public static ObjectPool<Quat4f> QUAT4F_POOL = ObjectPool.get(Quat4f.class);
    private static float[] buffer = new float[16];

    /**
     * Calculates player to world transform and returns accordingly.
     *
     * @param mc
     * @param delta
     * @returns javax.vecmath.Vector3f
     */
    public static Vector3f getWorldTranslation(Minecraft mc, float delta) {
        return new Vector3f((float) mc.player.prevPosX + (float) (mc.player.posX - mc.player.prevPosX) * delta, (float) mc.player.prevPosY + (float) (mc.player.posY - (float) mc.player.prevPosY) * delta, (float) mc.player.prevPosZ + (float) (mc.player.posZ - (float) mc.player.prevPosZ) * delta);
    }

    public static Vector3f getSmoothedEntityPosition(Entity entity, float delta) {
        return new Vector3f((float) entity.prevPosX + (float) (entity.posX - entity.prevPosX) * delta, (float) entity.prevPosY + (float) (entity.posY - (float) entity.prevPosY) * delta, (float) entity.prevPosZ + (float) (entity.posZ - (float) entity.prevPosZ) * delta);
    }

    public static Vector3f toVector3f(Vec3d vec3) {
        return new Vector3f((float) vec3.x, (float) vec3.y, (float) vec3.z);
    }

    public static Vector3f toVector3f(Vector3 vec3) {
        return new Vector3f(vec3.x, vec3.y, vec3.z);
    }

    public static Vector3 toVector3(Vec3d vec3) {
        return new Vector3((float) vec3.x, (float) vec3.y, (float) vec3.z);
    }

    public static Vector3 toVector3(Vector3f vec3) {
        return new Vector3(vec3.x, vec3.y, vec3.z);
    }

    public static Vec3d toVec3(Vector3f vec3) {
        return new Vec3d(vec3.x, vec3.y, vec3.z);
    }

    public static Vec3d toVec3(Vector3 vec3) {
        return new Vec3d(vec3.x, vec3.y, vec3.z);
    }

    public static Vec3d fromColor(int color) {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float blue = (float) (color >> 8 & 255) / 255.0F;
        float green = (float) (color & 255) / 255.0F;
        return new Vec3d(red, green, blue);
    }

    public static FloatBuffer setBufferFromTransform(FloatBuffer matrixBuffer, Transform transform) {
        transform.getOpenGLMatrix(buffer);
        matrixBuffer.clear();
        matrixBuffer.put(buffer);
        matrixBuffer.flip();
        return matrixBuffer;
    }

    public static Vec3d calculateRay(Entity base, float distance, float partialTick, Vector3f offset) {
        Vec3d vec3 = toVec3(getSmoothedEntityPosition(base, partialTick)).add(toVec3(offset));
        Vec3d vec31 = base.getLook(partialTick);
        Vec3d vec32 = vec3.addVector(vec31.x * distance, vec31.y * distance, vec31.z * distance);
        return new Vec3d((float) vec32.x, (float) vec32.y, (float) vec32.z);
    }


    public static Matrix4f toMatrix4f(Matrix4 matrix4) {
        Quaternion rotation = QUAT_POOL.get();
        matrix4.getRotation(rotation);

        Vector3 position = VEC3_POOL.get();
        matrix4.getTranslation(position);
        Matrix4f mat4 = new Matrix4f(toQuat4f(rotation), toVector3f(position), 1);

        QUAT_POOL.release(rotation);
        VEC3_POOL.release(position);
        return mat4;
    }

    public static Matrix4 toMatrix4(Transform transform) {
        Matrix4 mat4 = new Matrix4();
        mat4.idt();

        Quat4f rotation = QUAT4F_POOL.get();
        mat4.set(toVector3(transform.origin), toQuaternion(transform.getRotation(rotation)));
        QUAT4F_POOL.release(rotation);
        return mat4;
    }

    public static Matrix4f inverse(Matrix4f centerOfMassTransform) {
        Transform trans = TRANS_POOL.get();
        trans.set(centerOfMassTransform);
        trans.inverse();
        centerOfMassTransform.setIdentity();
        Matrix4f result = trans.getMatrix(centerOfMassTransform);
        TRANS_POOL.release(trans);
        return result;
    }

    public static Transform toTransform(Matrix4 mat4) {
        return new Transform(toMatrix4f(mat4));
    }

    public static Quat4f toQuat4f(Quaternion orientation) {
        return new Quat4f(orientation.x, orientation.y, orientation.z, orientation.w);
    }

    public static Quaternion toQuaternion(Quat4f orientation) {
        return new Quaternion(orientation.x, orientation.y, orientation.z, orientation.w);
    }
}
