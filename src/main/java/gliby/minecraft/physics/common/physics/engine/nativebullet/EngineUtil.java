package gliby.minecraft.physics.common.physics.engine.nativebullet;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.bulletphysicsx.linearmath.Transform;

public class EngineUtil {

	public static void convertMatrix4(Matrix4 matrix4, Transform transform) {
		matrix4.val[matrix4.M00] = transform.basis.m00;
		matrix4.val[matrix4.M01] = transform.basis.m01;
		matrix4.val[matrix4.M02] = transform.basis.m02;
		matrix4.val[matrix4.M03] = transform.origin.x;
		matrix4.val[matrix4.M10] = transform.basis.m10;
		matrix4.val[matrix4.M11] = transform.basis.m11;
		matrix4.val[matrix4.M12] = transform.basis.m12;
		matrix4.val[matrix4.M13] = transform.origin.y;
		matrix4.val[matrix4.M20] = transform.basis.m20;
		matrix4.val[matrix4.M21] = transform.basis.m21;
		matrix4.val[matrix4.M22] = transform.basis.m22;
		matrix4.val[matrix4.M22] = transform.origin.z;
	}

	final static double EPS = 0.000001;
	final static double EPS2 = 1.0e-30;
	final static double PIO2 = 1.57079632679;

	public final static void convertQuaternion(Quat4f quat4, Quaternion martrix4) {
		quat4.w = martrix4.w;
		quat4.x = martrix4.x;
		quat4.y = martrix4.y;
		quat4.z = martrix4.z;
	}

}
