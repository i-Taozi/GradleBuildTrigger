# CameraButton

Instagram-like button for taking photos or recording videos.

<img src="/.github/demo.gif">

[![Build Status](https://travis-ci.org/hluhovskyi/CameraButton.svg?branch=master)](https://travis-ci.org/hluhovskyi/CameraButton)

## Getting started

Add library as dependency to your `build.gradle`.

   ```groovy
    compile 'com.hluhovskyi.camerabutton:camerabutton:2.0.1'
    compile 'com.hluhovskyi.camerabutton:camerabutton-rxjava2:2.0.1'
    compile 'com.hluhovskyi.camerabutton:camerabutton-rxjava2-kotlin:2.0.1'
   ```
No need to include all dependencies, choose just one which covers your needs.

Please, feel free to open issues you are stuck with. PRs are also welcome :)

## How to use?

1. Add `CameraButton` to your xml markup

   ```xml
    <com.hluhovskyi.camerabutton.CameraButton
        android:id="@+id/camera_button"
        android:layout_width="@dimen/cb_layout_width_default"
        android:layout_height="@dimen/cb_layout_height_default" />
   ```
   
2. Find view and attach needed listeners

   ```java
    cameraButton.setOnTapEventListener(new CameraButton.OnTapEventListener() {
        @Override public void onTap() {
            takePhoto();
        }
    });
    cameraButton.setOnHoldEventListener(new CameraButton.OnHoldEventListener() {
        @Override public void onStart() {
            startRecordVideo();
        }
        @Override public void onFinish() {
            finishRecordVideo();
        }
        @Override public void onCancel() {
            cancelRecordVideo();
        }
    });
    cameraButton.setOnStateChangeListener(new CameraButton.OnStateChangeListener() {
        @Override public void onStateChanged(@NonNull CameraButton.State state) {
            dispatchStateChange(state);
        }
    });
   ```

3. Enjoy!
  
  ![Check full example](https://github.com/hluhovskyi/CameraButton/tree/master/sample)
  
  Take into account that you can get `ConsistencyValidationException` in case some part of the button overlaps other one or crosses view's boundaries. If you don't need "pixel-perfect" checks you can disable them by calling `button.shouldCheckConsistency(false)` otherwise you have to set correct sizes.
  
## RxJava and Kotlin

For now only RxJava version 2 is supported. It is implemented according to guidelines of ![JakeWharton/RxBinding](https://github.com/JakeWharton/RxBinding) library.

Example of usage:

   ```java
   
    RxCameraButton.stateChanges(button)
        .filter(state -> state == CameraButton.State.START_COLLAPSING)
        .subscribe(state -> hideButtons());
        
    RxCameraButton.tapEvents(button)
        .subscribe(event -> takePhoto());
   
   ```
All events have `componentN` function so if you are using Kotlin for development there is ability to use destruction declaration and also there are few extension methods:

   ```kotlin
   
    button.stateChanges()
        .filter { it == CameraButton.State.START_COLLAPSING }
        .subscribe { hideButtons() }
        
    button.stateChangeEvents()
        .filter { (view, state) -> 
            view.id == R.id.alert_button && state == CameraButton.State.PRESSED
        }
        .subscribe { showAlert() }
      
    button.tapEvents()
        .subscribe { takePhoto() }
   
   ```

## Customization

- `cb_main_circle_radius` or `setMainCircleRadius()`

Default value - `28dp`/`@dimen/cb_main_circle_radius_default`

<img width="250" src="/.github/arts/cb_main_circle_radius.png">


- `cb_main_circle_color` or `setMainCircleColor()`

Default value - `#ffffff`/`@color/cb_main_circle_color_default`

<img width="250" src="/.github/arts/cb_main_circle_color.png">


- `cb_stroke_width` or `setStrokeWidth()`

Default value - `12dp`/`@dimen/cb_stroke_width_default`

<img width="250" src="/.github/arts/cb_stroke_width.png">


- `cb_stroke_color` or `setStrokeColor()`

Default value - `#66FFFFFF`/`@color/cb_stroke_color_default`

<img width="250" src="/.github/arts/cb_stroke_color.png">


- `cb_main_circle_radius_expanded` or `setMainCircleRadiusExpanded()`

Default value - `24dp`/`@dimen/cb_main_circle_radius_expanded_default`

<img width="250" src="/.github/arts/cb_main_circle_radius_expanded.png">


- Expanded stroke width can't be set explicitly. It is calculated by following formula:

`stroke_width_expanded = min(layout_width, layout_height) - main_circle_expanded`

<img width="250" src="/.github/arts/cb_stroke_width_expanded.png">


- `cb_progress_arc_width` or `setProgressArcWidth()`

Default value - `4dp`/`@dimen/cb_progress_arc_width_default`

<img width="250" src="/.github/arts/cb_progress_arc_width.png">


- `cb_progress_arc_colors` or `setProgressArcColors()`

Default values - `[#feda75, #fa7e1e, #d62976, #962fbf, #4f5bd5]`/`@array/cb_progress_arc_colors_default`

<img width="250" src="/.github/arts/cb_progress_arc_colors.png">

To set values via xml you have to define all colors separately and **merge their references into one array**:

   ```xml
    <color name="my_color_1">#000000</color>
    <color name="my_color_2">#ffffff</color>

    <array name="my_progress_colors">
        <item>@color/my_color_1</item>
        <item>@color/my_color_2</item>
    </array>
    
    ...
    
    <com.hluhovskyi.camerabutton.CameraButton
        ...
        app:cb_progress_arc_colors="@color/my_progress_colors"/>  
   ```
        
To set values programmatically you have to call `setProgressArcColors` with **array of color values**:

   ```java
    button.setProgressArcColors(new int[]{Color.BLACK, Color.WHITE});
   ```

## License

```
Copyright (C) 2018 Artem Hluhovskyi

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
