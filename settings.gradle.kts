pluginManagement {
  repositories {
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
    resolutionStrategy {
      eachPlugin {
        if (requested.id.id == "com.google.devtools.ksp") {
          useModule("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:1.9.22-1.0.17")
        }
      }
    }
  }
  dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
      google()
      mavenCentral()
    }
  }

  rootProject.name = "BookApp"
  include(":app")
}
