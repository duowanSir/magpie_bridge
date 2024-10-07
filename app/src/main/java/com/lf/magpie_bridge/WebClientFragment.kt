package com.lf.magpie_bridge

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lf.magpie_bridge.databinding.FragmentWebClientBinding
import com.lf.magpie_bridge_js.BridgeWebContainer
import com.lf.magpie_bridge_js.BridgeWebContainer.Companion.getTimestamp
import com.lf.magpie_bridge_js.api.IMagpieBridgeAsyncHandler
import com.lf.magpie_bridge_js.api.IMagpieBridgePushHandler
import com.lf.magpie_bridge_js.api.IMagpieBridgeSyncHandler
import com.lf.magpie_bridge_js.model.MagpieContext

class WebClientFragment : Fragment() {
    companion object {
        private const val TAG = "WebClientFragment"
    }

    private lateinit var bridgeWebContainer: BridgeWebContainer
    private val sharedVM by lazy {
        ViewModelProvider(requireActivity()).get(SharedVM::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentWebClientBinding.inflate(inflater, container, false)

        // 找到 WebView 并加载一个网页
        val webView = binding.webview
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        bridgeWebContainer = BridgeWebContainer(requireContext(), webView)
        webView.loadUrl("http://192.168.3.52:9000/")
        bridgeWebContainer.addPushHandler(
            TestCommandJ2N.Print.name,
            object : IMagpieBridgePushHandler {
                override fun handle(context: MagpieContext, cmd: String, args: String) {
                    val currentVal = "[${context.beginTime}-${getTimestamp()}] ${context.traceId} $cmd $args"
                    sharedVM.appendLog(currentVal)
                }
            })
        bridgeWebContainer.registerSyncHandler(
            TestCommandJ2N.Print.name, object : IMagpieBridgeSyncHandler {
                override fun handle(context: MagpieContext, cmd: String, args: String): String {
                    val currentVal = "[${context.beginTime}-${getTimestamp()}] ${context.traceId} $cmd $args"
                    sharedVM.appendLog(currentVal)
                    val formattedTime = getTimestamp()
                    return "[$formattedTime] ${context.traceId} Android: sync ok"
                }
            }
        )
        bridgeWebContainer.registerAsyncHandler(
            TestCommandJ2N.Print.name, object : IMagpieBridgeAsyncHandler {
                override fun handle(context: MagpieContext, cmd: String, args: String, callback: (String) -> Unit) {

                }
            }
        )
        Log.d(TAG, "sharedVM=$sharedVM")
        sharedVM.message.observe(viewLifecycleOwner) {
            val (bridgeType, cmd, args) = it
            when (bridgeType) {
                "push" -> bridgeWebContainer.pushRequest(cmd, args)
                "sync" -> bridgeWebContainer.syncRequest(cmd, args) {
                    // 这里是回调
                    Log.d(TAG, "syncRequest callback: $it")
                    sharedVM.appendLog(it)
                }

                "async" -> bridgeWebContainer.asyncRequest(cmd, args) {
                    // 这里是回调
                    Log.d(TAG, "asyncRequest callback: $it")
                }
            }
        }

        return binding.root
    }
}