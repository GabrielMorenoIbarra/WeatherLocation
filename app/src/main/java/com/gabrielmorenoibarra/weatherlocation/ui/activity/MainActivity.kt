package com.gabrielmorenoibarra.weatherlocation.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gabrielmorenoibarra.generic.extension.isConnected
import com.gabrielmorenoibarra.generic.extension.view.gone
import com.gabrielmorenoibarra.generic.extension.view.hideKeyboard
import com.gabrielmorenoibarra.generic.extension.view.visible
import com.gabrielmorenoibarra.generic.util.manager.SearchManager
import com.gabrielmorenoibarra.weatherlocation.BuildConfig
import com.gabrielmorenoibarra.weatherlocation.R
import com.gabrielmorenoibarra.weatherlocation.data.api.parser.routes.WeatherApiParser
import com.gabrielmorenoibarra.weatherlocation.domain.model.usecase.Word
import com.gabrielmorenoibarra.weatherlocation.domain.model.usecase.request.Coordinate
import com.gabrielmorenoibarra.weatherlocation.ui.adapter.WordListAdapter
import com.gabrielmorenoibarra.weatherlocation.ui.fragment.LocationsFragment
import com.gabrielmorenoibarra.weatherlocation.viewmodel.WordViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_search.*
import kotlinx.android.synthetic.main.progress_bar.*
import kotlinx.android.synthetic.main.tv_no_results.*
import org.jetbrains.anko.longToast

class MainActivity
    : AppCompatActivity()
    , OnMapReadyCallback, LifecycleOwner {

    private lateinit var wordViewModel: WordViewModel
    private lateinit var googleMap: GoogleMap

    private lateinit var locationFragment: LocationsFragment

    private var etHasFocus = false
    private var showNoResults = false
    private var showHistoric = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initFragments()
        initGoogleMap()
        initClSearch()
        initTvCancel()
        val adapter = initAdapter()
        initViewModel(adapter)

//        showDemoInfo()
    }

    fun initFragments() {
        locationFragment = supportFragmentManager.findFragmentById(R.id.fLocations) as LocationsFragment
        locationFragment.setListener {
            wordViewModel.insert(Word(it))
        }
//        historicFragment = // TODO
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.let {
            this.googleMap = it

            // Add a marker in Sydney and move the camera
            val sydney = LatLng(-34.0, 151.0)
            this.googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
            this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        }
    }

    private fun initGoogleMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.googleMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun initClSearch() {
        SearchManager(et, ib) { s ->
            fetch(s)
        }

        et.setOnFocusChangeListener { _, hasFocus ->
            etHasFocus = hasFocus
            if (hasFocus) {
                showHistoricPerform()
            } else {
                hideHistoricPerform()
            }
        }
    }

    private fun hideHistoricPerform() {
        clHistoric.gone()
        clResults.visible()
        tvCancel.gone()
        if (showNoResults) tvNoResults.visible()
    }

    private fun showHistoricPerform() {
        clHistoric.visible()
        clResults.gone()
        tvCancel.visible()
        tvNoResults.gone()
    }

    private fun initTvCancel() {
        tvCancel.setOnClickListener {
            clearEtFocusPerform()
        }
    }

    private fun clearEtFocusPerform() {
        et.clearFocus()
        hideKeyboard()
    }

    private fun initAdapter(): WordListAdapter {
        val adapter = WordListAdapter(this)
        rvHistoric.adapter = adapter
        rvHistoric.layoutManager = LinearLayoutManager(this)
        return adapter
    }

    private fun initViewModel(adapter: WordListAdapter) {
        wordViewModel = ViewModelProviders.of(this).get(WordViewModel::class.java)
        wordViewModel.allWords.observe(this,
            Observer { words ->
                words?.let { adapter.setWords(it) }
            })
    }

    private fun fetch(s: String) {
        val connected = isConnected()
        if (connected) {
            if (s.trim().isEmpty() && etHasFocus) {
//                fetchHistoric()
                showHistoricPerform()
            } else {
                hideHistoricPerform()
                locationFragment.populate(s)
            }
        }
    }

    private fun showDemoInfo() {
        if (BuildConfig.DEBUG) {
            val north = 44.1f
            val south = -9.9f
            val east = -22.4f
            val west = 55.2f
            val coordinate = Coordinate(north, south, east, west)
            WeatherApiParser().get(
                coordinate,
                BuildConfig.USERNAME_IL_GEONAMES_SAMPLE,
                0, 20
            ) {
                val message = it.toString()
                longToast(message)
            }
        }
    }
}
