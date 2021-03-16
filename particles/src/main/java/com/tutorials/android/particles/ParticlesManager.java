/*
 * Copyright (C) 2021 TutorialsAndroid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tutorials.android.particles;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Interpolator;

import com.tutorials.android.particles.particles.Particles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * A helper manager class for configuring a set of particles and displaying them on the UI.
 */
public class ParticlesManager {
    public static final long INFINITE_DURATION = Long.MAX_VALUE;

    private final Random random = new Random();
    private final ParticlesGenerator particlesGenerator;
    private final ParticlesSource particlesSource;
    private final ViewGroup parentView;
    private final ParticlesView particlesView;

    private final Queue<Particles> recycledParticles = new LinkedList<>();
    private final List<Particles> particles = new ArrayList<>(300);
    private ValueAnimator animator;
    private long lastEmittedTimestamp;

    // All of the below configured values are in milliseconds despite the setter methods take them
    // in seconds as the parameters. The parameters for the setters are in seconds to allow for
    // users to better understand/visualize the dimensions.

    // Configured attributes for the entire particles group
    private int numInitialCount;
    private long emissionDuration;
    private float emissionRate, emissionRateInverse;
    private Interpolator fadeOutInterpolator;
    private Rect bound;

    // Configured attributes for each confetto
    private float velocityX, velocityDeviationX;
    private float velocityY, velocityDeviationY;
    private float accelerationX, accelerationDeviationX;
    private float accelerationY, accelerationDeviationY;
    private Float targetVelocityX, targetVelocityXDeviation;
    private Float targetVelocityY, targetVelocityYDeviation;
    private int initialRotation, initialRotationDeviation;
    private float rotationalVelocity, rotationalVelocityDeviation;
    private float rotationalAcceleration, rotationalAccelerationDeviation;
    private Float targetRotationalVelocity, targetRotationalVelocityDeviation;
    private long ttl;

    private ParticlesAnimationListener animationListener;

    public ParticlesManager(Context context, ParticlesGenerator particlesGenerator,
                            ParticlesSource particlesSource, ViewGroup parentView) {
        this(particlesGenerator, particlesSource, parentView, ParticlesView.newInstance(context));
    }

    public ParticlesManager(ParticlesGenerator particlesGenerator,
                            ParticlesSource particlesSource, ViewGroup parentView, ParticlesView particlesView) {
        this.particlesGenerator = particlesGenerator;
        this.particlesSource = particlesSource;
        this.parentView = parentView;
        this.particlesView = particlesView;
        this.particlesView.bind(particles);

        this.particlesView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                terminate();
            }
        });

        // Set the defaults
        this.ttl = -1;
        this.bound = new Rect(0, 0, parentView.getWidth(), parentView.getHeight());
    }

    /**
     * The number of particles initially emitted before any time has elapsed.
     *
     * @param numInitialCount the number of initial particles.
     * @return the particles manager so that the set calls can be chained.
     */
    public ParticlesManager setNumInitialCount(int numInitialCount) {
        this.numInitialCount = numInitialCount;
        return this;
    }

    /**
     * Configures how long this manager will emit new particles after the animation starts.
     *
     * @param emissionDurationInMillis how long to emit new particles in millis. This value can be
     *   {@link #INFINITE_DURATION} for a never-ending emission.
     * @return the particle manager so that the set calls can be chained.
     */
    public ParticlesManager setEmissionDuration(long emissionDurationInMillis) {
        this.emissionDuration = emissionDurationInMillis;
        return this;
    }

    /**
     * Configures how frequently this manager will emit new particles after the animation starts
     * if {@link #emissionDuration} is a positive value.
     *
     * @param emissionRate the rate of emission in # of particles per second.
     * @return the particles manager so that the set calls can be chained.
     */
    public ParticlesManager setEmissionRate(float emissionRate) {
        this.emissionRate = emissionRate / 1000f;
        this.emissionRateInverse = 1f / this.emissionRate;
        return this;
    }

    /**
     * @see #setVelocityX(float, float)
     *
     * @param velocityX the X velocity in pixels per second.
     * @return the particles manager so that the set calls can be chained.
     */
    public ParticlesManager setVelocityX(float velocityX) {
        return setVelocityX(velocityX, 0f);
    }

    /**
     * Set the velocityX used by this manager. This value defines the initial X velocity
     * for the generated particles. The actual particles's X velocity will be
     * (velocityX +- [0, velocityDeviationX]).
     *
     * @param velocityX the X velocity in pixels per second.
     * @param velocityDeviationX the deviation from X velocity in pixels per second.
     * @return the particles manager so that the set calls can be chained.
     */
    public ParticlesManager setVelocityX(float velocityX, float velocityDeviationX) {
        this.velocityX = velocityX / 1000f;
        this.velocityDeviationX = velocityDeviationX / 1000f;
        return this;
    }

    /**
     * @see #setVelocityY(float, float)
     *
     * @param velocityY the Y velocity in pixels per second.
     * @return the particles manager so that the set calls can be chained.
     */
    public ParticlesManager setVelocityY(float velocityY) {
        return setVelocityY(velocityY, 0f);
    }

    /**
     * Set the velocityY used by this manager. This value defines the initial Y velocity
     * for the generated particles. The actual particles's Y velocity will be
     * (velocityY +- [0, velocityDeviationY]). A positive Y velocity means that the velocity
     * is going down (because Y coordinate increases going down).
     *
     * @param velocityY the Y velocity in pixels per second.
     * @param velocityDeviationY the deviation from Y velocity in pixels per second.
     * @return the particles manager so that the set calls can be chained.
     */
    public ParticlesManager setVelocityY(float velocityY, float velocityDeviationY) {
        this.velocityY = velocityY / 1000f;
        this.velocityDeviationY = velocityDeviationY / 1000f;
        return this;
    }

    /**
     * @see #setAccelerationX(float, float)
     *
     * @param accelerationX the X acceleration in pixels per second^2.
     * @return the particles manager so that the set calls can be chained.
     */
    public ParticlesManager setAccelerationX(float accelerationX) {
        return setAccelerationX(accelerationX, 0f);
    }

    /**
     * Set the accelerationX used by this manager. This value defines the X acceleration
     * for the generated particles. The actual particles's X acceleration will be
     * (accelerationX +- [0, accelerationDeviationX]).
     *
     * @param accelerationX the X acceleration in pixels per second^2.
     * @param accelerationDeviationX the deviation from X acceleration in pixels per second^2.
     * @return the particles manager so that the set calls can be chained.
     */
    public ParticlesManager setAccelerationX(float accelerationX, float accelerationDeviationX) {
        this.accelerationX = accelerationX / 1000000f;
        this.accelerationDeviationX = accelerationDeviationX / 1000000f;
        return this;
    }

    /**
     * @see #setAccelerationY(float, float)
     *
     * @param accelerationY the Y acceleration in pixels per second^2.
     * @return the particles manager so that the set calls can be chained.
     */
    public ParticlesManager setAccelerationY(float accelerationY) {
        return setAccelerationY(accelerationY, 0f);
    }

    /**
     * Set the accelerationY used by this manager. This value defines the Y acceleration
     * for the generated particles. The actual particles's Y acceleration will be
     * (accelerationY +- [0, accelerationDeviationY]). A positive Y acceleration means that the
     * confetto will be accelerating downwards.
     *
     * @param accelerationY the Y acceleration in pixels per second^2.
     * @param accelerationDeviationY the deviation from Y acceleration in pixels per second^2.
     * @return the particles manager so that the set calls can be chained.
     */
    public ParticlesManager setAccelerationY(float accelerationY, float accelerationDeviationY) {
        this.accelerationY = accelerationY / 1000000f;
        this.accelerationDeviationY = accelerationDeviationY / 1000000f;
        return this;
    }

    /**
     * @see #setTargetVelocityX(float, float)
     *
     * @param targetVelocityX the target X velocity in pixels per second.
     * @return the particles manager so that the set calls can be chained.
     */
    public ParticlesManager setTargetVelocityX(float targetVelocityX) {
        return setTargetVelocityX(targetVelocityX, 0f);
    }

    /**
     * Set the target X velocity that particles can reach during the animation. The actual particles's
     * target X velocity will be (targetVelocityX +- [0, targetVelocityXDeviation]).
     *
     * @param targetVelocityX the target X velocity in pixels per second.
     * @param targetVelocityXDeviation  the deviation from target X velocity in pixels per second.
     * @return the particles manager so that the set calls can be chained.
     */
    public ParticlesManager setTargetVelocityX(float targetVelocityX,
                                               float targetVelocityXDeviation) {
        this.targetVelocityX = targetVelocityX / 1000f;
        this.targetVelocityXDeviation = targetVelocityXDeviation / 1000f;
        return this;
    }

    /**
     * @see #setTargetVelocityY(float, float)
     *
     * @param targetVelocityY the target Y velocity in pixels per second.
     * @return the particles manager so that the set calls can be chained.
     */
    public ParticlesManager setTargetVelocityY(float targetVelocityY) {
        return setTargetVelocityY(targetVelocityY, 0f);
    }

    /**
     * Set the target Y velocity that particles can reach during the animation. The actual particles's
     * target Y velocity will be (targetVelocityY +- [0, targetVelocityYDeviation]).
     *
     * @param targetVelocityY the target Y velocity in pixels per second.
     * @param targetVelocityYDeviation  the deviation from target Y velocity in pixels per second.
     * @return the particles manager so that the set calls can be chained.
     */
    public ParticlesManager setTargetVelocityY(float targetVelocityY,
                                               float targetVelocityYDeviation) {
        this.targetVelocityY = targetVelocityY / 1000f;
        this.targetVelocityYDeviation = targetVelocityYDeviation / 1000f;
        return this;
    }

    /**
     * @see #setInitialRotation(int, int)
     *
     * @param initialRotation the initial rotation in degrees.
     * @return the particles manager so that the set calls can be chained.
     */
    public ParticlesManager setInitialRotation(int initialRotation) {
        return setInitialRotation(initialRotation, 0);
    }

    /**
     * Set the initialRotation used by this manager. This value defines the initial rotation in
     * degrees for the generated particles. The actual particles's initial rotation will be
     * (initialRotation +- [0, initialRotationDeviation]).
     *
     * @param initialRotation the initial rotation in degrees.
     * @param initialRotationDeviation the deviation from initial rotation in degrees.
     * @return the particles manager so that the set calls can be chained.
     */
    public ParticlesManager setInitialRotation(int initialRotation, int initialRotationDeviation) {
        this.initialRotation = initialRotation;
        this.initialRotationDeviation = initialRotationDeviation;
        return this;
    }

    /**
     * @see #setRotationalVelocity(float, float)
     *
     * @param rotationalVelocity the initial rotational velocity in degrees per second.
     * @return the particles manager so that the set calls can be chained.
     */
    public ParticlesManager setRotationalVelocity(float rotationalVelocity) {
        return setRotationalVelocity(rotationalVelocity, 0f);
    }

    /**
     * Set the rotationalVelocity used by this manager. This value defines the the initial
     * rotational velocity for the generated particles. The actual particles's initial
     * rotational velocity will be (rotationalVelocity +- [0, rotationalVelocityDeviation]).
     *
     * @param rotationalVelocity the initial rotational velocity in degrees per second.
     * @param rotationalVelocityDeviation the deviation from initial rotational velocity in
     *   degrees per second.
     * @return the particles manager so that the set calls can be chained.
     */
    public ParticlesManager setRotationalVelocity(float rotationalVelocity,
                                                  float rotationalVelocityDeviation) {
        this.rotationalVelocity = rotationalVelocity / 1000f;
        this.rotationalVelocityDeviation = rotationalVelocityDeviation / 1000f;
        return this;
    }

    /**
     * @see #setRotationalAcceleration(float, float)
     *
     * @param rotationalAcceleration the rotational acceleration in degrees per second^2.
     * @return the particles manager so that the set calls can be chained.
     */
    public ParticlesManager setRotationalAcceleration(float rotationalAcceleration) {
        return setRotationalAcceleration(rotationalAcceleration, 0f);
    }

    /**
     * Set the rotationalAcceleration used by this manager. This value defines the the
     * acceleration of the rotation for the generated particles. The actual particles's rotational
     * acceleration will be (rotationalAcceleration +- [0, rotationalAccelerationDeviation]).
     *
     * @param rotationalAcceleration the rotational acceleration in degrees per second^2.
     * @param rotationalAccelerationDeviation the deviation from rotational acceleration in degrees
     *   per second^2.
     * @return the particles manager so that the set calls can be chained.
     */
    public ParticlesManager setRotationalAcceleration(float rotationalAcceleration,
                                                      float rotationalAccelerationDeviation) {
        this.rotationalAcceleration = rotationalAcceleration / 1000000f;
        this.rotationalAccelerationDeviation = rotationalAccelerationDeviation / 1000000f;
        return this;
    }

    /**
     * @see #setTargetRotationalVelocity(float, float)
     *
     * @param targetRotationalVelocity the target rotational velocity in degrees per second.
     * @return the particles manager so that the set calls can be chained.
     */
    public ParticlesManager setTargetRotationalVelocity(float targetRotationalVelocity) {
        return setTargetRotationalVelocity(targetRotationalVelocity, 0f);
    }

    /**
     * Set the target rotational velocity that particles can reach during the animation. The actual
     * particles's target rotational velocity will be
     * (targetRotationalVelocity +- [0, targetRotationalVelocityDeviation]).
     *
     * @param targetRotationalVelocity the target rotational velocity in degrees per second.
     * @param targetRotationalVelocityDeviation the deviation from target rotational velocity
     *   in degrees per second.
     * @return the particles manager so that the set calls can be chained.
     */
    public ParticlesManager setTargetRotationalVelocity(float targetRotationalVelocity,
                                                        float targetRotationalVelocityDeviation) {
        this.targetRotationalVelocity = targetRotationalVelocity / 1000f;
        this.targetRotationalVelocityDeviation = targetRotationalVelocityDeviation / 1000f;
        return this;
    }

    /**
     * Specifies a custom bound that the particles will clip to. By default, the particles will be
     * able to animate throughout the entire screen. The dimensions specified in bound is
     * global dimensions, e.g. x=0 is the top of the screen, rather than relative dimensions.
     *
     * @param bound the bound that clips the particles as they animate.
     * @return the particles manager so that the set calls can be chained.
     */
    public ParticlesManager setBound(Rect bound) {
        this.bound = bound;
        return this;
    }

    /**
     * Specifies a custom time to live for the particles generated by this manager. When a particles
     * reaches its time to live timer, it will disappear and terminate its animation.
     *
     * <p>The time to live value does not include the initial delay of the particles.
     *
     * @param ttlInMillis the custom time to live in milliseconds.
     * @return the particles manager so that the set calls can be chained.
     */
    public ParticlesManager setTTL(long ttlInMillis) {
        this.ttl = ttlInMillis;
        return this;
    }

    /**
     * Enables fade out for all of the particles generated by this manager. Fade out means that
     * the particles will animate alpha according to the fadeOutInterpolator according
     * to its TTL or, if TTL is not set, its bounds.
     *
     * @param fadeOutInterpolator an interpolator that interpolates animation progress [0, 1] into
     *   an alpha value [0, 1], 0 being transparent and 1 being opaque.
     * @return the particles manager so that the set calls can be chained.
     */
    public ParticlesManager enableFadeOut(Interpolator fadeOutInterpolator) {
        this.fadeOutInterpolator = fadeOutInterpolator;
        return this;
    }

    /**
     * Disables fade out for all of the particles generated by this manager.
     *
     * @return the particles manager so that the set calls can be chained.
     */
    public ParticlesManager disableFadeOut() {
        this.fadeOutInterpolator = null;
        return this;
    }

    /**
     * Enables or disables touch events for the particles generated by this manager. By enabling
     * touch, the user can touch individual confetto and drag/fling them on the screen independent
     * of their original animation state.
     *
     * @param touchEnabled whether or not to enable touch.
     * @return the particles manager so that the set calls can be chained.
     */
    public ParticlesManager setTouchEnabled(boolean touchEnabled) {
        this.particlesView.setTouchEnabled(touchEnabled);
        return this;
    }

    /**
     * Sets a {@link ParticlesAnimationListener} for this particles manager.
     *
     * @param listener the animation listener, or null to clear out the existing listener.
     * @return the particles manager so that the set calls can be chained.
     */
    public ParticlesManager setParticlesAnimationListener(ParticlesAnimationListener listener) {
        this.animationListener = listener;
        return this;
    }

    /**
     * Start the particles animation configured by this manager.
     *
     * @return the particles manager itself that just started animating.
     */
    public ParticlesManager animate() {
        if (animationListener != null) {
            animationListener.onAnimationStart(this);
        }

        cleanupExistingAnimation();
        attachParticlesViewToParent();
        addNewParticles(numInitialCount, 0);
        startNewAnimation();
        return this;
    }

    /**
     * Terminate the currently running animation if there is any.
     */
    public void terminate() {
        if (animator != null) {
            animator.cancel();
        }
        particlesView.terminate();

        if (animationListener != null) {
            animationListener.onAnimationEnd(this);
        }
    }

    private void cleanupExistingAnimation() {
        if (animator != null) {
            animator.cancel();
        }

        lastEmittedTimestamp = 0;
        final Iterator<Particles> iterator = particles.iterator();
        while (iterator.hasNext()) {
            removeParticles(iterator.next());
            iterator.remove();
        }
    }

    private void attachParticlesViewToParent() {
        final ViewParent currentParent = particlesView.getParent();
        if (currentParent != null) {
            if (currentParent != parentView) {
                ((ViewGroup) currentParent).removeView(particlesView);
                parentView.addView(particlesView);
            }
        } else {
            parentView.addView(particlesView);
        }

        particlesView.reset();
    }

    private void addNewParticles(int numparticles, long initialDelay) {
        for (int i = 0; i < numparticles; i++) {
            Particles particles = recycledParticles.poll();
            if (particles == null) {
                particles = particlesGenerator.generateParticles(random);
            }

            particles.reset();
            configureParticles(particles, particlesSource, random, initialDelay);
            particles.prepare(bound);

            addParticles(particles);
        }
    }

    private void startNewAnimation() {
        // Never-ending animator, we will cancel once the termination condition is reached.
        animator = ValueAnimator.ofInt(0)
                .setDuration(Long.MAX_VALUE);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                final long elapsedTime = valueAnimator.getCurrentPlayTime();
                processNewEmission(elapsedTime);
                updateParticles(elapsedTime);

                if (particles.size() == 0 && elapsedTime >= emissionDuration) {
                    terminate();
                } else {
                    particlesView.invalidate();
                }
            }
        });

        animator.start();
    }

    private void processNewEmission(long elapsedTime) {
        if (elapsedTime < emissionDuration) {
            if (lastEmittedTimestamp == 0) {
                lastEmittedTimestamp = elapsedTime;
            } else {
                final long timeSinceLastEmission = elapsedTime - lastEmittedTimestamp;

                // Randomly determine how many particles to emit
                final int numNewparticles = (int)
                        (random.nextFloat() * emissionRate * timeSinceLastEmission);
                if (numNewparticles > 0) {
                    lastEmittedTimestamp += Math.round(emissionRateInverse * numNewparticles);
                    addNewParticles(numNewparticles, elapsedTime);
                }
            }
        }
    }

    private void updateParticles(long elapsedTime) {
        final Iterator<Particles> iterator = particles.iterator();
        while (iterator.hasNext()) {
            final Particles particles = iterator.next();
            if (!particles.applyUpdate(elapsedTime)) {
                iterator.remove();
                removeParticles(particles);
            }
        }
    }

    private void addParticles(Particles particles) {
        this.particles.add(particles);
        if (animationListener != null) {
            animationListener.onParticlesEnter(particles);
        }
    }

    private void removeParticles(Particles particles) {
        if (this.animationListener != null) {
            this.animationListener.onParticlesExit(particles);
        }
        recycledParticles.add(particles);
    }

    protected void configureParticles(Particles particles, ParticlesSource particlesSource,
                                      Random random, long initialDelay) {
        particles.setInitialDelay(initialDelay);
        particles.setInitialX(particlesSource.getInitialX(random.nextFloat()));
        particles.setInitialY(particlesSource.getInitialY(random.nextFloat()));
        particles.setInitialVelocityX(getVarianceAmount(velocityX, velocityDeviationX, random));
        particles.setInitialVelocityY(getVarianceAmount(velocityY, velocityDeviationY, random));
        particles.setAccelerationX(getVarianceAmount(accelerationX, accelerationDeviationX, random));
        particles.setAccelerationY(getVarianceAmount(accelerationY, accelerationDeviationY, random));
        particles.setTargetVelocityX(targetVelocityX == null ? null
                : getVarianceAmount(targetVelocityX, targetVelocityXDeviation, random));
        particles.setTargetVelocityY(targetVelocityY == null ? null
                : getVarianceAmount(targetVelocityY, targetVelocityYDeviation, random));
        particles.setInitialRotation(
                getVarianceAmount(initialRotation, initialRotationDeviation, random));
        particles.setInitialRotationalVelocity(
                getVarianceAmount(rotationalVelocity, rotationalVelocityDeviation, random));
        particles.setRotationalAcceleration(
                getVarianceAmount(rotationalAcceleration, rotationalAccelerationDeviation, random));
        particles.setTargetRotationalVelocity(targetRotationalVelocity == null ? null
                : getVarianceAmount(targetRotationalVelocity, targetRotationalVelocityDeviation,
                        random));
        particles.setTTL(ttl);
        particles.setFadeOut(fadeOutInterpolator);
    }

    private float getVarianceAmount(float base, float deviation, Random random) {
        // Normalize random to be [-1, 1] rather than [0, 1]
        return base + (deviation * (random.nextFloat() * 2 - 1));
    }

    public interface ParticlesAnimationListener {
        void onAnimationStart(ParticlesManager particlesManager);
        void onAnimationEnd(ParticlesManager particlesManager);
        void onParticlesEnter(Particles particles);
        void onParticlesExit(Particles particles);
    }

    public static class ParticlesAnimationListenerAdapter implements ParticlesAnimationListener {
        @Override public void onAnimationStart(ParticlesManager particlesManager) {}
        @Override public void onAnimationEnd(ParticlesManager particlesManager) {}
        @Override public void onParticlesEnter(Particles particles) {}
        @Override public void onParticlesExit(Particles particles) {}
    }
}
