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

package com.tutorials.android.particles.sample;

import android.content.res.Resources;

import com.tutorials.android.particles.CommonParticles;
import com.tutorials.android.particles.ParticlesManager;
import com.tutorials.android.particles.ParticlesSource;

public class FallingParticlesFromPointActivity extends AbstractActivity {

    @Override
    protected ParticlesManager generateOnce() {
        return getCommonConfetti().oneShot();
    }

    @Override
    protected ParticlesManager generateStream() {
        return getCommonConfetti().stream(3000);
    }

    @Override
    protected ParticlesManager generateInfinite() {
        return getCommonConfetti().infinite();
    }

    private CommonParticles getCommonConfetti() {
        final int size = getResources().getDimensionPixelSize(R.dimen.default_particles_size);
        final ParticlesSource particlesSource = new ParticlesSource(-size, -size);
        final CommonParticles commonParticles =
                CommonParticles.rainingParticles(container, particlesSource, colors);

        final Resources res = getResources();
        final int velocitySlow = res.getDimensionPixelOffset(R.dimen.default_velocity_slow);
        final int velocityNormal = res.getDimensionPixelOffset(R.dimen.default_velocity_normal);
        final int velocityFast = res.getDimensionPixelOffset(R.dimen.default_velocity_fast);

        // Further configure it
        commonParticles.getParticlesManager()
                .setVelocityX(velocityFast, velocityNormal)
                .setAccelerationX(-velocityNormal, velocitySlow)
                .setTargetVelocityX(0, velocitySlow / 2f)
                .setVelocityY(velocityNormal, velocitySlow);

        return commonParticles;
    }
}
