package com.ongea

import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityCompat.requestPermissions
import android.support.v4.content.ContextCompat

class RunTimePermissions(val activity: Activity, val permissionList: List<String>, val code: Int)  {
    fun checkPermissions() {
        if (isPermissionGranted() != PackageManager.PERMISSION_GRANTED){
//            showAlert()
        }
    }

    //permission status
    private fun isPermissionGranted(): Int {
        // granted permission status contant: 0
        //denied permission status constant : 0
        var counter = 0;
        for(permission in permissionList) {
            counter+= ContextCompat.checkSelfPermission(activity, permission)
        }

        return counter

    }


    // Show alert dialog to request permissions
    private fun showAlert() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Need permission(s)")
        builder.setMessage("Ongea needs your permmission to a.")
        builder.setPositiveButton("OK", { dialog, which -> requestPermission() })
        builder.setNeutralButton("Cancel", null)
        val dialog = builder.create()
        dialog.show()
    }


    private fun deniedPermission(): String {
        for (permission in permissionList) {
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED) return permission
        }
        return  ""
    }

    private fun requestPermission() {
        val permissions = deniedPermission()
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions)){

        }else{
            ActivityCompat.requestPermissions(activity, permissionList.toTypedArray(), code)
        }
    }

    fun processPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray): Boolean {
        var result = 0
        if (grantResults.isNotEmpty()){
            for (item in grantResults) {
                result+= item
            }
        }

        if (result == PackageManager.PERMISSION_GRANTED) return true
        return false
    }
}