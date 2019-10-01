package gliby.minecraft.gman;

import org.apache.commons.math3.util.FastMath;

/**
 * <p>The Easing class holds a set of general-purpose motion
 * tweening functions by Robert Penner. This class is
 * essentially a port from Penner's ActionScript utility,
 * with a few added tweaks.</p>
 * <p>Examples:<pre>
 *    //no tween
 *    Easing e1 = Easing.LINEAR;
 *
 *    //backOut tween, the overshoot is Easing.Back.DEFAULT_OVERSHOOT
 *    Easing e2 = Easing.BACK_OUT;
 *
 *    //backOut tween, the overshoot is 1.85f
 *    Easing.Back e3 = new Easing.BackOut(1.85f);
 * </pre></p>
 * <a href="http://www.robertpenner.com/easing/">Robert Penner's Easing Functions</a>
 *
 * @author Robert Penner (functions)
 * @author davedes (java port)
 * https://github.com/mattdesl/cisc226game/blob/master/SpaceGame/src/space/engine/easing/Easing.java
 * <p>
 * Terms of Use: Easing Functions (Equations)
 * <p>
 * Open source under the MIT License and the 3-Clause BSD License.
 * MIT License
 * <p>
 * Copyright © 2001 Robert Penner
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * BSD License
 * <p>
 * Copyright © 2001 Robert Penner
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * <p>
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the author nor the names of contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public interface Easing {

    /**
     * Simple linear tweening - no easing.
     */
    Easing LINEAR = new Easing() {
        public float ease(float t, float b, float c, float d) {
            return c * t / d + b;
        }
    };
    /**
     * Quadratic easing in - accelerating from zero velocity.
     */
    Easing QUAD_IN = new Easing() {
        public float ease(float t, float b, float c, float d) {
            return c * (t /= d) * t + b;
        }
    };

    ///////////// QUADRATIC EASING: t^2 ///////////////////
    /**
     * Quadratic easing out - decelerating to zero velocity.
     */
    Easing QUAD_OUT = new Easing() {
        public float ease(float t, float b, float c, float d) {
            return -c * (t /= d) * (t - 2) + b;
        }
    };
    /**
     * Quadratic easing in/out - acceleration until halfway, then deceleration
     */
    Easing QUAD_IN_OUT = new Easing() {
        public float ease(float t, float b, float c, float d) {
            if ((t /= d / 2) < 1) return c / 2 * t * t + b;
            return -c / 2 * ((--t) * (t - 2) - 1) + b;
        }
    };
    /**
     * Cubic easing in - accelerating from zero velocity.
     */
    Easing CUBIC_IN = new Easing() {
        public float ease(float t, float b, float c, float d) {
            return c * (t /= d) * t * t + b;
        }
    };


    ///////////// CUBIC EASING: t^3 ///////////////////////
    /**
     * Cubic easing out - decelerating to zero velocity.
     */
    Easing CUBIC_OUT = new Easing() {
        public float ease(float t, float b, float c, float d) {
            return c * ((t = t / d - 1) * t * t + 1) + b;
        }
    };
    /**
     * Cubic easing in/out - acceleration until halfway, then deceleration.
     */
    Easing CUBIC_IN_OUT = new Easing() {
        public float ease(float t, float b, float c, float d) {
            if ((t /= d / 2) < 1) return c / 2 * t * t * t + b;
            return c / 2 * ((t -= 2) * t * t + 2) + b;
        }
    };
    /**
     * Quartic easing in - accelerating from zero velocity.
     */
    Easing QUARTIC_IN = new Easing() {
        public float ease(float t, float b, float c, float d) {
            return c * (t /= d) * t * t * t + b;
        }
    };

    ///////////// QUARTIC EASING: t^4 /////////////////////
    /**
     * Quartic easing out - decelerating to zero velocity.
     */
    Easing QUARTIC_OUT = new Easing() {
        public float ease(float t, float b, float c, float d) {
            return -c * ((t = t / d - 1) * t * t * t - 1) + b;
        }
    };
    /**
     * Quartic easing in/out - acceleration until halfway, then deceleration.
     */
    Easing QUARTIC_IN_OUT = new Easing() {
        public float ease(float t, float b, float c, float d) {
            if ((t /= d / 2) < 1) return c / 2 * t * t * t * t + b;
            return -c / 2 * ((t -= 2) * t * t * t - 2) + b;
        }
    };
    /**
     * Quintic easing in - accelerating from zero velocity.
     */
    Easing QUINTIC_IN = new Easing() {
        public float ease(float t, float b, float c, float d) {
            return c * (t /= d) * t * t * t * t + b;
        }
    };

    ///////////// QUINTIC EASING: t^5  ////////////////////
    /**
     * Quintic easing out - decelerating to zero velocity.
     */
    Easing QUINTIC_OUT = new Easing() {
        public float ease(float t, float b, float c, float d) {
            return c * ((t = t / d - 1) * t * t * t * t + 1) + b;
        }
    };
    /**
     * Quintic easing in/out - acceleration until halfway, then deceleration.
     */
    Easing QUINTIC_IN_OUT = new Easing() {
        public float ease(float t, float b, float c, float d) {
            if ((t /= d / 2) < 1) return c / 2 * t * t * t * t * t + b;
            return c / 2 * ((t -= 2) * t * t * t * t + 2) + b;
        }
    };
    /**
     * Sinusoidal easing in - accelerating from zero velocity.
     */
    Easing SINE_IN = new Easing() {
        public float ease(float t, float b, float c, float d) {
            return -c * (float) FastMath.cos(t / d * (FastMath.PI / 2)) + c + b;
        }
    };


    ///////////// SINUSOIDAL EASING: sin(t) ///////////////
    /**
     * Sinusoidal easing out - decelerating to zero velocity.
     */
    Easing SINE_OUT = new Easing() {
        public float ease(float t, float b, float c, float d) {
            return c * (float) FastMath.sin(t / d * (FastMath.PI / 2)) + b;
        }
    };
    /**
     * Sinusoidal easing in/out - accelerating until halfway, then decelerating.
     */
    Easing SINE_IN_OUT = new Easing() {
        public float ease(float t, float b, float c, float d) {
            return -c / 2 * ((float) FastMath.cos(FastMath.PI * t / d) - 1) + b;
        }
    };
    /**
     * Exponential easing in - accelerating from zero velocity.
     */
    Easing EXPO_IN = new Easing() {
        public float ease(float t, float b, float c, float d) {
            return (t == 0) ? b : c * (float) FastMath.pow(2, 10 * (t / d - 1)) + b;
        }
    };

    ///////////// EXPONENTIAL EASING: 2^t /////////////////
    /**
     * Exponential easing out - decelerating to zero velocity.
     */
    Easing EXPO_OUT = new Easing() {
        public float ease(float t, float b, float c, float d) {
            return (t == d) ? b + c : c * (-(float) FastMath.pow(2, -10 * t / d) + 1) + b;
        }
    };
    /**
     * Exponential easing in/out - accelerating until halfway, then decelerating.
     */
    Easing EXPO_IN_OUT = new Easing() {
        public float ease(float t, float b, float c, float d) {
            if (t == 0) return b;
            if (t == d) return b + c;
            if ((t /= d / 2) < 1) return c / 2 * (float) FastMath.pow(2, 10 * (t - 1)) + b;
            return c / 2 * (-(float) FastMath.pow(2, -10 * --t) + 2) + b;
        }
    };
    /**
     * Circular easing in - accelerating from zero velocity.
     */
    Easing CIRC_IN = new Easing() {
        public float ease(float t, float b, float c, float d) {
            return -c * ((float) FastMath.sqrt(1 - (t /= d) * t) - 1) + b;
        }
    };


    /////////// CIRCULAR EASING: sqrt(1-t^2) //////////////
    /**
     * Circular easing out - decelerating to zero velocity.
     */
    Easing CIRC_OUT = new Easing() {
        public float ease(float t, float b, float c, float d) {
            return c * (float) FastMath.sqrt(1 - (t = t / d - 1) * t) + b;
        }
    };
    /**
     * Circular easing in/out - acceleration until halfway, then deceleration.
     */
    Easing CIRC_IN_OUT = new Easing() {
        public float ease(float t, float b, float c, float d) {
            if ((t /= d / 2) < 1) return -c / 2 * ((float) FastMath.sqrt(1 - t * t) - 1) + b;
            return c / 2 * ((float) FastMath.sqrt(1 - (t -= 2) * t) + 1) + b;
        }
    };
    /**
     * An EasingIn instance using the default values.
     */
    Easing.Elastic ELASTIC_IN = new Easing.ElasticIn();

    /////////// ELASTIC EASING: exponentially decaying sine wave  //////////////
    /**
     * An ElasticOut instance using the default values.
     */
    Easing.Elastic ELASTIC_OUT = new Easing.ElasticOut();
    /**
     * An ElasticInOut instance using the default values.
     */
    Easing.Elastic ELASTIC_IN_OUT = new Easing.ElasticInOut();
    /**
     * An instance of BackIn using the default overshoot.
     */
    Easing.Back BACK_IN = new Easing.BackIn();
    /**
     * An instance of BackOut using the default overshoot.
     */
    Easing.Back BACK_OUT = new Easing.BackOut();
    /**
     * An instance of BackInOut using the default overshoot.
     */
    Easing.Back BACK_IN_OUT = new Easing.BackInOut();
    /**
     * Bounce easing out.
     */
    Easing BOUNCE_OUT = new Easing() {
        public float ease(float t, float b, float c, float d) {
            if ((t /= d) < (1 / 2.75f)) {
                return c * (7.5625f * t * t) + b;
            } else if (t < (2 / 2.75f)) {
                return c * (7.5625f * (t -= (1.5f / 2.75f)) * t + .75f) + b;
            } else if (t < (2.5f / 2.75f)) {
                return c * (7.5625f * (t -= (2.25f / 2.75f)) * t + .9375f) + b;
            } else {
                return c * (7.5625f * (t -= (2.625f / 2.75f)) * t + .984375f) + b;
            }
        }
    };
    /**
     * Bounce easing in.
     */
    Easing BOUNCE_IN = new Easing() {
        public float ease(float t, float b, float c, float d) {
            return c - Easing.BOUNCE_OUT.ease(d - t, 0, c, d) + b;
        }
    };

    /////////// BACK EASING: overshooting cubic easing: (s+1)*t^3 - s*t^2  //////////////
    /**
     * Bounce easing in/out.
     */
    Easing BOUNCE_IN_OUT = new Easing() {
        public float ease(float t, float b, float c, float d) {
            if (t < d / 2) return Easing.BOUNCE_IN.ease(t * 2, 0, c, d) * .5f + b;
            return Easing.BOUNCE_OUT.ease(t * 2 - d, 0, c, d) * .5f + c * .5f + b;
        }
    };

    /**
     * The basic function for easing.
     *
     * @param t the time (either frames or in seconds/milliseconds)
     * @param b the beginning value
     * @param c the value changed
     * @param d the duration time
     * @return the eased value
     */
    float ease(float t, float b, float c, float d);

    /**
     * A base class for elastic easings.
     */
    abstract class Elastic implements Easing {
        private float amplitude;
        private float period;

        /**
         * Creates a new Elastic easing with the specified settings.
         *
         * @param amplitude the amplitude for the elastic function
         * @param period    the period for the elastic function
         */
        public Elastic(float amplitude, float period) {
            this.amplitude = amplitude;
            this.period = period;
        }

        /**
         * Creates a new Elastic easing with default settings (-1f, 0f).
         */
        public Elastic() {
            this(-1f, 0f);
        }

        /**
         * Returns the period.
         *
         * @return the period for this easing
         */
        public float getPeriod() {
            return period;
        }

        /**
         * Sets the period to the given value.
         *
         * @param period the new period
         */
        public void setPeriod(float period) {
            this.period = period;
        }

        /**
         * Returns the amplitude.
         *
         * @return the amplitude for this easing
         */
        public float getAmplitude() {
            return amplitude;
        }

        /**
         * Sets the amplitude to the given value.
         *
         * @param amplitude the new amplitude
         */
        public void setAmplitude(float amplitude) {
            this.amplitude = amplitude;
        }
    }

    /**
     * An Elastic easing used for ElasticIn functions.
     */
    class ElasticIn extends Elastic {
        public ElasticIn(float amplitude, float period) {
            super(amplitude, period);
        }

        public ElasticIn() {
            super();
        }

        public float ease(float t, float b, float c, float d) {
            float a = getAmplitude();
            float p = getPeriod();
            if (t == 0) return b;
            if ((t /= d) == 1) return b + c;
            if (p == 0) p = d * .3f;
            float s = 0;
            if (a < FastMath.abs(c)) {
                a = c;
                s = p / 4;
            } else s = p / (float) (2 * FastMath.PI) * (float) FastMath.asin(c / a);
            return -(a * (float) FastMath.pow(2, 10 * (t -= 1)) * (float) FastMath.sin((t * d - s) * (2 * FastMath.PI) / p)) + b;
        }
    }

    /**
     * An Elastic easing used for ElasticOut functions.
     */
    class ElasticOut extends Elastic {
        public ElasticOut(float amplitude, float period) {
            super(amplitude, period);
        }

        public ElasticOut() {
            super();
        }

        public float ease(float t, float b, float c, float d) {
            float a = getAmplitude();
            float p = getPeriod();
            if (t == 0) return b;
            if ((t /= d) == 1) return b + c;
            if (p == 0) p = d * .3f;
            float s = 0;
            if (a < FastMath.abs(c)) {
                a = c;
                s = p / 4;
            } else s = p / (float) (2 * FastMath.PI) * (float) FastMath.asin(c / a);
            return a * (float) FastMath.pow(2, -10 * t) * (float) FastMath.sin((t * d - s) * (2 * FastMath.PI) / p) + c + b;
        }
    }

    /**
     * An Elastic easing used for ElasticInOut functions.
     */
    class ElasticInOut extends Elastic {
        public ElasticInOut(float amplitude, float period) {
            super(amplitude, period);
        }

        public ElasticInOut() {
            super();
        }

        public float ease(float t, float b, float c, float d) {
            float a = getAmplitude();
            float p = getPeriod();
            if (t == 0) return b;
            if ((t /= d / 2) == 2) return b + c;
            if (p == 0) p = d * (.3f * 1.5f);
            float s = 0;
            if (a < FastMath.abs(c)) {
                a = c;
                s = p / 4f;
            } else s = p / (float) (2 * FastMath.PI) * (float) FastMath.asin(c / a);
            if (t < 1)
                return -.5f * (a * (float) FastMath.pow(2, 10 * (t -= 1)) * (float) FastMath.sin((t * d - s) * (2 * FastMath.PI) / p)) + b;
            return a * (float) FastMath.pow(2, -10 * (t -= 1)) * (float) FastMath.sin((t * d - s) * (2 * FastMath.PI) / p) * .5f + c + b;
        }
    }

    /**
     * A base class for Back easings.
     */
    abstract class Back implements Easing {
        /**
         * The default overshoot is 10% (1.70158).
         */
        public static final float DEFAULT_OVERSHOOT = 1.70158f;

        private float overshoot;

        /**
         * Creates a new Back instance with the default overshoot (1.70158).
         */
        public Back() {
            this(DEFAULT_OVERSHOOT);
        }

        /**
         * Creates a new Back instance with the specified overshoot.
         *
         * @param overshoot the amount to overshoot by -- higher number
         *                  means more overshoot and an overshoot of 0 results in
         *                  cubic easing with no overshoot
         */
        public Back(float overshoot) {
            this.overshoot = overshoot;
        }

        /**
         * Returns the overshoot for this easing.
         *
         * @return this easing's overshoot
         */
        public float getOvershoot() {
            return overshoot;
        }

        /**
         * Sets the overshoot to the given value.
         *
         * @param overshoot the new overshoot
         */
        public void setOvershoot(float overshoot) {
            this.overshoot = overshoot;
        }
    }

    /////////// BOUNCE EASING: exponentially decaying parabolic bounce  //////////////

    /**
     * Back easing in - backtracking slightly, then reversing direction and moving to target.
     */
    class BackIn extends Back {
        public BackIn() {
            super();
        }

        public BackIn(float overshoot) {
            super(overshoot);
        }

        public float ease(float t, float b, float c, float d) {
            float s = getOvershoot();
            return c * (t /= d) * t * ((s + 1) * t - s) + b;
        }
    }

    /**
     * Back easing out - moving towards target, overshooting it slightly, then reversing and coming back to target.
     */
    class BackOut extends Back {
        public BackOut() {
            super();
        }

        public BackOut(float overshoot) {
            super(overshoot);
        }

        public float ease(float t, float b, float c, float d) {
            float s = getOvershoot();
            return c * ((t = t / d - 1) * t * ((s + 1) * t + s) + 1) + b;
        }
    }

    /**
     * Back easing in/out - backtracking slightly, then reversing direction and moving to target,
     * then overshooting target, reversing, and finally coming back to target.
     */
    class BackInOut extends Back {
        public BackInOut() {
            super();
        }

        public BackInOut(float overshoot) {
            super(overshoot);
        }

        public float ease(float t, float b, float c, float d) {
            float s = getOvershoot();
            if ((t /= d / 2) < 1) return c / 2 * (t * t * (((s *= (1.525)) + 1) * t - s)) + b;
            return c / 2 * ((t -= 2) * t * (((s *= (1.525)) + 1) * t + s) + 2) + b;
        }
    }
}