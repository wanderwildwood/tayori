plugins {
    id(ThunderbirdPlugins.Library.jvm)
    alias(libs.plugins.android.lint)
}

dependencies {
    api(projects.feature.autodiscovery.autoconfig)

    testImplementation(projects.core.logging.testing)
    testImplementation(projects.legacy.logging)
    testImplementation(libs.kxml2)
}

codeCoverage {
    branchCoverage = 64
    lineCoverage = 60
}
