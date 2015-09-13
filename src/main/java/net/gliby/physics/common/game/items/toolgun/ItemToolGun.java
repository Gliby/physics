/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.game.items.toolgun;

import javax.vecmath.Vector3f;

import io.netty.buffer.ByteBuf;
import net.gliby.gman.EntityUtility;
import net.gliby.gman.RawItem;
import net.gliby.physics.Physics;
import net.gliby.physics.client.SoundHandler;
import net.gliby.physics.common.entity.EntityToolGunBeam;
import net.gliby.physics.common.game.items.toolgun.actions.IToolGunAction;
import net.gliby.physics.common.game.items.toolgun.actions.ToolGunActionRegistry;
import net.gliby.physics.common.packets.MinecraftPacket;
import net.gliby.physics.common.physics.PhysicsWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 *
 */
public class ItemToolGun extends RawItem {

	public ItemToolGun(Physics physics) {
		setMaxStackSize(1);
		setCreativeTab(CreativeTabs.tabTools);
		setUnlocalizedName("physicstoolgun");
		setFull3D();
		physics.registerPacket(PacketToolGunUse.class, PacketToolGunUse.class, Side.SERVER);
		physics.registerPacket(PacketToolGunStoppedUsing.class, PacketToolGunStoppedUsing.class, Side.SERVER);
	}

	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
		return true;
	}

	@Override
	public float getDigSpeed(ItemStack itemstack, net.minecraft.block.state.IBlockState state) {
		return 0;
	}

	private static int currentMode = 0, lastMode;

	public static int getCurrentMode() {
		return currentMode;
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onMouseEvent(MouseEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemToolGun
				&& !mc.thePlayer.isSpectator()) {
			if (event.buttonstate) {
				if (event.button == 0) {
					Physics.getDispatcher().sendToServer(new PacketToolGunUse(currentMode,
							EntityUtility.toVector3f(EntityUtility.rayTrace(mc.thePlayer, 64).hitVec)));
				} else if (event.button == 1) {
					if (currentMode < ToolGunActionRegistry.getInstance().getValueDefinitions().size() - 1) {
						currentMode++;
					} else
						currentMode = 0;
					if (lastMode != currentMode) {
						SoundHandler.playLocalSound(mc, "ToolGun.Scroll");
						Physics.getDispatcher().sendToServer(new PacketToolGunStoppedUsing(lastMode));
					}
					lastMode = currentMode;
				}
			}
		}
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BOW;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return Integer.MAX_VALUE;
	}

	/**
	 * @return
	 */
	public static String getModeName() {
		if (!ToolGunActionRegistry.getInstance().getValueDefinitions().isEmpty())
			return ToolGunActionRegistry.getInstance().getValueDefinitions().get(currentMode);
		else
			return "Loading";
	}

	public static class PacketToolGunStoppedUsing extends MinecraftPacket
			implements IMessageHandler<PacketToolGunStoppedUsing, IMessage> {

		public PacketToolGunStoppedUsing() {
		}

		private int mode;

		public PacketToolGunStoppedUsing(int mode) {
			this.mode = mode;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			this.mode = buf.readInt();
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeInt(mode);
		}

		@Override
		public IMessage onMessage(final PacketToolGunStoppedUsing packet, final MessageContext ctx) {
			MinecraftServer.getServer().addScheduledTask(new Runnable() {
				@Override
				public void run() {
					EntityPlayerMP player = ctx.getServerHandler().playerEntity;
					World world = player.worldObj;
					PhysicsWorld physicsWorld = Physics.getInstance().getCommonProxy().getPhysicsOverworld()
							.getPhysicsByWorld(world);
					if (player.getCurrentEquippedItem() != null
							&& player.getCurrentEquippedItem().getItem() instanceof ItemToolGun) {
						IToolGunAction toolGunAction;
						if ((toolGunAction = ToolGunActionRegistry.getInstance().getActions()
								.get(packet.mode)) != null) {
							toolGunAction.stoppedUsing(physicsWorld, player);
						}
					}
				}
			});
			return null;
		}
		/*
		 * GravityMagnetMechanic mechanic = (GravityMagnetMechanic)
		 * physicsWorld.getMechanics().get("GravityMagnet"); if (mechanic !=
		 * null) { if (!mechanic.gravityMagnetEntityExists(player)) {
		 * mechanic.addGravityMagnetEntity(player); } else {
		 * mechanic.removeGravityMagnetEntity(player); } }
		 */
	}

	public static class PacketToolGunUse extends MinecraftPacket
			implements IMessageHandler<PacketToolGunUse, IMessage> {

		private Vector3f lookAt;
		private int mode;

		public PacketToolGunUse() {
		}

		public PacketToolGunUse(int mode, Vector3f lookAt) {
			this.mode = mode;
			this.lookAt = lookAt;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			this.mode = buf.readInt();
			this.lookAt = new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat());
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeInt(mode);
			buf.writeFloat(lookAt.x);
			buf.writeFloat(lookAt.y);
			buf.writeFloat(lookAt.z);
		}

		@Override
		public IMessage onMessage(final PacketToolGunUse packet, final MessageContext ctx) {
			MinecraftServer.getServer().addScheduledTask(new Runnable() {

				@Override
				public void run() {
					EntityPlayerMP player = ctx.getServerHandler().playerEntity;
					World world = player.worldObj;
					PhysicsWorld physicsWorld = Physics.getInstance().getCommonProxy().getPhysicsOverworld()
							.getPhysicsByWorld(world);
					if (player.getCurrentEquippedItem() != null
							&& player.getCurrentEquippedItem().getItem() instanceof ItemToolGun) {
						IToolGunAction toolGunAction;
						if ((toolGunAction = ToolGunActionRegistry.getInstance().getActions()
								.get(packet.mode)) != null) {
							if (toolGunAction.use(physicsWorld, player, packet.lookAt)) {
								EntityUtility.spawnEntitySynchronized(world,
										new EntityToolGunBeam(world, player, packet.lookAt));
							}
						}
					}
				}
			});
			return null;
		}
		/*
		 * GravityMagnetMechanic mechanic = (GravityMagnetMechanic)
		 * physicsWorld.getMechanics().get("GravityMagnet"); if (mechanic !=
		 * null) { if (!mechanic.gravityMagnetEntityExists(player)) {
		 * mechanic.addGravityMagnetEntity(player); } else {
		 * mechanic.removeGravityMagnetEntity(player); } }
		 */
	}

}