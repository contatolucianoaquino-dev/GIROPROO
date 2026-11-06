package com.giropro.feature

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.giropro.GiroProApp

/** Acessa o container de dependências a partir do Application dentro de um ViewModel. */
fun CreationExtras.container(): GiroProApp.Container =
    (this[APPLICATION_KEY] as GiroProApp).container

val GiroVmFactory: ViewModelProvider.Factory = viewModelFactory {
    initializer { com.giropro.feature.dashboard.DashboardViewModel(container()) }
    initializer { com.giropro.feature.calculator.AnalyzeViewModel(container()) }
    initializer { com.giropro.feature.vehicle.VehicleViewModel(container()) }
    initializer { com.giropro.feature.config.ConfigViewModel(container()) }
    initializer { com.giropro.feature.history.HistoryViewModel(container()) }
}
