package gliby.minecraft.physics.common.entity;

import com.google.common.base.Predicate;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.PhysicsConfig;
import net.minecraft.entity.Entity;

public interface IEntityPhysics {

    Predicate NOT_BLACKLISTED = new Predicate() {
        public boolean apply(Entity entityIn) {
            final String[] classes =  PhysicsConfig.PHYSICS_ENTITIES.entityColliderBlacklist;

            for (int i = 0; i < classes.length; i++) {
                Class clazz = Physics.entityBlacklistClassCache[i];
                if (clazz == null) {
                    try {
                        clazz = Physics.entityBlacklistClassCache[i] = Class.forName(classes[i]);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                if (clazz.isInstance(entityIn)) {
                    return false;
                }
            }

            return true;
        }

        public boolean apply(Object object) {
            return this.apply((Entity) object);
        }
    };

}
