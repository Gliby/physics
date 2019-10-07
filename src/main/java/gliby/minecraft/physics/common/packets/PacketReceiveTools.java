package gliby.minecraft.physics.common.packets;

import gliby.minecraft.physics.Physics;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class PacketReceiveTools extends MinecraftPacket implements IMessageHandler<PacketReceiveTools, IMessage> {

    private List<String> actions;

    public PacketReceiveTools() {
    }

    public PacketReceiveTools(List<String> actions) {
        this.actions = actions;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        actions = new ArrayList<String>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            String s = ByteBufUtils.readUTF8String(buf);
            actions.add(s);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(actions.size());
        for (int i = 0; i < actions.size(); i++) {
            ByteBufUtils.writeUTF8String(buf, actions.get(i));
        }
    }

    @Override
    public IMessage onMessage(final PacketReceiveTools message, MessageContext ctx) {
        FMLCommonHandler.instance().getMinecraftServerInstance()
                .addScheduledTask(new Runnable() {

                    @Override
                    public void run() {
                        Physics physics = Physics.getInstance();
                        physics.getGameManager().getToolGunRegistry().setValueDefinitions(message.actions);
                    }
                });
        return null;
    }
}
