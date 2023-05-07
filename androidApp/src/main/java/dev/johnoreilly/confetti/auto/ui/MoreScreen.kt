package dev.johnoreilly.confetti.auto.ui

import android.content.Intent
import android.net.Uri
import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.CarToast.LENGTH_SHORT
import androidx.car.app.HostException
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.auth.User


//class MoreScreen(
//    carContext: CarContext,
//    private val conference: String,
//    private val user: User?,
//    private val venueLat: Double?,
//    private val venueLon: Double?,
//) : Screen(carContext) {
//
//    override fun onGetTemplate(): Template {
//        val listBuilder = ItemList.Builder()
//        listBuilder.addItem(
//            Row.Builder()
//                .setTitle(carContext.getString(R.string.speakers))
//                .setOnClickListener { screenManager.push(SpeakersScreen(carContext, conference)) }
//                .build()
//        )
//
//        listBuilder.addItem(
//            Row.Builder()
//                .setTitle(carContext.getString(R.string.bookmarks))
//                .setOnClickListener { screenManager.push(
//                    BookmarksScreen(
//                        carContext,
//                        user,
//                        conference
//                    )) }
//                .build()
//        )
//
//        listBuilder.addItem(
//            Row.Builder()
//                .setTitle(carContext.getString(R.string.search))
//                .setOnClickListener { screenManager.push(SearchScreen(carContext, conference)) }
//                .build()
//        )
//
//        if (venueLat != null && venueLon != null) {
//            listBuilder.addItem(
//                Row.Builder()
//                    .setTitle(carContext.getString(R.string.auto_navigate_to))
//                    .setOnClickListener { navigateTo(venueLat, venueLon) }
//                    .build()
//            )
//        }
//
//        val isAuthenticated = user != null
//        listBuilder.addItem(
//            Row.Builder()
//                .setTitle(carContext.getString(
//                    if (isAuthenticated) {
//                        R.string.auto_sign_out
//                    } else {
//                        R.string.auto_sign_in
//                    }
//                ))
//                .setOnClickListener { screenManager.push(
//                    SignInScreen(
//                        carContext,
//                        isAuthenticated
//                    )
//                ) }
//                .build()
//        )
//
//        return ListTemplate.Builder().apply {
//            setTitle(carContext.getString(R.string.auto_more))
//            setHeaderAction(Action.BACK)
//            setLoading(false)
//            setSingleList(listBuilder.build())
//        }.build()
//    }
//
//    private fun navigateTo(latitude: Double, longitude: Double) {
//        val uri = Uri.parse("geo:0,0?q=$latitude,$longitude")
//        val intent = Intent(CarContext.ACTION_NAVIGATE, uri)
//
//        try {
//            carContext.startCarApp(intent)
//        } catch (e: HostException) {
//            CarToast.makeText(
//                carContext,
//                carContext.getString(R.string.auto_navigate_to_failed),
//                LENGTH_SHORT
//            ).show()
//        }
//    }
//}