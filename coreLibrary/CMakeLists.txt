# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)
# add_subdirectory(${CMAKE_SOURCE_DIR}/src/main/cpp/opengles)

# message(STATUS "cmake_source_dir = " + ${CMAKE_SOURCE_DIR})

#add_compile_options(-std=c++11)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

set(libs "${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}")
message(STATUS "libs dir:${libs}")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds it for you.
# Gradle automatically packages shared libraries with your APK.
include_directories(src/main/cpp/include)
include_directories(src/main/cpp/include/libyuv)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds it for you.
# Gradle automatically packages shared libraries with your APK.

add_library(
             OrbbecGLView
             SHARED
             IMPORTED
 )
set_target_properties(OrbbecGLView
                      PROPERTIES IMPORTED_LOCATION
                       ../../../../libs/armeabi-v7a/libObGLView.so)

add_library(OpenNI2
            SHARED
            IMPORTED
)

set_target_properties(OpenNI2
                       PROPERTIES IMPORTED_LOCATION
                       ../../../../libs/armeabi-v7a/libOpenNI2.so)


add_library(
             libyuv
             SHARED
             IMPORTED
 )
set_target_properties(libyuv
                      PROPERTIES IMPORTED_LOCATION
                       ../../../../libs/armeabi-v7a/libyuv.so)

add_library(c SHARED IMPORTED)
set_target_properties(c PROPERTIES IMPORTED_LOCATION ${libs}/libc.so)

add_library(dl SHARED IMPORTED)
set_target_properties(dl PROPERTIES IMPORTED_LOCATION ${libs}/libdl.so)

add_library(dlib SHARED IMPORTED)
set_target_properties(dlib PROPERTIES IMPORTED_LOCATION ${libs}/libdlib.so)

add_library(headsurface SHARED IMPORTED)
set_target_properties(headsurface PROPERTIES IMPORTED_LOCATION ${libs}/libheadsurface.so)

add_library(log SHARED IMPORTED)
set_target_properties(log PROPERTIES IMPORTED_LOCATION ${libs}/liblog.so)

add_library(m SHARED IMPORTED)
set_target_properties(m PROPERTIES IMPORTED_LOCATION ${libs}/libm.so)

add_library(opencv_calib3d SHARED IMPORTED)
set_target_properties(opencv_calib3d PROPERTIES IMPORTED_LOCATION ${libs}/libopencv_calib3d.so)

add_library(opencv_core SHARED IMPORTED)
set_target_properties(opencv_core PROPERTIES IMPORTED_LOCATION ${libs}/libopencv_core.so)

add_library(opencv_features2d SHARED IMPORTED)
set_target_properties(opencv_features2d PROPERTIES IMPORTED_LOCATION ${libs}/libopencv_features2d.so)

add_library(opencv_flann SHARED IMPORTED)
set_target_properties(opencv_flann PROPERTIES IMPORTED_LOCATION ${libs}/libopencv_flann.so)

add_library(opencv_highgui SHARED IMPORTED)
set_target_properties(opencv_highgui PROPERTIES IMPORTED_LOCATION ${libs}/libopencv_highgui.so)

add_library(opencv_imgproc SHARED IMPORTED)
set_target_properties(opencv_imgproc PROPERTIES IMPORTED_LOCATION ${libs}/libopencv_imgproc.so)

add_library(OpenMeshTools SHARED IMPORTED)
set_target_properties(OpenMeshTools PROPERTIES IMPORTED_LOCATION ${libs}/libOpenMeshTools.so)

add_library(OpenMeshCore SHARED IMPORTED)
set_target_properties(OpenMeshCore PROPERTIES IMPORTED_LOCATION ${libs}/libOpenMeshCore.so)

add_library(png16 SHARED IMPORTED)
set_target_properties(png16 PROPERTIES IMPORTED_LOCATION ${libs}/libpng16.so)
# add_subdirectory(${CMAKE_SOURCE_DIR}/src/main/cpp/libpng)

add_library(tbb SHARED IMPORTED)
set_target_properties(tbb PROPERTIES IMPORTED_LOCATION ${libs}/libtbb.so)

add_library(wrapx SHARED IMPORTED)
set_target_properties(wrapx PROPERTIES IMPORTED_LOCATION ${libs}/libwrapx.so)

add_library(z SHARED IMPORTED)
set_target_properties(z PROPERTIES IMPORTED_LOCATION ${libs}/libz.so)

add_library( # Sets the name of the library.
             OrbbecUtils

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             # Associated headers in the same location as their source
             # file are automatically included.
             src/main/cpp/OrbbecUtils.cpp )



# Searches for a specified prebuilt library and stores the path as a
# variable. Because system libraries are included in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in the
# build script, prebuilt third-party libraries, or system libraries.


target_link_libraries( # Specifies the target library.
                       OrbbecUtils
                       OpenNI2
                       jnigraphics
                       libyuv
                       OrbbecGLView
                       c
                       dl
                       dlib
                       headsurface
                       log
                       m
                       opencv_calib3d
                       opencv_core
                       opencv_highgui
                       opencv_imgproc
                       OpenMeshTools
                       OpenMeshCore
                       png16
                       wrapx
                       #OrbbecGLView
                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )

