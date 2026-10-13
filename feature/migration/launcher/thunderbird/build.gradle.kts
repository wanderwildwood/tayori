plugins {
    id(ThunderbirdPlugins.Library.android)
}

android {
    namespace = "app.k9mail.feature.migration.launcher.thunderbird"
}

dependencies {
    implementation(projects.feature.migration.launcher.api)
    implementation(libs.androidx.activity)
}

codeCoverage {
    lineCoverage = 0
}
