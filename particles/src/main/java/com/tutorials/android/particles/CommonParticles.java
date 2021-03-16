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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.ViewGroup;

import com.tutorials.android.particles.particles.BitmapParticles;

import java.util.List;

public class CommonParticles {
    private static int defaultParticlesSize;
    private static int defaultVelocitySlow;
    private static int defaultVelocityNormal;
    private static int defaultVelocityFast;
    private static int explosionRadius;

    private ParticlesManager particlesManager;

    private CommonParticles(ViewGroup container) {
        ensureStaticResources(container);
    }

    // region Pre-configured particles animations

    /**
     * @see #rainingParticles(ViewGroup, ParticlesSource, int[]) but with the default particles source
     * to be the top of the particles container viewgroup.
     *
     * @param container the container viewgroup to host the particles animation.
     * @param colors the set of colors to colorize the particles bitmaps.
     * @return the created common particles object.
     */
    public static CommonParticles rainingParticles(ViewGroup container, int[] colors) {
        final CommonParticles commonParticles = new CommonParticles(container);
        final ParticlesSource particlesSource = new ParticlesSource(0, -defaultParticlesSize,
                container.getWidth(), -defaultParticlesSize);
        commonParticles.configureRainingParticles(container, particlesSource, colors);
        return commonParticles;
    }

    /**
     * Configures a particles manager that has particles falling from the provided particles source.
     *
     * @param container the container viewgroup to host the particles animation.
     * @param particlesSource the source of the particles animation.
     * @param colors the set of colors to colorize the particles bitmaps.
     * @return the created common particles object.
     */
    public static CommonParticles rainingParticles(ViewGroup container,
                                                   ParticlesSource particlesSource, int[] colors) {
        final CommonParticles commonParticles = new CommonParticles(container);
        commonParticles.configureRainingParticles(container, particlesSource, colors);
        return commonParticles;
    }

    /**
     * Configures a particles manager that has particles exploding out in all directions from the
     * provided x and y coordinates.
     *
     * @param container the container viewgroup to host the particles animation.
     * @param x the x coordinate of the explosion source.
     * @param y the y coordinate of the explosion source.
     * @param colors the set of colors to colorize the particles bitmaps.
     * @return the created common particles object.
     */
    public static CommonParticles explosion(ViewGroup container, int x, int y, int[] colors) {
        final CommonParticles commonParticles = new CommonParticles(container);
        commonParticles.configureExplosion(container, x, y, colors);
        return commonParticles;
    }

    // endregion

    public ParticlesManager getParticlesManager() {
        return particlesManager;
    }

    /**
     * Starts a one-shot animation that emits all of the particles at once.
     *
     * @return the resulting {@link ParticlesManager} that's performing the animation.
     */
    public ParticlesManager oneShot() {
        return particlesManager.setNumInitialCount(100)
                .setEmissionDuration(0)
                .animate();
    }

    /**
     * Starts a stream of particles that animates for the provided duration.
     *
     * @param durationInMillis how long to animate the particles for.
     * @return the resulting {@link ParticlesManager} that's performing the animation.
     */
    public ParticlesManager stream(long durationInMillis) {
        return particlesManager.setNumInitialCount(0)
                .setEmissionDuration(durationInMillis)
                .setEmissionRate(50)
                .animate();
    }

    /**
     * Starts an infinite stream of particles.
     *
     * @return the resulting {@link ParticlesManager} that's performing the animation.
     */
    public ParticlesManager infinite() {
        return particlesManager.setNumInitialCount(0)
                .setEmissionDuration(ParticlesManager.INFINITE_DURATION)
                .setEmissionRate(50)
                .animate();
    }

    private ParticlesGenerator getDefaultGenerator(int[] colors) {
        final List<Bitmap> bitmaps = Utils.generateParticlesBitmaps(colors, defaultParticlesSize);
        final int numBitmaps = bitmaps.size();
        return random -> new BitmapParticles(bitmaps.get(random.nextInt(numBitmaps)));
    }

    private void configureRainingParticles(ViewGroup container, ParticlesSource particlesSource,
                                           int[] colors) {
        final Context context = container.getContext();
        final ParticlesGenerator generator = getDefaultGenerator(colors);

        particlesManager = new ParticlesManager(context, generator, particlesSource, container)
                .setVelocityX(0, defaultVelocitySlow)
                .setVelocityY(defaultVelocityNormal, defaultVelocitySlow)
                .setInitialRotation(180, 180)
                .setRotationalAcceleration(360, 180)
                .setTargetRotationalVelocity(360);
    }

    private void configureExplosion(ViewGroup container, int x, int y, int[] colors) {
        final Context context = container.getContext();
        final ParticlesGenerator generator = getDefaultGenerator(colors);
        final ParticlesSource particlesSource = new ParticlesSource(x, y);

        particlesManager = new ParticlesManager(context, generator, particlesSource, container)
                .setTTL(1000)
                .setBound(new Rect(
                        x - explosionRadius, y - explosionRadius,
                        x + explosionRadius, y + explosionRadius
                ))
                .setVelocityX(0, defaultVelocityFast)
                .setVelocityY(0, defaultVelocityFast)
                .enableFadeOut(Utils.getDefaultAlphaInterpolator())
                .setInitialRotation(180, 180)
                .setRotationalAcceleration(360, 180)
                .setTargetRotationalVelocity(360);
    }

    private static void ensureStaticResources(ViewGroup container) {
        if (defaultParticlesSize == 0) {
            final Resources res = container.getResources();
            defaultParticlesSize = res.getDimensionPixelSize(R.dimen.default_particles_size);
            defaultVelocitySlow = res.getDimensionPixelOffset(R.dimen.default_velocity_slow);
            defaultVelocityNormal = res.getDimensionPixelOffset(R.dimen.default_velocity_normal);
            defaultVelocityFast = res.getDimensionPixelOffset(R.dimen.default_velocity_fast);
            explosionRadius = res.getDimensionPixelOffset(R.dimen.default_explosion_radius);
        }
    }
}
