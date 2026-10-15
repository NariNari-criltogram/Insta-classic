package com.suf.igclassic

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

class MainActivity : Activity() {

    private lateinit var web: WebView

    private val js = """
    (function(){
      if(!window.__noReelsStyle){
        window.__noReelsStyle = true;
        var s = document.createElement('style');
        s.textContent = 'a[href^="/reels"], a[href*="/reel/"], a[href*="/reels/"]{display:none !important}';
        (document.head || document.documentElement).appendChild(s);
      }
      function clean(){
        if(/^\/reels?(\/|$)/.test(location.pathname)){ location.replace('/'); return; }
        document.querySelectorAll('a[href*="/reel/"], a[href*="/reels"]').forEach(function(a){
          var art = a.closest('article');
          if(art){ art.style.display = 'none'; } else { a.style.display = 'none'; }
        });
      }
      clean();
      if(!window.__noReelsObs){
        window.__noReelsObs = true;
        new MutationObserver(clean).observe(document.documentElement, {childList:true, subtree:true});
        setInterval(clean, 1000);
      }
    })();
    """.trimIndent()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        setContentView(web)

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true)

        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = true
            cacheMode = WebSettings.LOAD_DEFAULT
            userAgentString =
                "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
        }

        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val uri = request.url
                val host = uri.host ?: return false
                val path = uri.path ?: ""
                if (host.endsWith("instagram.com") && (path.startsWith("/reel/") || path.startsWith("/reels"))) {
                    view.loadUrl("https://www.instagram.com/")
                    return true
                }
                val internal = host.endsWith("instagram.com") || host.endsWith("facebook.com") ||
                    host.endsWith("cdninstagram.com") || host.endsWith("fbcdn.net")
                if (!internal) {
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                    return true
                }
                return false
            }
            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                view.evaluateJavascript(js, null)
            }
            override fun onPageFinished(view: WebView, url: String?) {
                view.evaluateJavascript(js, null)
            }
        }

        if (savedInstanceState == null) web.loadUrl("https://www.instagram.com/")
        else web.restoreState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        web.saveState(outState)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}
