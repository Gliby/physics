package net.gliby.minecraft.physics.common.entity;

import com.google.common.base.Predicate;

import net.gliby.minecraft.gman.settings.INIProperties;
import net.gliby.minecraft.gman.settings.ObjectSetting;
import net.gliby.minecraft.gman.settings.Setting.Listener;
import net.gliby.minecraft.physics.Physics;
import net.minecraft.entity.Entity;

public interface IEntityPhysics {

	public static final Predicate NOT_PHYSICS_OBJECT = new Predicate() {
		public boolean apply(Entity entityIn) {
			ObjectSetting setting = Physics.getInstance().getSettings()
					.getObjectSetting("PhysicsEntities.EntityColliderBlacklist");
			final String[] classes = (String[]) setting.getSettingData();
			if (Physics.entityBlacklistClassCache == null) {
				Physics.entityBlacklistClassCache = new Class[classes.length];
				setting.addReadListener(new Listener() {
					@Override
					public void listen(INIProperties ini) {
						Physics.entityBlacklistClassCache = new Class[classes.length];
					}
				});
			}

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
