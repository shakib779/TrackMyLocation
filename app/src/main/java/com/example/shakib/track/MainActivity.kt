package com.example.shakib.track

import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback

    val REQUEST_CODE = 1000;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        val buttonMap = findViewById<Button>(R.id.button_map)
        val buttonGallery = findViewById<Button>(R.id.button_gallery)

        if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION))
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE)

        else{
            buildLocationReequest()
            buildLocationCallback()

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

            buttonMap.setOnClickListener(View.OnClickListener {

                if (ActivityCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ){
                    ActivityCompat.requestPermissions(this@MainActivity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE)
                    return@OnClickListener
                }


                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())

                buttonMap.isEnabled = !buttonMap.isEnabled

            });

        }



        buttonGallery.setOnClickListener {
            Toast.makeText(this@MainActivity, "You clicked Gallery.", Toast.LENGTH_SHORT).show()
        }

    }

    private fun buildLocationReequest() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = 10f

    }

    private fun buildLocationCallback() {
        locationCallback = object :LocationCallback(){
            override fun onLocationResult(p0: LocationResult?)  {
                var location = p0!!.locations.get(p0!!.locations.size-1)

                val intent = Intent(this@MainActivity, MapsActivity::class.java)
                intent.putExtra("Longitude", location.longitude.toString())
                intent.putExtra("Latitude", location.latitude.toString())
                startActivity(intent)
            }
        }
    }
}
