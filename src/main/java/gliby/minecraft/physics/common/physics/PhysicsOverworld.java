package gliby.minecraft.physics.common.physics;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.common.entity.mechanics.BounceMechanic;
import gliby.minecraft.physics.common.entity.mechanics.EnvironmentGravityMechanic;
import gliby.minecraft.physics.common.entity.mechanics.EnvironmentResponseMechanic;
import gliby.minecraft.physics.common.entity.mechanics.RigidBodyMechanic;
import gliby.minecraft.physics.common.physics.engine.javabullet.JavaPhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.nativebullet.NativePhysicsWorld;
import gliby.minecraft.physics.common.physics.mechanics.ToolMechanics;
import gliby.minecraft.physics.common.physics.mechanics.gravitymagnets.GravityModifierMechanic;
import gliby.minecraft.physics.common.physics.mechanics.physicsgun.PickUpMechanic;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.vecmath.Vector3f;
import java.util.HashMap;
import java.util.Map;

// TODO FIXME PhysicsWorld: create ability to choose between multi-thread of single thread physics world, should solve crashes relating to concurrency.

public class PhysicsOverworld {

    private static final AxisAlignedBB BLOCK_BREAK_BB = new AxisAlignedBB(-1.75, -1.75, -1.75, 1.75, 1.75, 1.75);
    static int world;
    private final BiMap<String, RigidBodyMechanic> mechanicsMap = HashBiMap.create();
    private final Physics physics;
    protected Map<World, PhysicsWorld> physicsWorlds = new HashMap<World, PhysicsWorld>();

    public PhysicsOverworld(final Physics physics) {
        MinecraftForge.EVENT_BUS.register(this);
        this.physics = physics;
        // Registers available mechanics.
        // TODO finish: mechanics
        getMechanicsMap().put("EnvironmentGravity", new EnvironmentGravityMechanic());
        getMechanicsMap().put("EnvironmentResponse", new EnvironmentResponseMechanic());
        getMechanicsMap().put("Bounce", new BounceMechanic());

        // TODO feature: get these mechanics working properly
        //getMechanicsMap().put("ActivateRedstone", new ActivateRedstoneMechanic());
        //getMechanicsMap().put("BlockInheritance", new BlockInheritanceMechanic());
        //getMechanicsMap().put("ClientBlockInheritance", new ClientBlockInheritanceMechanic().setCommon(true));
    }

    /**
     * @return
     */
    public PhysicsWorld getPhysicsByWorld(final World access) {
        PhysicsWorld physicsWorld = getPhysicsWorldMap().get(access);
        if (physicsWorld == null) {
            final Vector3f gravity = new Vector3f(0, physics.getSettings().getFloatSetting("PhysicsEngine.GravityForce").getFloatValue(), 0);
            physicsWorld = createPhysicsWorld(
                    !physics.getSettings().getBooleanSetting("PhysicsEngine.UseJavaPhysics").getBooleanValue(),
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
                            return physics.getSettings().getIntegerSetting("PhysicsEngine.TickRate").getIntValue();
                        }

                        @Override
                        public Vector3f getRegularGravity() {
                            return gravity;
                        }
                    });
            physicsWorld.getMechanics().put("PickUp", new PickUpMechanic(physicsWorld, 20));
            physicsWorld.getMechanics().put("GravityMagnet", new GravityModifierMechanic(physicsWorld, 20));
            physicsWorld.getMechanics().put("ToolMan", new ToolMechanics(physics.getGameManager().getToolGunRegistry(), physicsWorld, 20));
            // worldStepSimulator.getMechanics().put("EntityCollision",
            // new EntityCollisionResponseMechanic(world, worldStepSimulator,
            // true,
            // 20));
            physicsWorld.create();
            getPhysicsWorldMap().put(access, physicsWorld);
            Physics.getLogger().info(String.format("Started running new physics world on %s (%s tick(s) per second.)", physicsWorld.toString(), physicsWorld.getPhysicsConfiguration().getTicksPerSecond()));
        }

        return physicsWorld;
    }

    @SubscribeEvent
    public void onWorldTick(final TickEvent.WorldTickEvent event) {
        if (event.world.playerEntities.size() > 0 && !event.world.isRemote) {
            getPhysicsByWorld(event.world).update();
        }

    }

    @SubscribeEvent
    public void onUnload(final WorldEvent.Unload event) {
        final PhysicsWorld physicsWorld;
        if ((physicsWorld = getPhysicsWorldMap().get(event.world)) != null) {
            physicsWorld.dispose();
            getPhysicsWorldMap().remove(event.world);
            Physics.getLogger().info("Destroyed " + event.world.getWorldInfo().getWorldName() + " physics world.");
        }
    }

    @SubscribeEvent
    public void onBlockEvent(final BlockEvent event) {
        final PhysicsWorld physicsWorld;
        if ((physicsWorld = getPhysicsWorldMap().get(event.world)) != null) {
            final AxisAlignedBB bb = BLOCK_BREAK_BB.offset(event.pos.getX(), event.pos.getY(), event.pos.getZ());
            physicsWorld.awakenArea(new Vector3f((float) bb.minX, (float) bb.minY, (float) bb.minZ),
                    new Vector3f((float) bb.maxX, (float) bb.maxY, (float) bb.maxZ));
        }
    }

    /**
     * @return the mechanicsMap
     */
    public BiMap<String, RigidBodyMechanic> getMechanicsMap() {
        return mechanicsMap;
    }

    /**
     * @param string
     * @return
     */
    public RigidBodyMechanic getMechanicFromName(final String string) {
        final RigidBodyMechanic mechanic = mechanicsMap.get(string);
        if (mechanic == null) {
            Physics.getLogger().debug("Mechanic: " + string + " doesn't exists, or is misspelled.");
        }
        return mechanic;
    }

    public Map<World, PhysicsWorld> getPhysicsWorldMap() {
        return physicsWorlds;
    }

    private PhysicsWorld createPhysicsWorld(final boolean useNative, final IPhysicsWorldConfiguration physicsConfig) {
        final PhysicsWorld physicsWorld = useNative ? new NativePhysicsWorld(physics, this, physicsConfig) : new JavaPhysicsWorld(physics, this, physicsConfig);
        return physicsWorld;
    }
}
