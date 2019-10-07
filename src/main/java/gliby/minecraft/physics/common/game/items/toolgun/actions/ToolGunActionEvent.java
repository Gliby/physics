package gliby.minecraft.physics.common.game.items.toolgun.actions;

import net.minecraftforge.fml.common.eventhandler.Event;

/**
 *
 */
public class ToolGunActionEvent extends Event {

    public ToolGunActionEvent(Object action) {
    }

    public static class Register extends ToolGunActionEvent {

        Register(Object action, boolean isDefault) {
            super(action);
        }
    }

}
