package dev.johnoreilly.confetti.backend.import

import GridTable
import dev.johnoreilly.confetti.backend.datastore.ConferenceId
import dev.johnoreilly.confetti.backend.datastore.DConfig
import dev.johnoreilly.confetti.backend.datastore.DLink
import dev.johnoreilly.confetti.backend.datastore.DPartner
import dev.johnoreilly.confetti.backend.datastore.DPartnerGroup
import dev.johnoreilly.confetti.backend.datastore.DRoom
import dev.johnoreilly.confetti.backend.datastore.DSession
import dev.johnoreilly.confetti.backend.datastore.DSpeaker
import dev.johnoreilly.confetti.backend.datastore.DVenue
import dev.johnoreilly.confetti.backend.datastore.DataStore
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import net.mbonnin.bare.graphql.asBoolean
import net.mbonnin.bare.graphql.asList
import net.mbonnin.bare.graphql.asMap
import net.mbonnin.bare.graphql.asString
import net.mbonnin.bare.graphql.cast

object Sessionize {
    private val devFestStockholm2023 = "https://sessionize.com/api/v2/nt4ryvlm/view/all"
    private val droidConLondon2022 = "https://sessionize.com/api/v2/qi0g29hw/view/All"
    private val kotlinConf2023 = "https://sessionize.com/api/v2/rje6khfn/view/All"
    private val androidMakers2023 = "https://sessionize.com/api/v2/72i2tw4v/view/All"
    private val droidconNYC2023 = "https://sessionize.com/api/v2/gxz4vyyr/view/All"

    data class SessionizeData(
        val rooms: List<DRoom>,
        val sessions: List<DSession>,
        val speakers: List<DSpeaker>,
    )

    suspend fun importAndroidMakers2024(): Int {
        return writeData(
            getData(
                url = "https://sessionize.com/api/v2/ok1n6jgj/view/All",
                gridSmartUrl = "https://sessionize.com/api/v2/ok1n6jgj/view/GridSmart",
            ),
            config = DConfig(
                id = ConferenceId.AndroidMakers2024.id,
                name = "AndroidMakers by droidcon 2024",
                timeZone = "Europe/Paris",
                themeColor = "0xffFB5C49"
            ),
            venue = DVenue(
                id = "conference",
                name = "Beffroi de Montrouge",
                address = "Av. de la République, 92120 Montrouge",
                description = mapOf(
                    "en" to "Cool venue",
                    "fr" to "Venue fraiche",
                ),
                latitude = 48.8188958,
                longitude = 2.3193016,
                imageUrl = "https://www.beffroidemontrouge.com/wp-content/uploads/2019/09/moebius-1.jpg",
                floorPlanUrl = "https://storage.googleapis.com/androidmakers-static/floor_plan.png"
            ),
            partnerGroups = listOf(
                DPartnerGroup(
                    key = "gold",
                    partners = listOf(
                        DPartner(
                            name = "RevenueCat",
                            logoUrl = "https://storage.googleapis.com/androidmakers-static/partners/revenuecat.png",
                            logoUrlDark = "https://storage.googleapis.com/androidmakers-static/partners/revenuecat_dark.png",
                            url = "https://www.revenuecat.com"
                        )
                    )
                ),
                DPartnerGroup(
                    key = "silver",
                    partners = listOf(
                        DPartner(
                            name = "bitrise",
                            logoUrl = "https://storage.googleapis.com/androidmakers-static/partners/bitrise.png",
                            logoUrlDark = "https://storage.googleapis.com/androidmakers-static/partners/bitrise_dark.png",
                            url = "https://bitrise.io/?utm_source=sponsorship&utm_medium=referral&utm_campaign=androidmakers-paris-2024&utm_content=droidcon-homepage"
                        ),
                        DPartner(
                            name = "zimperium",
                            logoUrl = "https://storage.googleapis.com/androidmakers-static/partners/zimperium.png",
                            logoUrlDark = "https://storage.googleapis.com/androidmakers-static/partners/zimperium_dark.png",
                            url = "https://www.zimperium.com/"
                        )
                    )
                ),
                DPartnerGroup(
                    key = "bronze",
                    partners = listOf(
                        DPartner(
                            name = "appvestor",
                            logoUrl = "https://storage.googleapis.com/androidmakers-static/partners/appvestor.png",
                            logoUrlDark = "https://storage.googleapis.com/androidmakers-static/partners/appvestor_dark.png",
                            url = "https://appvestor.com/"
                        ),
                        DPartner(
                            name = "koin",
                            logoUrl = "https://storage.googleapis.com/androidmakers-static/partners/koin.png",
                            logoUrlDark = "https://storage.googleapis.com/androidmakers-static/partners/koin_dark.png",
                            url = "https://www.kotzilla.io/"
                        ),
                        DPartner(
                            name = "runway",
                            logoUrl = "https://storage.googleapis.com/androidmakers-static/partners/runway.png",
                            logoUrlDark = "https://storage.googleapis.com/androidmakers-static/partners/runway_dark.png",
                            url = "https://runway.team/"
                        ),
                        DPartner(
                            name = "yavin",
                            logoUrl = "https://storage.googleapis.com/androidmakers-static/partners/yavin.png",
                            logoUrlDark = "https://storage.googleapis.com/androidmakers-static/partners/yavin_dark.png",
                            url = "https://yavin.com/"
                        ),
                    )
                ),
                DPartnerGroup(
                    key = "startup",
                    partners = listOf(
                        DPartner(
                            name = "screenshotbot",
                            logoUrl = "https://storage.googleapis.com/androidmakers-static/partners/screenshotbot.png",
                            logoUrlDark = "https://storage.googleapis.com/androidmakers-static/partners/screenshotbot_dark.png",
                            url = "https://screenshotbot.io/"
                        )
                    )
                ),
                DPartnerGroup(
                    key = "lanyard",
                    partners = listOf(
                        DPartner(
                            name = "amo",
                            logoUrl = "https://storage.googleapis.com/androidmakers-static/partners/amo.png",
                            logoUrlDark = "https://storage.googleapis.com/androidmakers-static/partners/amo_dark.png",
                            url = "amo.co"
                        )
                    )
                ),
                DPartnerGroup(
                    key = "community",
                    partners = listOf(
                        DPartner(
                            name = "DevCafé",
                            logoUrl = "https://storage.googleapis.com/androidmakers-static/partners/devcafe.png",
                            logoUrlDark = "https://storage.googleapis.com/androidmakers-static/partners/devcafe_dark.png",
                            url = "https://twitter.com/DevCafeYt"
                        ),
                        DPartner(
                            name = "Groundbreaker",
                            logoUrl = "https://storage.googleapis.com/androidmakers-static/partners/groundbreaker.png",
                            logoUrlDark = "https://storage.googleapis.com/androidmakers-static/partners/groundbreaker_dark.png",
                            url = "https://groundbreaker.org/"
                        ),
                        DPartner(
                            name = "leboncoin",
                            logoUrl = "https://storage.googleapis.com/androidmakers-static/partners/leboncoin.png",
                            logoUrlDark = "https://storage.googleapis.com/androidmakers-static/partners/leboncoin_dark.png",
                            url = "https://medium.com/leboncoin-tech-blog"
                        ),
                        DPartner(
                            name = "stickermule",
                            logoUrl = "https://storage.googleapis.com/androidmakers-static/partners/stickermule.png",
                            logoUrlDark = "https://storage.googleapis.com/androidmakers-static/partners/stickermule_dark.png",
                            url = "https://www.stickermule.com/eu/custom-stickers"
                        ),
                        DPartner(
                            name = "WomenTechMakers",
                            logoUrl = "https://storage.googleapis.com/androidmakers-static/partners/womentechmakers.png",
                            logoUrlDark = "https://storage.googleapis.com/androidmakers-static/partners/womentechmakers_dark.png",
                            url = "https://www.linkedin.com/company/womentechmakers/"
                        ),
                        DPartner(
                            name = "Women who code",
                            logoUrl = "https://storage.googleapis.com/androidmakers-static/partners/women who code.png",
                            logoUrlDark = "https://storage.googleapis.com/androidmakers-static/partners/women who code_dark.png",
                            url = "https://womenwhocode.com/"
                        )
                    )

                )
            )
        )
    }

    suspend fun importDevFestVenice2023(): Int {
        return writeData(
            getData("https://sessionize.com/api/v2/idarcge5/view/All"),
            config = DConfig(
                id = ConferenceId.DevFestVenice2023.id,
                name = "DevFest Venice 2023",
                timeZone = "Europe/Rome",
                themeColor = "0xFF0000FF"
            ),
            venue = DVenue(
                id = "main",
                name = "Campus Scientifico Università Ca' Foscari",
                address = "Via Torino, 155 Mestre, Venezia VE, 30170",
                description = mapOf(
                    "en" to "Campus Scientifico Università Ca' Foscari",
                ),
                latitude = 45.4779997,
                longitude = 12.2551719,
                imageUrl = "https://live.staticflickr.com/65535/51266563060_17dacd7037_5k.jpg",
                floorPlanUrl = null,
            ),
        )
    }

    suspend fun importDevFestWarsaw2023(): Int {
        return writeData(
            getData("https://sessionize.com/api/v2/y1y2lmxn/view/All"),
            config = DConfig(
                id = ConferenceId.DevFestWarsaw2023.id,
                name = "DevFest Warsaw 2023",
                timeZone = "Europe/Warsaw",
                themeColor = "0xFFFF0000"
            ),
            venue = DVenue(
                id = "main",
                name = "Google for Startups Campus Warsaw",
                address = "Plac Konesera 10, 03-736 Warszawa",
                description = mapOf(
                    "en" to "Google for Startups Campus Warsaw",
                ),
                latitude = 52.2561388,
                longitude = 21.0453105,
                imageUrl = "https://i.postimg.cc/GmVdqZsq/campus-outside.jpg",
                floorPlanUrl = null
            )
        )
    }

    suspend fun importDevFestIreland2023(): Int {
        return writeData(
            getData("https://sessionize.com/api/v2/g3y2wy77/view/All"),
            config = DConfig(
                id = ConferenceId.DevFestIreland2023.id,
                name = "DevFest Ireland 2023",
                timeZone = "Europe/Dublin",
                themeColor = "0xFF009A44"
            ),
            venue = DVenue(
                id = "main",
                name = "Griffith College",
                address = "S Circular Rd, Dublin 8, D08 V04N",
                description = mapOf(
                    "en" to "",
                ),
                latitude = 53.3346241,
                longitude = -6.2798278,
                imageUrl = "https://www.griffith.ie/themes/custom/griffith2022/assets/logos/logo-2x-black.webp",
                floorPlanUrl = null
            )
        )
    }

    suspend fun importDevFestMelbourne2023(): Int {
        return writeData(
            getData("https://sessionize.com/api/v2/3ken899c/view/All"),
            config = DConfig(
                id = ConferenceId.DevFestMelbourne2023.id,
                name = "DevFest Melbourne 2023",
                timeZone = "Australia/Melbourne"
            ),
            venue = DVenue(
                id = "main",
                name = "REA Group, Melbourne",
                address = "511 Church St, Richmond VIC 3121",
                description = mapOf(
                    "en" to "REA Group, Melbourne",
                ),
                latitude = -37.8288482,
                longitude = 144.9974788,
                imageUrl = "https://secure.meetupstatic.com/photos/event/2/0/9/3/600_515708339.webp?w=750",
                floorPlanUrl = null
            )
        )
    }


    suspend fun importDevFestGeorgia2023(): Int {
        return writeData(
            getData("https://sessionize.com/api/v2/1ukaofb3/view/All"),
            config = DConfig(
                id = ConferenceId.DevFestGeorgia2023.id,
                name = "DevFest Georgia 2023",
                timeZone = "Asia/Tbilisi"
            ),
            venue = DVenue(
                id = "main",
                name = "Kakha Bendukidze Campus",
                address = "Kakha Bendukidze Campus",
                description = mapOf(
                    "en" to "Kakha Bendukidze Campus",
                    "fr" to "Kakha Bendukidze Campus.",
                ),
                latitude = 41.8057275,
                longitude = 44.76534,
                imageUrl = "https://media.licdn.com/dms/image/D4D22AQEPV8xCr3VbjQ/feedshare-shrink_800/0/1686483593031?e=2147483647&v=beta&t=D0SSUeoOA_08qlF4Ze65Tu4ol_ZP6oDycoeZHD26ueA",
                floorPlanUrl = null
            )
        )
    }

    suspend fun importDroidconLisbon2023(): Int {
        return writeData(
            getData("https://sessionize.com/api/v2/2mpjrh6b/view/All"),
            config = DConfig(
                id = ConferenceId.DroidConLisbon2023.id,
                name = "droidcon Lisbon",
                timeZone = "Europe/Lisbon"
            ),
            venue = DVenue(
                id = "main",
                name = "Fórum Cultural de Alcochete",
                address = "Fórum Cultural de Alcochete",
                description = mapOf(
                    "en" to "Fórum Cultural de Alcochete",
                    "fr" to "Fórum Cultural de Alcochete.",
                ),
                latitude = 38.7458978,
                longitude = -8.9743295,
                imageUrl = "https://static.wixstatic.com/media/eb9bc0_1da6dc27109b4624b43f71701f60e90e~mv2.jpg/v1/crop/x_0,y_622,w_4096,h_1828/fill/w_531,h_237,al_c,q_80,usm_0.66_1.00_0.01,enc_auto/forum%20cultural%20alcochete.jpg",
                floorPlanUrl = null
            )
        )
    }

    suspend fun importDroidconBerlin2023(): Int {
        return writeData(
            getData("https://sessionize.com/api/v2/axmfv7vn/view/All"),
            config = DConfig(
                id = ConferenceId.DroidconBerlin2023.id,
                name = "droidcon Berlin",
                timeZone = "Europe/Berlin"
            ),
            venue = DVenue(
                id = "main",
                name = "CityCube Berlin",
                address = "Messedamm 26, 14055 Berlin, Germany",
                description = mapOf(
                    "en" to "CityCube Berlin",
                    "fr" to "CityCube Berlin",
                ),
                latitude = 52.500218,
                longitude = 13.270753,
                imageUrl = "https://berlin.droidcon.com/wp-content/uploads/2022/05/CitycubeBW.png",
                floorPlanUrl = null
            )
        )
    }

    private val businessDesignCenter = DVenue(
        id = "main",
        name = "Business Design Center",
        address = "52 Upper St, London N1 0QH, United Kingdom",
        description = mapOf(
            "en" to "Cool venue",
            "fr" to "Venue fraiche",
        ),
        latitude = 51.5342463,
        longitude = -0.1068864,
        imageUrl = "https://london.droidcon.com/wp-content/uploads/sites/3/2022/07/Venue2-1.png",
        floorPlanUrl = null
    )

    suspend fun importDroidconLondon2023(): Int {
        return writeData(
            sessionizeData = GridTable.getData("64k7lmps"),
            config = DConfig(
                id = ConferenceId.DroidconLondon2023.id,
                name = "droidcon London 2023",
                timeZone = "Europe/London",
                days = listOf(
                    LocalDate(2023, 10, 26),
                    LocalDate(2023, 10, 27)
                ),
            ),
            venue = businessDesignCenter,
            partnerGroups = emptyList()
        )
    }

    suspend fun importDroidconSF2023(): Int {
        return writeData(
            sessionizeData = GridTable.getData("eewr8kdk"),
            config = DConfig(
                id = ConferenceId.DroidconSF2023.id,
                name = "droidcon San Francisco 2023",
                timeZone = "America/Los_Angeles",
                days = listOf(
                    LocalDate(2023, 6, 8),
                    LocalDate(2023, 6, 9)
                ),
            ),
            venue = DVenue(
                id = "main",
                name = "Mission Bay Conference Center",
                address = "1675 Owens Street, San Francisco, CA 94143-3008",
                latitude = 37.7679982,
                longitude = -122.3934354,
                description = emptyMap(),
                imageUrl = "https://www.nodesummit.com/wp-content/uploads/UCSF-Mission-Bay-Center_node-summit.jpg",
                floorPlanUrl = null
            ),
            partnerGroups = emptyList()
        )
    }

    suspend fun importDroidconNYC2023(): Int {
        return writeData(
            sessionizeData = getData(droidconNYC2023),
            config = DConfig(
                id = ConferenceId.DroidconNYC2023.id,
                name = "droidcon New York 2023",
                timeZone = "America/New_York",
                days = listOf(
                    LocalDate(2023, 9, 14),
                    LocalDate(2023, 9, 15)
                ),
                themeColor = "0xFFFFBE29"
            ),
            venue = DVenue(
                id = "main",
                name = "Jay Conference Bryant Park",
                address = "109 W 39th St, New York, NY 10018",
                latitude = 40.7533911,
                longitude = -73.9860439,
                description = emptyMap(),
                imageUrl = "https://nyc.droidcon.com/wp-content/uploads/sites/2/2023/04/nyc-hotel.png",
                floorPlanUrl = null
            ),
            partnerGroups = emptyList()
        )
    }

    suspend fun importDroidConLondon2022(): Int {
        return writeData(
            getData(droidConLondon2022),
            config = DConfig(
                id = ConferenceId.DroidConLondon2022.id,
                name = "droidcon London",
                timeZone = "Europe/London"
            ),
            venue = DVenue(
                id = "main",
                name = "Business Design Center",
                address = "52 Upper St, London N1 0QH, United Kingdom",
                description = mapOf(
                    "en" to "Cool venue",
                    "fr" to "Venue fraiche",
                ),
                latitude = 51.5342463,
                longitude = -0.1068864,
                imageUrl = "https://london.droidcon.com/wp-content/uploads/sites/3/2022/07/Venue2-1.png",
                floorPlanUrl = null
            )
        )
    }

    suspend fun importKotlinConf2023(): Int {
        return writeData(
            getData(kotlinConf2023).let {
                it.copy(sessions = it.sessions.filter { it.start.date.dayOfMonth != 12 })
            },
            config = DConfig(
                id = ConferenceId.KotlinConf2023.id,
                name = "KotlinConf 2023",
                timeZone = "Europe/Amsterdam"
            ),
            venue = businessDesignCenter
        )
    }

    private suspend fun getLinks(id: String): List<DLink> {
        val data =
            getJsonUrl("https://raw.githubusercontent.com/paug/AndroidMakersBackend/main/service-graphql/src/main/resources/links.json")

        return data.asMap.get(id)?.asList.orEmpty()
            .map {
                DLink(
                    it.asMap.get("type").asString,
                    it.asMap.get("url").asString
                )
            }
    }

    suspend fun importAndroidMakers2023(): Int {
        return writeData(
            getData(androidMakers2023, null, ::getLinks),
            config = DConfig(
                id = ConferenceId.AndroidMakers2023.id,
                name = "Android Makers by droidcon",
                timeZone = "Europe/Paris",
                days = listOf(LocalDate(2023, 4, 27), LocalDate(2023, 4, 28))
            ),
            venue = DVenue(
                id = "main",
                name = "Beffroi de Montrouge",
                address = "Av. de la République, 92120 Montrouge",
                description = mapOf(
                    "en" to "Cool venue",
                    "fr" to "Venue fraiche",
                ),
                latitude = 48.8188958,
                longitude = 2.3193016,
                imageUrl = "https://www.beffroidemontrouge.com/wp-content/uploads/2019/09/moebius-1.jpg",
                floorPlanUrl = null
            ),
            partnerGroups = partnerGroups("https://raw.githubusercontent.com/paug/AndroidMakersApp/ce800d6eefa4f83d34690161637d7f98918ee4a3/data/sponsors.json")
        )
    }

    suspend fun importDevFestStockholm2023(): Int {
        return writeData(
            getData(devFestStockholm2023),
            config = DConfig(
                id = ConferenceId.DevFestStockholm2023.id,
                name = "DevFest Stockholm 2023",
                timeZone = "Europe/Stockholm",
                themeColor = "0xFFFECC02"
            ),
            venue = DVenue(
                id = "main",
                name = "Google Stockholm",
                address = "Kungsbron 2, 111 22 Stockholm, Sweden",
                description = mapOf(
                    "en" to "Google Office",
                ),
                latitude = 59.333388,
                longitude = 18.0543053,
                imageUrl = "https://mir-s3-cdn-cf.behance.net/project_modules/2800_opt_1/045b9561443603.5a6f296b48565.jpg",
                floorPlanUrl = null
            ),
        )
    }

    suspend fun importDevFestSriLanka2023(): Int {
        return writeData(
            getData("https://sessionize.com/api/v2/zdn3ivmz/view/All"),
            config = DConfig(
                id = ConferenceId.DevFestSriLanka2023.id,
                name = "DevFest Sri Lanka 2023",
                timeZone = "Asia/Colombo",
                themeColor = "0xFFFFBE29"
            ),
            venue = DVenue(
                id = "main",
                name = "Bishop's College Auditorium, Colombo",
                address = "WV83+2V5, Boyd Pl, Colombo, Sri Lanka",
                description = mapOf(
                    "en" to "Google Office",
                ),
                latitude = 6.915,
                longitude = 79.854722,
                imageUrl = "https://www.bcauditorium.lk/wp-content/uploads/2019/08/front-nuga-tree2.jpg",
                floorPlanUrl = null
            ),
        )
    }

    suspend fun importDroidconAmman2024(): Int {
        return writeData(
            getData("https://sessionize.com/api/v2/83eexqxt/view/All"),
            config = DConfig(
                id = ConferenceId.DroidconAmman2024.id,
                name = "Droidcon Amman 2024",
                timeZone = "Asia/Amman",
                themeColor = "0xFF008000"
            ),
            venue = DVenue(
                id = "main",
                name = "Le Royal Hotels & Resorts",
                address = "XW35+9J Amman, Jordan",
                description = emptyMap(),
                latitude = 31.95338374344055,
                longitude = 35.90903552574762,
                imageUrl = "https://assets-global.website-files.com/6087d683edfb0155b650657d/6305d4a3712874801d149634_website-01.jpg",
                floorPlanUrl = null
            ),
        )
    }

    suspend fun importSheDevWarsaw2024(): Int {
        return writeData(
            getData("https://sessionize.com/api/v2/b0q6oh1z/view/All"),
            config = DConfig(
                id = ConferenceId.SheDevWarsaw2024.id,
                name = "SHEDEV Warsaw 2024",
                timeZone = "Europe/Warsaw",
                themeColor = "0xFF512DA8"
            ),
            venue = DVenue(
                id = "main",
                name = "Google for Startups Campus Warsaw",
                address = "Plac Konesera 10, 03-736 Warszawa",
                description = mapOf(
                    "en" to "Google for Startups Campus Warsaw",
                ),
                latitude = 52.2561388,
                longitude = 21.0453105,
                imageUrl = "https://i.postimg.cc/GmVdqZsq/campus-outside.jpg",
                floorPlanUrl = null
            ),
        )
    }

    internal fun writeData(
        sessionizeData: SessionizeData,
        config: DConfig,
        venue: DVenue,
        partnerGroups: List<DPartnerGroup> = emptyList()
    ): Int {
        return DataStore().write(
            sessions = sessionizeData.sessions.sortedBy { it.start },
            rooms = sessionizeData.rooms,
            speakers = sessionizeData.speakers,
            partnerGroups = partnerGroups,
            config = config,
            venues = listOf(venue)
        )
    }

    /**
     * @param gridSmartUrl extra json to get the service sessions from. The service sessions are not always in the View/All url
     */
    private suspend fun getData(
        url: String,
        gridSmartUrl: String? = null,
        linksFor: suspend ((String) -> List<DLink>) = { emptyList() }
    ): SessionizeData {
        val data = getJsonUrl(url)

        val categories = data.asMap["categories"].asList.map { it.asMap }
            .flatMap {
                it["items"].asList
            }.map {
                it.asMap
            }.map {
                it["id"] to it["name"]
            }.toMap()


        var sessions = getSessions(data!!, categories, linksFor)
        if (gridSmartUrl != null) {
            sessions = sessions + getServiceSessions(gridSmartUrl, categories, linksFor)
        }

        var rooms = data.asMap["rooms"].asList.map { it.asMap }.map {
            DRoom(
                id = it.get("id").toString(),
                name = it.get("name").asString
            )
        }
        if (rooms.isEmpty()) {
            rooms = sessions.flatMap { it.rooms }.distinct().map { DRoom(id = it, name = it) }
        }
        val speakers = data.asMap["speakers"].asList.map { it.asMap }.map {
            DSpeaker(
                id = it.get("id").asString,
                name = it.get("fullName").asString,
                photoUrl = it.get("profilePicture")?.asString,
                bio = it.get("bio")?.asString,
                tagline = it.get("tagLine")?.asString,
                city = null,
                company = null,
                companyLogoUrl = null,
                links = it.get("links").asList.map { it.asMap }.map {
                    DLink(
                        key = it.get("linkType").asString,
                        url = it.get("url").asString
                    )
                }
            )
        }

        return SessionizeData(
            rooms = rooms,
            sessions = sessions,
            speakers = speakers
        )
    }

    private suspend fun getServiceSessions(
        gridSmart: String,
        categories: Map<Any?, Any?>,
        linksFor: suspend (String) -> List<DLink>
    ): List<DSession> {
        val data = getJsonUrl(gridSmart)

        return data.asList.flatMap { it.asMap["rooms"].asList }
            .flatMap { it.asMap["sessions"].asList }
            .map {
                it.asMap
            }
            .mapNotNull {
                if ((it.get("isServiceSession") as? Boolean) != true) {
                    // Filter service sessions
                    return@mapNotNull null
                }
                if (it.get("startsAt") == null || it.get("endsAt") == null) {
                    /**
                     * Guard against sessions that are not scheduled.
                     */
                    return@mapNotNull null
                }
                val tags = it.get("categoryItems")?.asList.orEmpty().mapNotNull { categoryId ->
                    categories.get(categoryId)?.asString
                }
                DSession(
                    id = it.get("id").asString,
                    type = if (it.get("isServiceSession").cast()) "service" else "talk",
                    title = it.get("title").asString,
                    description = it.get("description")?.asString,
                    language = tags.toLanguage(),
                    start = it.get("startsAt").asString.let { LocalDateTime.parse(it) },
                    end = it.get("endsAt").asString.let { LocalDateTime.parse(it) },
                    complexity = null,
                    feedbackId = null,
                    tags = tags,
                    rooms = listOf(it.get("roomId").toString()),
                    speakers = it.get("speakers")?.asList.orEmpty().map { it.asMap["id"].asString },
                    shortDescription = null,
                    links = linksFor(it.get("id").asString),
                )
            }

    }

    private suspend fun getSessions(
        data: Any,
        categories: Map<Any?, Any?>,
        linksFor: suspend (String) -> List<DLink>
    ): List<DSession> {
        return data.asMap["sessions"].asList.map {
            it.asMap
        }.mapNotNull {
            if (it.get("startsAt") == null || it.get("endsAt") == null) {
                /**
                 * Guard against sessions that are not scheduled.
                 */
                return@mapNotNull null
            }
            val tags = it.get("categoryItems").asList.mapNotNull { categoryId ->
                categories.get(categoryId)?.asString
            }
            DSession(
                id = it.get("id").asString,
                type = if (it.get("isServiceSession").cast()) "service" else "talk",
                title = it.get("title").asString,
                description = it.get("description")?.asString,
                language = tags.toLanguage(),
                start = it.get("startsAt").asString.let { LocalDateTime.parse(it) },
                end = it.get("endsAt").asString.let { LocalDateTime.parse(it) },
                complexity = null,
                feedbackId = null,
                tags = tags,
                rooms = listOf(it.get("roomId").toString()),
                speakers = it.get("speakers").asList.map { it.asString },
                shortDescription = null,
                links = linksFor(it.get("id").asString),
            )
        }
    }
}

private fun List<String>.toLanguage(): String {
    return if(contains("French")) "French" else "English"
}
