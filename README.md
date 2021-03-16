![](https://github.com/TutorialsAndroid/Particles/blob/main/sample/src/main/res/mipmap-xxhdpi/ic_launcher.png)

#Particles

Particles is an android library that will help you to show particles on your app screen

## ScreenShot
![](https://github.com/TutorialsAndroid/MenuSheet/blob/main/art/device-2021-03-15-192350.gif)

## Setup
The simplest way to use MenuSheet is to add the library as dependency to your build.

## Gradle

Add it in your root build.gradle at the end of repositories:

    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }

Step 2. Add the dependency

    dependencies {
            implementation 'com.github.TutorialsAndroid:Particles:v1.0'
    }


## Simple usage
------------

The only thing you need to get particles on your screen is a parent view to host the `ParticlesView`
and thus the particles animation. From this point on, this parent view is referred to as `container`.

Please note that the library uses measurements from `container` to figure out how to best animate
the particles. If the `container` is not measured when creating the particles, then nothing will
show up on the screen. A common pitfall is creating particles inside activity lifecycle as the views
are most likely not measured at those points.

You can generate pre-configured particles from `CommonParticles`. You only need to provide it with
the parent `container`, a `ParticlesSource`, and an array of possible colors for the particles.
The default particles shapes are circle, triangle, and square.

```java
CommonParticles.rainingParticles(container, new int[] {
                            Color.BLACK,Color.BLUE,Color.GREEN,Color.RED,Color.YELLOW
                    }).oneShot();
```


More custom usage
-----------------

First, we need to define what our individual particles is through the `ParticlesGenerator`. Each call of `generateParticles` must generate a brand new `Particles` object (the `ParticlesManager` will recycle the generated particles as needed so youmight see fewer and fewer calls to `generateParticles` as the animation goes on). We pass in a`Random` into `generateParticles` in case you want to randomly generate a particles from a list
of possible particles.

A simple `ParticlesGenerator` might look like this:

```java
    final List<Bitmap> allPossibleParticles = Utils.generateParticlesBitmaps(new int[] { Color.BLACK }, 20 /* size */);
    // Alternatively, we provide some helper methods inside `Utils` to generate square, circle,
    // and triangle bitmaps.
    // Utils.generateParticlesBitmaps(new int[] { Color.BLACK }, 20 /* size */);

    final int numParticles = allPossibleParticles.size();
    final ParticlesGenerator particlesGenerator = new ParticlesGenerator() {
        @Override
        public Particles generateParticles(Random random) {
            final Bitmap bitmap = allPossibleParticles.get(random.nextInt(numParticles));
            return new BitmapParticles(bitmap);
        }
    };
```

Once we have our `ParticlesGenerator`, we'll need to define a `ParticlesSource` from which particles
will appear out of. This source can be any arbitrary point or line.

```java
final int containerMiddleX = container.getWidth() / 2;
final int containerMiddleY = container.getHeight() / 2;
final ParticlesSource particlesSource = new ParticlesSource(containerMiddleX, containerMiddleY);
```

Now you are ready! construct your `ParticlesManager`, configure the animation to your liking, and
then call `animate()`!

```java
new ParticlesManager(context, particlesGenerator, particlesSource, container)
        .setEmissionDuration(1000)
        .setEmissionRate(100)
        .setVelocityX(20, 10)
        .setVelocityY(100)
        .setRotationalVelocity(180, 180)
        .animate();
```

The `animate()` call will create and configure the various `Particles` objects on demand,
create a new `ParticlesView`, initialize the proper states for all of the components, attach the
view to the `container` and start the animation. The `ParticlesView` will auto-detach itself once
all of the particles have terminated and are off the screen.

For more sample usage of the library, please check out the
[particles-sample]( ) app that's
included in this project.


Configuration
-------------

The `ParticlesManager` is easily configurable. For simplicity's sake, all of the velocity and
acceleration attributes are in pixels per second or pixels per second^2, whereas all of the raw
time attributes (such as `ttl` and `emissionDuration`) are in milliseconds.

You will notice that most of the setters for the physical attributes (e.g. velocity, acceleration,
rotation) can take in either one argument for the actual value or two arguments. The second
argument allows you to specify a random deviation if you want to randomize the behavior among
all of the generated particles.

For example:

```java
particlesManager.setVelocityX(200f, 50f);
```

The generated particles will have an initial X velocity of anywhere between `200 - 50` or `150` and
`200 + 50` or `250`, eventually distributed.

`enableFadeOut(Interpolator fadeOutInterpolator)` is another interesting method. You can specify
that fade out occurs as a particles nears its boundary (either reaching the physical boundary
specified in `bound` (this is either the entirety of `container` or set in `setBound`) or reaching
`ttl`). The interpolator essentially takes in a value between 0 and 1 (0 means that the particles
is at its source, 1 means the particles is at its bound) and outputs an alpha value between 0 and 1
(0 is transparent and 1 is opaque). This way, we allow you to have the full power of specifying
how the fade out occurs.

Or, if you are lazy, you can just use `Utils.getDefaultAlphaInterpolator()`.


Advanced usage
==============

Enable touch and drag
---------------------

If you call `particlesManager.setTouchEnabled(true)`, you can allow the user to touch and drag
the particles that are on the screen. When the user let go of the particles, the particles will
start at that location with the user initiated velocity and the pre-configured acceleration
and resume animation from there.


Custom Particles
---------------

It's very easy to define a custom `Particles` (see `BitmapParticles`). You simply need to extend
from the `Particles` class and implement `drawInternal`. The function will provide you with a
`Canvas` to draw on as well as a work `Matrix` and `Paint` so you don't have to allocate objects
in the draw path. You then need to essentially draw your particles however you want onto the canvas
using the specified `x`, `y`, and `rotation`.

The cool part is that you can interpret `rotation` however you want. Instead of an angle in degrees,
you can choose to interpret rotation where each degree corresponds to a new shape or new image.
This way, you can achieve some cool animation effects as the particles flow through the screen.


Changing particles configuration mid-animation
---------------------------------------------

If you have a handle on the `ParticlesManager`, you can actually very easily change the configuration
mid-animation for more unique experiences. For example:

```java
particlesManager.setEmissionRate(100)
        .animate();

new Handler().postDelayed(new Runnable() {
    @Override public void run() {
        particlesManager.setEmissionRate(20);
    }
}, 3000);
```

The above snippet will configure the initial emission rate to be 100 particles per second and start
the animation. After 3 seconds, it will reduce the emission rate to 20 particles per second. This 
applies to all attributes (e.g. changing velocity or acceleration based on some outside condition).


## License

* [Apache Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

```
Copyright 2021 MenuSheet

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
