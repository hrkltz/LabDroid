package io.github.herrherklotz.chameleon.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import io.github.herrherklotz.chameleon.BuildConfig
import io.github.herrherklotz.chameleon.Chameleon
import io.github.herrherklotz.chameleon.databinding.FragmentMainBinding
import io.github.herrherklotz.chameleon.helper.PermissionUtil


class MainFragment : Fragment() {
    private val REQUEST_BACKGROUND = 0
    private val REQUEST_LOCATION = 1
    private val REQUEST_BACKGROUND_LOCATION = 2
    private var _activity: Activity? = null
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // This is MainActivity instance u can use this as MainActivity mMainActivity = (MainActivity)mActivity;
        _activity = context as Activity
    }

    override fun onDetach() {
        super.onDetach()
        _activity = null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (requireActivity().window.decorView.systemUiVisibility > 0) {
            // show system UI
            requireActivity().window.decorView.systemUiVisibility = (0)
            (activity as AppCompatActivity?)!!.supportActionBar!!.show()
        }

        binding.button1.setOnClickListener {
            val intent = Intent()
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
            startActivityForResult(intent, REQUEST_BACKGROUND)
            /* TODO
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val powerManager = _activity!!.getSystemService(Context.POWER_SERVICE) as PowerManager

                    if (powerManager.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID)) {
                        binding.button1.isEnabled = false
                        binding.button1.text = "Granted"
                    }
                }
            }.launch(intent)
             */
        }

        binding.button2.setOnClickListener {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest
                    .permission.ACCESS_COARSE_LOCATION), REQUEST_LOCATION)
        }

        binding.button3.setOnClickListener {
            AlertDialog.Builder(_activity!!)
                    .setTitle("Access location in the background")
                    .setMessage("LabDroid collects location data to enable the GPS node to " +
                            "deliver GPS information to your script even when the app is closed " +
                            "or not in use.")
                    .setPositiveButton("Open Settings") { _, _ ->
                        requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                                REQUEST_BACKGROUND_LOCATION)
                    }
                    .setNegativeButton("DENY") { dialog, _ -> dialog.dismiss() }
                    .create()
                    .show()
        }
    }


    override fun onResume() {
        super.onResume()

        val powerManager = _activity!!.getSystemService(Context.POWER_SERVICE) as PowerManager

        if (powerManager.isIgnoringBatteryOptimizations(_activity!!.packageName)) {
            binding.button1.isEnabled = false
            binding.button1.text = "Granted"
        }

        if (PermissionUtil.hasLocationPermission(_activity!!)) {
            binding.button2.isEnabled = false
            binding.button2.text = "Granted"
        }

        if (PermissionUtil.hasBackgroundLocationPermission(_activity!!)) {
            binding.button3.isEnabled = false
            binding.button3.text = "Granted"
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Chameleon.LOG_D("onActivityResult: " + requestCode)

        when (requestCode) {
            REQUEST_BACKGROUND -> {
                val powerManager = _activity!!.getSystemService(Context.POWER_SERVICE) as PowerManager

                if (powerManager.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID)) {
                    binding.button1.isEnabled = false
                    binding.button1.text = "Granted"
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Chameleon.LOG_D("PermissionResult: " + requestCode + " " + grantResults[0])

        when (requestCode) {
            REQUEST_LOCATION -> {
                if (grantResults[0] == 0) {
                    binding.button2.isEnabled = false
                    binding.button2.text = "Granted"
                } else {
                    AlertDialog.Builder(_activity!!)
                            .setTitle("Access location denied")
                            .setMessage("LabDroid collects location data to enable the GPS node " +
                                    "to deliver GPS information to your script which allows you " +
                                    "to use your geo-location information for your algorithm. \n" +
                                    "\n\nWithout this permission the GPS node will be deactivated " +
                                    "and you wont be able to use any geo-location information in " +
                                    "your script.")
                            .setPositiveButton("Update Settings") { _, _ ->
                                val intent = Intent()
                                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                intent.data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                                startActivityForResult(intent, REQUEST_BACKGROUND)
                            }
                            .setNegativeButton("No thanks") { dialog, _ -> dialog.dismiss() }
                            .create()
                            .show()
                }
            }
        }
    }
}