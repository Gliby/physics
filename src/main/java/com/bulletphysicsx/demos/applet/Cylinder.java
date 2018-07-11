/* 
 * Copyright (c) 2002-2004 LWJGL Project
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are 
 * met:
 * 
 * * Redistributions of source code must retain the above copyright 
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'LWJGL' nor the names of 
 *   its contributors may be used to endorse or promote products derived 
 *   from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.bulletphysicsx.demos.applet;

/**
 * Cylinder.java
 * <p/>
 * <p/>
 * Created 23-dec-2003
 *
 * @author Erik Duijs
 */
public class Cylinder extends Quadric {

    /**
     * Constructor for Cylinder.
     */
    public Cylinder() {
        super();
    }

    /**
     * draws a cylinder oriented along the z axis. The base of the
     * cylinder is placed at z = 0, and the top at z=height. Like a sphere, a
     * cylinder is subdivided around the z axis into slices, and along the z axis
     * into stacks.
     * <p/>
     * Note that if topRadius is set to zero, then this routine will generate a
     * cone.
     * <p/>
     * If the orientation is set to GLU.OUTSIDE (with glu.quadricOrientation), then
     * any generated normals point away from the z axis. Otherwise, they point
     * toward the z axis.
     * <p/>
     * If texturing is turned on (with glu.quadricTexture), then texture
     * coordinates are generated so that t ranges linearly from 0.0 at z = 0 to
     * 1.0 at z = height, and s ranges from 0.0 at the +y axis, to 0.25 at the +x
     * axis, to 0.5 at the -y axis, to 0.75 at the -x axis, and back to 1.0 at the
     * +y axis.
     *
     * @param baseRadius Specifies the radius of the cylinder at z = 0.
     * @param topRadius  Specifies the radius of the cylinder at z = height.
     * @param height     Specifies the height of the cylinder.
     * @param slices     Specifies the number of subdivisions around the z axis.
     * @param stacks     Specifies the number of subdivisions along the z axis.
     */
    public void draw(Graphics3D gl, float baseRadius, float topRadius, float height, int slices, int stacks) {

        float da, r, dr, dz;
        float x, y, z, nz, nsign;
        int i, j;

        if (super.orientation == GLU_INSIDE) {
            nsign = -1.0f;
        } else {
            nsign = 1.0f;
        }

        da = 2.0f * (float) Math.PI / slices;
        dr = (topRadius - baseRadius) / stacks;
        dz = height / stacks;
        nz = (baseRadius - topRadius) / height;
        // Z component of normal vectors

        if (super.drawStyle == GLU_POINT) {
            /*
            GL11.glBegin(GL11.GL_POINTS);
			for (i = 0; i < slices; i++) {
				x = cos((i * da));
				y = sin((i * da));
				normal3f(x * nsign, y * nsign, nz * nsign);

				z = 0.0f;
				r = baseRadius;
				for (j = 0; j <= stacks; j++) {
					GL11.glVertex3f((x * r), (y * r), z);
					z += dz;
					r += dr;
				}
			}
			GL11.glEnd();
			*/
        } else if (super.drawStyle == GLU_LINE || super.drawStyle == GLU_SILHOUETTE) {
			/*
			// Draw rings
			if (super.drawStyle == GLU_LINE) {
				z = 0.0f;
				r = baseRadius;
				for (j = 0; j <= stacks; j++) {
					GL11.glBegin(GL11.GL_LINE_LOOP);
					for (i = 0; i < slices; i++) {
						x = cos((i * da));
						y = sin((i * da));
						normal3f(x * nsign, y * nsign, nz * nsign);
						GL11.glVertex3f((x * r), (y * r), z);
					}
					GL11.glEnd();
					z += dz;
					r += dr;
				}
			} else {
				// draw one ring at each end
				if (baseRadius != 0.0) {
					GL11.glBegin(GL11.GL_LINE_LOOP);
					for (i = 0; i < slices; i++) {
						x = cos((i * da));
						y = sin((i * da));
						normal3f(x * nsign, y * nsign, nz * nsign);
						GL11.glVertex3f((x * baseRadius), (y * baseRadius), 0.0f);
					}
					GL11.glEnd();
					GL11.glBegin(GL11.GL_LINE_LOOP);
					for (i = 0; i < slices; i++) {
						x = cos((i * da));
						y = sin((i * da));
						normal3f(x * nsign, y * nsign, nz * nsign);
						GL11.glVertex3f((x * topRadius), (y * topRadius), height);
					}
					GL11.glEnd();
				}
			}
			// draw length lines
			GL11.glBegin(GL11.GL_LINES);
			for (i = 0; i < slices; i++) {
				x = cos((i * da));
				y = sin((i * da));
				normal3f(x * nsign, y * nsign, nz * nsign);
				GL11.glVertex3f((x * baseRadius), (y * baseRadius), 0.0f);
				GL11.glVertex3f((x * topRadius), (y * topRadius), (height));
			}
			GL11.glEnd();
			*/
        } else if (super.drawStyle == GLU_FILL) {
            float ds = 1.0f / slices;
            float dt = 1.0f / stacks;
            float t = 0.0f;
            z = 0.0f;
            r = baseRadius;
            for (j = 0; j < stacks; j++) {
                float s = 0.0f;
                gl.begin(Graphics3D.QUAD_STRIP);
                for (i = 0; i <= slices; i++) {
                    if (i == slices) {
                        x = sin(0.0f);
                        y = cos(0.0f);
                    } else {
                        x = sin((i * da));
                        y = cos((i * da));
                    }
                    if (nsign == 1.0f) {
                        normal3f(gl, (x * nsign), (y * nsign), (nz * nsign));
                        TXTR_COORD(gl, s, t);
                        gl.addVertex((x * r), (y * r), z);
                        normal3f(gl, (x * nsign), (y * nsign), (nz * nsign));
                        TXTR_COORD(gl, s, t + dt);
                        gl.addVertex((x * (r + dr)), (y * (r + dr)), (z + dz));
                    } else {
                        normal3f(gl, x * nsign, y * nsign, nz * nsign);
                        TXTR_COORD(gl, s, t);
                        gl.addVertex((x * r), (y * r), z);
                        normal3f(gl, x * nsign, y * nsign, nz * nsign);
                        TXTR_COORD(gl, s, t + dt);
                        gl.addVertex((x * (r + dr)), (y * (r + dr)), (z + dz));
                    }
                    s += ds;
                } // for slices
                gl.end();
                r += dr;
                t += dt;
                z += dz;
            } // for stacks
        }
    }
}
