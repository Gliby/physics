package gliby.minecraft.physics.client.render;

import com.bulletphysicsx.linearmath.Transform;
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

    public static final AxisAlignedBB ZERO_BB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
    private static float[] buffer = new float[16];

    /**
     * Calculates player to world transform and returns accordingly.
     *
     * @param mc
     * @param delta
     * @returns javax.vecmath.Vector3f
     */
    public static Vector3f getCameraTranslation(Minecraft mc, float delta) {
        return new Vector3f((float) mc.player.prevPosX + (float) (mc.player.posX - mc.player.prevPosX) * delta, (float) mc.player.prevPosY + (float) (mc.player.posY - (float) mc.player.prevPosY) * delta, (float) mc.player.prevPosZ + (float) (mc.player.posZ - (float) mc.player.prevPosZ) * delta);
    }

    public static Vector3f getSmoothedEntityPosition(Entity entity, float delta) {
        return new Vector3f((float) entity.prevPosX + (float) (entity.posX - entity.prevPosX) * delta, (float) entity.prevPosY + (float) (entity.posY - (float) entity.prevPosY) * delta, (float) entity.prevPosZ + (float) (entity.posZ - (float) entity.prevPosZ) * delta);
    }

    public static Vector3f toVector3f(Vec3d vec3) {
        return new Vector3f((float) vec3.x, (float) vec3.y, (float) vec3.z);
    }

    public static Vector3f toVector3f(org.terasology.math.geom.Vector3f vec3) {
        return new Vector3f(vec3.x, vec3.y, vec3.z);
    }

    public static org.terasology.math.geom.Vector3f toVector3fTera(Vec3d vec3) {
        return new org.terasology.math.geom.Vector3f((float) vec3.x, (float) vec3.y, (float) vec3.z);
    }

    public static org.terasology.math.geom.Vector3f toVector3fTera(Vector3f vec3) {
        return new org.terasology.math.geom.Vector3f(vec3.x, vec3.y, vec3.z);
    }

    public static Vec3d toVec3(Vector3f vec3) {
        return new Vec3d(vec3.x, vec3.y, vec3.z);
    }

    public static Vec3d toVec3(org.terasology.math.geom.Vector3f vec3) {
        return new Vec3d(vec3.x, vec3.y, vec3.z);
    }

    public static float getVolumeOfBoundingBox(AxisAlignedBB bb) {
        double width = bb.maxX - bb.minX;
        double height = bb.maxY - bb.minY;
        double length = bb.maxZ - bb.minZ;

        return (float) (width * height * length);
    }

    public static float getAreaOfBoundingBoxBottomFace(AxisAlignedBB bb) {
        double width = bb.maxX - bb.minX;
        double length = bb.maxZ - bb.minZ;
        return (float) (width * length);
    }

    public static Vec3d[] getCorners(AxisAlignedBB boundingBox) {
        return new Vec3d[]{
                new Vec3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ),
                new Vec3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ),
                new Vec3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ),
                new Vec3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ),
                new Vec3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ),
                new Vec3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ),
                new Vec3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ),
                new Vec3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ),
        };
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


    public static Matrix4f toMatrix4f(org.terasology.math.geom.Matrix4f matrix4) {
        Matrix4f mat4 = new Matrix4f();

        mat4.m00 = matrix4.m00;
        mat4.m01 = matrix4.m01;
        mat4.m02 = matrix4.m02;
        mat4.m03 = matrix4.m03;


        mat4.m10 = matrix4.m10;
        mat4.m11 = matrix4.m11;
        mat4.m12 = matrix4.m12;
        mat4.m13 = matrix4.m13;

        mat4.m20 = matrix4.m20;
        mat4.m21 = matrix4.m21;
        mat4.m22 = matrix4.m22;
        mat4.m23 = matrix4.m23;

        mat4.m30 = matrix4.m30;
        mat4.m31 = matrix4.m31;
        mat4.m32 = matrix4.m32;
        mat4.m33 = matrix4.m33;

        return mat4;
    }

    static Matrix4f MAT4 = new Matrix4f();

    public static Matrix4f toMatrix4fRef(org.terasology.math.geom.Matrix4f matrix4) {
        MAT4.m00 = matrix4.m00;
        MAT4.m01 = matrix4.m01;
        MAT4.m02 = matrix4.m02;
        MAT4.m03 = matrix4.m03;


        MAT4.m10 = matrix4.m10;
        MAT4.m11 = matrix4.m11;
        MAT4.m12 = matrix4.m12;
        MAT4.m13 = matrix4.m13;

        MAT4.m20 = matrix4.m20;
        MAT4.m21 = matrix4.m21;
        MAT4.m22 = matrix4.m22;
        MAT4.m23 = matrix4.m23;

        MAT4.m30 = matrix4.m30;
        MAT4.m31 = matrix4.m31;
        MAT4.m32 = matrix4.m32;
        MAT4.m33 = matrix4.m33;

        return MAT4;
    }



    public static org.terasology.math.geom.Matrix4f toMatrix4fTera(Transform transform) {
        transform.getMatrix(MAT4);
        org.terasology.math.geom.Matrix4f mat4 = new org.terasology.math.geom.Matrix4f();
        mat4.m00 = MAT4.m00;
        mat4.m01 = MAT4.m01;
        mat4.m02 = MAT4.m02;
        mat4.m03 = MAT4.m03;


        mat4.m10 = MAT4.m10;
        mat4.m11 = MAT4.m11;
        mat4.m12 = MAT4.m12;
        mat4.m13 = MAT4.m13;

        mat4.m20 = MAT4.m20;
        mat4.m21 = MAT4.m21;
        mat4.m22 = MAT4.m22;
        mat4.m23 = MAT4.m23;

        mat4.m30 = MAT4.m30;
        mat4.m31 = MAT4.m31;
        mat4.m32 = MAT4.m32;
        mat4.m33 = MAT4.m33;
        return mat4;
    }

    public static Matrix4f inverse(Matrix4f centerOfMassTransform) {
        Transform trans = new Transform();
        trans.set(centerOfMassTransform);
        trans.inverse();
        centerOfMassTransform.setIdentity();
        Matrix4f result = trans.getMatrix(centerOfMassTransform);
        return result;
    }

    public static Transform toTransform(org.terasology.math.geom.Matrix4f mat4) {
        return new Transform(toMatrix4f(mat4));
    }

    static Transform transform = new Transform();

    public static Transform toTransformRef(org.terasology.math.geom.Matrix4f mat4) {
        transform.set(toMatrix4fRef(mat4));
        return transform;
    }

    public static Quat4f toQuat4f(org.terasology.math.geom.Quat4f orientation) {
        return new Quat4f(orientation.x, orientation.y, orientation.z, orientation.w);
    }

    public static org.terasology.math.geom.Quat4f toTeraQuaternion(Quat4f orientation) {
        return new org.terasology.math.geom.Quat4f(orientation.x, orientation.y, orientation.z, orientation.w);
    }
}
