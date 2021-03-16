package com.tutorials.android.particles.sample;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.tutorials.android.particles.ParticlesGenerator;
import com.tutorials.android.particles.ParticlesManager;
import com.tutorials.android.particles.ParticlesSource;
import com.tutorials.android.particles.particles.BitmapParticles;
import com.tutorials.android.particles.particles.Particles;

import java.util.Random;

public class FallingWithTouchActivity extends AbstractActivity implements ParticlesGenerator {
    private int size;
    private int velocitySlow, velocityNormal;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Resources res = getResources();
        size = res.getDimensionPixelSize(R.dimen.big_particles_size);
        velocitySlow = res.getDimensionPixelOffset(R.dimen.default_velocity_slow);
        velocityNormal = res.getDimensionPixelOffset(R.dimen.default_velocity_normal);

        bitmap = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(res, R.drawable.snowflake),
                size,
                size,
                false
        );
    }

    @Override
    protected ParticlesManager generateOnce() {
        return getConfettiManager().setNumInitialCount(20)
                .setEmissionDuration(0)
                .animate();
    }

    @Override
    protected ParticlesManager generateStream() {
        return getConfettiManager().setNumInitialCount(0)
                .setEmissionDuration(3000)
                .setEmissionRate(20)
                .animate();
    }

    @Override
    protected ParticlesManager generateInfinite() {
        return getConfettiManager().setNumInitialCount(0)
                .setEmissionDuration(ParticlesManager.INFINITE_DURATION)
                .setEmissionRate(20)
                .animate();
    }

    private ParticlesManager getConfettiManager() {
        final ParticlesSource source = new ParticlesSource(0, -size, container.getWidth(), -size);
        return new ParticlesManager(this, this, source, container)
                .setVelocityX(0, velocitySlow)
                .setVelocityY(velocityNormal, velocitySlow)
                .setRotationalVelocity(180, 90)
                .setTouchEnabled(true);
    }

    @Override
    public Particles generateParticles(Random random) {
        return new BitmapParticles(bitmap);
    }
}
