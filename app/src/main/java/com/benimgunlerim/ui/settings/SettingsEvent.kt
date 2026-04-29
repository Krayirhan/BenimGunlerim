package com.benimgunlerim.ui.settings

sealed class SettingsEvent {
    object DataCleared : SettingsEvent()
    object ExportFailed : SettingsEvent()
    object ExportSaved : SettingsEvent()
    object ExportWriteFailed : SettingsEvent()
    object ImportReadFailed : SettingsEvent()
    object ImportParseFailed : SettingsEvent()
    data class ImportSuccess(val tasks: Int, val routines: Int) : SettingsEvent()
}
