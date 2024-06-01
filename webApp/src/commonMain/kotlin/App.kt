import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Operation
import confetti.web.GetConferencesQuery
import kotlinx.coroutines.flow.Flow

@Composable
fun App() {
    MaterialTheme {

        Box(Modifier.fillMaxWidth().fillMaxHeight()) {
            Box(Modifier.align(Alignment.Center)) {
                val responseState = remember { conferenceData() }.collectAsState(null)

                val response = responseState.value
                when {
                    response == null -> {
                        CircularProgressIndicator()
                    }
                    response.data != null -> {
                        ConferenceList(response.data!!.conferences)
                    }
                    else -> {
                        response.toException().printStackTrace()
                        Text("Oops something went wrong: ${response.toException().message}")
                    }
                }
            }
        }
    }
}

fun <D: Operation.Data> ApolloResponse<D>.toException(): Exception {
    when  {
        exception != null -> return exception!!
        else -> return Exception("no data and no exception")
    }
}

@Composable
fun ConferenceList(conferences: List<GetConferencesQuery.Conference>) {
    LazyColumn(Modifier.width(400.dp)) {
        items(conferences.size) {
            val conference = conferences.get(it)
            Row {
                Text(conference.name, Modifier.weight(1.0f))
                Text(conference.days.first().toString())
            }
        }
    }
}
fun conferenceData(): Flow<ApolloResponse<GetConferencesQuery.Data>> {
    return ApolloClient.Builder()
        .serverUrl("https://confetti-app.dev/graphql")
        .build()
        .query(GetConferencesQuery())
        .toFlow()
}