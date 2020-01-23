package com.example.tsundoku

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.google.type.LatLng
import java.util.concurrent.TimeUnit

/*
 * This class contains the state of the game.  The two important pieces of state are the index
 * of the geofence, which is the geofence that the game thinks is active, and the state of the
 * hint being shown.  If the hint matches the geofence, then the Activity won't update the geofence
 * as it cycles through various activity states.
 *
 * These states are stored in SavedState, which matches the Android lifecycle.  Destroying the
 * associated Activity with the back action will delete all state and reset the game, while
 * the Home action will cause the state to be saved, even if the game is terminated by Android in
 * the background.
 *
 *
 *
 */

//A class that holds data that drives the view

class GeofenceViewModel(state: SavedStateHandle) : ViewModel() {
//    private val _geofenceIndex = state.getLiveData(GEOFENCE_INDEX_KEY, -1)
//    private val _hintIndex = state.getLiveData(HINT_INDEX_KEY, 0)
//    val geofenceIndex: LiveData<Int>
//        get() = _geofenceIndex
//
//    val geofenceHintResourceId = Transformations.map(geofenceIndex) {
//        val index = geofenceIndex?.value ?: -1
//        when {
//            index < 0 -> R.string.not_started_hint
//            index < GeofencingConstants.NUM_LANDMARKS -> GeofencingConstants.LANDMARK_DATA[geofenceIndex.value!!].hint
//            else -> R.string.geofence_over
//        }
//    }
//
////    val geofenceImageResourceId = Transformations.map(geofenceIndex) {
////        val index = geofenceIndex.value ?: -1
////        when {
////            index < GeofencingConstants.NUM_LANDMARKS -> R.drawable.android_map
////            else -> R.drawable.android_treasure
////        }
////    }
//
////    fun updateHint(currentIndex: Int) {
////        _hintIndex.value = currentIndex+1
////    }
////
////    fun geofenceActivated() {
////        _geofenceIndex.value = _hintIndex.value
////    }
//
////    fun geofenceIsActive() =_geofenceIndex.value == _hintIndex.value
////    fun nextGeofenceIndex() = _hintIndex.value ?: 0
//}
//
////private const val HINT_INDEX_KEY = "hintIndex"
////private const val GEOFENCE_INDEX_KEY = "geofenceIndex"
//
//data class LandmarkDataObject(val id: String, val hint: Int, val latLong: LatLng)
//
//internal object GeofencingConstants {
//
//    val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(1)
//
//    val LANDMARK_DATA = arrayOf(
//            LandmarkDataObject(
//                    "elliot_bay_books",
//                    R.string.elliot_bay_books_location,
//                    LatLng(47.614756, -122.319433)),
//
//            LandmarkDataObject(
//                    "third_place_books",
//                    R.string.third_place_books_location,
//                    LatLng(47.676021, -122.306298)),
//
//            LandmarkDataObject(
//                    "mercer_st_books",
//                    R.string.mercer_st_books_location,
//                    LatLng(47.624447, -122.356301)),
//
//            LandmarkDataObject(
//                    "phinney_books",
//                    R.string.phinney_books_location,
//                    LatLng(47.682800, -122.355525))
//    )
//
//    val NUM_LANDMARKS = LANDMARK_DATA.size
//    const val GEOFENCE_RADIUS_IN_METERS = 100f
//    const val EXTRA_GEOFENCE_INDEX = "GEOFENCE_INDEX"
}