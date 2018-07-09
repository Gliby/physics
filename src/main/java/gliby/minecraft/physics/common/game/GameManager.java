package gliby.minecraft.physics.common.game;

import gliby.minecraft.gman.RawItem;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.common.game.items.ItemPhysicsGun;
import gliby.minecraft.physics.common.game.items.toolgun.ItemToolGun;
import gliby.minecraft.physics.common.game.items.toolgun.actions.ToolGunActionRegistry;
import gliby.minecraft.physics.common.game.items.toolgun.actions.ToolGunAlignAction;
import gliby.minecraft.physics.common.game.items.toolgun.actions.ToolGunAttachAction;
import gliby.minecraft.physics.common.game.items.toolgun.actions.ToolGunAttractAction;
import gliby.minecraft.physics.common.game.items.toolgun.actions.ToolGunChangeGravityAction;
import gliby.minecraft.physics.common.game.items.toolgun.actions.ToolGunRemoveAction;
import gliby.minecraft.physics.common.game.items.toolgun.actions.ToolGunReviveAction;
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
