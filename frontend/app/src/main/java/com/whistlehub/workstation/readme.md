Using pre-built binaries and headers

Oboe is distributed as a prefab package via Google Maven (search for "oboe").
Prefab support was added to Android Studio 4.0 so you'll need to be using this version of Android
Studio or above.

Add the oboe dependency to your app's build.gradle file. Replace "X.X.X" with the latest stable
version of Oboe:

```kotlin
    implementation("com.google.oboe:oboe:X.X.X")
```

```kotlin
android {
    buildFeatures {
        prefab true
    }
}
```

Include and link to oboe by updating your CMakeLists.txt:

```text
find_package (oboe REQUIRED CONFIG)
target_link_libraries(native-lib oboe::oboe) # You may have other libraries here such as `log`.
```

Here's a complete example CMakeLists.txt file:

```text
cmake_minimum_required(VERSION 3.4.1)

# Build our own native library
add_library (native-lib SHARED native-lib.cpp )

# Find the Oboe package
find_package (oboe REQUIRED CONFIG)

# Specify the libraries which our native library is dependent on, including Oboe
target_link_libraries(native-lib log oboe::oboe)
```

For app/build.gradle.kts add parentheses:

```text
  arguments("-DANDROID_STL=c++_shared")
```

```text
'externalNativeBuild(kotlin. jvm. functions. Function1<? super com. android. build. api. dsl. ExternalNativeBuildFlags,kotlin. Unit>)' is marked unstable with @Incubating
```

Gradle의 externalNativeBuild 관련 API가 @Incubating 애노테이션으로 표시되어 있음
즉, 해당 API가 아직 완전히 안정화되지 않았으며 앞으로 변경되거나 제거될 가능성이 있다는 의미
현재 이 API를 사용하더라도 빌드에는 문제가 없지만, 향후 Android Gradle Plugin 업데이트 시에 API가 변경될 수 있으므로 주의가 필요

### CMAKE_PROJECT_NAME

`${CMAKE_PROJECT_NAME}`은 CMake에서 프로젝트 이름을 지정할 때 자동으로 설정되는 변수 
예를 들어, CMakeLists.txt 상단에 다음과 같이 작성하면:

```cmake
project("whistlehub")
```

이렇게 하면 CMake는 프로젝트 이름을 "whistlehub"로 설정하고
이 값은 `${CMAKE_PROJECT_NAME}` 또는 `${PROJECT_NAME}` 변수로 사용

```cmake
add_library(${CMAKE_PROJECT_NAME} SHARED whistlehub.cpp)
```

"whistlehub"라는 이름의 **공유 라이브러리**를 생성하게 됩니다.

만약 라이브러리 이름을 별도로 지정하고 싶다면, 아래와 같이 변수로 따로 선언

```cmake
set(MY_LIB_NAME "my_custom_lib")
add_library(${MY_LIB_NAME} SHARED whistlehub.cpp)
```

이렇게 하면 생성되는 라이브러리의 이름이 "my_custom_lib"가 됩니다.

요약하면, `${CMAKE_PROJECT_NAME}`은 `project("whistlehub")` 명령어로 설정된 값이며
이를 이용해 네이티브 라이브러리 이름을 자동으로 프로젝트 이름과 동일하게 지정할 수 있음