package gliby.minecraft.physics.client.render;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.bulletphysicsx.linearmath.Transform;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;

/**
 *
 */
public class VecUtility {


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
        return new Vector3f((float) vec3.x, (float) vec3.y, (float) vec3.z);
    }

    public static Vector3 toVector3(Vec3d vec3) {
        return new Vector3((float) vec3.x, (float) vec3.y, (float) vec3.z);
    }

    public static Vector3 toVector3(Vector3f vec3) {
        return new Vector3((float) vec3.x, (float) vec3.y, (float) vec3.z);
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

    public static float[] getMatrix4Values(Matrix4 matrix4f) {
        return matrix4f.getValues();
    }

    public static float[] getMatrix4Values(Matrix4f matrix4f) {
        final float[] matrix = {
                matrix4f.m00,
                matrix4f.m01,
                matrix4f.m02,
                matrix4f.m03,

                matrix4f.m10,
                matrix4f.m11,
                matrix4f.m12,
                matrix4f.m13,

                matrix4f.m20,
                matrix4f.m21,
                matrix4f.m22,
                matrix4f.m23,

                matrix4f.m30,
                matrix4f.m31,
                matrix4f.m32,
                matrix4f.m33,
        };
        return matrix;
    }

    public static Matrix4f toMatrix4f(Matrix4 matrix4f) {
        return new Matrix4f(getMatrix4Values(matrix4f));
    }

    public static Matrix4 toMatrix4(Matrix4f matrix4f) {
        return new Matrix4(getMatrix4Values(matrix4f));
    }

    public static Matrix4 toMatrix4(Transform transform) {
        return new Matrix4(getMatrix4Values(transform.getMatrix(new Matrix4f())));
    }

    public static Matrix4f inverse(Matrix4f centerOfMassTransform) {
        Transform trans = new Transform(centerOfMassTransform);
        trans.inverse();
        centerOfMassTransform.setIdentity();
        return trans.getMatrix(centerOfMassTransform);
    }

    public static Transform toTransform(Matrix4 mat4) {
        return new Transform(toMatrix4f(mat4));
    }

    public static Quat4f toQuat4f(Quaternion orientation) {
        return new Quat4f(orientation.x, orientation.y, orientation.z, orientation.w);
    }
}
