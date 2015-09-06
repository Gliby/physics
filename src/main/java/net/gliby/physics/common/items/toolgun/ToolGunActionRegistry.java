/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.items.toolgun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.gliby.physics.Physics;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.ImmutableMap;

/**
 *
 */
public class ToolGunActionRegistry {

	private static ToolGunActionRegistry instance = new ToolGunActionRegistry();

	private Map<Integer, IToolGunAction> actions;

	public ImmutableMap<Integer, IToolGunAction> getActions() {
		return ImmutableMap.copyOf(actions);
	}

	public ToolGunActionRegistry() {
		actions = new HashMap<Integer, IToolGunAction>();
		valueDefinitions = new ArrayList<String>();
	}

	private static int actionIndex;

	public void registerAction(IToolGunAction action, String modID) {
		if (!MinecraftForge.EVENT_BUS.post(new ToolGunActionEvent.Register(action, modID != null ? modID.equals(Physics.MOD_ID) : false))) {
			actions.put(actionIndex++, action);
			valueDefinitions.add(action.getName());
		}
	}

	public static ToolGunActionRegistry getInstance() {
		return instance;
	}

	private List<String> valueDefinitions;

	public void setValueDefinitions(List<String> list) {
		this.valueDefinitions = list;
	}

	/**
	 * @return
	 */
	public List<String> getValueDefinitions() {
		return valueDefinitions;
	}

}
