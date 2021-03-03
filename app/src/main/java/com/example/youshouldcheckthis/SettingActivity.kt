package com.example.youshouldcheckthis

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        // ActionBar Customize
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.abs_layout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        findViewById<TextView>(R.id.abs_id).text = "설정"

        //Load settings
        this.loadAllSettingFromPreference()

        //fab Save
        findViewById<FloatingActionButton>(R.id.fab_save).setOnClickListener{
            this.saveAllSettingToPreference()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            overridePendingTransition(R.anim.none, R.anim.to_right)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.none, R.anim.to_right)
    }
    fun saveAllSettingToPreference(){
        setPreferenceSetting("setting_increase_alarm", "CheckBox")
        setPreferenceSetting("setting_decrease_alarm", "CheckBox")
        setPreferenceSetting("setting_increase_rate_limit", "EditText")
        setPreferenceSetting("setting_decrease_rate_limit", "EditText")
    }
    fun setPreferenceSetting(strId:String, strType: String){
        if(strType=="CheckBox"){
            val elem = findViewById<CheckBox>(resources.getIdentifier(strId, "id", packageName))
            val prefStock: SharedPreferences = baseContext.getSharedPreferences("pref_$strId", Context.MODE_PRIVATE)
            prefStock.edit().putString("pref_$strId", elem.isChecked.toString()).apply()
        }else if(strType=="EditText"){
            val elem = findViewById<EditText>(resources.getIdentifier(strId, "id", packageName))
            val prefStock: SharedPreferences = baseContext.getSharedPreferences("pref_$strId", Context.MODE_PRIVATE)
            prefStock.edit().putString("pref_$strId", elem.text.toString()).apply()
        }
    }
    fun loadAllSettingFromPreference(){
        getPreferenceSetting("setting_increase_alarm", "CheckBox")
        getPreferenceSetting("setting_decrease_alarm", "CheckBox")
        getPreferenceSetting("setting_increase_rate_limit", "EditText")
        getPreferenceSetting("setting_decrease_rate_limit", "EditText")
    }
    fun getPreferenceSetting(strId:String, strType: String){
        if(strType=="CheckBox"){
            val elem = findViewById<CheckBox>(resources.getIdentifier(strId, "id", packageName))
            val prefStock: SharedPreferences = baseContext.getSharedPreferences("pref_$strId", Context.MODE_PRIVATE)
            elem.isChecked = prefStock.getString("pref_$strId", null).toBoolean()
        }else if(strType=="EditText"){
            val elem = findViewById<EditText>(resources.getIdentifier(strId, "id", packageName))
            val prefStock: SharedPreferences = baseContext.getSharedPreferences("pref_$strId", Context.MODE_PRIVATE)
            elem.setText(prefStock.getString("pref_$strId", null))
        }
    }
}