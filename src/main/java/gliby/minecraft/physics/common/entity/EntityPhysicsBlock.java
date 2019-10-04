package gliby.minecraft.physics.common.entity;

import com.badlogic.gdx.math.Quaternion;
import com.bulletphysicsx.collision.broadphase.CollisionFilterGroups;
import com.bulletphysicsx.linearmath.QuaternionUtil;
import com.bulletphysicsx.linearmath.Transform;
import gliby.minecraft.gman.BlockStateToMetadata;
import gliby.minecraft.gman.networking.GDataSerializers;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.client.render.RenderHandler;
import gliby.minecraft.physics.client.render.VecUtility;
import gliby.minecraft.physics.common.blocks.PhysicsBlockMetadata;
import gliby.minecraft.physics.common.entity.mechanics.RigidBodyMechanic;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.ICollisionShape;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 *
 */

// TODO rewrite to use entity actual position and rotation, upgrading the update frequency as well.
// TODO improvement: Remove ability to spawn tile-entites.
public class EntityPhysicsBlock extends EntityPhysicsBase implements IEntityAdditionalSpawnData {

    //    protected static final DataParameter<Vector3f> PHYSICS_POSITION = EntityDataManager.<Vector3f>createKey(EntityPhysicsBlock.class, Physics.VECTOR3F);
    protected static final DataParameter<Quat4f> PHYSICS_ROTATION = EntityDataManager.<Quat4f>createKey(EntityPhysicsBlock.class, GDataSerializers.QUAT4F);
    /**
     * Client-side render position, basically a smoothed position.
     */
    @SideOnly(Side.CLIENT)
    public Vector3f renderPosition = new Vector3f();
    /**
     * Client-side render rotation, basically a smoothed rotation.
     */
    @SideOnly(Side.CLIENT)
    public Quat4f renderRotation = new Quat4f();
    /**
     * Common variable, physics object's absolute position.
     */
//    private Vector3f physicsPosition = new Vector3f();
    /**
     * Block.
     */
    private IBlockState blockState;
    /**
     * Common variable, physics object's absolute rotation.
     */
    private Quat4f physicsRotation = new Quat4f();
    /**
     * Reference to block's built rigid body.
     */
    private IRigidBody rigidBody;
    /**
     * Reference to rigid bodies -> collision shape.
     */
    private ICollisionShape collisionShape;
    /**
     * If true, physics block will not use generated collision shape, but rather a
     * simple box shape.
     */
    private boolean defaultCollisionShape;
    /**
     * Collision with entites(including players).
     */
    private boolean collisionEnabled = true;
    /**
     * Rigid body mass.
     */
    private float mass;
    /**
     * Friction.
     */
    private float friction;
    private Vector3f linearVelocity, angularVelocity;
    @SideOnly(Side.CLIENT)
    private BlockPos blockPosition;
    private @SideOnly(Side.CLIENT)
    boolean hasLight;

    public EntityPhysicsBlock(World world) {
        super(world);
        noClip = true;
        setSize(0.85f, 1.05f);
    }

    public EntityPhysicsBlock(World world, PhysicsWorld physicsWorld, IBlockState blockState, float x, float y,
                              float z) {
        super(world, physicsWorld);

        this.blockState = blockState;
        QuaternionUtil.setEuler(physicsRotation, 0, 0, 0);
        this.physicsRotation.set(physicsRotation);

        if (world.isRemote) {
            this.renderPosition = new Vector3f(x, y, z);
            this.renderRotation = new Quat4f(physicsRotation);
        }
        Physics physics = Physics.getInstance();

        PhysicsBlockMetadata metadata = physics.getBlockManager().getPhysicsBlockMetadata()
                .get(physics.getBlockManager().getBlockIdentity(getBlockState().getBlock()));
        boolean metadataExists = metadata != null;

        this.mass = metadataExists ? metadata.mass : 10;
        this.friction = metadataExists ? metadata.friction : 0.5f;
        this.defaultCollisionShape = metadataExists && metadata.defaultCollisionShape;
        this.collisionEnabled = !metadataExists || metadata.collisionEnabled;

        if (this.defaultCollisionShape)
            this.collisionShape = physicsWorld.getDefaultShape();
        else
            this.collisionShape = physicsWorld.getBlockCache().getShape(world, new BlockPos(x, y, z), getBlockState());


//        Physics.getLogger().error("Block doesn't exist, couldn't create collision shape");

        setLocationAndAngles(x, y, z, 0, 0);

        createPhysicsObject(physicsWorld);
        if (metadata != null) {
            if (metadata.mechanics != null)
                this.mechanics.addAll(metadata.mechanics);
        }
    }

    @Override
    public void entityInit() {
        super.entityInit();
        this.dataManager.register(PHYSICS_ROTATION, physicsRotation = new Quat4f());
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        return super.getEntityBoundingBox();
    }

    /**
     * Enables entity->entity collision.
     */
    @Override
    public AxisAlignedBB getEntityBoundingBox() {
        return collisionEnabled ? new AxisAlignedBB(0.20f, 0, 0.20f, 0.80f, 1.05f, 0.80f).offset(
                Math.round(posX * 100.0f) / 100.0f, Math.round(posY * 100.0f) / 100.0f,
                Math.round(posZ * 100.0f) / 100.0f) : new AxisAlignedBB(Vec3d.ZERO, Vec3d.ZERO);
    }

    @Override
    protected void createPhysicsObject(PhysicsWorld physicsWorld) {
        Transform transform = new Transform();
        transform.setIdentity();
        transform.origin.set(VecUtility.toVector3f(getPositionVector()));
        transform.setRotation(this.physicsRotation);
        rigidBody = physicsWorld.createRigidBody(this, transform, Math.abs(mass), collisionShape);
        rigidBody.getProperties().put(EnumRigidBodyProperty.BLOCKSTATE.getName(), blockState);

        for (int i = 0; i < mechanics.size(); i++) {
            RigidBodyMechanic mechanic = mechanics.get(i);
            mechanic.onCreatePhysics(rigidBody);
        }

        if (collisionEnabled)
            physicsWorld.addRigidBody(rigidBody);
        else
            physicsWorld.addRigidBody(rigidBody, CollisionFilterGroups.CHARACTER_FILTER,
                    CollisionFilterGroups.ALL_FILTER);

        if (mass < 0)
            rigidBody.setGravity(new Vector3f());
        rigidBody.setFriction(friction);
        if (linearVelocity != null)
            rigidBody.setLinearVelocity(linearVelocity);
        if (angularVelocity != null)
            rigidBody.setAngularVelocity(angularVelocity);
    }

    @Override
    public void onCommonUpdate() {
        this.prevDistanceWalkedModified = this.distanceWalkedModified;
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.prevRotationPitch = this.rotationPitch;
        this.prevRotationYaw = this.rotationYaw;

    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getBrightnessForRender() {
        AxisAlignedBB renderAABB = this.getRenderBoundingBox();
        float width = (float) (renderAABB.maxX - renderAABB.minX);
        float height = (float) (renderAABB.maxY - renderAABB.minY);
        float length = (float) (renderAABB.maxZ - renderAABB.minZ);
        float lightX = (float) (renderAABB.minX + (width / 2));
        float lightY = (float) (renderAABB.minY + (height / 2));
        float lightZ = (float) (renderAABB.minZ + (length / 2));
        blockPosition = new BlockPos(lightX, lightY, lightZ);


        if (this.world.isBlockLoaded(blockPosition))
            return this.world.getCombinedLight(blockPosition, 0);
        else
            return 0;

    }

    @Override
    public void onServerUpdate() {
        if (rigidBody != null && rigidBody.isValid()) {
            // Update position from given rigid body.
            final Vector3f newPosition = rigidBody.getPosition();

            setPosition(newPosition.getX(), newPosition.getY(),
                    newPosition.getZ());

            Vector3f velocity = new Vector3f();
            setVelocity(velocity.getX(), velocity.getY(), velocity.getZ());

            // Update rotation from given rigid body.
            final Quat4f newQuat = rigidBody.getRotation();
            physicsRotation.set(newQuat.getX(), newQuat.getY(), newQuat.getZ(),
                    newQuat.getW());
            // Set location and angles, so client could have proper bounding
            // boxes.
            // Check if rigidBody is active, and if the last written postion
            // and rotation has changed.
            if (isDirty() && physicsRotation != null) {
//                this.dataManager.set(PHYSICS_POSITION, physicsPosition);
                this.dataManager.set(PHYSICS_ROTATION, physicsRotation);
            }
        }

    }

    @Override
    public void onClientUpdate() {
        // No-collision check.

        this.onGround = false;

        // Read dataWatcher objects, then set.
        physicsRotation = this.dataManager.get(PHYSICS_ROTATION);

        // Force vanilla entity bounding box to follow custom physics render
        // bounding box.
//        this.setEntityBoundingBox(getRenderBoundingBox());
        // setPosition(renderPosition.x + 0.5f, renderPosition.y,
        // renderPosition.z + 0.5f);

    }

    @Override
    public void writeEntityToNBT(final NBTTagCompound tagCompound) {

        ResourceLocation resourcelocation = ForgeRegistries.BLOCKS.getKey(getBlockState().getBlock());
        tagCompound.setString("Block", resourcelocation == null ? "" : resourcelocation.toString());
        tagCompound.setByte("Data", (byte) getBlockState().getBlock().getMetaFromState(getBlockState()));

        tagCompound.setFloat("Mass", mass);
        tagCompound.setFloat("Friction", friction);

        if (defaultCollisionShape)
            tagCompound.setBoolean("DefaultCollisionShape", defaultCollisionShape);
        if (!collisionEnabled)
            tagCompound.setBoolean("CollisionEnabled", collisionEnabled);

//        // Remove original tags.
//        tagCompound.removeTag("Pos");
        tagCompound.removeTag("Rotation");

//        // Add removed tags, but with additional values.
//        tagCompound.setTag("Pos", newDoubleNBTList(physicsPosition.x, physicsPosition.y, physicsPosition.z));
//        // adjust actually position
//        setPosition(physicsPosition.x, physicsPosition.y, physicsPosition.z);

        tagCompound.setTag("Rotation", newFloatNBTList(physicsRotation.x, physicsRotation.y, physicsRotation.z, physicsRotation.w));

        Vector3f linearVelocity = rigidBody.getLinearVelocity();
        tagCompound.setTag("LinearVelocity",
                newFloatNBTList(linearVelocity.x, linearVelocity.y, linearVelocity.z));
        Vector3f angularVelocity = rigidBody.getAngularVelocity();
        tagCompound.setTag("AngularVelocity",
                newFloatNBTList(angularVelocity.x, angularVelocity.y, angularVelocity.z));

        super.writeEntityToNBT(tagCompound);

    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tagCompound) {
        super.readEntityFromNBT(tagCompound);

        this.physicsWorld = Physics.getInstance().getPhysicsOverworld().getPhysicsByWorld(world);
        int data = tagCompound.getByte("Data") & 255;

        if (tagCompound.hasKey("Block", 8))
            this.blockState = Block.getBlockFromName(tagCompound.getString("Block")).getStateFromMeta(data);
        else if (tagCompound.hasKey("TileID", 99))
            this.blockState = Block.getBlockById(tagCompound.getInteger("TileID")).getStateFromMeta(data);
        else
            this.blockState = Block.getBlockById(tagCompound.getByte("Tile") & 255).getStateFromMeta(data);

        this.mass = (float) (tagCompound.hasKey("Mass") ? tagCompound.getFloat("Mass") : 1.0);
        this.friction = (float) (tagCompound.hasKey("Friction") ? tagCompound.getFloat("Friction") : 0.5);
        this.defaultCollisionShape = tagCompound.hasKey("DefaultCollisionShape") && tagCompound.getBoolean("DefaultCollisionShape");
        this.collisionEnabled = tagCompound.hasKey("CollisionEnabled") ? tagCompound.getBoolean("CollisionEnabled")
                : collisionEnabled;

        if (tagCompound.hasKey("CollisionShape")) {
            this.collisionShape = physicsWorld.getBlockCache().getShape(world, new BlockPos((int) posX, (int) posY, (int) posZ), blockState);
        } else
            this.collisionShape = physicsWorld.getDefaultShape();

//        this.physicsPosition.set((float) posX, (float) posY, (float) posZ);
        if (tagCompound.hasKey("Rotation")) {
            NBTTagList list = tagCompound.getTagList("Rotation", 5);
            this.physicsRotation.set(list.getFloatAt(0), list.getFloatAt(1), list.get(2) != null ? list.getFloatAt(2) : 0,
                    list.get(3) != null ? list.getFloatAt(3) : 0);
        }

        if (tagCompound.hasKey("LinearVelocity")) {
            NBTTagList linearVelocity = tagCompound.getTagList("LinearVelocity", 5);
            this.linearVelocity = new Vector3f(linearVelocity.getFloatAt(0), linearVelocity.getFloatAt(1),
                    linearVelocity.getFloatAt(2));
        }

        if (tagCompound.hasKey("AngularVelocity")) {
            NBTTagList angularVelocity = tagCompound.getTagList("AngularVelocity", 5);
            this.linearVelocity = new Vector3f(angularVelocity.getFloatAt(0), angularVelocity.getFloatAt(1),
                    angularVelocity.getFloatAt(2));
        }

        // Read from NBT gets called multiple times.
        if (!doesPhysicsObjectExist()) {
            createPhysicsObject(physicsWorld);
        } else {
            updatePhysicsObject(physicsWorld);
        }

    }

    @Override
    protected void updatePhysicsObject(PhysicsWorld physicsWorld) {
        Transform transform = new Transform();
        transform.setIdentity();
        transform.origin.set(VecUtility.toVector3f(getPositionVector()));
        transform.setRotation(this.physicsRotation);
        rigidBody.setWorldTransform(transform);
        // Used for specific block mechanics.
        rigidBody.getProperties().put(EnumRigidBodyProperty.BLOCKSTATE.getName(), blockState);
        if (mass < 0)
            rigidBody.setGravity(new Vector3f());
        rigidBody.setFriction(friction);
        if (linearVelocity != null)
            rigidBody.setLinearVelocity(linearVelocity);
        if (angularVelocity != null)
            rigidBody.setAngularVelocity(angularVelocity);

    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        super.writeSpawnData(buffer);

        BlockStateToMetadata.serializeBlockState(blockState, buffer);
        buffer.writeBoolean(collisionEnabled);
//        buffer.writeFloat(physicsPosition.x);
//        buffer.writeFloat(physicsPosition.y);
//        buffer.writeFloat(physicsPosition.z);

        buffer.writeFloat(physicsRotation.x);
        buffer.writeFloat(physicsRotation.y);
        buffer.writeFloat(physicsRotation.z);
        buffer.writeFloat(physicsRotation.w);
    }

    @Override
    public void readSpawnData(ByteBuf buffer) {
        super.readSpawnData(buffer);
        this.blockState = BlockStateToMetadata.deserializeBlockState(buffer);
        this.collisionEnabled = buffer.readBoolean();
//        this.physicsPosition = new Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
        this.physicsRotation = new Quat4f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
        this.renderPosition = VecUtility.toVector3f(getPositionVector());
        this.renderRotation = new Quat4f(physicsRotation);
        // Create dynamic light source if we can!
        if (getBlockState().getBlock().getLightValue(this.blockState) > 0 && !hasLight) {
            RenderHandler.getLightHandler().create(this, getBlockState().getBlock().getLightValue(this.blockState));
            hasLight = true;
        }

    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderOnFire() {
        return isBurning();
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    /**
     * @return
     */
    public IBlockState getBlockState() {
        return blockState;
    }

    @Override
    public boolean isDirty() {
        return rigidBody.isActive();
    }

    @Override
    protected boolean doesPhysicsObjectExist() {
        return rigidBody != null && rigidBody.isValid();
    }

    /**
     * @return
     */
    public IRigidBody getRigidBody() {
        return rigidBody;
    }


    // Returns the direction of the block based on the RigidBody.
    public Vector3f getDirection(Vector3f base) {
        Vector3f up = base;
        Vector3f direction = QuaternionUtil.quatRotate(physicsRotation, up, new Vector3f());
        direction.normalize();
        return direction;
    }


    @Override
    protected void dispose() {
        align();

        if (doesPhysicsObjectExist()) {
            physicsWorld.removeRigidBody(this.rigidBody);
            this.rigidBody = null;
        }
    }

    // Replace physics block back to world.
    protected void align() {
        if (rigidBody != null && rigidBody.isValid()) {
            // Calculate blockpos
            Vec3d position = getPositionVector().addVector(0.5f, 1, 0.5f);
            BlockPos pos = new BlockPos(MathHelper.floor(position.x), MathHelper.floor(position.y), MathHelper.floor(position.z));

            // Calculate forward.
            Vector3f forward = new Vector3f(0, 1, 0);
//

//            if (getBlockState().getPropertyKeys().contains(BlockHorizontal.FACING)) {
//                EnumFacing blockFacing = getBlockState().getValue(BlockHorizontal.FACING);
//                if (blockFacing != null) {
//                    Vec3i blockDirection = blockFacing.getDirectionVec();
//                    forward.set(blockDirection.getX(), blockDirection.getY(), blockDirection.getZ());
//                    forward.scale(-1);
//                }
//            }

            // Get direction of rigid body
            Vector3f direction = getDirection(forward);
            // Get EnumFacing based of rigidbody direction.
            EnumFacing enumFacing = EnumFacing.getFacingFromVector(direction.x, direction.y, direction.z);

            // Create fake player, used for block placement emulation.
            FakePlayer fakePlayer = FakePlayerFactory.getMinecraft((WorldServer)world);
            // Create LibGDX quat, useful for getting euler angles.
            Quaternion eulerQuat = new Quaternion(physicsRotation.x, physicsRotation.y, physicsRotation.z, physicsRotation.w);
            // Set new orientation of player.
//            fakePlayer.setPositionAndRotation(fakePlayer.posX, fakePlayer.posX, fakePlayer. posZ, eulerQuat.getYaw(), eulerQuat.getPitch());

            // create new state.
            IBlockState state = getBlockState().getBlock().getStateForPlacement(world, pos, enumFacing, pos.getX(), pos.getY(), pos.getZ(),
                    getBlockState().getBlock().getMetaFromState(getBlockState()), fakePlayer);

            // place block.
            world.setBlockState(pos, state);
        } else
            world.setBlockState(new BlockPos(posX, posY + 0.5F, posZ), getBlockState());
    }


    @Override
    public void interpolate() {
        this.renderPosition.interpolate(VecUtility.toVector3f(getPositionVector()), 0.15f);
        this.renderRotation.interpolate(physicsRotation, 0.15f);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(-0.2f, -0.2f, -0.2f, 1.3f, 1.2f, 1.2f).offset(renderPosition.x,
                renderPosition.y, renderPosition.z);
    }

    @Override
    public void onCommonInit() {
    }

    @Override
    public void onClientInit() {
    }

    @Override
    public void onServerInit() {
    }

}