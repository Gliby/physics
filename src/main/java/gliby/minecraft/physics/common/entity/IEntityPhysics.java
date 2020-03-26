package gliby.minecraft.physics.common.entity;

import com.google.common.base.Predicate;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.PhysicsConfig;
import net.minecraft.entity.Entity;

public interface IEntityPhysics {

    Predicate NOT_BLACKLISTED = new Predicate() {
        public boolean apply(Entity entityIn) {
            final String[] classes = PhysicsConfig.PHYSICS_ENTITIES.entityColliderBlacklist;

            if (classes.length > Physics.entityBlacklistClassCache.length) {
                Physics.entityBlacklistClassCache = new Class[classes.length];
                // cache classes from config
                for (int i = 0; i < classes.length; i++) {
                    try {
                        Physics.entityBlacklistClassCache[i] = Class.forName(classes[i]);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }

            for (int i = 0; i < Physics.entityBlacklistClassCache.length;  i++){
                Class clazz = Physics.entityBlacklistClassCache[i];
                if (clazz != null && clazz.isInstance(entityIn)) {
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
