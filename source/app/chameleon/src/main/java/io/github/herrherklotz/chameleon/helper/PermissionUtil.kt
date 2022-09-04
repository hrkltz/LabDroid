/*
* Copyright 2015 The Android Open Source ProjectObject
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package io.github.herrherklotz.chameleon.helper

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager


/**
 * Utility class that wraps access to the runtime permissions API in M and provides basic helper
 * methods.
 */
object PermissionUtil {
    private const val PERMISSION_ALL = 0

    /**
     * Check that all given permissions have been granted by verifying that each entry in the
     * given array is of the value [PackageManager.PERMISSION_GRANTED].
     *
     * @see Activity.onRequestPermissionsResult
     */
    fun verifyPermissions(grantResults: IntArray): Boolean {
        // Verify that each required permission has been granted, otherwise return false.
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) return false
        }
        return true
    }

    /**
     * Returns true if the Activity has access to a given permission.
     * Always returns true on platforms below M.
     *
     * @see Activity.checkSelfPermission
     */
    fun hasSelfPermission(activity: Activity, permission: String): Boolean {
        return activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    /* TBD
    fun Context.checkSinglePermission(permission: String) : Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }
    */

    fun requestPermission(activity: Activity, permission: String) {
        activity.requestPermissions(arrayOf(permission), PERMISSION_ALL)
    }

    fun requestAllPermissions(activity: Activity) {
        try {
            val info = activity.packageManager.getPackageInfo(activity.applicationContext.packageName, PackageManager.GET_PERMISSIONS)
            if (info.requestedPermissions != null) {
                for (permission in info.requestedPermissions) {
                    if (permission == Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        continue

                    if (!hasSelfPermission(activity, permission))
                        activity.requestPermissions(arrayOf(permission), PERMISSION_ALL)
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hasLocationPermission(activity: Activity): Boolean {
        return hasSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) ||
                hasSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    fun hasBackgroundLocationPermission(activity: Activity): Boolean {
        return hasSelfPermission(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }
}