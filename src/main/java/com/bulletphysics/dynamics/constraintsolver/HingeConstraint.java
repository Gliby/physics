/*
 * Java port of Bullet (c) 2008 Martin Dvorak <jezek2@advel.cz>
 *
 * Bullet Continuous Collision Detection and Physics Library
 * Copyright (c) 2003-2008 Erwin Coumans  http://www.bulletphysics.com/
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from
 * the use of this software.
 * 
 * Permission is granted to anyone to use this software for any purpose, 
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 
 * 1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

/* Hinge Constraint by Dirk Gregorius. Limits added by Marcus Hennix at Starbreeze Studios */

package com.bulletphysics.dynamics.constraintsolver;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.QuaternionUtil;
import com.bulletphysics.linearmath.ScalarUtil;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.linearmath.TransformUtil;

import javax.vecmath.Matrix3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.apache.commons.math3.util.FastMath;

/**
 * Hinge constraint between two rigid bodies each with a pivot point that descibes
 * the axis location in local space. Axis defines the orientation of the hinge axis.
 *
 * @author jezek2
 */
public class HingeConstraint extends TypedConstraint {

    private JacobianEntry[] jac/*[3]*/ = new JacobianEntry[]{new JacobianEntry(), new JacobianEntry(), new JacobianEntry()}; // 3 orthogonal linear constraints
    private JacobianEntry[] jacAng/*[3]*/ = new JacobianEntry[]{new JacobianEntry(), new JacobianEntry(), new JacobianEntry()}; // 2 orthogonal angular constraints+ 1 for limit/motor

    private final Transform rbAFrame = new Transform(); // constraint axii. Assumes z is hinge axis.
    private final Transform rbBFrame = new Transform();

    private float motorTargetVelocity;
    private float maxMotorImpulse;

    private float limitSoftness;
    private float biasFactor;
    private float relaxationFactor;

    private float lowerLimit;
    private float upperLimit;

    private float kHinge;

    private float limitSign;
    private float correction;

    private float accLimitImpulse;

    private boolean angularOnly;
    private boolean enableAngularMotor;
    private boolean solveLimit;

    public HingeConstraint() {
        super(TypedConstraintType.HINGE_CONSTRAINT_TYPE);
        enableAngularMotor = false;
    }

    public HingeConstraint(RigidBody rbA, RigidBody rbB, Vector3f pivotInA, Vector3f pivotInB, Vector3f axisInA, Vector3f axisInB) {
        super(TypedConstraintType.HINGE_CONSTRAINT_TYPE, rbA, rbB);
        angularOnly = false;
        enableAngularMotor = false;

        rbAFrame.origin.set(pivotInA);

        // since no frame is given, assume this to be zero angle and just pick rb transform axis
        Vector3f rbAxisA1 = new Vector3f();
        Vector3f rbAxisA2 = new Vector3f();

        Transform centerOfMassA = rbA.getCenterOfMassTransform(new Transform());
        centerOfMassA.basis.getColumn(0, rbAxisA1);
        float projection = axisInA.dot(rbAxisA1);

        if (projection >= 1.0f - BulletGlobals.SIMD_EPSILON) {
            centerOfMassA.basis.getColumn(2, rbAxisA1);
            rbAxisA1.negate();
            centerOfMassA.basis.getColumn(1, rbAxisA2);
        } else if (projection <= -1.0f + BulletGlobals.SIMD_EPSILON) {
            centerOfMassA.basis.getColumn(2, rbAxisA1);
            centerOfMassA.basis.getColumn(1, rbAxisA2);
        } else {
            rbAxisA2.cross(axisInA, rbAxisA1);
            rbAxisA1.cross(rbAxisA2, axisInA);
        }

        rbAFrame.basis.setRow(0, rbAxisA1.x, rbAxisA2.x, axisInA.x);
        rbAFrame.basis.setRow(1, rbAxisA1.y, rbAxisA2.y, axisInA.y);
        rbAFrame.basis.setRow(2, rbAxisA1.z, rbAxisA2.z, axisInA.z);

        Quat4f rotationArc = QuaternionUtil.shortestArcQuat(axisInA, axisInB, new Quat4f());
        Vector3f rbAxisB1 = QuaternionUtil.quatRotate(rotationArc, rbAxisA1, new Vector3f());
        Vector3f rbAxisB2 = new Vector3f();
        rbAxisB2.cross(axisInB, rbAxisB1);

        rbBFrame.origin.set(pivotInB);
        rbBFrame.basis.setRow(0, rbAxisB1.x, rbAxisB2.x, -axisInB.x);
        rbBFrame.basis.setRow(1, rbAxisB1.y, rbAxisB2.y, -axisInB.y);
        rbBFrame.basis.setRow(2, rbAxisB1.z, rbAxisB2.z, -axisInB.z);

        // start with free
        lowerLimit = 1e30f;
        upperLimit = -1e30f;
        biasFactor = 0.3f;
        relaxationFactor = 1.0f;
        limitSoftness = 0.9f;
        solveLimit = false;
    }

    public HingeConstraint(RigidBody rbA, Vector3f pivotInA, Vector3f axisInA) {
        super(TypedConstraintType.HINGE_CONSTRAINT_TYPE, rbA);
        angularOnly = false;
        enableAngularMotor = false;

        // since no frame is given, assume this to be zero angle and just pick rb transform axis
        // fixed axis in worldspace
        Vector3f rbAxisA1 = new Vector3f();
        Transform centerOfMassA = rbA.getCenterOfMassTransform(new Transform());
        centerOfMassA.basis.getColumn(0, rbAxisA1);

        float projection = rbAxisA1.dot(axisInA);
        if (projection > BulletGlobals.FLT_EPSILON) {
            rbAxisA1.scale(projection);
            rbAxisA1.sub(axisInA);
        } else {
            centerOfMassA.basis.getColumn(1, rbAxisA1);
        }

        Vector3f rbAxisA2 = new Vector3f();
        rbAxisA2.cross(axisInA, rbAxisA1);

        rbAFrame.origin.set(pivotInA);
        rbAFrame.basis.setRow(0, rbAxisA1.x, rbAxisA2.x, axisInA.x);
        rbAFrame.basis.setRow(1, rbAxisA1.y, rbAxisA2.y, axisInA.y);
        rbAFrame.basis.setRow(2, rbAxisA1.z, rbAxisA2.z, axisInA.z);

        Vector3f axisInB = new Vector3f();
        axisInB.negate(axisInA);
        centerOfMassA.basis.transform(axisInB);

        Quat4f rotationArc = QuaternionUtil.shortestArcQuat(axisInA, axisInB, new Quat4f());
        Vector3f rbAxisB1 = QuaternionUtil.quatRotate(rotationArc, rbAxisA1, new Vector3f());
        Vector3f rbAxisB2 = new Vector3f();
        rbAxisB2.cross(axisInB, rbAxisB1);

        rbBFrame.origin.set(pivotInA);
        centerOfMassA.transform(rbBFrame.origin);
        rbBFrame.basis.setRow(0, rbAxisB1.x, rbAxisB2.x, axisInB.x);
        rbBFrame.basis.setRow(1, rbAxisB1.y, rbAxisB2.y, axisInB.y);
        rbBFrame.basis.setRow(2, rbAxisB1.z, rbAxisB2.z, axisInB.z);

        // start with free
        lowerLimit = 1e30f;
        upperLimit = -1e30f;
        biasFactor = 0.3f;
        relaxationFactor = 1.0f;
        limitSoftness = 0.9f;
        solveLimit = false;
    }

    public HingeConstraint(RigidBody rbA, RigidBody rbB, Transform rbAFrame, Transform rbBFrame) {
        super(TypedConstraintType.HINGE_CONSTRAINT_TYPE, rbA, rbB);
        this.rbAFrame.set(rbAFrame);
        this.rbBFrame.set(rbBFrame);
        angularOnly = false;
        enableAngularMotor = false;

        // flip axis
        this.rbBFrame.basis.m02 *= -1f;
        this.rbBFrame.basis.m12 *= -1f;
        this.rbBFrame.basis.m22 *= -1f;

        // start with free
        lowerLimit = 1e30f;
        upperLimit = -1e30f;
        biasFactor = 0.3f;
        relaxationFactor = 1.0f;
        limitSoftness = 0.9f;
        solveLimit = false;
    }

    public HingeConstraint(RigidBody rbA, Transform rbAFrame) {
        super(TypedConstraintType.HINGE_CONSTRAINT_TYPE, rbA);
        this.rbAFrame.set(rbAFrame);
        this.rbBFrame.set(rbAFrame);
        angularOnly = false;
        enableAngularMotor = false;

        // not providing rigidbody B means implicitly using worldspace for body B

        // flip axis
        this.rbBFrame.basis.m02 *= -1f;
        this.rbBFrame.basis.m12 *= -1f;
        this.rbBFrame.basis.m22 *= -1f;

        this.rbBFrame.origin.set(this.rbAFrame.origin);
        rbA.getCenterOfMassTransform(new Transform()).transform(this.rbBFrame.origin);

        // start with free
        lowerLimit = 1e30f;
        upperLimit = -1e30f;
        biasFactor = 0.3f;
        relaxationFactor = 1.0f;
        limitSoftness = 0.9f;
        solveLimit = false;
    }

    @Override
    public void buildJacobian() {
        Vector3f tmp = new Vector3f();
        Vector3f tmp1 = new Vector3f();
        Vector3f tmp2 = new Vector3f();
        Vector3f tmpVec = new Vector3f();
        Matrix3f mat1 = new Matrix3f();
        Matrix3f mat2 = new Matrix3f();

        Transform centerOfMassA = rbA.getCenterOfMassTransform(new Transform());
        Transform centerOfMassB = rbB.getCenterOfMassTransform(new Transform());

        appliedImpulse = 0f;

        if (!angularOnly) {
            Vector3f pivotAInW = new Vector3f(rbAFrame.origin);
            centerOfMassA.transform(pivotAInW);

            Vector3f pivotBInW = new Vector3f(rbBFrame.origin);
            centerOfMassB.transform(pivotBInW);

            Vector3f relPos = new Vector3f();
            relPos.sub(pivotBInW, pivotAInW);

            Vector3f[] normal/*[3]*/ = new Vector3f[]{new Vector3f(), new Vector3f(), new Vector3f()};
            if (relPos.lengthSquared() > BulletGlobals.FLT_EPSILON) {
                normal[0].set(relPos);
                normal[0].normalize();
            } else {
                normal[0].set(1f, 0f, 0f);
            }

            TransformUtil.planeSpace1(normal[0], normal[1], normal[2]);

            for (int i = 0; i < 3; i++) {
                mat1.transpose(centerOfMassA.basis);
                mat2.transpose(centerOfMassB.basis);

                tmp1.sub(pivotAInW, rbA.getCenterOfMassPosition(tmpVec));
                tmp2.sub(pivotBInW, rbB.getCenterOfMassPosition(tmpVec));

                jac[i].init(
                        mat1,
                        mat2,
                        tmp1,
                        tmp2,
                        normal[i],
                        rbA.getInvInertiaDiagLocal(new Vector3f()),
                        rbA.getInvMass(),
                        rbB.getInvInertiaDiagLocal(new Vector3f()),
                        rbB.getInvMass());
            }
        }

        // calculate two perpendicular jointAxis, orthogonal to hingeAxis
        // these two jointAxis require equal angular velocities for both bodies

        // this is unused for now, it's a todo
        Vector3f jointAxis0local = new Vector3f();
        Vector3f jointAxis1local = new Vector3f();

        rbAFrame.basis.getColumn(2, tmp);
        TransformUtil.planeSpace1(tmp, jointAxis0local, jointAxis1local);

        // TODO: check this
        //getRigidBodyA().getCenterOfMassTransform().getBasis() * m_rbAFrame.getBasis().getColumn(2);

        Vector3f jointAxis0 = new Vector3f(jointAxis0local);
        centerOfMassA.basis.transform(jointAxis0);

        Vector3f jointAxis1 = new Vector3f(jointAxis1local);
        centerOfMassA.basis.transform(jointAxis1);

        Vector3f hingeAxisWorld = new Vector3f();
        rbAFrame.basis.getColumn(2, hingeAxisWorld);
        centerOfMassA.basis.transform(hingeAxisWorld);

        mat1.transpose(centerOfMassA.basis);
        mat2.transpose(centerOfMassB.basis);
        jacAng[0].init(jointAxis0,
                mat1,
                mat2,
                rbA.getInvInertiaDiagLocal(new Vector3f()),
                rbB.getInvInertiaDiagLocal(new Vector3f()));

        // JAVA NOTE: reused mat1 and mat2, as recomputation is not needed
        jacAng[1].init(jointAxis1,
                mat1,
                mat2,
                rbA.getInvInertiaDiagLocal(new Vector3f()),
                rbB.getInvInertiaDiagLocal(new Vector3f()));

        // JAVA NOTE: reused mat1 and mat2, as recomputation is not needed
        jacAng[2].init(hingeAxisWorld,
                mat1,
                mat2,
                rbA.getInvInertiaDiagLocal(new Vector3f()),
                rbB.getInvInertiaDiagLocal(new Vector3f()));

        // Compute limit information
        float hingeAngle = getHingeAngle();

        //set bias, sign, clear accumulator
        correction = 0f;
        limitSign = 0f;
        solveLimit = false;
        accLimitImpulse = 0f;

        if (lowerLimit < upperLimit) {
            if (hingeAngle <= lowerLimit * limitSoftness) {
                correction = (lowerLimit - hingeAngle);
                limitSign = 1.0f;
                solveLimit = true;
            } else if (hingeAngle >= upperLimit * limitSoftness) {
                correction = upperLimit - hingeAngle;
                limitSign = -1.0f;
                solveLimit = true;
            }
        }

        // Compute K = J*W*J' for hinge axis
        Vector3f axisA = new Vector3f();
        rbAFrame.basis.getColumn(2, axisA);
        centerOfMassA.basis.transform(axisA);

        kHinge = 1.0f / (getRigidBodyA().computeAngularImpulseDenominator(axisA) +
                getRigidBodyB().computeAngularImpulseDenominator(axisA));
    }

    @Override
    public void solveConstraint(float timeStep) {
        Vector3f tmp = new Vector3f();
        Vector3f tmp2 = new Vector3f();
        Vector3f tmpVec = new Vector3f();

        Transform centerOfMassA = rbA.getCenterOfMassTransform(new Transform());
        Transform centerOfMassB = rbB.getCenterOfMassTransform(new Transform());

        Vector3f pivotAInW = new Vector3f(rbAFrame.origin);
        centerOfMassA.transform(pivotAInW);

        Vector3f pivotBInW = new Vector3f(rbBFrame.origin);
        centerOfMassB.transform(pivotBInW);

        float tau = 0.3f;

        // linear part
        if (!angularOnly) {
            Vector3f rel_pos1 = new Vector3f();
            rel_pos1.sub(pivotAInW, rbA.getCenterOfMassPosition(tmpVec));

            Vector3f rel_pos2 = new Vector3f();
            rel_pos2.sub(pivotBInW, rbB.getCenterOfMassPosition(tmpVec));

            Vector3f vel1 = rbA.getVelocityInLocalPoint(rel_pos1, new Vector3f());
            Vector3f vel2 = rbB.getVelocityInLocalPoint(rel_pos2, new Vector3f());
            Vector3f vel = new Vector3f();
            vel.sub(vel1, vel2);

            for (int i = 0; i < 3; i++) {
                Vector3f normal = jac[i].linearJointAxis;
                float jacDiagABInv = 1f / jac[i].getDiagonal();

                float rel_vel;
                rel_vel = normal.dot(vel);
                // positional error (zeroth order error)
                tmp.sub(pivotAInW, pivotBInW);
                float depth = -(tmp).dot(normal); // this is the error projected on the normal
                float impulse = depth * tau / timeStep * jacDiagABInv - rel_vel * jacDiagABInv;
                appliedImpulse += impulse;
                Vector3f impulse_vector = new Vector3f();
                impulse_vector.scale(impulse, normal);

                tmp.sub(pivotAInW, rbA.getCenterOfMassPosition(tmpVec));
                rbA.applyImpulse(impulse_vector, tmp);

                tmp.negate(impulse_vector);
                tmp2.sub(pivotBInW, rbB.getCenterOfMassPosition(tmpVec));
                rbB.applyImpulse(tmp, tmp2);
            }
        }


        {
            // solve angular part

            // get axes in world space
            Vector3f axisA = new Vector3f();
            rbAFrame.basis.getColumn(2, axisA);
            centerOfMassA.basis.transform(axisA);

            Vector3f axisB = new Vector3f();
            rbBFrame.basis.getColumn(2, axisB);
            centerOfMassB.basis.transform(axisB);

            Vector3f angVelA = getRigidBodyA().getAngularVelocity(new Vector3f());
            Vector3f angVelB = getRigidBodyB().getAngularVelocity(new Vector3f());

            Vector3f angVelAroundHingeAxisA = new Vector3f();
            angVelAroundHingeAxisA.scale(axisA.dot(angVelA), axisA);

            Vector3f angVelAroundHingeAxisB = new Vector3f();
            angVelAroundHingeAxisB.scale(axisB.dot(angVelB), axisB);

            Vector3f angAorthog = new Vector3f();
            angAorthog.sub(angVelA, angVelAroundHingeAxisA);

            Vector3f angBorthog = new Vector3f();
            angBorthog.sub(angVelB, angVelAroundHingeAxisB);

            Vector3f velrelOrthog = new Vector3f();
            velrelOrthog.sub(angAorthog, angBorthog);

            {
                // solve orthogonal angular velocity correction
                float relaxation = 1f;
                float len = velrelOrthog.length();
                if (len > 0.00001f) {
                    Vector3f normal = new Vector3f();
                    normal.normalize(velrelOrthog);

                    float denom = getRigidBodyA().computeAngularImpulseDenominator(normal) +
                            getRigidBodyB().computeAngularImpulseDenominator(normal);
                    // scale for mass and relaxation
                    // todo:  expose this 0.9 factor to developer
                    velrelOrthog.scale((1f / denom) * relaxationFactor);
                }

                // solve angular positional correction
                // TODO: check
                //Vector3f angularError = -axisA.cross(axisB) *(btScalar(1.)/timeStep);
                Vector3f angularError = new Vector3f();
                angularError.cross(axisA, axisB);
                angularError.negate();
                angularError.scale(1f / timeStep);
                float len2 = angularError.length();
                if (len2 > 0.00001f) {
                    Vector3f normal2 = new Vector3f();
                    normal2.normalize(angularError);

                    float denom2 = getRigidBodyA().computeAngularImpulseDenominator(normal2) +
                            getRigidBodyB().computeAngularImpulseDenominator(normal2);
                    angularError.scale((1f / denom2) * relaxation);
                }

                tmp.negate(velrelOrthog);
                tmp.add(angularError);
                rbA.applyTorqueImpulse(tmp);

                tmp.sub(velrelOrthog, angularError);
                rbB.applyTorqueImpulse(tmp);

                // solve limit
                if (solveLimit) {
                    tmp.sub(angVelB, angVelA);
                    float amplitude = ((tmp).dot(axisA) * relaxationFactor + correction * (1f / timeStep) * biasFactor) * limitSign;

                    float impulseMag = amplitude * kHinge;

                    // Clamp the accumulated impulse
                    float temp = accLimitImpulse;
                    accLimitImpulse = FastMath.max(accLimitImpulse + impulseMag, 0f);
                    impulseMag = accLimitImpulse - temp;

                    Vector3f impulse = new Vector3f();
                    impulse.scale(impulseMag * limitSign, axisA);

                    rbA.applyTorqueImpulse(impulse);

                    tmp.negate(impulse);
                    rbB.applyTorqueImpulse(tmp);
                }
            }

            // apply motor
            if (enableAngularMotor) {
                // todo: add limits too
                Vector3f angularLimit = new Vector3f();
                angularLimit.set(0f, 0f, 0f);

                Vector3f velrel = new Vector3f();
                velrel.sub(angVelAroundHingeAxisA, angVelAroundHingeAxisB);
                float projRelVel = velrel.dot(axisA);

                float desiredMotorVel = motorTargetVelocity;
                float motor_relvel = desiredMotorVel - projRelVel;

                float unclippedMotorImpulse = kHinge * motor_relvel;
                // todo: should clip against accumulated impulse
                float clippedMotorImpulse = unclippedMotorImpulse > maxMotorImpulse ? maxMotorImpulse : unclippedMotorImpulse;
                clippedMotorImpulse = clippedMotorImpulse < -maxMotorImpulse ? -maxMotorImpulse : clippedMotorImpulse;
                Vector3f motorImp = new Vector3f();
                motorImp.scale(clippedMotorImpulse, axisA);

                tmp.add(motorImp, angularLimit);
                rbA.applyTorqueImpulse(tmp);

                tmp.negate(motorImp);
                tmp.sub(angularLimit);
                rbB.applyTorqueImpulse(tmp);
            }
        }
    }

    public void updateRHS(float timeStep) {
    }

    public float getHingeAngle() {
        Transform centerOfMassA = rbA.getCenterOfMassTransform(new Transform());
        Transform centerOfMassB = rbB.getCenterOfMassTransform(new Transform());

        Vector3f refAxis0 = new Vector3f();
        rbAFrame.basis.getColumn(0, refAxis0);
        centerOfMassA.basis.transform(refAxis0);

        Vector3f refAxis1 = new Vector3f();
        rbAFrame.basis.getColumn(1, refAxis1);
        centerOfMassA.basis.transform(refAxis1);

        Vector3f swingAxis = new Vector3f();
        rbBFrame.basis.getColumn(1, swingAxis);
        centerOfMassB.basis.transform(swingAxis);

        return ScalarUtil.atan2Fast(swingAxis.dot(refAxis0), swingAxis.dot(refAxis1));
    }

    public void setAngularOnly(boolean angularOnly) {
        this.angularOnly = angularOnly;
    }

    public void enableAngularMotor(boolean enableMotor, float targetVelocity, float maxMotorImpulse) {
        this.enableAngularMotor = enableMotor;
        this.motorTargetVelocity = targetVelocity;
        this.maxMotorImpulse = maxMotorImpulse;
    }

    public void setLimit(float low, float high) {
        setLimit(low, high, 0.9f, 0.3f, 1.0f);
    }

    public void setLimit(float low, float high, float _softness, float _biasFactor, float _relaxationFactor) {
        lowerLimit = low;
        upperLimit = high;

        limitSoftness = _softness;
        biasFactor = _biasFactor;
        relaxationFactor = _relaxationFactor;
    }

    public float getLowerLimit() {
        return lowerLimit;
    }

    public float getUpperLimit() {
        return upperLimit;
    }

    public Transform getAFrame(Transform out) {
        out.set(rbAFrame);
        return out;
    }

    public Transform getBFrame(Transform out) {
        out.set(rbBFrame);
        return out;
    }

    public boolean getSolveLimit() {
        return solveLimit;
    }

    public float getLimitSign() {
        return limitSign;
    }

    public boolean getAngularOnly() {
        return angularOnly;
    }

    public boolean getEnableAngularMotor() {
        return enableAngularMotor;
    }

    public float getMotorTargetVelosity() {
        return motorTargetVelocity;
    }

    public float getMaxMotorImpulse() {
        return maxMotorImpulse;
    }

}
