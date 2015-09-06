/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.items;

import java.util.Random;

import net.gliby.physics.Physics;
import net.gliby.physics.common.entity.EntityPhysicsBlock;
import net.gliby.physics.common.physics.PhysicsWorld;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 *
 */
public class ItemSpawnRigidBody extends Item {
	// TODO Physics: POST-DEBUG Remove/replace this.
	public ItemSpawnRigidBody() {
		setMaxStackSize(1);
		setCreativeTab(CreativeTabs.tabMisc);
		setUnlocalizedName("physicsblockspawner");
	}

	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
		world.playSoundAtEntity(player, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
		if (!world.isRemote) {
			PhysicsWorld physicsWorld = Physics.getInstance().getCommonProxy().getPhysicsOverworld().getPhysicsByWorld(player.worldObj);
//			EntityPhysicsBlock block = new EntityPhysicsBlock(world, physicsWorld, Block.getStateById(152), (float) player.posX, (float) player.posY, (float) player.posZ);
			EntityPhysicsBlock block = new EntityPhysicsBlock(world, physicsWorld, Block.getBlockById(5).getStateFromMeta(new Random().nextInt(3) + 1), (float) player.posX, (float) player.posY, (float) player.posZ);
//			EntityPhysicsBlock block = new EntityPhysicsBlock(world, physicsWorld, Block.getBlockById(52).getStateFromMeta(2), (float) player.posX, (float) player.posY + player.getEyeHeight(), (float) player.posZ);
//			EntityPhysicsBlock block = new EntityPhysicsBlock(world, physicsWorld, Block.getStateById(14), (float) player.posX, (float) player.posY, (float) player.posZ);
//			EntityPhysicsBlock block = new EntityPhysicsBlock(world, physicsWorld, Block.getBlockById(45).getStateFromMeta(0), (float) player.posX, (float) player.posY, (float) player.posZ);
			world.spawnEntityInWorld(block);
		}

		return itemStack;
	}

}