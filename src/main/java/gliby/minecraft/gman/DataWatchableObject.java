/**
 * Copyright (c) 2015, Mine Fortress.
 */
package gliby.minecraft.gman;

import net.minecraft.entity.Entity;

/**
 *
 */
public abstract class DataWatchableObject {

	protected int dataWatcherIndex;
	protected Entity entity;

	public DataWatchableObject(Entity entity) {
		this.entity = entity;
		dataWatcherIndex = this.entity.getDataWatcher().getAllWatched().size() - 1;
	}

	public abstract void write(Object... obj);

	public abstract void read(Object... obj);

	public boolean hasUpdated() {
		return entity.getDataWatcher().hasObjectChanged();
	}

	/**
	 * @return
	 */
	public int getNextIndex() {
		return ++dataWatcherIndex;
	}
}
