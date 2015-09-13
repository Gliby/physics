/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.packets;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import net.gliby.physics.common.game.items.toolgun.actions.ToolGunActionRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 *
 */
public class PacketPlayerJoin extends MinecraftPacket implements IMessageHandler<PacketPlayerJoin, IMessage> {

	private List<String> actions;

	public PacketPlayerJoin() {
	}

	public PacketPlayerJoin(List<String> actions) {
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
	public IMessage onMessage(final PacketPlayerJoin message, MessageContext ctx) {
		Minecraft.getMinecraft().addScheduledTask(new Runnable() {
			
			@Override
			public void run() {
				ToolGunActionRegistry.getInstance().setValueDefinitions(message.actions);
			}
		});
		return null;
	}
}
