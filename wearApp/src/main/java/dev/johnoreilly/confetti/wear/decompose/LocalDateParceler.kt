package dev.johnoreilly.confetti.wear.decompose

import android.os.Parcel
import kotlinx.parcelize.Parceler
import kotlinx.datetime.LocalDate

object LocalDateParceler : Parceler<LocalDate?> {
    override fun create(parcel: Parcel) = parcel.readNullable {
        LocalDate.fromEpochDays(parcel.readInt())
    }

    override fun LocalDate?.write(parcel: Parcel, flags: Int) {
        parcel.writeNullable(this) {
            parcel.writeInt(toEpochDays())
        }
    }
}

inline fun <T> Parcel.readNullable(reader: () -> T) =
    if (readInt() != 0) reader() else null

inline fun <T> Parcel.writeNullable(value: T?, writer: T.() -> Unit) {
    if (value != null) {
        writeInt(1)
        value.writer()
    } else {
        writeInt(0)
    }
}
