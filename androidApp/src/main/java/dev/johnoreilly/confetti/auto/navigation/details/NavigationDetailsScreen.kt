package dev.johnoreilly.confetti.auto.navigation.details

import android.location.Geocoder
import android.location.Location
import android.text.SpannableString
import android.text.Spanned
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarColor
import androidx.car.app.model.CarIcon
import androidx.car.app.model.CarLocation
import androidx.car.app.model.Distance
import androidx.car.app.model.DistanceSpan
import androidx.car.app.model.ForegroundCarColorSpan
import androidx.car.app.model.ItemList
import androidx.car.app.model.Place
import androidx.car.app.model.PlaceListMapTemplate
import androidx.car.app.model.PlaceMarker
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat
import dev.johnoreilly.confetti.GetConferencesQuery
import dev.johnoreilly.confetti.GetVenueQuery
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.auto.sessions.SessionsScreen
import dev.johnoreilly.confetti.auto.utils.METERS_TO_KMS
import dev.johnoreilly.confetti.auto.utils.getAddressForLocation
import dev.johnoreilly.confetti.auto.utils.navigateTo
import org.koin.core.component.KoinComponent
import kotlin.math.roundToInt

class NavigationDetailsScreen(
    carContext: CarContext,
    val data: Pair<GetConferencesQuery.Conference, GetVenueQuery.Venue>,
    private val location: Location?
) : Screen(carContext), KoinComponent {

    private var mGeocoder: Geocoder = Geocoder(carContext)

    override fun onGetTemplate(): Template {
        val listBuilder = showPOIDetails(data)
        val items = listBuilder.build()

        val anchor = getAnchorLocation(data.second)

        return PlaceListMapTemplate.Builder().apply {
            setTitle(data.first.name)
            setHeaderAction(Action.BACK)
            setAnchor(anchor)
            setCurrentLocationEnabled(true)
            setItemList(items)
        }.build()
    }

    private fun showPOIDetails(
        data: Pair<GetConferencesQuery.Conference, GetVenueQuery.Venue>
    ) : ItemList.Builder {
        val listBuilder = ItemList.Builder()

        val conference = data.first
        val venue = data.second

        val venueLocation = Location(venue.name)
        venueLocation.latitude = venue.latitude ?: 0.0
        venueLocation.longitude = venue.longitude ?: 0.0

        val venueAddress = getAddressForLocation(mGeocoder, venueLocation)

        val distanceMeters = location?.distanceTo(venueLocation)?.roundToInt() ?: 0
        val distanceKm: Int = distanceMeters / METERS_TO_KMS

        val description = SpannableString("   \u00b7 " + venue.name)
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
        description.setSpan(
            venueAddress,
            0,
            1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        listBuilder.addItem(
            Row.Builder()
                .setOnClickListener {
                    screenManager.push(SessionsScreen(carContext, conference.id))
                }
                .setTitle(venue.name)
                .addText(description)
                .setBrowsable(false)
                .build())

        listBuilder.addItem(
            Row.Builder()
                .setOnClickListener {
                    navigateTo(carContext, venue.latitude ?: 0.0, venue.longitude ?: 0.0)
                }
                .setTitle(carContext.getString(R.string.auto_navigate_to))
                .setBrowsable(true)
                .setImage(
                    CarIcon.Builder(
                        IconCompat.createWithResource(carContext, R.drawable.ic_outlined_navigation),
                    ).build(),
                    Row.IMAGE_TYPE_ICON
                )
                .build()
        )

        return listBuilder
    }

    private fun getAnchorLocation(venue: GetVenueQuery.Venue): Place {
        return Place.Builder(
            CarLocation.create(venue.latitude ?: 0.0, venue.longitude ?: 0.0)
        )
            .setMarker(PlaceMarker.Builder().setColor(CarColor.BLUE).build())
            .build()
    }
}