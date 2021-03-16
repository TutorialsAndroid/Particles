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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.tutorials.android.particles.particles.Particles;

import java.util.List;

/**
 * A helper temporary view that helps render the particles. This view will attach itself to the
 * view root, perform the animation, and then once all of the particles has completed its animation,
 * it will automatically remove itself from the parent.
 */
public class ParticlesView extends View implements View.OnLayoutChangeListener {
    private List<Particles> particles;
    private boolean terminated;

    private boolean touchEnabled;
    private Particles draggedParticles;

    public static ParticlesView newInstance(Context context) {
        final ParticlesView particlesView = new ParticlesView(context, null);
        particlesView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final int defaultElevation = context.getResources().getDimensionPixelOffset(
                    R.dimen.particles_default_elevation);
            particlesView.setElevation(defaultElevation);
        }

        return particlesView;
    }

    public ParticlesView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Sets the list of particles to be animated by this view.
     *
     * @param particles the list of particles to be animated.
     */
    public void bind(List<Particles> particles) {
        this.particles = particles;
    }

    /**
     * @see ParticlesManager#setTouchEnabled(boolean)
     *
     * @param touchEnabled whether or not to enable touch
     */
    public void setTouchEnabled(boolean touchEnabled) {
        this.touchEnabled = touchEnabled;
    }

    /**
     * Terminate the current running animation (if any) and remove this view from the parent.
     */
    public void terminate() {
        if (!terminated) {
            this.terminated = true;
            getParent().requestLayout();
        }
    }

    /**
     * Reset the internal state of this view to allow for a new particles animation.
     */
    public void reset() {
        this.terminated = false;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        final ViewGroup parent = (ViewGroup) getParent();
        parent.removeOnLayoutChangeListener(this);
        parent.addOnLayoutChangeListener(this);

        // If we did not bind before attaching to the window, that means this particlesView no longer
        // has a particlesManager backing it and should just be terminated.
        if (particles == null) {
            terminate();
        }
    }

    @Override
    public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6,
            int i7) {
        if (terminated) {
            final ViewParent parent = getParent();
            if (parent instanceof ViewGroup) {
                final ViewGroup vg = (ViewGroup) parent;
                vg.removeViewInLayout(this);
                vg.removeOnLayoutChangeListener(this);
                vg.invalidate();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!terminated) {
            canvas.save();
            for (Particles particles : this.particles) {
                particles.draw(canvas);
            }
            canvas.restore();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = false;
        if (touchEnabled) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    for (Particles particles : particles) {
                        if (particles.onTouchDown(event)) {
                            draggedParticles = particles;
                            handled = true;
                            break;
                        }
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (draggedParticles != null) {
                        draggedParticles.onTouchMove(event);
                        handled = true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (draggedParticles != null) {
                        draggedParticles.onTouchUp(event);
                        draggedParticles = null;
                        handled = true;
                    }
                    break;
            }
        }

        return handled || super.onTouchEvent(event);
    }
}
