package gliby.minecraft.physics.common.entity;

import com.badlogic.gdx.math.Quaternion;
import com.bulletphysicsx.collision.broadphase.CollisionFilterGroups;
import com.bulletphysicsx.linearmath.QuaternionUtil;
import com.bulletphysicsx.linearmath.Transform;
import gliby.minecraft.gman.BlockStateToMetadata;
import gliby.minecraft.gman.networking.GDataSerializers;
import gliby.minecraft.gman.settings.FloatSetting;
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
import net.minecraft.entity.Entity;
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
import java.lang.ref.WeakReference;
import java.util.List;

public class EntityPhysicsBlock extends EntityPhysicsBase implements IEntityAdditionalSpawnData {

    protected static final DataParameter<Quat4f> PHYSICS_ROTATION = EntityDataManager.<Quat4f>createKey(EntityPhysicsBlock.class, GDataSerializers.QUAT4F);
    /**
     * Client-side render position, basically a smoothed position.
     */
    protected Vector3f renderPosition = new Vector3f(0,0,0);

    /**
     * Client-side render rotation, basically a smoothed rotation.
     */
    protected Quat4f renderRotation = new Quat4f(0, 0, 0, 1);

    /**
     * BlockState of the original non-physics Block.
     */
    protected IBlockState blockState;
    /**
     * Common variable, physics object's absolute rotation.
     */
    protected Quat4f physicsRotation = new Quat4f(0, 0, 0, 1);
    /**
     * Reference to block's built rigid body.
     */
    protected IRigidBody rigidBody;
    /**
     * Reference to rigid bodies -> collision shape.
     */
    protected ICollisionShape collisionShape;
    /**
     * If true, physics block will not use generated collision shape, but rather a
     * simple box shape.
     */
    protected boolean defaultCollisionShape;
    /**
     * RigidBody collision status.
     */
    protected boolean collisionEnabled = true;

    /**
     * Entity Collision.
     */
    protected boolean entityCollisionEnabled = true;

    /**
     * Rigid body mass.
     */
    protected float mass;
    /**
     * Friction.
     */
    protected float friction;

    protected Vector3f linearVelocity, angularVelocity;

    public EntityPhysicsBlock(World world) {
        super(world);
        setSize(1.0f, 1.0f);

        if (world.isRemote) {
            this.renderPosition = new Vector3f((float) posX, (float) posY, (float) posZ);
        }
    }

    public EntityPhysicsBlock(World world, PhysicsWorld physicsWorld, IBlockState blockState, double x, double y,
                              double z) {
        super(world, physicsWorld);
        setSize(1.0f, 1.0f);

        this.blockState = blockState;
        QuaternionUtil.setEuler(physicsRotation, 0, 0, 0);
        this.physicsRotation.set(physicsRotation);

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
        else {
            this.collisionShape = physicsWorld.getBlockCache().getShape(world, new BlockPos(x, y, z), getBlockState());
        }

        setLocationAndAngles(x, y, z, 0, 0);
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;

        if (metadata != null) {
            if (metadata.mechanics != null)
                this.mechanics.addAll(metadata.mechanics);
        }

        createPhysicsObject(physicsWorld);

    }

    /**
     * Is physics collision enabled? (Not related to entity collision)
     * @return
     */
    public boolean isCollisionEnabled() {
        return collisionEnabled;
    }

    public EntityPhysicsBlock setEntityCollisionEnabled(boolean entityCollisionEnabled) {
        this.entityCollisionEnabled = entityCollisionEnabled;
        return this;
    }

    @SideOnly(Side.CLIENT)
    public Vector3f getRenderPosition() {
        return renderPosition;
    }

    @SideOnly(Side.CLIENT)
    public Quat4f getRenderRotation() {
        return renderRotation;
    }

    @Override
    public void onCommonInit() {
        this.dataManager.register(PHYSICS_ROTATION, physicsRotation = new Quat4f(0, 0,0, 1));
    }

    @Override
    public void onClientInit() {
    }

    @Override
    public void onServerInit() {
    }


    @Override
    public void onCommonUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        // Collision handling, ripped from EntityBoat
        List<Entity> list = this.world.getEntitiesInAABBexcluding(this, this.getCollisionBoundingBox(), NOT_PHYSICS);

        if (!list.isEmpty())
        {

            for (int j = 0; j < list.size(); ++j)
            {
                Entity entity = list.get(j);

                if (!entity.isPassenger(this))
                {
                    {
                        this.applyEntityCollision(entity);
                    }
                }
            }
        }

    }

    @Override
    public void onServerUpdate() {
        if (rigidBody != null && rigidBody.isValid()) {
            // tell the entity tracker to update us if we are dirty.
            isAirBorne = isDirty();
            // Update position from given rigid body.
            final Vector3f newPosition = rigidBody.getPosition();

            setPositionAndUpdate(newPosition.getX(), newPosition.getY(),
                    newPosition.getZ());


            Vector3f velocity = new Vector3f();
            setEntityVelocity(velocity.getX(), velocity.getY(), velocity.getZ());

            // Update rotation from given rigid body.
            final Quat4f newQuat = rigidBody.getRotation();
            physicsRotation.set(newQuat.getX(), newQuat.getY(), newQuat.getZ(),
                    newQuat.getW());
            // Set location and angles, so client could have proper bounding
            // boxes.
            // Check if rigidBody is active, and if the last written postion
            // and rotation has changed.
            if (isDirty()) {
                this.dataManager.set(PHYSICS_ROTATION, physicsRotation);
                this.dataManager.setDirty(PHYSICS_ROTATION);

            }
        }

    }

    @Override
    public void onClientUpdate() {
        // No-collision check.

        this.onGround = false;

        // Read data parameter objects, then set.
        physicsRotation = this.dataManager.get(PHYSICS_ROTATION);
    }

    @Override
    public void interpolate() {
        final FloatSetting blockInterp = Physics.getInstance().getSettings().getFloatSetting("Render.BlockInterpolation");
        final float interp = blockInterp.getFloatValue();
        this.renderPosition.interpolate(VecUtility.toVector3f(getPositionVector()), interp);
        this.renderRotation.interpolate(physicsRotation, interp);
    }

    @Override
    protected void createPhysicsObject(PhysicsWorld physicsWorld) {
        Transform transform = new Transform();
        transform.setIdentity();
        transform.origin.set(VecUtility.toVector3f(getPositionVector()));
        transform.setRotation(this.physicsRotation);
        rigidBody = physicsWorld.createRigidBody(this, transform, Math.abs(mass), collisionShape);
        rigidBody.getProperties().put(EnumRigidBodyProperty.BLOCKSTATE.getName(), blockState);

        for (int i = 0; i < getMechanics().size(); i++) {
            RigidBodyMechanic mechanic = getMechanics().get(i);
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
    public boolean canBeCollidedWith() {
        return !isDead && entityCollisionEnabled;
    }

    /**
     * Returns true if this entity should push and be pushed by other entities when colliding.
     */
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
    protected boolean isPhysicsValid() {
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
        Vector3f direction = QuaternionUtil.quatRotate(physicsRotation, base, new Vector3f());
        direction.normalize();
        return direction;
    }


    @Override
    protected void dispose() {
        align();

        if (isPhysicsValid()) {
            getPhysicsWorld().removeRigidBody(this.rigidBody);
            this.rigidBody = null;
            this.collisionShape = null;
        }
    }

    // Replace physics block back to world.
    protected void align() {
        if (rigidBody != null && rigidBody.isValid()) {
            // Calculate blockpos
            BlockPos pos = getPhysicsBlockPos();

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
            FakePlayer fakePlayer = FakePlayerFactory.getMinecraft((WorldServer) world);
            // Create LibGDX quat, useful for getting euler angles.
            Quaternion eulerQuat = new Quaternion(physicsRotation.x, physicsRotation.y, physicsRotation.z, physicsRotation.w);
            // Set new orientation of player.
//            fakePlayer.setPositionAndRotation(fakePlayer.posX, fakePlayer.posX, fakePlayer. posZ, eulerQuat.getYaw(), eulerQuat.getPitch());

            // create new state.
            try {
                // try catch because: IllegalArgumentException: Cannot set property PropertyEnum{name=half, clazz=class net.minecraft.block.BlockSlab$EnumBlockHalf, values=[top, bottom]} as it does not exist in BlockStateContainer{block=minecraft:double_stone_slab, properties=[seamless, variant]}
                IBlockState state = getBlockState().getBlock().getStateForPlacement(world, pos, enumFacing, pos.getX(), pos.getY(), pos.getZ(),
                        getBlockState().getBlock().getMetaFromState(getBlockState()), fakePlayer);
                // place block.
                world.setBlockState(pos, state);
            } catch (IllegalArgumentException e) {
                Physics.getLogger().error("Tried aligning invalid BlockState: ");
                e.printStackTrace();
            }

        } else
            world.setBlockState(new BlockPos(posX, posY + 0.5F, posZ), getBlockState());
    }

    /**
     * Returns the BlockPos of the physics block, as if it were aligned to the voxel grid.
     *
     * @return
     */
    private BlockPos getPhysicsBlockPos() {
        Vec3d position = getPositionVector().addVector(0.5f, 1, 0.5f);
        return new BlockPos(MathHelper.floor(position.x), MathHelper.floor(position.y), MathHelper.floor(position.z));
    }


    /**
     * Returns a boundingBox used to collide the entity with other entities and blocks. This enables the entity to be
     * pushable on contact, like boats or minecarts.
     */
    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        return super.getEntityBoundingBox().offset(width/2, 0.0f, width/2);
    }

    /**
     * Enables entity->entity collision.
     */
    @Override
    public AxisAlignedBB getEntityBoundingBox() {
        return collisionEnabled || entityCollisionEnabled ? new AxisAlignedBB(0, 0, 0, width, height, width).offset(
                posX, posY, posZ) : VecUtility.ZERO_BB;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return getEntityBoundingBox();
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
        BlockPos.PooledMutableBlockPos blockPosition = BlockPos.PooledMutableBlockPos.retain(lightX, lightY, lightZ);

        int brightness = 0;
        if (this.world.isBlockLoaded(blockPosition))
            brightness = this.world.getCombinedLight(blockPosition, 0);

        blockPosition.release();
        return brightness;
    }


    public void applyEntityCollision(Entity entityIn)
    {
        if (NOT_PHYSICS.apply(entityIn) && (entityIn.getEntityBoundingBox().minY <= this.getEntityBoundingBox().minY))
        {
            super.applyEntityCollision(entityIn);
        }
    }


    @Override
    public void writeSpawnData(ByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeBoolean(collisionEnabled);
        buffer.writeBoolean(entityCollisionEnabled);

        buffer.writeFloat(physicsRotation.x);
        buffer.writeFloat(physicsRotation.y);
        buffer.writeFloat(physicsRotation.z);
        buffer.writeFloat(physicsRotation.w);
        BlockStateToMetadata.serializeBlockState(blockState, buffer);
    }

    @Override
    public void readSpawnData(ByteBuf buffer) {
        super.readSpawnData(buffer);
        this.collisionEnabled = buffer.readBoolean();
        this.entityCollisionEnabled = buffer.readBoolean();
        this.physicsRotation = new Quat4f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
        this.blockState = BlockStateToMetadata.deserializeBlockState(buffer);

        this.renderPosition = VecUtility.toVector3f(getPositionVector());
        this.renderRotation = new Quat4f(physicsRotation);

        // Create dynamic light source if we can!
        /**
         * How much lighting this block has.
         */
        int lightValue = getBlockState().getLightValue(world, getPhysicsBlockPos());
        if (lightValue > 0) {
            RenderHandler.getLightHandler().create(this, lightValue);
        }

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
        tagCompound.setBoolean("EntityCollisionEnabled", entityCollisionEnabled);


        // Replace vanilla entity Rotation with our own.
        tagCompound.removeTag("Rotation");
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
        this.physicsWorld = new WeakReference<PhysicsWorld>(Physics.getInstance().getPhysicsOverworld().getPhysicsByWorld(world));

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
        this.entityCollisionEnabled = tagCompound.hasKey("EntityCollisionEnabled") ? tagCompound.getBoolean("EntityCollisionEnabled") : entityCollisionEnabled;

        if (tagCompound.hasKey("CollisionShape")) {
            BlockPos.PooledMutableBlockPos blockPos = BlockPos.PooledMutableBlockPos.retain((int) posX, (int) posY, (int) posZ);
            this.collisionShape = getPhysicsWorld().getBlockCache().getShape(world, blockPos, blockState);
            blockPos.release();
        } else
            this.collisionShape = getPhysicsWorld().getDefaultShape();

//        this.physicsPosition.set((float) posX, (float) posY, (float) posZ);
        if (tagCompound.hasKey("Rotation")) {
            NBTTagList list = tagCompound.getTagList("Rotation", 5);
            this.physicsRotation.set(list.getFloatAt(0), list.getFloatAt(1), list.getFloatAt(2), list.getFloatAt(3));
        }

        if (tagCompound.hasKey("LinearVelocity")) {
            NBTTagList linearVelocity = tagCompound.getTagList("LinearVelocity", 5);
            this.linearVelocity = new Vector3f(linearVelocity.getFloatAt(0), linearVelocity.getFloatAt(1),
                    linearVelocity.getFloatAt(2));
        }

        if (tagCompound.hasKey("AngularVelocity")) {
            NBTTagList angularVelocity = tagCompound.getTagList("AngularVelocity", 5);
            this.angularVelocity = new Vector3f(angularVelocity.getFloatAt(0), angularVelocity.getFloatAt(1),
                    angularVelocity.getFloatAt(2));
        }

        // Read from NBT gets called multiple times.
        if (!isPhysicsValid()) {
            createPhysicsObject(getPhysicsWorld());
        } else {
            updatePhysicsObject(getPhysicsWorld());
        }
        super.readEntityFromNBT(tagCompound);
    }

}