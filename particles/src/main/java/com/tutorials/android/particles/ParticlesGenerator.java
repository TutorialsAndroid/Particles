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

import com.tutorials.android.particles.particles.Particles;

import java.util.Random;

public interface ParticlesGenerator {
    /**
     * Generate a random particles to animate.
     *
     * @param random a {@link Random} that can be used to generate random particles.
     * @return the randomly generated particles.
     */
    Particles generateParticles(Random random);
}
