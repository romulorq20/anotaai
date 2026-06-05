cmake_minimum_required(VERSION 3.22)
project(native-libs)

# Set the C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Enable 16 KB page size support
# This ensures proper alignment for Android 15+ devices
# Starting with Android 15 (API 35), the default page size can be 16KB
if(ANDROID_PLATFORM_LEVEL GREATER_EQUAL 35)
    add_compile_options(-fno-addrsig)
    add_link_options("-Wl,-z,max-page-size=16384")
endif()

# Add any native libraries here
# Example: add_library(image_processing_util_jni SHARED src/main/cpp/image_processing_util.cpp)
