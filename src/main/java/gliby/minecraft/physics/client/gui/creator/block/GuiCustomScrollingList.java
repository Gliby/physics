package gliby.minecraft.physics.client.gui.creator.block;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.List;

public abstract class GuiCustomScrollingList {
    protected final int listWidth;
    protected final int listHeight;
    protected final int top;
    protected final int bottom;
    protected final int right;
    protected final int left;
    protected final int slotHeight;
    private final Minecraft client;
    protected int mouseX;
    protected int mouseY;
    protected int selectedIndex = -1;
    private int scrollUpActionId;
    private int scrollDownActionId;
    private float initialMouseClickY = -2.0F;
    private float scrollFactor;
    private float scrollDistance;
    private long lastClickTime = 0L;
    private boolean highlightSelected = true;
    private boolean hasHeader;
    private int headerHeight;
    private boolean canMove;

    public GuiCustomScrollingList(Minecraft client, int width, int height, int top, int bottom, int left,
                                  int entryHeight) {
        this.client = client;
        this.listWidth = width;
        this.listHeight = height;
        this.top = top;
        this.bottom = bottom;
        this.slotHeight = entryHeight;
        this.left = left;
        this.right = width + this.left;
    }

    @Deprecated // Unused, remove in 1.9.3?
    public void setHighlightSelected(boolean highlightSelected) {
        this.highlightSelected = highlightSelected;
    }

    protected void setHeaderInfo(boolean hasHeader, int headerHeight) {
        this.hasHeader = hasHeader;
        this.headerHeight = headerHeight;

        if (!hasHeader) {
            this.headerHeight = 0;
        }
    }

    protected abstract int getSize();

    protected abstract void elementClicked(int index, boolean doubleClick);

    protected abstract boolean isSelected(int index);

    protected int getContentHeight() {
        return this.getSize() * this.slotHeight + this.headerHeight;
    }

    protected abstract void drawBackground();

    protected abstract void drawSlot(int var1, int var2, int var3, int var4, Tessellator var5);

    protected void func_27260_a(int p_27260_1_, int p_27260_2_, Tessellator p_27260_3_) {
    }

    protected void func_27255_a(int p_27255_1_, int p_27255_2_) {
    }

    protected void func_27257_b(int p_27257_1_, int p_27257_2_) {
    }

    public int func_27256_c(int p_27256_1_, int p_27256_2_) {
        int var3 = this.left + 1;
        int var4 = this.left + this.listWidth - 7;
        int var5 = p_27256_2_ - this.top - this.headerHeight + (int) this.scrollDistance - 4;
        int var6 = var5 / this.slotHeight;
        return p_27256_1_ >= var3 && p_27256_1_ <= var4 && var6 >= 0 && var5 >= 0 && var6 < this.getSize() ? var6 : -1;
    }

    public void registerScrollButtons(@SuppressWarnings("rawtypes") List p_22240_1_, int p_22240_2_, int p_22240_3_) {
        this.scrollUpActionId = p_22240_2_;
        this.scrollDownActionId = p_22240_3_;
    }

    private void applyScrollLimits() {
        int var1 = this.getContentHeight() - (this.bottom - this.top - 4);

        if (var1 < 0) {
            var1 /= 2;
        }

        if (this.scrollDistance < 0.0F) {
            this.scrollDistance = 0.0F;
        }

        if (this.scrollDistance > (float) var1) {
            this.scrollDistance = (float) var1;
        }
    }

    public void actionPerformed(GuiButton button) {
        if (button.enabled) {
            if (button.id == this.scrollUpActionId) {
                this.scrollDistance -= (float) (this.slotHeight * 2 / 3);
                this.initialMouseClickY = -2.0F;
                this.applyScrollLimits();
            } else if (button.id == this.scrollDownActionId) {
                this.scrollDistance += (float) (this.slotHeight * 2 / 3);
                this.initialMouseClickY = -2.0F;
                this.applyScrollLimits();
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.drawBackground();
        int listLength = this.getSize();
        int scrollBarLeft = this.left + this.listWidth - 6;
        int scrollBarRight = scrollBarLeft + 6;
        int boxLeft = this.left;
        int entryRight = scrollBarLeft - 1;
        int baseY;
        int slotId;
        int slotBuffer;
        int slotTop;
        int viewHeight = this.bottom - this.top;


        if (Mouse.isButtonDown(0)) {
            if (this.initialMouseClickY == -1.0F) {
                boolean var7 = true;

                if (mouseY >= this.top && mouseY <= this.bottom) {
                    baseY = mouseY - this.top - this.headerHeight + (int) this.scrollDistance - 4;
                    slotId = baseY / this.slotHeight;

                    if (mouseX >= boxLeft && mouseX <= entryRight && slotId >= 0 && baseY >= 0 && slotId < listLength) {
                        boolean var12 = slotId == this.selectedIndex
                                && System.currentTimeMillis() - this.lastClickTime < 250L;
                        this.elementClicked(slotId, var12);
                        this.selectedIndex = slotId;
                        this.lastClickTime = System.currentTimeMillis();
                    } else if (mouseX >= boxLeft && mouseX <= entryRight && baseY < 0) {
                        this.func_27255_a(mouseX - boxLeft, mouseY - this.top + (int) this.scrollDistance - 4);
                        var7 = false;
                    }

                    if (mouseX >= scrollBarLeft && mouseX <= scrollBarRight) {
                        this.scrollFactor = -1.0F;
                        slotTop = this.getContentHeight() - (this.bottom - this.top - 4);

                        if (slotTop < 1) {
                            slotTop = 1;
                        }

                        slotBuffer = (int) ((float) ((this.bottom - this.top) * (this.bottom - this.top))
                                / (float) this.getContentHeight());

                        if (slotBuffer < 32) {
                            slotBuffer = 32;
                        }

                        if (slotBuffer > this.bottom - this.top - 8) {
                            slotBuffer = this.bottom - this.top - 8;
                        }

                        this.scrollFactor /= (float) (this.bottom - this.top - slotBuffer) / (float) slotTop;
                    } else {
                        this.scrollFactor = 1.0F;
                    }

                    if (var7) {
                        this.initialMouseClickY = (float) mouseY;
                    } else {
                        this.initialMouseClickY = -2.0F;
                    }
                } else {
                    this.initialMouseClickY = -2.0F;
                }
            } else if (this.initialMouseClickY >= 0.0F) {
                this.scrollDistance -= ((float) mouseY - this.initialMouseClickY) * this.scrollFactor;
                this.initialMouseClickY = (float) mouseY;
            }
        } else {
            while (Mouse.next() && canMove) {
                int var16 = Mouse.getEventDWheel();

                if (var16 != 0) {
                    if (var16 > 0) {
                        var16 = -1;
                    } else if (var16 < 0) {
                        var16 = 1;
                    }

                    this.scrollDistance += (float) (var16 * this.slotHeight / 2);
                }
            }

            this.initialMouseClickY = -1.0F;
        }

        this.applyScrollLimits();

        Tessellator tess = Tessellator.getInstance();
        ScaledResolution res = new ScaledResolution(client);
        double scaleW = client.displayWidth / res.getScaledWidth_double();
        double scaleH = client.displayHeight / res.getScaledHeight_double();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int)(left      * scaleW), (int)(client.displayHeight - (bottom * scaleH)),
                (int)(listWidth * scaleW), (int)(viewHeight * scaleH));

        BufferBuilder buffer = tess.getBuffer();
        if (this.client.world != null) {
            this.drawGradientRect(this.left, this.top, this.right, this.bottom, 0xC0101010, 0xD0101010);
        } else // Draw dark dirt background
        {
            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            this.client.renderEngine.bindTexture(Gui.OPTIONS_BACKGROUND);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            final float scale = 32.0F;
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(this.left, this.bottom, 0.0D).tex(this.left / scale, (this.bottom + (int) this.scrollDistance) / scale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
            buffer.pos(this.right, this.bottom, 0.0D).tex(this.right / scale, (this.bottom + (int) this.scrollDistance) / scale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
            buffer.pos(this.right, this.top, 0.0D).tex(this.right / scale, (this.top + (int) this.scrollDistance) / scale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
            buffer.pos(this.left, this.top, 0.0D).tex(this.left / scale, (this.top + (int) this.scrollDistance) / scale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
            tess.draw();
        }

        // boxRight = this.listWidth / 2 - 92 - 16;
        baseY = this.top + 4 - (int) this.scrollDistance;

        if (this.hasHeader) {
            this.func_27260_a(entryRight, baseY, tess);
        }


        for (slotId = 0; slotId < listLength; ++slotId) {
            slotTop = baseY + slotId * this.slotHeight + this.headerHeight;
            slotBuffer = this.slotHeight - 4;

            if (slotTop <= this.bottom && slotTop + slotBuffer >= this.top) {
                if (this.highlightSelected && this.isSelected(slotId)) {
                    int min = this.left;
                    int max = entryRight;
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.disableTexture2D();
                    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
                    buffer.pos(min, slotTop + slotBuffer + 2, 0).tex(0, 1).color(0x80, 0x80, 0x80, 0xFF).endVertex();
                    buffer.pos(max, slotTop + slotBuffer + 2, 0).tex(1, 1).color(0x80, 0x80, 0x80, 0xFF).endVertex();
                    buffer.pos(max, slotTop - 2, 0).tex(1, 0).color(0x80, 0x80, 0x80, 0xFF).endVertex();
                    buffer.pos(min, slotTop - 2, 0).tex(0, 0).color(0x80, 0x80, 0x80, 0xFF).endVertex();
                    buffer.pos(min + 1, slotTop + slotBuffer + 1, 0).tex(0, 1).color(0x00, 0x00, 0x00, 0xFF).endVertex();
                    buffer.pos(max - 1, slotTop + slotBuffer + 1, 0).tex(1, 1).color(0x00, 0x00, 0x00, 0xFF).endVertex();
                    buffer.pos(max - 1, slotTop - 1, 0).tex(1, 0).color(0x00, 0x00, 0x00, 0xFF).endVertex();
                    buffer.pos(min + 1, slotTop - 1, 0).tex(0, 0).color(0x00, 0x00, 0x00, 0xFF).endVertex();
                    tess.draw();
                    GlStateManager.enableTexture2D();
                }

                this.drawSlot(slotId, entryRight, slotTop, slotBuffer, tess);
            }
        }

        GlStateManager.disableDepth();

        int border = 4;

        int extraHeight = (this.getContentHeight() + border) - viewHeight;
        if (extraHeight > 0) {
            int height = (viewHeight * viewHeight) / this.getContentHeight();

            if (height < 32) height = 32;

            if (height > viewHeight - border * 2)
                height = viewHeight - border * 2;

            int barTop = (int) this.scrollDistance * (viewHeight - height) / extraHeight + this.top;
            if (barTop < this.top) {
                barTop = this.top;
            }

            GlStateManager.disableTexture2D();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(scrollBarLeft, this.bottom, 0.0D).tex(0.0D, 1.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            buffer.pos(scrollBarRight, this.bottom, 0.0D).tex(1.0D, 1.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            buffer.pos(scrollBarRight, this.top, 0.0D).tex(1.0D, 0.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            buffer.pos(scrollBarLeft, this.top, 0.0D).tex(0.0D, 0.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            tess.draw();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(scrollBarLeft, barTop + height, 0.0D).tex(0.0D, 1.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            buffer.pos(scrollBarRight, barTop + height, 0.0D).tex(1.0D, 1.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            buffer.pos(scrollBarRight, barTop, 0.0D).tex(1.0D, 0.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            buffer.pos(scrollBarLeft, barTop, 0.0D).tex(0.0D, 0.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            tess.draw();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(scrollBarLeft, barTop + height - 1, 0.0D).tex(0.0D, 1.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
            buffer.pos(scrollBarRight - 1, barTop + height - 1, 0.0D).tex(1.0D, 1.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
            buffer.pos(scrollBarRight - 1, barTop, 0.0D).tex(1.0D, 0.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
            buffer.pos(scrollBarLeft, barTop, 0.0D).tex(0.0D, 0.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
            tess.draw();
        }

//        this.drawScreen(mouseX, mouseY);
        GlStateManager.enableTexture2D();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    protected void drawGradientRect(int left, int top, int right, int bottom, int color1, int color2) {
        GuiUtils.drawGradientRect(0, left, top, right, bottom, color1, color2);
    }

//    private void overlayBackground(int p_22239_1_, int p_22239_2_, int p_22239_3_, int p_22239_4_) {
//        Tessellator var5 = Tessellator.getInstance();
//        WorldRenderer worldr = var5.getWorldRenderer();
//        this.client.renderEngine.bindTexture(Gui.optionsBackground);
//        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
//        float var6 = 32.0F;
//        worldr.startDrawingQuads();
//        worldr.setColorRGBA_I(4210752, p_22239_4_);
//        worldr.addVertexWithUV(0.0D, p_22239_2_, 0.0D, 0.0D, (float) p_22239_2_ / var6);
//        worldr.addVertexWithUV((double) this.listWidth + 30, p_22239_2_, 0.0D,
//                (float) (this.listWidth + 30) / var6, (float) p_22239_2_ / var6);
//        worldr.setColorRGBA_I(4210752, p_22239_3_);
//        worldr.addVertexWithUV((double) this.listWidth + 30, p_22239_1_, 0.0D,
//                (float) (this.listWidth + 30) / var6, (float) p_22239_1_ / var6);
//        worldr.addVertexWithUV(0.0D, p_22239_1_, 0.0D, 0.0D, (float) p_22239_1_ / var6);
//        var5.draw();
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


    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
    }
}