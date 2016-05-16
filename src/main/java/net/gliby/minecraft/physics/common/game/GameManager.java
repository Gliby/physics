package net.gliby.minecraft.physics.common.game;

import net.gliby.minecraft.gman.RawItem;
import net.gliby.minecraft.physics.Physics;
import net.gliby.minecraft.physics.common.game.items.ItemPhysicsGun;
import net.gliby.minecraft.physics.common.game.items.toolgun.ItemToolGun;
import net.gliby.minecraft.physics.common.game.items.toolgun.actions.ToolGunActionRegistry;
import net.gliby.minecraft.physics.common.game.items.toolgun.actions.ToolGunAlignAction;
import net.gliby.minecraft.physics.common.game.items.toolgun.actions.ToolGunAttachAction;
import net.gliby.minecraft.physics.common.game.items.toolgun.actions.ToolGunAttractAction;
import net.gliby.minecraft.physics.common.game.items.toolgun.actions.ToolGunChangeGravityAction;
import net.gliby.minecraft.physics.common.game.items.toolgun.actions.ToolGunRemoveAction;
import net.gliby.minecraft.physics.common.game.items.toolgun.actions.ToolGunReviveAction;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class GameManager {

	private Physics physics;

	public GameManager(Physics physics) {
		this.physics = physics;
	}

	private ToolGunActionRegistry toolGunRegistry;
	
	private static Item itemRigidBodySpawner;
	public RawItem itemPhysicsGun, itemToolgun;

	public void preInit() {
		GameRegistry.registerItem(itemToolgun = new ItemToolGun(physics), itemToolgun.getUnlocalizedName());
		GameRegistry.registerItem(itemPhysicsGun = new ItemPhysicsGun(physics), itemPhysicsGun.getUnlocalizedName());
		MinecraftForge.EVENT_BUS.register(itemPhysicsGun);
		MinecraftForge.EVENT_BUS.register(itemToolgun);

		toolGunRegistry = new ToolGunActionRegistry();
		toolGunRegistry.registerAction(new ToolGunAttachAction(), Physics.MOD_ID);
		toolGunRegistry.registerAction(new ToolGunReviveAction(), Physics.MOD_ID);
		toolGunRegistry.registerAction(new ToolGunAlignAction(), Physics.MOD_ID);
		toolGunRegistry.registerAction(new ToolGunAttractAction(), Physics.MOD_ID);
		toolGunRegistry.registerAction(new ToolGunChangeGravityAction(), Physics.MOD_ID);
		toolGunRegistry.registerAction(new ToolGunRemoveAction(), Physics.MOD_ID);
		// toolGunRegistry.registerAction(new ToolGunMotorAction(),
		// Physics.MOD_ID);
	}

	public ToolGunActionRegistry getToolGunRegistry() {
		return toolGunRegistry;
	}

}
