package com.example.lab7

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt
import java.math.*
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.pow

class MainActivity : AppCompatActivity() {
    private val MY_PERMISSIONS_REQUEST_LOCATION = 1
    private var locationManager: LocationManager? = null
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            showInfo(location)
        }

        override fun onProviderDisabled(provider: String) {
            showInfo()
        }

        override fun onProviderEnabled(provider: String) {
            showInfo()
        }

        override fun onStatusChanged(
            provider: String, status: Int,
            extras: Bundle
        ) {
            showInfo()
        }
    }
    // текущие данные по широте и долготе
    private var userLongitude: Double = 0.0;
    private var userLatitude: Double = 0.0;
    private var currentLongitudeGPS = 0.0;
    private var currentLatitudeGPS = 0.0;
    private var currentLongitudeNetwork = 0.0;
    private var currentLatitudeNetwork = 0.0;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
// разрешение на геолокацию
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {

            } else {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION
                )
            }
        } else {

        }
    }

    override fun onResume() {
        super.onResume()
        startTracking()
    }

    override fun onPause() {
        super.onPause()
        stopTracking()
    }

    fun startTracking() {
// Проверяем есть ли разрешение
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
// Здесь код работы с разрешениями...
        } else {
            locationManager!!.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 1000, 10f, locationListener
            )
            locationManager!!.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 1000, 10f, locationListener
            )
            showInfo()
        }
    }
    private fun showInfo(location: Location? = null) {
        val isGpsOn = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkOn = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        findViewById<TextView>(R.id.gps_status).text =
            if (isGpsOn) "GPS ON" else "GPS OFF"
        findViewById<TextView>(R.id.network_status).text =
            if (isNetworkOn) "Network ON" else "Network OFF"
        if (location != null) {
// настоящие широта и долгота в переменные
            if (location.provider == LocationManager.GPS_PROVIDER) {
                findViewById<TextView>(R.id.gps_coords).text =
                    "GPS: широта = " + location.latitude.toString() +
                            ", долгота = " + location.longitude.toString()
                this.currentLongitudeGPS = location.longitude
                this.currentLatitudeGPS = location.latitude
// сравнивание дистанции с нов. Координатами по GPS
                if (checkDistance(
                        location.longitude,
                        location.latitude,
                        this.userLongitude,
                        this.userLatitude
                    ) <= 100
                ) {
                    var selectedPointText = findViewById<TextView>(R.id.selectedPointText)
                    selectedPointText.text =
                        "Выбрана точка с долготой - ${this.userLongitude} и широтой - ${this.userLatitude}. Вы входите в радиус 100 метров"
                } else {
                    var selectedPointText = findViewById<TextView>(R.id.selectedPointText)
                    selectedPointText.text =
                        "Выбрана точка с долготой - ${this.userLongitude} и широтой - ${this.userLatitude}."
                }
            }
// просмотр по интернету а не по GPS
            if (location.provider == LocationManager.NETWORK_PROVIDER) {
                findViewById<TextView>(R.id.network_coords).text =
                    "Network: широта = " + location.latitude.toString() +
                            ", долгота = " + location.longitude.toString()
                this.currentLatitudeNetwork = location.latitude
                this.currentLongitudeGPS = location.longitude
                if (checkDistance(
                        location.longitude,
                        location.latitude,
                        this.userLongitude,
                        this.userLatitude
                    ) <= 100
                ) {
                    var selectedPointText = findViewById<TextView>(R.id.selectedPointText)
                    selectedPointText.text =
                        "Выбрана точка с долготой - ${this.userLongitude} и широтой - ${this.userLatitude}. Вы входите в радиус 100 метров"
                } else {
                    var selectedPointText = findViewById<TextView>(R.id.selectedPointText)
                    selectedPointText.text =
                        "Выбрана точка с долготой - ${this.userLongitude} и широтой - ${this.userLatitude}."
                }
            }
        }
    }

    // текст из полей
    fun setPointButtonClick(view: View) {
        this.userLatitude = findViewById<EditText>(R.id.latitude).text.toString().toDouble();
        this.userLongitude = findViewById<EditText>(R.id.longitude).text.toString().toDouble();
        var selectedPointText = findViewById<TextView>(R.id.selectedPointText)
        selectedPointText.text =
            "Выбрана точка с долготой - ${this.userLongitude} и широтой - ${this.userLatitude}"
        var networkDistance = checkDistance(
            this.currentLongitudeNetwork,
            currentLatitudeNetwork,
            this.userLongitude,
            this.userLatitude
        )
        val df = DecimalFormat("#.####")
        df.roundingMode = RoundingMode.DOWN
        val startLongitude1 = df.format( this.userLongitude).toDouble()
        var gpskDistance = checkDistance(
            this.currentLongitudeGPS,
            this.currentLatitudeGPS,
            this.userLongitude,
            this.userLatitude

            //this.currentLatitudeGPS,
            //this.currentLongitudeGPS,
            //this.userLatitude,
            //this.userLongitude,
        )
        if (networkDistance <= 100 || gpskDistance.toInt() >= 100) {
            selectedPointText.text =
                "Выбрана точка с долготой - ${this.userLongitude} и широтой - ${this.userLatitude} Вы входите в радиус 100 метров "
        }
    }

    fun checkDistance(
        startLongitude: Double,
        startLatitude: Double,
        endLongitude: Double,
        endLatitude: Double
    ): Float {
        val results = FloatArray(1)
        val df = DecimalFormat("#.####")
        df.roundingMode = RoundingMode.DOWN
        val startLongitude1 = df.format(startLongitude).toDouble()
        val startLatitude1 = df.format(startLatitude).toDouble()
        val endLongitude1 = df.format(endLongitude).toDouble()
        val endLatitude1 = df.format(endLatitude).toDouble()
// функция встроенная для подсчета метров
        Location.distanceBetween(

            //startLatitude1,
           // startLongitude1,
            32.0374,
            111.4017,
            32.0374,
            111.4017,
            //endLatitude1,
           // endLongitude1,

            results
        );
        return results[0]
    }





























    fun stopTracking() {
        locationManager!!.removeUpdates(locationListener)
    }

    fun buttonOpenSettings(view: View) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
// Разрешение есть, заново выполняем требуемое действие
        } else {
// Разрешения нет...
        }
    }
}
