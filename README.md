
# TruckMe

## Description

This repository contains **Admin** and **Driver** product flavor merge into single codebase.

## Development
### Requirements & Environment setup
- [**Android Studio Artic Fox**](https://developer.android.com/studio/archive) Beta (or newer) to be able to build the app.
- JDK 1.8 or [**JDK11**](https://www.oracle.com/ph/java/technologies/javase-jdk11-downloads.html) (working on my development setup).
- Kotlin-enabled project.

## Installation
First clone this repository (See the options on the right hand side.)

In Android Studio, use the "Open an existing Android Studio project", find the downloaded project folder and select the (`truck-me-android`).

1. [Get an API Key](https://console.cloud.google.com/google/maps-apis/credentials?project=truckme-debug-326812).
2. Open the `local.properties` file in root project (you must switch project structure from `Android` to `Project` in the top-left corner).
3. Add a single line to `local.properties` that looks like `MAPS_API_KEY=YOUR_API_KEY`, where `YOUR_API_KEY` is the API key you obtained in the first step.
4. For `Places` and `Directions` API Key. Just copy your `API KEY` and place this key to `gradle.properties` `MAPS_API_KEY="PLACE_YOUR_API_KEY_HERE"`, where `PLACE_YOUR_API_KEY_HERE` is the same as your API key in the `local.properties`. See pull request [#23](https://github.com/forceporquillo/truck-me-android/pull/23)
5. Build and run.

## Switching between Admin and Driver build variant
Navigate to ```Build Variants```. (Can be found on the bottom left side of project panel) 

<p align="start">
<img src="/screenshots/build_variant.png"/>
</p>

Note: use only the ```adminDebug``` and ```driverDebug``` type for testing.

Clean and run the project.

Alternatively use the `./gradlew build` command to build the project directly.

## Modules

Modules are collection of source files and build settings that allow you to divide a project into discrete units of functionality. In this case apart from dividing by functionality/responsibility, existing the following dependence between them:

The above graph shows the app modularization:
-    `:app` module depends on `:core` and `:lib`.
-    `:core` and `:lib` doesn’t have any dependency.

#### App module

The `:app` module is an [com.android.application](https://developer.android.com/studio/build/), which is needed to create the app bundle.  It is also responsible for initiating the [dependency graph](https://github.com/google/dagger), [play core](https://developer.android.com/reference/com/google/android/play/core/release-notes) and another project global libraries, differentiating especially between different app environments.

#### Core module

The `:core` module is an [com.android.library](https://developer.android.com/studio/projects/android-library) for serving network requests, accessing to the database and shared preferences. Providing the data source for the many features that require it.

#### Libs module

The `:libs` module is an [com.android.library](https://developer.android.com/studio/projects/dynamic-delivery) only contains inset animations, drag and UI layout manipulation.


# License
```
Copyright 2021 Force Porquillo (strongforce)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
