package com.example.youshouldcheckthis

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.view.View.OnFocusChangeListener
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.iterator
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.GsonBuilder
import com.google.gson.internal.GsonBuildConfig
import com.google.gson.reflect.TypeToken

public interface InterfaceMainActivityForAdapter{
    fun refreshStockView(viewGroupParent: ViewGroup, listViewItemList: ArrayList<ListViewItem>, index: Int)
    fun makeToastText(text: String, lengthToast:Int)
    fun setPreferenceStockList(list: ArrayList<ListViewItem>)
    fun getPreferenceStockList():ArrayList<ListViewItem>?
}

class MainActivity : AppCompatActivity() {
    private var mToast:Toast? = null
    private var setting = Setting()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ActionBar Customize
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.abs_layout)
        findViewById<TextView>(R.id.abs_id).text = "관심 종목"

        //ListView
        val listview: ListView = findViewById<View>(R.id.stock_list_view) as ListView
        val adapter = ListViewAdapter()
        adapter.interfaceMainActivityForAdapter = object: InterfaceMainActivityForAdapter{
            override fun refreshStockView(viewGroupParent: ViewGroup, listViewItemList: ArrayList<ListViewItem>, index: Int) {
                runOnUiThread{
                    adapter.notifyDataSetChanged()
                }
            }
            override fun makeToastText(text: String, lengthToast:Int){
                runOnUiThread{
                    mToast?.cancel()
                    mToast = Toast.makeText(baseContext, text, lengthToast)
                    mToast?.show()
                }
            }
            override fun setPreferenceStockList(list: ArrayList<ListViewItem>){
                val prefStock: SharedPreferences = baseContext.getSharedPreferences("pref_stock_list", Context.MODE_PRIVATE)
                val gson = GsonBuilder().create()
                val json = gson.toJson(list)
                prefStock.edit().putString("pref_stock_list", json).apply()
            }
            override fun getPreferenceStockList():ArrayList<ListViewItem>?{
                val prefStock: SharedPreferences = baseContext.getSharedPreferences("pref_stock_list", Context.MODE_PRIVATE)
                val gson = GsonBuilder().create()
                val json = prefStock.getString("pref_stock_list", null) ?: return null
                val type = object: TypeToken<ArrayList<ListViewItem>>(){}.type
                return gson.fromJson(json, type)
            }
        }
        adapter.rootView = findViewById<View>(android.R.id.content).rootView
        listview.adapter = adapter

        //Load Stocks From Preference To Adapter
        val tmp = adapter.interfaceMainActivityForAdapter.getPreferenceStockList()
        if(tmp!=null) {
            adapter.listViewItemList = tmp
            adapter.refreshAllStockList(false)
        }

        //Load Setting TODO: 로드
        this.setting = Setting()

        // + / x Btn
        findViewById<FloatingActionButton>(R.id.fab_add).setOnClickListener {
            if(adapter.isRemoveMode){ // x Btn -> Cancel
                adapter.isRemoveMode= false
                adapter.setCheckBoxInvisible()
            }else{ // + Btn -> Add
                val builder = AlertDialog.Builder(this)
                val dialogView = layoutInflater.inflate(R.layout.custom_dialog, null)
                val dialogText = dialogView.findViewById<EditText>(R.id.dialogEditText)
                val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                builder.setView(dialogView)
                        .setNegativeButton("취소") {dialog: DialogInterface?, which: Int ->
                            imm.hideSoftInputFromWindow(dialogText.windowToken, 0)
                        }
                        .setPositiveButton("확인"){ dialog: DialogInterface?, which: Int ->
                            adapter.addItem(dialogText.text.toString())
                            imm.hideSoftInputFromWindow(dialogText.windowToken, 0)
                        }
                        .setCancelable(false)
                        .show()
                Handler(Looper.getMainLooper()).postDelayed({
                    imm.showSoftInput(dialogText, InputMethodManager.SHOW_FORCED)
                }, 200L)
            }
        }

        //Remove Btn
        findViewById<FloatingActionButton>(R.id.fab_remove).setOnClickListener {
            var todo = arrayListOf<Int>()
            var i:Int= 0
            for(i in listview.count-1 downTo 0){
                val curCheckBox = listview.getChildAt(i).findViewById<CheckBox>(R.id.stock_checkbox)
                if(curCheckBox.isChecked){
                    todo.add(i)
                }
            }
            adapter.setCheckBoxInvisible()
            adapter.isRemoveMode = false
            for(i in todo){
                adapter.removeItem(i)
            }
            adapter.interfaceMainActivityForAdapter.setPreferenceStockList(adapter.listViewItemList)
            runOnUiThread{
                adapter.notifyDataSetChanged()
            }
        }

        //Periodic Refresh
        Thread(
                Runnable{
                    try{
                        while(true){
                            adapter.refreshAllStockList(true)
                            Log.i("MainActivity","PeriodicRefreshThread")
                            Thread.sleep(60000)
                        }
                    }catch(e:Exception){
                        e.printStackTrace()
                    }
                }
        ).start()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_setting, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.settingItem) {
            val settingActivity = SettingActivity()
            val intent = Intent(this, settingActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.to_left, R.anim.none)
        }
        return super.onOptionsItemSelected(item)
    }
}