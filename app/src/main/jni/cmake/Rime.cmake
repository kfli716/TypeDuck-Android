option(BUILD_TEST "" OFF)
option(BUILD_STATIC "" ON)
add_subdirectory(librime)
target_compile_options(rime-static PRIVATE "-ffile-prefix-map=${CMAKE_CURRENT_SOURCE_DIR}=.")

# plugins didn't use target_link_libraries, the usage-requirements won't work, include manually
set(PLUGIN_INCLUDES "")
find_package(Boost)
foreach(boost_lib ${Boost_LIBRARIES})
  unset(includes)
  get_target_property(includes ${boost_lib} INTERFACE_INCLUDE_DIRECTORIES)
  list(APPEND PLUGIN_INCLUDES ${includes})
endforeach()
include_directories(${PLUGIN_INCLUDES})
