package gliby.minecraft.physics.common.game.items.toolgun;

import gliby.minecraft.gman.EntityUtility;
import gliby.minecraft.gman.RawItem;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.client.SoundHandler;
import gliby.minecraft.physics.common.entity.EntityToolGunBeam;
import gliby.minecraft.physics.common.game.items.toolgun.actions.IToolGunAction;
import gliby.minecraft.physics.common.packets.MinecraftPacket;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import io.netty.buffer.ByteBuf;
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

import javax.vecmath.Vector3f;

/**
 *
 */
public class ItemToolGun extends RawItem {

    Physics physics;
    private int currentMode = 0, lastMode;

    public ItemToolGun(Physics physics) {
        this.physics = physics;
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

    public int getCurrentMode() {
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
                    event.setCanceled(true);
                } else if (event.button == 1) {
                    if (currentMode < physics.getGameManager().getToolGunRegistry().getValueDefinitions().size() - 1) {
                        currentMode++;
                    } else
                        currentMode = 0;
                    if (lastMode != currentMode) {
                        SoundHandler.playLocalSound(mc, "ToolGun.Scroll");
                        Physics.getDispatcher().sendToServer(new PacketToolGunStoppedUsing(lastMode));
                        event.setCanceled(true);
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
    public String getModeName() {
        if (!physics.getGameManager().getToolGunRegistry().getValueDefinitions().isEmpty())
            return physics.getGameManager().getToolGunRegistry().getValueDefinitions().get(currentMode);
        else
            return "Loading";
    }

    public static class PacketToolGunStoppedUsing extends MinecraftPacket
            implements IMessageHandler<PacketToolGunStoppedUsing, IMessage> {

        private int mode;

        public PacketToolGunStoppedUsing() {
        }

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
                    Physics physics = Physics.getInstance();
                    PhysicsWorld physicsWorld = Physics.getInstance().getPhysicsOverworld().getPhysicsByWorld(world);
                    if (player.getCurrentEquippedItem() != null
                            && player.getCurrentEquippedItem().getItem() instanceof ItemToolGun) {
                        IToolGunAction toolGunAction;
                        if ((toolGunAction = physics.getGameManager().getToolGunRegistry().getActions()
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
         * physicsWorld.getMechanics().get("GravityMagnet"); if (mechanic != null) { if
         * (!mechanic.gravityMagnetEntityExists(player)) {
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
                    Physics physics = Physics.getInstance();
                    PhysicsWorld physicsWorld = physics.getPhysicsOverworld().getPhysicsByWorld(world);
                    if (player.getCurrentEquippedItem() != null
                            && player.getCurrentEquippedItem().getItem() instanceof ItemToolGun) {
                        IToolGunAction toolGunAction;
                        if ((toolGunAction = physics.getGameManager().getToolGunRegistry().getActions()
                                .get(packet.mode)) != null) {
                            if (toolGunAction.use(physicsWorld, player, packet.lookAt)) {
                                world.spawnEntityInWorld(new EntityToolGunBeam(world, player, packet.lookAt));
                            }
                        }
                    }
                }
            });
            return null;
        }
        /*
         * GravityMagnetMechanic mechanic = (GravityMagnetMechanic)
         * physicsWorld.getMechanics().get("GravityMagnet"); if (mechanic != null) { if
         * (!mechanic.gravityMagnetEntityExists(player)) {
         * mechanic.addGravityMagnetEntity(player); } else {
         * mechanic.removeGravityMagnetEntity(player); } }
         */
    }

}