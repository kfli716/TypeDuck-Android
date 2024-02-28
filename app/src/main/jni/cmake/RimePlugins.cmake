# if you want to add some new plugins, add them to librime_jni/rime_jni.cc too
set(RIME_PLUGINS
  rime-dictionary-lookup-filter
)

# plugins didn't use target_link_libraries, the usage-requirements won't work, include manually
set(PLUGIN_INCLUDES "")
find_package(Boost)
foreach(boost_lib ${Boost_LIBRARIES})
  unset(includes)
  get_target_property(includes ${boost_lib} INTERFACE_INCLUDE_DIRECTORIES)
  list(APPEND PLUGIN_INCLUDES ${includes})
endforeach()
include_directories(${PLUGIN_INCLUDES})

# move plugins
file(GLOB old_plugin_files "librime/plugins/*")
foreach(file ${old_plugin_files})
  if(IS_DIRECTORY ${file}) # plugin is directory
    file(REMOVE "${file}")
  endif()
endforeach()
foreach(plugin ${RIME_PLUGINS})
  execute_process(COMMAND ln -s 
    "${CMAKE_SOURCE_DIR}/${plugin}"
    "${CMAKE_SOURCE_DIR}/librime/plugins"
  )
endforeach()
