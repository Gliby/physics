package gliby.minecraft.physics.common.physics.engine.javabullet;

import com.bulletphysicsx.collision.broadphase.DbvtBroadphase;
import com.bulletphysicsx.collision.dispatch.*;
import com.bulletphysicsx.collision.dispatch.CollisionWorld.RayResultCallback;
import com.bulletphysicsx.collision.shapes.*;
import com.bulletphysicsx.collision.shapes.voxel.JBulletVoxelWorldShape;
import com.bulletphysicsx.dynamics.DiscreteDynamicsWorld;
import com.bulletphysicsx.dynamics.RigidBody;
import com.bulletphysicsx.dynamics.RigidBodyConstructionInfo;
import com.bulletphysicsx.dynamics.constraintsolver.*;
import com.bulletphysicsx.linearmath.DefaultMotionState;
import com.bulletphysicsx.linearmath.Transform;
import com.google.gson.Gson;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.common.physics.IPhysicsWorldConfiguration;
import gliby.minecraft.physics.common.physics.PhysicsOverworld;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.*;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JavaPhysicsWorld extends PhysicsWorld {

    private static final Transform transform = new Transform();
    private final PhysicsOverworld physicsOverworld;
    private final Physics physics;
    private List<IRigidBody> rigidBodies;
    private List<IConstraint> constraints;

    private DiscreteDynamicsWorld dynamicsWorld;
    private RigidBody blockCollisionBody;
    private List<IRope> ropes;

    public JavaPhysicsWorld(final Physics physics, final PhysicsOverworld physicsOverworld,
                            final IPhysicsWorldConfiguration physicsConfig) {
        super(physicsConfig);
        this.physics = physics;
        this.physicsOverworld = physicsOverworld;
    }

    @Override
    public void create() {
        ropes = new ArrayList<IRope>();
        rigidBodies = new ArrayList<IRigidBody>();
        constraints = new ArrayList<IConstraint>();
        final DbvtBroadphase broadphase = new DbvtBroadphase();

        broadphase.getOverlappingPairCache().setInternalGhostPairCallback(new GhostPairCallback());
        final CollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
        final CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);

        dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, new SequentialImpulseConstraintSolver(),
                collisionConfiguration);
        dynamicsWorld.setGravity(getPhysicsConfiguration().getRegularGravity());

        final Matrix3f rot = new Matrix3f();
        rot.setIdentity();
        final Transform identityTransform = new Transform(new Matrix4f(rot, new Vector3f(0, 0, 0), 1.0f));

        // Create block collision connection to bullet.
        final JBulletVoxelWorldShape blockCollisionHandler = new JBulletVoxelWorldShape(
                new JavaVoxelProvider(getPhysicsConfiguration().getWorld(), physics, this));
        blockCollisionHandler.calculateLocalInertia(0, new Vector3f());
        final RigidBodyConstructionInfo blockConsInf = new RigidBodyConstructionInfo(0,
                new DefaultMotionState(identityTransform), blockCollisionHandler, new Vector3f());
        blockCollisionBody = new RigidBody(blockConsInf);
        blockCollisionBody.setCollisionFlags(CollisionFlags.STATIC_OBJECT | blockCollisionBody.getCollisionFlags());
        dynamicsWorld.addRigidBody(blockCollisionBody);

        super.create();
    }

    @Override
    protected void update() {
        if (dynamicsWorld != null) {
            final float delta = getDelta();
            final int maxSubStep = Math.max(1, Math.round(delta / 10));
            dynamicsWorld.stepSimulation(1, maxSubStep);
            super.update();
        }
    }

    @Override
    public void addRigidBody(final IRigidBody body) {
        dynamicsWorld.addRigidBody((RigidBody) body.getBody());
        rigidBodies.add(body);
    }

    @Override
    public void addRigidBody(final IRigidBody body, final short collisionFilterGroup, final short collisionFilterMask) {
        dynamicsWorld.addRigidBody((RigidBody) body.getBody(), collisionFilterGroup, collisionFilterMask);
        rigidBodies.add(body);
    }

    @Override
    public void removeRigidBody(final IRigidBody body) {
        dynamicsWorld.removeRigidBody((RigidBody) body.getBody());
        rigidBodies.remove(body);
        body.dispose();
    }

    @Override
    public void awakenArea(final Vector3f min, final Vector3f max) {
        if (dynamicsWorld != null) {
            dynamicsWorld.awakenRigidBodiesInArea(min, max);
        }
    }

    @Override
    public void rayTest(final Vector3f rayFromWorld, final Vector3f rayToWorld, final IRayResult resultCallback) {
        dynamicsWorld.rayTest(rayFromWorld, rayToWorld, (RayResultCallback) resultCallback.getRayResultCallback());
    }

    @Override
    public void clearRayTest(IRayResult resultCallback) {

    }

    @Override
    public void removeCollisionObject(final ICollisionObject collisionObject) {
        dynamicsWorld.removeCollisionObject((CollisionObject) collisionObject.getCollisionObject());
    }

    @Override
    public void setGravity(final Vector3f newGravity) {
        dynamicsWorld.setGravity(newGravity);
    }

    @Override
    public void addCollisionObject(final ICollisionObject object) {
        dynamicsWorld.addCollisionObject((CollisionObject) object.getCollisionObject());
    }

    @Override
    public void addCollisionObject(final ICollisionObject object, final short collisionFilterGroup,
                                   final short collisionFilterMask) {
        dynamicsWorld.addCollisionObject((CollisionObject) object.getCollisionObject(), collisionFilterGroup,
                collisionFilterMask);
    }

    @Override
    public List<IRigidBody> getRigidBodies() {
        return rigidBodies;
    }

    @Override
    public IRigidBody createRigidBody(final Entity owner, final Transform transform, final float mass, final ICollisionShape shape) {
        final Vector3f localInertia = new Vector3f(0, 0, 0);
        if (mass != 0) {
            shape.calculateLocalInertia(mass, localInertia);
        }
        final DefaultMotionState motionState = new DefaultMotionState(transform);
        final RigidBodyConstructionInfo constructionInfo = new RigidBodyConstructionInfo(mass, motionState,
                (CollisionShape) shape.getCollisionShape(), localInertia);
        final RigidBody body = new RigidBody(constructionInfo);
        return new JavaRigidBody(this, body, owner);

    }

    @Override
    public ICollisionShape createBoxShape(final Vector3f extents) {
        return new gliby.minecraft.physics.common.physics.engine.javabullet.JavaCollisionShape(this,
                new BoxShape(extents));
    }

    @Override
    public IRayResult createClosestRayResultCallback(final Vector3f rayFromWorld, final Vector3f rayToWorld) {
        return new JavaClosestRayResultCallback(new CollisionWorld.ClosestRayResultCallback(rayFromWorld, rayToWorld));
    }

    @Override
    public IGhostObject createPairCachingGhostObject() {
        return new JavaPairCachingGhostObject(this, new PairCachingGhostObject());
    }

    @Override
    public IRigidBody upCastRigidBody(final Object collisionObject) {
        final RigidBody upCasted = RigidBody.upcast((CollisionObject) collisionObject);
        for (int i = 0; i < rigidBodies.size(); i++) {
            final IRigidBody body = rigidBodies.get(i);
            if (body.getBody() == upCasted) {
                return body;
            }
        }
        return null;
    }

    @Override
    public IConstraintPoint2Point createPoint2PointConstraint(final IRigidBody rigidBody, final Vector3f relativePivot) {
        return new JavaConstraintPoint2Point(this,
                new Point2PointConstraint((RigidBody) rigidBody.getBody(), relativePivot));
    }

    @Override
    public void addConstraint(final IConstraint constraint) {
        dynamicsWorld.addConstraint((TypedConstraint) constraint.getConstraint());
        constraints.add(constraint);

    }

    @Override
    public void removeConstraint(final IConstraint constraint) {
        dynamicsWorld.removeConstraint((TypedConstraint) constraint.getConstraint());
        constraints.remove(constraint);

    }

    @Override
    public String writeBlockCollisionShape(final ICollisionShape collisionShape) {
        final Gson gson = new Gson();
        final ArrayList<CollisionPart> collisionParts = new ArrayList<CollisionPart>();
        if (collisionShape.isBoxShape()) {
            collisionParts.add(new CollisionPart(false, null,
                    ((BoxShape) collisionShape.getCollisionShape()).getOriginalExtent()));
        } else if (collisionShape.isCompoundShape()) {
            final CompoundShape compoundShape = (CompoundShape) collisionShape.getCollisionShape();
            for (int i = 0; i < compoundShape.getChildList().size(); i++) {
                final CompoundShapeChild child = compoundShape.getChildList().get(i);
                collisionParts.add(
                        new CollisionPart(true, child.transform, ((BoxShape) child.childShape).getOriginalExtent()));
            }
        }
        return gson.toJson(collisionParts.toArray(), CollisionPart[].class);
    }

    @Override
    public ICollisionShape readBlockCollisionShape(final String json) {
        final Gson gson = new Gson();
        final List collisionParts = Arrays.asList(gson.fromJson(json, CollisionPart[].class));
        final CompoundShape shape = new CompoundShape();
        if (collisionParts.size() == 1) {
            final CollisionPart part = (CollisionPart) collisionParts.get(0);
            if (!part.compoundShape) {
                return createBoxShape(part.extent);
            }
        }

        for (int i = 0; i < collisionParts.size(); i++) {
            final CollisionPart part = (CollisionPart) collisionParts.get(i);
            shape.addChildShape(part.transform, new BoxShape(part.extent));
        }
        return new JavaCollisionShape(this, shape);
    }


    @Override
    public IConstraintGeneric6Dof createGeneric6DofConstraint(final IRigidBody rbA, final IRigidBody rbB, final Transform frameInA,
                                                              final Transform frameInB, final boolean useLinearReferenceFrameA) {
        return new JavaConstraintGeneric6Dof(this, new Generic6DofConstraint((RigidBody) rbA.getBody(),
                (RigidBody) rbB.getBody(), frameInA, frameInB, useLinearReferenceFrameA));
    }

    @Override
    public IRope createRope(final Vector3f startPos, final Vector3f endPos, final int detail) {
        return new JavaRope(startPos, endPos, detail);
    }

    @Override
    public void addRope(final IRope rope) {
        rope.create(this);
        ropes.add(rope);
    }

    @Override
    public List<IRope> getRopes() {
        return ropes;
    }

    @Override
    public void removeRope(final IRope rope) {
        rope.dispose(this);
        ropes.remove(rope);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[ " + rigidBodies.size() + " rigid bodies" + "]";
    }

    @Override
    public ICollisionShape createSphereShape(final float radius) {
        return new JavaCollisionShape(this, new SphereShape(radius));
    }

    @Override
    public boolean isValid() {
        return dynamicsWorld != null;
    }

    @Override
    public IRigidBody createInertialessRigidbody(final Entity owner, final Transform transform, final float mass,
                                                 final ICollisionShape shape) {
        final DefaultMotionState motionState = new DefaultMotionState(transform);
        final RigidBodyConstructionInfo constructionInfo = new RigidBodyConstructionInfo(mass, motionState,
                (CollisionShape) shape.getCollisionShape());
        final RigidBody body = new RigidBody(constructionInfo);
        return new JavaRigidBody(this, body, owner);
    }

    @Override
    public IConstraintSlider createSliderConstraint(final IRigidBody rbA, final IRigidBody rbB, final Transform frameInA,
                                                    final Transform frameInB, final boolean useLinearReferenceFrameA) {
        return new JavaConstraintSlider(new SliderConstraint((RigidBody) rbA.getBody(), (RigidBody) rbB.getBody(),
                frameInA, frameInB, useLinearReferenceFrameA));
    }

    @Override
    public ICollisionShape buildCollisionShape(final List<AxisAlignedBB> bbs, final Vector3f offset) {
        final CompoundShape compoundShape = new CompoundShape();
        for (final AxisAlignedBB bb : bbs) {
            final AxisAlignedBB relativeBB = AxisAlignedBB.fromBounds((bb.minX - offset.getX()) * 0.5f,
                    (bb.minY - offset.getY()) * 0.5f, (bb.minZ - offset.getZ()) * 0.5f,
                    (bb.maxX - offset.getX()) * 0.5f, (bb.maxY - offset.getY()) * 0.5f,
                    (bb.maxZ - offset.getZ()) * 0.5f);
            final Vector3f extents = new Vector3f((float) relativeBB.maxX - (float) relativeBB.minX,
                    (float) relativeBB.maxY - (float) relativeBB.minY,
                    (float) relativeBB.maxZ - (float) relativeBB.minZ);
            transform.setIdentity();
            transform.origin.set((float) relativeBB.minX + (float) relativeBB.maxX - 0.5f,
                    (float) relativeBB.minY + (float) relativeBB.maxY - 0.5f,
                    (float) relativeBB.minZ + (float) relativeBB.maxZ - 0.5f);
            compoundShape.addChildShape(transform, new BoxShape(extents));
        }
        return new JavaCollisionShape(this, compoundShape);
    }

    @Override
    public void dispose() {
        dynamicsWorld.removeRigidBody(blockCollisionBody);
        blockCollisionBody.destroy();



        for (IRigidBody body : rigidBodies) {
            RigidBody rigidBody = (RigidBody) body.getBody();
            dynamicsWorld.removeRigidBody(rigidBody);
            body.dispose();
        }


        final int numCollisionObjects = dynamicsWorld.getNumCollisionObjects();
        for (int i = 0; i < numCollisionObjects; i++) {
            final CollisionObject disposedObject = dynamicsWorld.getCollisionObjectArray().get(i);
            dynamicsWorld.removeCollisionObject(disposedObject);
        }


        rigidBodies.clear();
        constraints.clear();
        ropes.clear();

        dynamicsWorld.clearForces();
        dynamicsWorld.destroy();
        dynamicsWorld = null;
        super.dispose();
    }

}
