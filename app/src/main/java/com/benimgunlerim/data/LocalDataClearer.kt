package com.benimgunlerim.data

/** Thin interface so SettingsViewModel can clear app data. */
interface LocalDataClearer {
    suspend fun clearAllLocalData()
}
