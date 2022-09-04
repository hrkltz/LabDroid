package io.github.herrherklotz.chameleon.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import io.github.herrherklotz.chameleon.databinding.FragmentWebBinding


class WebFragment : Fragment() {
    private var _binding: FragmentWebBinding? = null
    private val binding get() = _binding!!

    val ARG_SECTION_URL = "section_url"
    val ARG_FULLSCREEN = "fullscreen"


    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        requireActivity().window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            // Set the content to appear under the system bars so that the
            // content doesn't resize when the system bars hide and show.
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            // Hide the nav bar and status bar
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN)

        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
    }


    private fun showSystemUI() {
        requireActivity().window.decorView.systemUiVisibility = (0)
        (activity as AppCompatActivity?)!!.supportActionBar!!.show()
    }


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        _binding = FragmentWebBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }


    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if ((arguments != null) && (arguments?.containsKey(ARG_FULLSCREEN)!!)) {
            if (requireActivity().window.decorView.systemUiVisibility == 0)
                hideSystemUI()
        }

        binding.webview.apply {
            if (arguments != null && arguments?.containsKey(ARG_SECTION_URL)!!)
                loadUrl(arguments?.getString(ARG_SECTION_URL)!!)
            else
                loadUrl("http://localhost:8080/screen/log.html")

            settings.javaScriptEnabled = true

            // Load all links inside this WebView
            webViewClient = WebViewClient()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()

        binding.webview.destroy()

        _binding = null
    }
}