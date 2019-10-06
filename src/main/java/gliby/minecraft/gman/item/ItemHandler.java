package gliby.minecraft.gman.item;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
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
            MinecraftForge.EVENT_BUS.register(forcedAnimationHandler = new ForcedAnimationHandler());
            MinecraftForge.EVENT_BUS.register(itemBreakHandler = new ItemBreakHandler());
        }

        if (!forcedAnimationItem.contains(item.getClass())) {
            forcedAnimationItem.add(new AlwaysUsedItem(shouldSwing, canHitBlocks, item.getClass()));
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
                ItemStack itemStack = event.player.getHeldItemMainhand();
                boolean isClient = event.player == mc.getRenderViewEntity();
                boolean isFirstPerson = mc.gameSettings.thirdPersonView == 0 && isClient;

                if (itemStack != null) {
                    Item item = itemStack.getItem();
                    AlwaysUsedItem itemInfo = getAlwaysUsedItem(item);
                    if (itemInfo != null) {
                        if (!itemInfo.isSwingable() && isClient) {
                            mc.playerController.resetBlockRemoving();
                            if (mc.player.ticksSinceLastSwing < 2) {
                                mc.entityRenderer.itemRenderer.equippedProgressMainHand = 1F;
                            }
                            mc.player.ticksSinceLastSwing = 10000;
                            mc.player.isSwingInProgress = false;
                            mc.player.swingProgressInt = 0;
                            mc.player.swingProgress = 0;

                            if (!isFirstPerson && event.phase == TickEvent.Phase.END) {
                                mc.player.resetActiveHand();
                                mc.player.setActiveHand(EnumHand.MAIN_HAND);
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
            if (event.getPlayer().getHeldItemMainhand() != null) {
                AlwaysUsedItem itemInfo = getAlwaysUsedItem(event.getPlayer().getHeldItemMainhand().getItem());
                if (itemInfo != null && !itemInfo.canHit()) {
                    event.setCanceled(true);
                }
            }
        }
    }

}
