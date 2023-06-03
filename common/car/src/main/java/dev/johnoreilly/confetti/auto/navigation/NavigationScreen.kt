package dev.johnoreilly.confetti.auto.navigation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.text.SpannableString
import android.text.Spanned
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarColor
import androidx.car.app.model.CarLocation
import androidx.car.app.model.Distance
import androidx.car.app.model.DistanceSpan
import androidx.car.app.model.ForegroundCarColorSpan
import androidx.car.app.model.ItemList
import androidx.car.app.model.Metadata
import androidx.car.app.model.Place
import androidx.car.app.model.PlaceListMapTemplate
import androidx.car.app.model.PlaceMarker
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.core.content.ContextCompat
import dev.johnoreilly.confetti.ConferencesVenueComponent
import dev.johnoreilly.confetti.DefaultConferencesVenueComponent
import dev.johnoreilly.confetti.GetConferencesQuery
import dev.johnoreilly.confetti.GetVenueQuery
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.auto.navigation.details.NavigationDetailsScreen
import dev.johnoreilly.confetti.auto.sessions.SessionsScreen
import dev.johnoreilly.confetti.auto.ui.ErrorScreen
import dev.johnoreilly.confetti.auto.utils.METERS_TO_KMS
import dev.johnoreilly.confetti.auto.utils.defaultComponentContext
import kotlin.math.roundToInt

class NavigationScreen(
    carContext: CarContext,
) : Screen(carContext) {

    private var defaultLocation: Location? = null

    private val component: ConferencesVenueComponent =
        DefaultConferencesVenueComponent(
            componentContext = defaultComponentContext(),
            onConferenceSelected = { id -> screenManager.push(SessionsScreen(carContext, id)) },
        )

    init {
        component.uiState.subscribe { invalidate() }
    }

    override fun onGetTemplate(): Template {
        val result = component.uiState.value

        if (defaultLocation == null) {
            setLocation()
        }

        var listBuilder = ItemList.Builder()
        val loading = when (result) {
            ConferencesVenueComponent.Loading -> {
                true
            }

            ConferencesVenueComponent.Error -> {
                return ErrorScreen(carContext, R.string.auto_conferences_failed).onGetTemplate()
            }

            is ConferencesVenueComponent.Success -> {
                listBuilder = createPOIList(result.data)
                false
            }
        }

        val items = listBuilder.build()
        val anchor = getAnchorLocation()

        return PlaceListMapTemplate.Builder().apply {
            setTitle(carContext.getString(R.string.app_name))
            setHeaderAction(Action.APP_ICON)
            setLoading(loading)
            setAnchor(anchor)
            setCurrentLocationEnabled(true)
            if (!loading) {
                setItemList(items)
            }
        }.build()
    }

    private fun createPOIList(
        data: Map<GetConferencesQuery.Conference, GetVenueQuery.Venue?>
    ): ItemList.Builder {
        val listBuilder = ItemList.Builder()

        for (conference in data.keys) {

            val venue = data[conference]

            val venueLocation = Location(venue?.name ?: conference.name)
            venueLocation.latitude = venue?.latitude ?: 0.0
            venueLocation.longitude = venue?.longitude ?: 0.0

            val distanceMeters = defaultLocation?.distanceTo(venueLocation)?.roundToInt() ?: 0
            val distanceKm: Int = distanceMeters / METERS_TO_KMS

            val description = SpannableString("  \u00b7 " + (venue?.name ?: carContext.getString(R.string.auto_placeholder)))
            description.setSpan(
                DistanceSpan.create(Distance.create(distanceKm.toDouble(), Distance.UNIT_KILOMETERS)),
                0,
                1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            description.setSpan(
                ForegroundCarColorSpan.create(CarColor.BLUE),
                0,
                1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            listBuilder.addItem(
                Row.Builder()
                    .setOnClickListener {
                        if (venue != null) {
                            screenManager.push(NavigationDetailsScreen(carContext, Pair(conference, venue), defaultLocation))
                        }
                    }
                    .setTitle(conference.name)
                    .addText(description)
                    .setBrowsable(true)
                    .setMetadata(
                        Metadata.Builder()
                            .setPlace(
                                Place.Builder(
                                    CarLocation.create(venueLocation)
                                )
                                    .setMarker(PlaceMarker.Builder().build())
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
        }


        return listBuilder
    }

    private fun getAnchorLocation(): Place {
        return Place.Builder(
            CarLocation.create(defaultLocation?.latitude ?: 0.0, defaultLocation?.longitude ?: 0.0)
        )
            .setMarker(PlaceMarker.Builder().setColor(CarColor.BLUE).build())
            .build()
    }

    private fun setLocation() {
        if (ContextCompat.checkSelfPermission(carContext, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val locationManager = carContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            defaultLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 1000, 1f
            ) {
                if (defaultLocation?.latitude == it.latitude &&
                    defaultLocation?.longitude == it.longitude) {
                    return@requestLocationUpdates
                }

                defaultLocation = it
                invalidate()
            }
        }
    }
}