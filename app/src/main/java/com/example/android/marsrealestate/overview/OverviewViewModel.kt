package com.example.android.marsrealestate.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.marsrealestate.network.MarsApi
import com.example.android.marsrealestate.network.MarsProperty
import kotlinx.coroutines.*


enum class MarsApiStatus {
    LOADING,
    ERROR,
    DONE
}

/**
 * The [ViewModel] that is attached to the [OverviewFragment].
 */
class OverviewViewModel : ViewModel() {

    // Create a Coroutine scope using a job to be able to cancel when needed
    private var viewModelJob = Job()

    // The Coroutine runs using the Main (UI) dispatcher
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    // The internal MutableLiveData String that stores the most recent response status
    private val _status = MutableLiveData<MarsApiStatus>()

    // The external immutable LiveData for the status String
    val status: LiveData<MarsApiStatus>
        get() = _status

    // Internally, we use a MutableLiveData, because we will be updating the List of MarsProperty
    // with new values
    private val _properties = MutableLiveData<List<MarsProperty>>()

    // The external LiveData interface to the properties is immutable, so only this class can modify
    val properties: LiveData<List<MarsProperty>>
        get() = _properties

    /**
     * Call getMarsRealEstateProperties() on init so we can display status immediately.
     */
    init {
        getMarsRealEstateProperties()
    }

    /**
     * Gets Mars real estate property information from the Mars API Retrofit service and updates the
     * [MarsProperty] [List] [LiveData]. The Retrofit service returns a List<MarsProperty>
     */
    private fun getMarsRealEstateProperties() {
        coroutineScope.launch {
            // this will run on a thread managed by coroutines
            withContext(Dispatchers.IO) {
                try {
                    _status.postValue(MarsApiStatus.LOADING)

                    // Get the List<MarsProperty> object for our Retrofit request
                    val listResult = MarsApi.retrofitService.getProperties()
                    _status.postValue(MarsApiStatus.DONE)
                    _properties.postValue(listResult)
                } catch (ex: Exception) {
                    _status.postValue(MarsApiStatus.ERROR)
                    _properties.postValue(ArrayList())
                }
            }
        }
    }

    /**
     * When the [ViewModel] is finished, we cancel our coroutine [viewModelJob], which tells the
     * Retrofit service to stop.
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}
