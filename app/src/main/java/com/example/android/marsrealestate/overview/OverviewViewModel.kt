package com.example.android.marsrealestate.overview

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.marsrealestate.network.MarsApi
import com.example.android.marsrealestate.network.MarsProperty
import kotlinx.coroutines.*


enum class MarsApiStatus {
    LOADING,
    ERROR,
    DONE,
    NO_INTERNET_CONNECTION
}

/**
 * The [ViewModel] that is attached to the [OverviewFragment].
 */
class OverviewViewModel(application: Application) : AndroidViewModel(application) {

    // Create a Coroutine scope using a job to be able to cancel when needed
    private var viewModelJob = Job()

    // The Coroutine runs using the Main (UI) dispatcher
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    // The internal MutableLiveData that stores the most recent request
    private val _status = MutableLiveData<MarsApiStatus>()

    // The external immutable LiveData for the request status
    val status: LiveData<MarsApiStatus>
        get() = _status

    // Internally, we use a MutableLiveData, because we will be updating the List of MarsProperty
    // with new values
    private val _properties = MutableLiveData<List<MarsProperty>>()

    // The external LiveData interface to the properties is immutable, so only this class can modify
    val properties: LiveData<List<MarsProperty>>
        get() = _properties

    /**
     * Call checkConnectionIsAvaliable() on init so we can check connection before of the connect
     * at API/Retrofit for display status immediately.
     */
    init {
        checkConnectionIsAvaliable()
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

                    delay(100)

                    // Get the List<MarsProperty> object for our Retrofit request
                    val listResult = MarsApi.retrofitService.getProperties()

                    _status.postValue(MarsApiStatus.DONE)

                    _properties.postValue(listResult)
                } catch (ex: Throwable) {
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

    private fun checkConnectionIsAvaliable() {

        val application = getApplication<Application>()

        val connectivityManager: ConnectivityManager =
                application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkInfo = connectivityManager.activeNetworkInfo

        if (networkInfo != null && networkInfo.isConnected)
            getMarsRealEstateProperties()
        else
            _status.value = MarsApiStatus.NO_INTERNET_CONNECTION
    }
}

