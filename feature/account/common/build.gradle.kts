plugins {
    id(ThunderbirdPlugins.Library.androidCompose)
}

android {
    namespace = "app.k9mail.feature.account.common"
    resourcePrefix = "account_common_"
}

dependencies {
    implementation(projects.core.common)

    // The shared About dialog and Licences list (Mudita's MMD). Compose Material 3 comes from
    // Bolt's JetBrains artifact, so MMD's own copy of it is left out to avoid two of every class.
    implementation(libs.jetbrains.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.mmd) {
        exclude(group = "androidx.compose.material3", module = "material3")
    }

    implementation(projects.mail.common)

    testImplementation(projects.core.ui.compose.testing)
}

codeCoverage {
    branchCoverage = 0
    lineCoverage = 0
}
