package gliby.minecraft.physics.common.physics.engine;

import gliby.minecraft.physics.common.physics.PhysicsWorld;

import javax.vecmath.Vector3f;
import java.util.List;

/**
 *
 */
public interface ICollisionShape {

    float getVolume();

    PhysicsWorld getPhysicsWorld();

    /**
     * @return
     */
    Object getCollisionShape();

    /**
     * @return
     */
    int getShapeType();

    /**
     * @return
     */
    boolean isBoxShape();

    /**
     * @return
     */
    boolean isCompoundShape();

    void setLocalScaling(Vector3f localScaling);

    /**
     * @param mass
     * @param localInertia
     */
    void calculateLocalInertia(float mass, Object localInertia);

    /**
     * Only applies to box shapes.
     *
     * @param halfExtent
     */
    void getHalfExtentsWithMargin(Vector3f halfExtent);

    /**
     * @return
     */
    List<ICollisionShapeChildren> getChildren();

    void dispose();
}
