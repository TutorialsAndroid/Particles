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

import com.tutorials.android.particles.CommonParticles;
import com.tutorials.android.particles.ParticlesManager;

public class FallingParticlesFromTopActivity extends AbstractActivity {

    @Override
    protected ParticlesManager generateOnce() {
        return CommonParticles.rainingParticles(container, colors)
                .oneShot();
    }

    @Override
    protected ParticlesManager generateStream() {
        return CommonParticles.rainingParticles(container, colors)
                .stream(3000);
    }

    @Override
    protected ParticlesManager generateInfinite() {
        return CommonParticles.rainingParticles(container, colors)
                .infinite();
    }
}
