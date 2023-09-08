package hk.eduhk.typeduck.util

import hk.eduhk.typeduck.BuildConfig

object Const {
    const val buildGitHash = BuildConfig.BUILD_GIT_HASH
    const val displayVersionName = "${BuildConfig.BUILD_VERSION_NAME}-${BuildConfig.BUILD_TYPE}"
    const val originalGitRepo = "https://github.com/TypeDuck-HK/TypeDuck-Android"
    const val currentGitRepo = BuildConfig.BUILD_GIT_REPO
}
