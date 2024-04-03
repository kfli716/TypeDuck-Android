# Since OpenCC doesn't include its headers in the binary dir,
# we need to install them manually.
file(GLOB LIBOPENCC_HEADERS
  librime/deps/opencc/src/*.hpp
  "${CMAKE_BINARY_DIR}/librime/deps/opencc/src/opencc_config.h"
)
file(COPY ${LIBOPENCC_HEADERS} DESTINATION "${CMAKE_BINARY_DIR}/include/opencc")
