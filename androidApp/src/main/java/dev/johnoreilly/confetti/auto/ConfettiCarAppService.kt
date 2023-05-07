package dev.johnoreilly.confetti.auto

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.SessionInfo
import androidx.car.app.validation.HostValidator
import dev.johnoreilly.confetti.R

//class ConfettiCarAppService: CarAppService() {
//
//    override fun createHostValidator(): HostValidator {
//        return HostValidator.Builder(applicationContext)
//            .addAllowedHosts(R.array.hosts_allowlist)
//            .build()
//    }
//
//    override fun onCreateSession(sessionInfo: SessionInfo): Session {
//        return object : Session() {
//
//            override fun onCreateScreen(intent: Intent): Screen {
//                return ConferencesScreen(carContext)
//            }
//        }
//    }
//}