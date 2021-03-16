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

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.tutorials.android.particles.ParticlesManager;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public abstract class AbstractActivity extends AppCompatActivity implements View.OnClickListener {
    protected ViewGroup container;

    protected int goldDark, goldMed, gold, goldLight;
    protected int[] colors;

    private final List<ParticlesManager> activeParticlesManagers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutRes());

        container = findViewById(R.id.container);
        findViewById(R.id.generate_particles_once_btn).setOnClickListener(this);
        findViewById(R.id.generate_particles_stream_btn).setOnClickListener(this);
        findViewById(R.id.generate_particles_infinite_btn).setOnClickListener(this);
        findViewById(R.id.generate_particles_stop_btn).setOnClickListener(this);

        goldDark = ContextCompat.getColor(this,R.color.gold_dark);
        goldMed = ContextCompat.getColor(this,R.color.gold_med);
        gold = ContextCompat.getColor(this,R.color.gold);
        goldLight = ContextCompat.getColor(this,R.color.gold_light);
        colors = new int[] { goldDark, goldMed, gold, goldLight };
    }

    @LayoutRes
    protected int getLayoutRes() {
        return R.layout.activity_particles;
    }

    @Override
    public void onClick(View view) {
        final int id = view.getId();
        if (id == R.id.generate_particles_once_btn) {
            activeParticlesManagers.add(generateOnce());
        } else if (id == R.id.generate_particles_stream_btn) {
            activeParticlesManagers.add(generateStream());
        } else if (id == R.id.generate_particles_infinite_btn) {
            activeParticlesManagers.add(generateInfinite());
        } else {
            for (ParticlesManager particlesManager : activeParticlesManagers) {
                particlesManager.terminate();
            }
            activeParticlesManagers.clear();
        }
    }

    protected abstract ParticlesManager generateOnce();
    protected abstract ParticlesManager generateStream();
    protected abstract ParticlesManager generateInfinite();
}
