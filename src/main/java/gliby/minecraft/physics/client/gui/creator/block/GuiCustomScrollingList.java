//package gliby.minecraft.physics.client.gui.creator.block;
//
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.Gui;
//import net.minecraft.client.gui.GuiButton;
//import net.minecraft.client.gui.ScaledResolution;
//import net.minecraft.client.renderer.BufferBuilder;
//import net.minecraft.client.renderer.GlStateManager;
//import net.minecraft.client.renderer.OpenGlHelper;
//import net.minecraft.client.renderer.Tessellator;
//import net.minecraft.client.renderer.WorldRenderer;
//import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
//import org.lwjgl.input.Mouse;
//import org.lwjgl.opengl.GL11;
//
//import java.util.List;
//
//public abstract class GuiCustomScrollingList {
//    protected final int listWidth;
//    protected final int listHeight;
//    protected final int top;
//    protected final int bottom;
//    protected final int right;
//    protected final int left;
//    protected final int slotHeight;
//    private final Minecraft client;
//    protected int mouseX;
//    protected int mouseY;
//    protected int selectedIndex = -1;
//    private int scrollUpActionId;
//    private int scrollDownActionId;
//    private float initialMouseClickY = -2.0F;
//    private float scrollFactor;
//    private float scrollDistance;
//    private long lastClickTime = 0L;
//    private boolean field_25123_p = true;
//    private boolean field_27262_q;
//    private int field_27261_r;
//    private boolean canMove;
//
//    public GuiCustomScrollingList(Minecraft client, int width, int height, int top, int bottom, int left,
//                                  int entryHeight) {
//        this.client = client;
//        this.listWidth = width;
//        this.listHeight = height;
//        this.top = top;
//        this.bottom = bottom;
//        this.slotHeight = entryHeight;
//        this.left = left;
//        this.right = width + this.left;
//    }
//
//    public void func_27258_a(boolean p_27258_1_) {
//        this.field_25123_p = p_27258_1_;
//    }
//
//    protected void func_27259_a(boolean p_27259_1_, int p_27259_2_) {
//        this.field_27262_q = p_27259_1_;
//        this.field_27261_r = p_27259_2_;
//
//        if (!p_27259_1_) {
//            this.field_27261_r = 0;
//        }
//    }
//
//    protected abstract int getSize();
//
//    protected abstract void elementClicked(int index, boolean doubleClick);
//
//    protected abstract boolean isSelected(int index);
//
//    protected int getContentHeight() {
//        return this.getSize() * this.slotHeight + this.field_27261_r;
//    }
//
//    protected abstract void drawBackground();
//
//    protected abstract void drawSlot(int var1, int var2, int var3, int var4, Tessellator var5);
//
//    protected void func_27260_a(int p_27260_1_, int p_27260_2_, Tessellator p_27260_3_) {
//    }
//
//    protected void func_27255_a(int p_27255_1_, int p_27255_2_) {
//    }
//
//    protected void func_27257_b(int p_27257_1_, int p_27257_2_) {
//    }
//
//    public int func_27256_c(int p_27256_1_, int p_27256_2_) {
//        int var3 = this.left + 1;
//        int var4 = this.left + this.listWidth - 7;
//        int var5 = p_27256_2_ - this.top - this.field_27261_r + (int) this.scrollDistance - 4;
//        int var6 = var5 / this.slotHeight;
//        return p_27256_1_ >= var3 && p_27256_1_ <= var4 && var6 >= 0 && var5 >= 0 && var6 < this.getSize() ? var6 : -1;
//    }
//
//    public void registerScrollButtons(@SuppressWarnings("rawtypes") List p_22240_1_, int p_22240_2_, int p_22240_3_) {
//        this.scrollUpActionId = p_22240_2_;
//        this.scrollDownActionId = p_22240_3_;
//    }
//
//    private void applyScrollLimits() {
//        int var1 = this.getContentHeight() - (this.bottom - this.top - 4);
//
//        if (var1 < 0) {
//            var1 /= 2;
//        }
//
//        if (this.scrollDistance < 0.0F) {
//            this.scrollDistance = 0.0F;
//        }
//
//        if (this.scrollDistance > (float) var1) {
//            this.scrollDistance = (float) var1;
//        }
//    }
//
//    public void actionPerformed(GuiButton button) {
//        if (button.enabled) {
//            if (button.id == this.scrollUpActionId) {
//                this.scrollDistance -= (float) (this.slotHeight * 2 / 3);
//                this.initialMouseClickY = -2.0F;
//                this.applyScrollLimits();
//            } else if (button.id == this.scrollDownActionId) {
//                this.scrollDistance += (float) (this.slotHeight * 2 / 3);
//                this.initialMouseClickY = -2.0F;
//                this.applyScrollLimits();
//            }
//        }
//    }
//
//    public void drawScreen(int mouseX, int mouseY, float p_22243_3_) {
//        this.mouseX = mouseX;
//        this.mouseY = mouseY;
//        this.drawBackground();
//
//        boolean isHovering = mouseX >= this.left && mouseX <= this.left + this.listWidth &&
//                mouseY >= this.top && mouseY <= this.bottom;
//        int listLength     = this.getSize();
//        int scrollBarWidth = 6;
//        int scrollBarRight = this.left + this.listWidth;
//        int scrollBarLeft  = scrollBarRight - scrollBarWidth;
//        int entryLeft      = this.left;
//        int entryRight     = scrollBarLeft - 1;
//        int viewHeight     = this.bottom - this.top;
//        int border         = 4;
//
//        if (Mouse.isButtonDown(0))
//        {
//            if (this.initialMouseClickY == -1.0F)
//            {
//                if (isHovering)
//                {
//                    int mouseListY = mouseY - this.top - this.headerHeight + (int)this.scrollDistance - border;
//                    int slotIndex = mouseListY / this.slotHeight;
//
//                    if (mouseX >= entryLeft && mouseX <= entryRight && slotIndex >= 0 && mouseListY >= 0 && slotIndex < listLength)
//                    {
//                        this.elementClicked(slotIndex, slotIndex == this.selectedIndex && System.currentTimeMillis() - this.lastClickTime < 250L);
//                        this.selectedIndex = slotIndex;
//                        this.lastClickTime = System.currentTimeMillis();
//                    }
//                    else if (mouseX >= entryLeft && mouseX <= entryRight && mouseListY < 0)
//                    {
//                        this.clickHeader(mouseX - entryLeft, mouseY - this.top + (int)this.scrollDistance - border);
//                    }
//
//                    if (mouseX >= scrollBarLeft && mouseX <= scrollBarRight)
//                    {
//                        this.scrollFactor = -1.0F;
//                        int scrollHeight = this.getContentHeight() - viewHeight - border;
//                        if (scrollHeight < 1) scrollHeight = 1;
//
//                        int var13 = (int)((float)(viewHeight * viewHeight) / (float)this.getContentHeight());
//
//                        if (var13 < 32) var13 = 32;
//                        if (var13 > viewHeight - border*2)
//                            var13 = viewHeight - border*2;
//
//                        this.scrollFactor /= (float)(viewHeight - var13) / (float)scrollHeight;
//                    }
//                    else
//                    {
//                        this.scrollFactor = 1.0F;
//                    }
//
//                    this.initialMouseClickY = mouseY;
//                }
//                else
//                {
//                    this.initialMouseClickY = -2.0F;
//                }
//            }
//            else if (this.initialMouseClickY >= 0.0F)
//            {
//                this.scrollDistance -= ((float)mouseY - this.initialMouseClickY) * this.scrollFactor;
//                this.initialMouseClickY = (float)mouseY;
//            }
//        }
//        else
//        {
//            this.initialMouseClickY = -1.0F;
//        }
//
//        this.applyScrollLimits();
//
//        Tessellator tess = Tessellator.getInstance();
//        BufferBuilder worldr = tess.getBuffer();
//
//        ScaledResolution res = new ScaledResolution(client);
//        double scaleW = client.displayWidth / res.getScaledWidth_double();
//        double scaleH = client.displayHeight / res.getScaledHeight_double();
//        GL11.glEnable(GL11.GL_SCISSOR_TEST);
//        GL11.glScissor((int)(left      * scaleW), (int)(client.displayHeight - (bottom * scaleH)),
//                (int)(listWidth * scaleW), (int)(viewHeight * scaleH));
//
//        if (this.client.world != null)
//        {
//            this.drawGradientRect(this.left, this.top, this.right, this.bottom, 0xC0101010, 0xD0101010);
//        }
//        else // Draw dark dirt background
//        {
//            GlStateManager.disableLighting();
//            GlStateManager.disableFog();
//            this.client.renderEngine.bindTexture(Gui.OPTIONS_BACKGROUND);
//            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//            final float scale = 32.0F;
//            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
//            worldr.pos(this.left,  this.bottom, 0.0D).tex(this.left  / scale, (this.bottom + (int)this.scrollDistance) / scale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
//            worldr.pos(this.right, this.bottom, 0.0D).tex(this.right / scale, (this.bottom + (int)this.scrollDistance) / scale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
//            worldr.pos(this.right, this.top,    0.0D).tex(this.right / scale, (this.top    + (int)this.scrollDistance) / scale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
//            worldr.pos(this.left,  this.top,    0.0D).tex(this.left  / scale, (this.top    + (int)this.scrollDistance) / scale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
//            tess.draw();
//        }
//
//        int baseY = this.top + border - (int)this.scrollDistance;
//
//        if (this.hasHeader) {
//            this.drawHeader(entryRight, baseY, tess);
//        }
//
//        for (int slotIdx = 0; slotIdx < listLength; ++slotIdx)
//        {
//            int slotTop = baseY + slotIdx * this.slotHeight + this.headerHeight;
//            int slotBuffer = this.slotHeight - border;
//
//            if (slotTop <= this.bottom && slotTop + slotBuffer >= this.top)
//            {
//                if (this.highlightSelected && this.isSelected(slotIdx))
//                {
//                    int min = this.left;
//                    int max = entryRight;
//                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//                    GlStateManager.disableTexture2D();
//                    worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
//                    worldr.pos(min,     slotTop + slotBuffer + 2, 0).tex(0, 1).color(0x80, 0x80, 0x80, 0xFF).endVertex();
//                    worldr.pos(max,     slotTop + slotBuffer + 2, 0).tex(1, 1).color(0x80, 0x80, 0x80, 0xFF).endVertex();
//                    worldr.pos(max,     slotTop              - 2, 0).tex(1, 0).color(0x80, 0x80, 0x80, 0xFF).endVertex();
//                    worldr.pos(min,     slotTop              - 2, 0).tex(0, 0).color(0x80, 0x80, 0x80, 0xFF).endVertex();
//                    worldr.pos(min + 1, slotTop + slotBuffer + 1, 0).tex(0, 1).color(0x00, 0x00, 0x00, 0xFF).endVertex();
//                    worldr.pos(max - 1, slotTop + slotBuffer + 1, 0).tex(1, 1).color(0x00, 0x00, 0x00, 0xFF).endVertex();
//                    worldr.pos(max - 1, slotTop              - 1, 0).tex(1, 0).color(0x00, 0x00, 0x00, 0xFF).endVertex();
//                    worldr.pos(min + 1, slotTop              - 1, 0).tex(0, 0).color(0x00, 0x00, 0x00, 0xFF).endVertex();
//                    tess.draw();
//                    GlStateManager.enableTexture2D();
//                }
//
//                this.drawSlot(slotIdx, entryRight, slotTop, slotBuffer, tess);
//            }
//        }
//
//        GlStateManager.disableDepth();
//
//        int extraHeight = (this.getContentHeight() + border) - viewHeight;
//        if (extraHeight > 0)
//        {
//            int height = (viewHeight * viewHeight) / this.getContentHeight();
//
//            if (height < 32) height = 32;
//
//            if (height > viewHeight - border*2)
//                height = viewHeight - border*2;
//
//            int barTop = (int)this.scrollDistance * (viewHeight - height) / extraHeight + this.top;
//            if (barTop < this.top)
//            {
//                barTop = this.top;
//            }
//
//            GlStateManager.disableTexture2D();
//            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
//            worldr.pos(scrollBarLeft,  this.bottom, 0.0D).tex(0.0D, 1.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
//            worldr.pos(scrollBarRight, this.bottom, 0.0D).tex(1.0D, 1.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
//            worldr.pos(scrollBarRight, this.top,    0.0D).tex(1.0D, 0.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
//            worldr.pos(scrollBarLeft,  this.top,    0.0D).tex(0.0D, 0.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
//            tess.draw();
//            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
//            worldr.pos(scrollBarLeft,  barTop + height, 0.0D).tex(0.0D, 1.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
//            worldr.pos(scrollBarRight, barTop + height, 0.0D).tex(1.0D, 1.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
//            worldr.pos(scrollBarRight, barTop,          0.0D).tex(1.0D, 0.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
//            worldr.pos(scrollBarLeft,  barTop,          0.0D).tex(0.0D, 0.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
//            tess.draw();
//            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
//            worldr.pos(scrollBarLeft,      barTop + height - 1, 0.0D).tex(0.0D, 1.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
//            worldr.pos(scrollBarRight - 1, barTop + height - 1, 0.0D).tex(1.0D, 1.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
//            worldr.pos(scrollBarRight - 1, barTop,              0.0D).tex(1.0D, 0.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
//            worldr.pos(scrollBarLeft,      barTop,              0.0D).tex(0.0D, 0.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
//            tess.draw();
//        }
//
//        this.drawScreen(mouseX, mouseY);
//        GlStateManager.enableTexture2D();
//        GlStateManager.shadeModel(GL11.GL_FLAT);
//        GlStateManager.enableAlpha();
//        GlStateManager.disableBlend();
//        GL11.glDisable(GL11.GL_SCISSOR_TEST);
//    }
//
//    protected void drawGradientRect(int par1, int par2, int par3, int par4, int par5, int par6) {
//        float f = (float) (par5 >> 24 & 255) / 255.0F;
//        float f1 = (float) (par5 >> 16 & 255) / 255.0F;
//        float f2 = (float) (par5 >> 8 & 255) / 255.0F;
//        float f3 = (float) (par5 & 255) / 255.0F;
//        float f4 = (float) (par6 >> 24 & 255) / 255.0F;
//        float f5 = (float) (par6 >> 16 & 255) / 255.0F;
//        float f6 = (float) (par6 >> 8 & 255) / 255.0F;
//        float f7 = (float) (par6 & 255) / 255.0F;
//        GL11.glDisable(GL11.GL_TEXTURE_2D);
//        GL11.glEnable(GL11.GL_BLEND);
//        GL11.glDisable(GL11.GL_ALPHA_TEST);
//        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
//        GL11.glShadeModel(GL11.GL_SMOOTH);
//        Tessellator tessellator = Tessellator.getInstance();
//        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
//        worldrenderer.startDrawingQuads();
//        worldrenderer.setColorRGBA_F(f1, f2, f3, f);
//        worldrenderer.addVertex(par3, par2, 0.0D);
//        worldrenderer.addVertex(par1, par2, 0.0D);
//        worldrenderer.setColorRGBA_F(f5, f6, f7, f4);
//        worldrenderer.addVertex(par1, par4, 0.0D);
//        worldrenderer.addVertex(par3, par4, 0.0D);
//        tessellator.draw();
//        GL11.glShadeModel(GL11.GL_FLAT);
//        GL11.glDisable(GL11.GL_BLEND);
//        GL11.glEnable(GL11.GL_ALPHA_TEST);
//        GL11.glEnable(GL11.GL_TEXTURE_2D);
//    }
//
//    public void setCanMove(boolean canMove) {
//        this.canMove = canMove;
//    }
//}