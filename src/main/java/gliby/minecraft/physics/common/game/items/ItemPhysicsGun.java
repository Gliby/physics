package gliby.minecraft.physics.common.game.items;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import gliby.minecraft.gman.EntityUtility;
import gliby.minecraft.gman.RawItem;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.common.entity.EntityPhysicsBase;
import gliby.minecraft.physics.common.packets.MinecraftPacket;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IRayResult;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import gliby.minecraft.physics.common.physics.mechanics.physicsgun.OwnedPickedObject;
import gliby.minecraft.physics.common.physics.mechanics.physicsgun.PickUpMechanic;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemPhysicsGun extends RawItem {

	public ItemPhysicsGun(Physics physics) {
		setUnlocalizedName("physicsgun");
		setCreativeTab(CreativeTabs.tabTools);
		setMaxStackSize(1);
		setMaxDamage(0);
		setFull3D();
		physics.registerPacket(PacketPhysicsGunWheel.class, PacketPhysicsGunWheel.class, Side.SERVER);
		physics.registerPacket(PacketPhysicsGunPick.class, PacketPhysicsGunPick.class, Side.SERVER);
	}

	/*
	 * @Override public float getStrVsBlock(ItemStack stack, Block block) { return
	 * 0; }
	 */

	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
		return true;
	}

	@Override
	public float getDigSpeed(ItemStack itemstack, net.minecraft.block.state.IBlockState state) {
		return 0;
	}

	@SideOnly(Side.CLIENT)
	private boolean holdingDown;

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onMouseEvent(MouseEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemPhysicsGun
				&& !mc.thePlayer.isSpectator()) {
			if (event.dwheel != 0) {
				if (holdingDown) {
					Physics.getDispatcher().sendToServer(new PacketPhysicsGunWheel(event.dwheel > 0));
					event.setCanceled(true);
				}
			}

			if (event.button == 0) {
				holdingDown = event.buttonstate;
				Physics.getDispatcher().sendToServer(new PacketPhysicsGunPick(event.buttonstate));
				event.setCanceled(true);
			}
		}
	}

	/**
	 * returns the action that specifies what animation to play when the items is
	 * being used
	 */
	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BOW;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return Integer.MAX_VALUE;
	}

	/**
	 * Sent when used. Handles picking up objects.
	 *
	 */
	public static class PacketPhysicsGunPick extends MinecraftPacket
			implements IMessageHandler<PacketPhysicsGunPick, IMessage> {

		private boolean picking;

		public PacketPhysicsGunPick() {
		}

		public PacketPhysicsGunPick(boolean picking) {
			this.picking = picking;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			this.picking = buf.readBoolean();
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeBoolean(picking);
		}

		@Override
		public IMessage onMessage(final PacketPhysicsGunPick packet, final MessageContext ctx) {
			MinecraftServer.getServer().addScheduledTask(new Runnable() {
				@Override
				public void run() {
					EntityPlayerMP player = ctx.getServerHandler().playerEntity;
					World world = player.worldObj;
					PhysicsWorld physicsWorld = Physics.getInstance().getPhysicsOverworld()
							.getPhysicsByWorld(player.worldObj);
					PickUpMechanic mechanic = (PickUpMechanic) physicsWorld.getMechanics().get("PickUp");
					if (player != null && physicsWorld != null && mechanic != null) {
						if (packet.picking) {
							if (player.getCurrentEquippedItem() != null
									&& player.getCurrentEquippedItem().getItem() instanceof ItemPhysicsGun) {
								Vector3 offset = new Vector3(0.5f, 0.5f, 0.5f);
								Vector3 eyePos = EntityUtility.getPositionEyes(player);
								Vector3 eyeLook = EntityUtility.toVector3(player.getLook(1));
								Vector3 lookAt = new Vector3(eyePos);
								eyeLook.scl(64);
								lookAt.add(eyeLook);
								eyePos.sub(offset);
								lookAt.sub(offset);

								IRayResult rayCallback = physicsWorld.createClosestRayResultCallback(eyePos, lookAt);
								physicsWorld.rayTest(eyePos, lookAt, rayCallback);
								OwnedPickedObject object;
								if (rayCallback.hasHit() && mechanic.getOwnedPickedObject(player) == null) {
									IRigidBody body = physicsWorld.upcastRigidBody(rayCallback.getCollisionObject());
									if (body != null && player.canEntityBeSeen(body.getOwner())) {
										Vector3 localHit = new Vector3(rayCallback.getHitPointWorld());
										Matrix4 bodyTransform = body.getWorldTransform(new Matrix4());
										Vector3 bodyPosition = bodyTransform.getTranslation(new Vector3());

										localHit.sub(new Vector3(bodyPosition.x, bodyPosition.y, bodyPosition.z));
										((EntityPhysicsBase) body.getOwner()).pick(player, localHit);
										mechanic.addOwnedPickedObject(player,
												new OwnedPickedObject(body, player, rayCallback, eyePos, eyeLook));
									}
								}
							}
						} else {
							OwnedPickedObject object = null;
							if ((object = mechanic.getOwnedPickedObject(player)) != null) {
								((EntityPhysicsBase) object.getRigidBody().getOwner()).unpick();
								mechanic.removeOwnedPickedObject(object);
							}

						}
					}
				}
			});
			return null;
		}
	}

	/**
	 * Handles physics gun wheel.
	 *
	 */
	public static class PacketPhysicsGunWheel extends MinecraftPacket
			implements IMessageHandler<PacketPhysicsGunWheel, IMessage> {

		private boolean wheelIncreased;

		public PacketPhysicsGunWheel() {
		}

		public PacketPhysicsGunWheel(boolean picking) {
			this.wheelIncreased = picking;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			this.wheelIncreased = buf.readBoolean();
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeBoolean(wheelIncreased);
		}

		@Override
		public IMessage onMessage(final PacketPhysicsGunWheel packet, final MessageContext ctx) {
			MinecraftServer.getServer().addScheduledTask(new Runnable() {

				@Override
				public void run() {
					EntityPlayerMP player = ctx.getServerHandler().playerEntity;
					World world = player.worldObj;
					PhysicsWorld physicsWorld = Physics.getInstance().getPhysicsOverworld()
							.getPhysicsByWorld(player.worldObj);
					if (player.getCurrentEquippedItem() != null
							&& player.getCurrentEquippedItem().getItem() instanceof ItemPhysicsGun) {
						PickUpMechanic mechanic = (PickUpMechanic) physicsWorld.getMechanics().get("PickUp");
						if (mechanic != null) {
							OwnedPickedObject object = mechanic.getOwnedPickedObject(player);
							if (object != null) {
								object.setPickDistance(MathHelper.clamp_float(
										object.getPickDistance() + (packet.wheelIncreased ? 1 : -1), 1.5f, 64));
							}
						}
					}
				}
			});
			return null;
		}
	}
}