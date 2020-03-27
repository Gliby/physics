package gliby.minecraft.physics.common.physics;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.PhysicsConfig;
import gliby.minecraft.physics.common.entity.actions.ActivateRedstoneAction;
import gliby.minecraft.physics.common.entity.actions.BlockInheritanceAction;
import gliby.minecraft.physics.common.entity.actions.EnvironmentResponseAction;
import gliby.minecraft.physics.common.entity.actions.RigidBodyAction;
import gliby.minecraft.physics.common.physics.engine.javabullet.JavaPhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.nativebullet.NativePhysicsWorld;
import gliby.minecraft.physics.common.physics.mechanics.EntityCollisionResponseMechanic;
import gliby.minecraft.physics.common.physics.mechanics.ToolMechanics;
import gliby.minecraft.physics.common.physics.mechanics.gravitymagnets.GravityModifierMechanic;
import gliby.minecraft.physics.common.physics.mechanics.physicsgun.PickUpMechanic;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.vecmath.Vector3f;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// TODO (0.8.0) bind step speed to match game speed.
public class PhysicsOverworld {

    // Bullet Offset.
    public static final float OFFSET = 0.5f;

    private static final AxisAlignedBB BLOCK_BREAK_BB = new AxisAlignedBB(-1.75, -1.75, -1.75, 1.75, 1.75, 1.75);

    private final BiMap<String, RigidBodyAction> rigidBodyActions = HashBiMap.create();

    protected Physics physics;

    protected Map<World, PhysicsWorld> physicsWorlds = new HashMap<World, PhysicsWorld>();

    public PhysicsOverworld(final Physics physics) {
        this.physics = physics;

        // Registers available mechanics.
        // TODO (0.6.0) finish: global mechanics
        getRigidBodyMechanicsMap().put("EnvironmentResponse", new EnvironmentResponseAction());
//        getRigidBodyMechanicsMap().put("Bounce", new BounceMechanic());

        // TODO (0.6.0) feature: get these rigidbody mechanics working properly
        getRigidBodyMechanicsMap().put("ActivateRedstone", new ActivateRedstoneAction());
        getRigidBodyMechanicsMap().put("BlockInheritance", new BlockInheritanceAction().setCommon(true));
        //getMechanicsMap().put("ClientBlockInheritance", new ClientBlockInheritanceMechanic().setCommon(true));
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * @return
     */
    public PhysicsWorld getPhysicsByWorld(final World access) {
        PhysicsWorld physicsWorld = getPhysicsWorldMap().get(access);
        if (physicsWorld == null) {
            final Vector3f gravity = new Vector3f(0, PhysicsConfig.PHYSICS_ENGINE.gravityForce, 0);
            // TODO (0.6.0) add world border support
            physicsWorld = createPhysicsWorld(
                    !PhysicsConfig.PHYSICS_ENGINE.useJavaPhysics,
                    new IPhysicsWorldConfiguration() {

                        @Override
                        public boolean shouldSimulate(final World world, final PhysicsWorld physicsWorld) {
                            return !world.playerEntities.isEmpty();
                        }

                        @Override
                        public World getWorld() {
                            return access;
                        }

                        @Override
                        public final int getTicksPerSecond() {
                            return PhysicsConfig.PHYSICS_ENGINE.ticksPerSecond;
                        }

                        @Override
                        public Vector3f getRegularGravity() {
                            return gravity;
                        }
                    });
            physicsWorld.getMechanics().put("PickUp", new PickUpMechanic(physicsWorld, 20));
            physicsWorld.getMechanics().put("GravityMagnet", new GravityModifierMechanic(physicsWorld, 20));
            physicsWorld.getMechanics().put("ToolMan", new ToolMechanics(physics.getGameManager().getToolGunRegistry(), physicsWorld, 20));
            if (PhysicsConfig.PHYSICS_ENTITIES.entityColliders)
                physicsWorld.getMechanics().put("EntityCollision", new EntityCollisionResponseMechanic(access, physicsWorld, 20));

            physicsWorld.create();
            getPhysicsWorldMap().put(access, physicsWorld);
            Physics.getLogger().info(String.format("Started running new physics world on %s (%s tick(s) per second.)", physicsWorld.toString(), physicsWorld.getPhysicsConfiguration().getTicksPerSecond()));
        }

        return physicsWorld;
    }

    @SubscribeEvent
    public void onWorldTick(final TickEvent.WorldTickEvent event) {
        // Simulate physics on World Tick.
        if (event.world.playerEntities.size() > 0 && !event.world.isRemote) {
            getPhysicsByWorld(event.world).update();
        }

    }

    @SubscribeEvent
    public void onUnload(final WorldEvent.Unload event) {
        // Destroy physics world on unload.
        final PhysicsWorld physicsWorld;
        if ((physicsWorld = getPhysicsWorldMap().get(event.getWorld())) != null) {
            physicsWorld.dispose();
            getPhysicsWorldMap().remove(event.getWorld());
            Physics.getLogger().info("Destroyed " + event.getWorld().getWorldInfo().getWorldName() + " physics world.");
        }
    }

    @SubscribeEvent
    public void onBlockEvent(final BlockEvent event) {
        // Wake up rigid bodies on BlockEvent.
        final PhysicsWorld physicsWorld;
        if ((physicsWorld = getPhysicsWorldMap().get(event.getWorld())) != null) {
            final AxisAlignedBB bb = BLOCK_BREAK_BB.offset(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());
            physicsWorld.awakenArea(new Vector3f((float) bb.minX, (float) bb.minY, (float) bb.minZ),
                    new Vector3f((float) bb.maxX, (float) bb.maxY, (float) bb.maxZ));
        }
    }

    /**
     * @return the mechanicsMap
     */
    public BiMap<String, RigidBodyAction> getRigidBodyMechanicsMap() {
        return rigidBodyActions;
    }

    /**
     * @param string
     * @return
     */
    public RigidBodyAction getActionByName(final String string) {
        final RigidBodyAction mechanic = rigidBodyActions.get(string);
        if (mechanic == null) {
            Physics.getLogger().debug("Mechanic: " + string + " doesn't exists, or is misspelled.");
        }
        return mechanic;
    }

    public Map<World, PhysicsWorld> getPhysicsWorldMap() {
        return physicsWorlds;
    }

    private PhysicsWorld createPhysicsWorld(boolean useNative, final IPhysicsWorldConfiguration physicsConfig) {
        // TODO (0.6.0) Add NativePhysics macOS compatibility.
        final boolean isMac = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("mac");
        if (useNative && isMac) {
            Physics.getLogger().info("Forced non-native physics under OSX.");
            useNative = false;
        }

        return useNative ? new NativePhysicsWorld(physics, this, physicsConfig) : new JavaPhysicsWorld(physics, this, physicsConfig);
    }
}
