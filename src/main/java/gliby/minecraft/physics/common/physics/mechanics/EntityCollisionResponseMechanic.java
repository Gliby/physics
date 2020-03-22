package gliby.minecraft.physics.common.physics.mechanics;

import com.bulletphysicsx.collision.broadphase.CollisionFilterGroups;
import com.bulletphysicsx.collision.dispatch.CollisionFlags;
import com.bulletphysicsx.linearmath.Transform;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.common.entity.IEntityPhysics;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IGhostObject;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.vecmath.Vector3f;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class EntityCollisionResponseMechanic extends PhysicsMechanic {

    private World world;
    private Map<Integer, Long> timeAdded;
    private Map<Integer, IGhostObject> ghostObjects;
    private Transform entityTransform = new Transform();

    /**
     * @param physicsWorld
     * @param ticksPerSecond
     */
    public EntityCollisionResponseMechanic(World world, PhysicsWorld physicsWorld,
                                           int ticksPerSecond) {
        super(physicsWorld, ticksPerSecond);
        this.world = world;
        ghostObjects = new HashMap<Integer, IGhostObject>();
        timeAdded = new HashMap<Integer, Long>();
    }

    @Override
    public void update() {
        for (int i = 0; i < physicsWorld.getRigidBodies().size(); i++) {
            IRigidBody rigidBody = physicsWorld.getRigidBodies().get(i);
            Entity entityBody = rigidBody.getOwner();

            Vector3f minBB, maxBB;
            // Get rigidBody BB.
            rigidBody.getAabb(minBB = new Vector3f(), maxBB = new Vector3f());
            // Create AABB, and offset.
            AxisAlignedBB axisAlignedBB = new AxisAlignedBB(minBB.x, minBB.y, minBB.z, maxBB.x, maxBB.y, maxBB.z)
                    .offset(0.5f, 0.5f, 0.5f);
            List<Entity> intersectingEntites;

            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            intersectingEntites = world.getEntitiesWithinAABB(Entity.class, axisAlignedBB,
                    IEntityPhysics.NOT_BLACKLISTED);


            for (Entity entity : intersectingEntites) {
                if (entity.isDead)
                    return;

                entityTransform.setIdentity();
                entityTransform.origin.set(new Vector3f((float) entity.posX - 0.5f, (float) entity.posY + 0.25f,
                        (float) entity.posZ - 0.5f));

                if (entity instanceof IProjectile) {
                    Vector3f direction = new Vector3f();
                    direction.setX((float) (entity.motionX));
                    direction.setY((float) (entity.motionY));
                    direction.setZ((float) (entity.motionZ));
                    boolean moving = direction.length() > 0;
                    if (moving) {
                        direction.scale(Physics.getConfig().getGame().getProjectileImpulseForce());
                        rigidBody.applyCentralImpulse(direction);
                        rigidBody.activate();
                    }
                    continue;
                }

                IGhostObject ghostObject;
                if ((ghostObject = ghostObjects.get(entity.getEntityId())) != null) {

                    // Update
                    ghostObject.setWorldTransform(entityTransform);
                    timeAdded.put(entity.getEntityId(), System.currentTimeMillis());
                } else {
                    ghostObject = physicsWorld.createPairCachingGhostObject();
                    ghostObject.setInterpolationWorldTransform(entityTransform);
                    ghostObject.setWorldTransform(entityTransform);
                    AxisAlignedBB enlargedBB = entity.getEntityBoundingBox().grow(0.1f, -0.1f, 0.1f);
                    Vector3f bounds = new Vector3f(
                            (float) enlargedBB.maxX - (float) enlargedBB.minX,
                            (float) enlargedBB.maxY - (float) enlargedBB.minY,
                            (float) enlargedBB.maxZ - (float) enlargedBB.minZ);
                    bounds.scale(0.5f);

                    // Create ghost object
                    ghostObject.setCollisionShape(physicsWorld.createBoxShape(bounds));
                    ghostObject.setCollisionFlags(CollisionFlags.CHARACTER_OBJECT);
                    physicsWorld.addCollisionObject(ghostObject, CollisionFilterGroups.CHARACTER_FILTER,
                            (short) (CollisionFilterGroups.STATIC_FILTER | CollisionFilterGroups.DEFAULT_FILTER));
                    ghostObjects.put(entity.getEntityId(), ghostObject);
                    timeAdded.put(entity.getEntityId(), System.currentTimeMillis());
                }
            }
        }

        Iterator<Map.Entry<Integer, Long>> iterator = timeAdded.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Long> entry = iterator.next();
            int entityId = entry.getKey();
            long timeAdded = entry.getValue();
            if ((System.currentTimeMillis() - timeAdded) / 1000.0f > Physics.getConfig().getPhysicsEntities().getEntityColliderCleanupTime()) {
                physicsWorld.removeCollisionObject(ghostObjects.get(entityId));
                ghostObjects.remove(entityId);
                iterator.remove();
            }
        }
    }

    @Override
    public String getName() {
        return "EntityResponseMechanic";
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * net.gliby.physics.common.physics.worldmechanics.PhysicsMechanic#init()
     */
    @Override
    public void init() {
    }

    @Override
    public void dispose() {
        world = null;
        physicsWorld = null;
        entityTransform = null;
        ghostObjects.clear();
        timeAdded.clear();
    }

}
