package gliby.minecraft.physics.client.render;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.bulletphysicsx.linearmath.QuaternionUtil;
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


    public static final int M01 = 3;
    public static final int M02 = 6;
    public static final int M10 = 1;
    public static final int M11 = 4;
    public static final int M12 = 7;
    public static final int M20 = 2;
    public static final int M21 = 5;
    public static final int M22 = 8;
    static final int M00 = 0;
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


    public static Matrix4f toMatrix4f(Matrix4 matrix4) {
        Matrix4f mat4 = new Matrix4f(toQuat4f(matrix4.getRotation(new Quaternion())), toVector3f(matrix4.getTranslation(new Vector3())), 1);
        return new Matrix4f();
    }

//    public static Matrix4 toMatrix4(Matrix4f matrix4f) {
//        Matrix4 mat4 = new Matrix4();
//        Transform trans =new Transform(matrix4f);
//        mat4.set()
//
//    }

    public static Matrix4 toMatrix4(Transform transform) {
        Matrix4 mat4 = new Matrix4();
        mat4.idt();
        mat4.set(toVector3(transform.origin), toQuaternion(transform.getRotation(new Quat4f())));
        return mat4;
    }

    public static Matrix4f inverse(Matrix4f centerOfMassTransform) {
        Transform trans = new Transform(centerOfMassTransform);
        trans.inverse();
        centerOfMassTransform.setIdentity();
        return trans.getMatrix(centerOfMassTransform);
    }

    public static Transform toTransform(Matrix4 mat4) {
        Transform transform = new Transform();
        return new Transform(toMatrix4f(mat4));
    }

    public static Quat4f toQuat4f(Quaternion orientation) {
        return new Quat4f(orientation.x, orientation.y, orientation.z, orientation.w);
    }

    public static Quaternion toQuaternion(Quat4f orientation) {
        return new Quaternion(orientation.x, orientation.y, orientation.z, orientation.w);
    }
}
