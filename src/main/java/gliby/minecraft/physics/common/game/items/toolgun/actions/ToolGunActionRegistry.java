package gliby.minecraft.physics.common.game.items.toolgun.actions;

import com.google.common.collect.ImmutableMap;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class ToolGunActionRegistry {

    private static ToolGunActionRegistry instance;
    private static int actionIndex;
    private Map<Integer, IToolGunAction> actions;
    private List<String> valueDefinitions;

    public ToolGunActionRegistry() {
        actions = new HashMap<Integer, IToolGunAction>();
        valueDefinitions = new ArrayList<String>();
    }

    public ImmutableMap<Integer, IToolGunAction> getActions() {
        return ImmutableMap.copyOf(actions);
    }

    public void registerAction(IToolGunAction action, String modID) {
        if (!MinecraftForge.EVENT_BUS
                .post(new ToolGunActionEvent.Register(action, modID != null && modID.equals(Physics.ID)))) {
            actions.put(actionIndex++, action);
            valueDefinitions.add(action.getName());
        }
    }

    public void stopUsingAll(PhysicsWorld physicsWorld, EntityPlayerMP player) {
        for (IToolGunAction action : getActions().values()) {
            action.stoppedUsing(physicsWorld, player);
        }
    }

    /**
     * @return
     */
    public List<String> getValueDefinitions() {
        return valueDefinitions;
    }

    public void setValueDefinitions(List<String> list) {
        this.valueDefinitions = list;
    }

}
