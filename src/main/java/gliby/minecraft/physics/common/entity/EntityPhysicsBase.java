package gliby.minecraft.physics.common.entity;

import com.google.gson.Gson;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.common.entity.mechanics.RigidBodyMechanic;
import gliby.minecraft.physics.common.game.items.ItemPhysicsGun;
import gliby.minecraft.physics.common.physics.PhysicsOverworld;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import gliby.minecraft.physics.common.physics.mechanics.physicsgun.OwnedPickedObject;
import gliby.minecraft.physics.common.physics.mechanics.physicsgun.PickUpMechanic;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// TODO feature implement proper collision detection/response, stop using minecraft AABB
//TODO feature: Replace death timer with physics object limit.

/**
 *
 */
public abstract class EntityPhysicsBase extends Entity implements IEntityAdditionalSpawnData, IEntityPhysics {

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

    // Custom Vector serializer.
    public static final DataSerializer<Quat4f> QUAT4F = new DataSerializer<Quat4f>()
    {
        public void write(PacketBuffer buf, Quat4f value)
        {
            buf.writeFloat(value.getX());
            buf.writeFloat(value.getY());
            buf.writeFloat(value.getZ());
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

    public static final int TICKS_PER_SECOND = 20;
    public List<RigidBodyMechanic> mechanics = new ArrayList<RigidBodyMechanic>();
    public EntityPlayer pickerEntity;
    // Shared
    public Vector3f pickLocalHit;
    protected PhysicsWorld physicsWorld;

    protected static final DataParameter<Integer> PICKER_ID = EntityDataManager.<Integer>createKey(EntityPhysicsBase.class, DataSerializers.VARINT);
    protected static final DataParameter<Vector3f> PICK_OFFSET = EntityDataManager.<Vector3f>createKey(EntityPhysicsBase.class, VECTOR3F);

    private int lastTickActive;
    private boolean naturalDeath = true;



    /**
     * Client or Load constructor.
     *
     * @param world
     */
    public EntityPhysicsBase(World world) {
        super(world);
    }

    /**
     * Server constructor.
     *
     * @param world
     */

    public EntityPhysicsBase(World world, PhysicsWorld physicsWorld) {
        super(world);
        this.physicsWorld = physicsWorld;
    }

    /**
     * If true, entity network will be updated.
     *
     * @return
     */
    public abstract boolean isDirty();

    public void spawnRemoveParticle() {
        if (this.world.isRemote) {
            for (int i = 0; i < 20; ++i) {
                double d0 = this.rand.nextGaussian() * 0.02D;
                double d1 = this.rand.nextGaussian() * 0.02D;
                double d2 = this.rand.nextGaussian() * 0.02D;
                double d3 = 10.0D;
                this.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL,
                        this.posX + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width
                                - d0 * d3,
                        this.posY + (double) (this.rand.nextFloat() * this.height) - d1 * d3, this.posZ
                                + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width - d2 * d3,
                        d0, d1, d2);
            }
        }
    }

    @Override
    public void setDead() {
        spawnRemoveParticle();
        for (int i = 0; i < mechanics.size(); i++) {
            mechanics.get(i).dispose();
        }
        mechanics.clear();

        if (!world.isRemote) {
            if (doesPhysicsObjectExist()) {
                Vector3f minBB = new Vector3f(), maxBB = new Vector3f();
                getRigidBody().getAabb(minBB, maxBB);
                physicsWorld.awakenArea(minBB, maxBB);
            }
            dispose();
        }

        super.setDead();
    }

    public boolean isNaturalDeath() {
        return naturalDeath;
    }

    public void pick(Entity picker, Vector3f pickPoint) {
        this.pickerEntity = (EntityPlayer) picker;
        this.pickLocalHit = pickPoint;
        this.dataManager.set(PICKER_ID, picker.getEntityId());
        if (pickLocalHit != null)
        this.dataManager.set(PICK_OFFSET, pickLocalHit);
    }

    public void unpick() {
        this.pickerEntity = null;
        this.dataManager.set(PICKER_ID, -1);
        if (pickLocalHit != null)
            this.dataManager.set(PICK_OFFSET, pickLocalHit = new Vector3f());
    }

    /**
     *
     */
    protected abstract void dispose();

    public abstract void onCommonInit();

    public abstract void onServerInit();

    public abstract void onCommonUpdate();

    public abstract void onServerUpdate();

    @SideOnly(Side.CLIENT)
    public abstract void onClientInit();

    @SideOnly(Side.CLIENT)
    public abstract void onClientUpdate();

    @SideOnly(Side.CLIENT)
    public abstract void interpolate();

    /**
     * @return
     */
    protected boolean doesPhysicsObjectExist() {
        return false;
    }

    protected abstract void createPhysicsObject(PhysicsWorld physicsWorld);

    // TODO bug: entity tracker has a hard time keeping up with physics base
    // entities and eventually crashes the game.

    protected abstract void updatePhysicsObject(PhysicsWorld physicsWorld);

    /**
     * @return
     */
    public abstract IRigidBody getRigidBody();

    @Override
    public void readEntityFromNBT(NBTTagCompound tagCompound) {
        if (!world.isRemote) {
            mechanics.clear();
            PhysicsOverworld overworld = Physics.getInstance().getPhysicsOverworld();
            // TODO improvement: block property nbt saving

            Gson gson = new Gson();
            /*
             * if (tagCompound.hasKey("Properties")) { this.getRigidBody().getProperties()
             * .putAll(gson.fromJson(tagCompound.getString("Properties"), Map.class)); }
             */

            ArrayList<String> mechanicsByNames = gson.fromJson(tagCompound.getString("Mechanics"), ArrayList.class);
            if (mechanicsByNames != null) {
                for (int i = 0; i < mechanicsByNames.size(); i++) {
                    String mechanicString = mechanicsByNames.get(i);
                    RigidBodyMechanic mechanic = overworld.getMechanicFromName(mechanicString);
                    if (mechanic != null)
                        mechanics.add(overworld.getMechanicFromName(mechanicString));
                }
            }
        }
    }

    public void writeEntityToNBT(final NBTTagCompound tagCompound) {
        ArrayList<String> mechanicsByNames = new ArrayList<String>();
        for (int i = 0; i < mechanics.size(); i++) {
            mechanicsByNames
                    .add(Physics.getInstance().getPhysicsOverworld().getMechanicsMap().inverse().get(mechanics.get(i)));
        }
        // TODO improvement: block property nbt saving
        Gson gson = new Gson();
        /*
         * tagCompound.setString("Properties",
         * gson.toJson(this.getRigidBody().getProperties()));
         */
        tagCompound.setString("Mechanics", gson.toJson(mechanicsByNames));
    }

    @Override
    public void entityInit() {
        this.dataManager.register(PICK_OFFSET, pickLocalHit = new Vector3f());
        this.dataManager.register(PICKER_ID, -1);

        onCommonInit();
        if (this.world.isRemote)
            onClientInit();
        else
            onServerInit();

    }

    @Override
    public final void onUpdate() {
        super.onUpdate();
        if (this.world.isRemote) {
            int pickerId = dataManager.get(PICKER_ID);
            if (pickerId != -1) {
                Entity entity = this.world.getEntityByID(pickerId);
                if (entity instanceof EntityPlayer) {
                    this.pickerEntity = (EntityPlayer) entity;
                    if (this.pickerEntity != null) {
                        this.pickLocalHit = dataManager.get(PICK_OFFSET);
                    } else {
                        this.pickLocalHit = null;
                    }
                }
            } else {
                this.pickerEntity = null;
            }
            onClientUpdate();
        } else {
            onServerUpdate();
            // Check if picker exists.
            if (pickerEntity != null) {
                PickUpMechanic mechanic = (PickUpMechanic) physicsWorld.getMechanics().get("PickUp");
                // Continue if mechanic exists.
                if (mechanic != null) {
                    Item item = pickerEntity.getHeldItem(EnumHand.MAIN_HAND) != null ? pickerEntity.getHeldItem(EnumHand.MAIN_HAND).getItem() : null;
                    // Check if held item isn't physics gun.
                    if (!(item instanceof ItemPhysicsGun)) {
                        OwnedPickedObject object = null;
                        // Continue if picked object exists.
                        if ((object = mechanic.getOwnedPickedObject(pickerEntity)) != null) {
                            // Alert's dataWatcher that item shouldn't be
                            // picked.
                            unpick();

                            // Remove picked object
                            mechanic.removeOwnedPickedObject(object);
                        }
                    }
                }
            }

            IRigidBody rigidBody = getRigidBody();
            if (rigidBody != null && rigidBody.isValid()) {
                if (rigidBody.isActive())
                    lastTickActive = ticksExisted;

                if ((ticksExisted - lastTickActive) / TICKS_PER_SECOND > Physics.getInstance().getSettings()
                        .getFloatSetting("PhysicsEntities.InactivityDeathTime").getFloatValue()) {
                    this.setDead();
                }
                // Update mechanics.
                for (int i = 0; i < mechanics.size(); i++) {
                    RigidBodyMechanic mechanic = mechanics.get(i);
                    if (mechanic.isEnabled())
                        mechanic.update(getRigidBody(), physicsWorld, this, world.isRemote ? Side.CLIENT : Side.SERVER);
                }

                if (rigidBody.getProperties().containsKey(EnumRigidBodyProperty.DEAD.getName())) {
                    this.naturalDeath = false;
                    this.setDead();
                }
            }
        }
    }

    /**
     * @return
     */
    public abstract AxisAlignedBB getRenderBoundingBox();

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        List<RigidBodyMechanic> savedMechanics = new ArrayList<RigidBodyMechanic>();
        for (int i = 0; i < mechanics.size(); i++) {
            RigidBodyMechanic mechanic = mechanics.get(i);
            if (mechanic.isCommon()) {
                savedMechanics.add(mechanic);
            }
        }

        PhysicsOverworld overworld = Physics.getInstance().getPhysicsOverworld();

        buffer.writeInt(savedMechanics.size());
        for (int i = 0; i < savedMechanics.size(); i++) {
            ByteBufUtils.writeUTF8String(buffer, overworld.getMechanicsMap().inverse().get(savedMechanics.get(i)));
        }

        buffer.writeInt(dataManager.get(PICKER_ID));

        buffer.writeFloat(this.pickLocalHit.x);
        buffer.writeFloat(this.pickLocalHit.y);
        buffer.writeFloat(this.pickLocalHit.z);
    }

    @Override
    public void readSpawnData(ByteBuf buffer) {
        int size = buffer.readInt();
        PhysicsOverworld overworld = Physics.getInstance().getPhysicsOverworld();
        for (int i = 0; i < size; i++) {
            String mechanicName = ByteBufUtils.readUTF8String(buffer);
            RigidBodyMechanic mechanic = overworld.getMechanicFromName(mechanicName);
            mechanics.add(mechanic);
        }

        this.pickerEntity = (EntityPlayer) this.world.getEntityByID(buffer.readInt());
        Vector3f readPick = new Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
        if (pickerEntity != null)
            this.pickLocalHit = readPick;
    }

}
