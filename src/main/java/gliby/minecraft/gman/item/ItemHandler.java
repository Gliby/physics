package gliby.minecraft.gman.item;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

/**
 * Core code based off @iChun's swing-less, and bowAnimation locked item code.
 */
public class ItemHandler {

    private static ItemHandler instance;
    private ForcedAnimationHandler forcedAnimationHandler;
    private ItemBreakHandler itemBreakHandler;
    private List<AlwaysUsedItem> forcedAnimationItem;

    private ItemHandler() {
        super();
    }

    public static ItemHandler getInstance() {
        if (instance == null) {
            instance = new ItemHandler();
        }
        return instance;
    }

    public void addAlwaysUsedItem(Item item, boolean shouldSwing, boolean canHitBlocks) {
        if (forcedAnimationHandler == null) {
            forcedAnimationItem = new ArrayList<AlwaysUsedItem>();
            FMLCommonHandler.instance().bus().register(forcedAnimationHandler = new ForcedAnimationHandler());
            MinecraftForge.EVENT_BUS.register(itemBreakHandler = new ItemBreakHandler());
        }

        if (!forcedAnimationItem.contains(item.getClass())) {
            forcedAnimationItem.add(new AlwaysUsedItem(shouldSwing, canHitBlocks, item.getClass()));
        } else try {
            throw new Exception("Excuse me?! Sadly this here program attempted to register multiple 'forced to use' items. Quite a no-no, I'd say.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AlwaysUsedItem getAlwaysUsedItem(Item item) {
        for (int i = 0; i < forcedAnimationItem.size(); i++) {
            AlwaysUsedItem forcedUseItem = forcedAnimationItem.get(i);
            if (forcedUseItem.getItemClass() == item.getClass()) return forcedUseItem;
        }
        return null;
    }

    private class AlwaysUsedItem {

        private boolean shouldSwing;

        private Class<? extends Item> itemClass;

        private boolean canHit;

        /**
         * @param shouldSwing
         * @param itemClass
         */
        public AlwaysUsedItem(boolean shouldSwing, boolean canHit, Class<? extends Item> itemClass) {
            super();
            this.shouldSwing = shouldSwing;
            this.itemClass = itemClass;
            this.canHit = canHit;
        }

        /**
         * @return the shouldSwing
         */
        public boolean isSwingable() {
            return shouldSwing;
        }

        /**
         * @param shouldSwing the shouldSwing to set
         */
        public void setShouldSwing(boolean shouldSwing) {
            this.shouldSwing = shouldSwing;
        }

        /**
         * @return the itemClass
         */
        public Class<? extends Item> getItemClass() {
            return itemClass;
        }

        /**
         * @param itemClass the itemClass to set
         */
        public void setItemClass(Class<? extends Item> itemClass) {
            this.itemClass = itemClass;
        }

        /**
         * @return
         */
        public boolean canHit() {
            return canHit;
        }
    }

    private class ForcedAnimationHandler {

        @SideOnly(Side.CLIENT)
        @SubscribeEvent
        public void onPlayerTick(TickEvent.PlayerTickEvent event) {
            Minecraft mc = Minecraft.getMinecraft();
            if (event.side.isClient()) {
                ItemStack itemStack = event.player.getActiveItemStack();
                boolean isClient = event.player == mc.getRenderViewEntity();
                boolean isFirstPerson = mc.gameSettings.thirdPersonView == 0 && isClient;

                if (itemStack != null) {
                    Item item = itemStack.getItem();
                    AlwaysUsedItem itemInfo = getAlwaysUsedItem(item);
                    if (itemInfo != null) {
                        if (!itemInfo.isSwingable() && isClient) {
                            // TODO 1.12.2 item renderer AT
//                            mc.entityRenderer.itemRenderer.itemToRender = mc.thePlayer.inventory.getCurrentItem();
//                            mc.entityRenderer.itemRenderer.equippedItemSlot = mc.thePlayer.inventory.currentItem;
//                            mc.entityRenderer.itemRenderer.equippedProgress = 1.0f;
//                            mc.entityRenderer.itemRenderer.prevEquippedProgress = 1.0f;
                            mc.player.isSwingInProgress = false;
                            mc.player.swingProgressInt = 0;
                            mc.player.swingProgress = 0;
                        }

                        if (event.phase.equals(TickEvent.Phase.END)) {
                            if (event.player.getItemInUseCount() <= 0 && !isFirstPerson) {
                                event.player.clearItemInUse();
                                event.player.setItemInUse(itemStack, Integer.MAX_VALUE);
                            }
                        }
                    }
                }
            }
        }
    }

    private class ItemBreakHandler {

        @SideOnly(Side.CLIENT)
        @SubscribeEvent
        public void onBlockBreak(BlockEvent.BreakEvent event) {
            if (event.getPlayer().getCurrentEquippedItem() != null) {
                AlwaysUsedItem itemInfo = getAlwaysUsedItem(event.getPlayer().getCurrentEquippedItem().getItem());
                if (itemInfo != null && !itemInfo.canHit()) {
                    event.setCanceled(true);
                }
            }
        }
    }

}
