package gliby.minecraft.physics.client.keybindings;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Type;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class KeyManager {

    @SideOnly(Side.CLIENT)
    private final List<KeyEvent> keyEvents = new ArrayList<KeyEvent>();

    private boolean[] keyDown;

    @SideOnly(Side.CLIENT)
    public List<KeyEvent> getKeyEvents() {
        return keyEvents;
    }

    public String getKeyName(EnumBinding binding) {
        for (int i = 0; i < keyEvents.size(); i++) {
            final KeyEvent event = keyEvents.get(i);
            if (event.keyBind == binding) {
                return Keyboard.getKeyName(event.keyID);
            }
        }
        return null;
    }

    public void init() {
        for (EnumBinding bind : EnumBinding.values()) {
            Object object = null;
            try {
                object = bind.clazz.getConstructor().newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            keyEvents.add((KeyEvent) object);
        }
        registerKeyBindings();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void keyEvent(net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent event) {
        for (int i = 0; i < keyEvents.size(); i++) {
            final KeyEvent keyEvent = keyEvents.get(i);
            final KeyBinding keyBinding = keyEvents.get(i).forgeKeyBinding;
            final int keyCode = keyBinding.getKeyCode();
            final boolean state = (keyCode < 0 ? Mouse.isButtonDown(keyCode + 100) : Keyboard.isKeyDown(keyCode));
            final boolean tickEnd = true;
            if (state != keyDown[i] || (state && keyEvent.repeating)) {
                if (state) {
                    keyEvent.keyDown(keyBinding, tickEnd, state != keyDown[i]);
                } else {
                    keyEvent.keyUp(keyBinding, tickEnd);
                }
                if (tickEnd) {
                    keyDown[i] = state;
                }
            }
        }
    }

    private KeyBinding[] registerKeyBindings() {
        final KeyBinding[] keyBinding = new KeyBinding[keyEvents.size()];
        for (int i = 0; i < keyBinding.length; i++) {
            final KeyEvent keyEvent = this.keyEvents.get(i);
            keyBinding[i] = new KeyBinding(keyEvent.keyBind.name, keyEvent.keyID, "key.categories.multiplayer");
            this.keyDown = new boolean[keyBinding.length];
            keyEvent.forgeKeyBinding = keyBinding[i];
            ClientRegistry.registerKeyBinding(keyBinding[i]);
        }
        return keyBinding;
    }

    @SubscribeEvent
    public void tick(TickEvent event) {
        if (event.side == Side.CLIENT) {
            if (event.type == Type.CLIENT) {
//				if(event.phase == Phase.END) {
                this.keyEvent(null);
//				}
            }
        }
    }
}
