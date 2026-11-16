package com.rabbi.stream;

import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.content.*;
import android.net.Uri;
import android.webkit.*;
import android.widget.*;
import androidx.media3.common.*;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import org.json.*;
import java.util.*;

public class MainActivity extends Activity {
  LinearLayout root,top,bottom,controls;
  EditText search;
  TextView status,label;
  WebView web;
  PlayerView playerView;
  ExoPlayer player;
  ArrayList<String[]> queue=new ArrayList<>();
  int current=-1;
  String detectedUrl="",detectedTitle="Video";
  boolean nativeMode=false,full=false,block=true;
  final String HOME="https://xhamster.com/";
  final String[] blocked={"doubleclick.net","googlesyndication.com","googleadservices.com","taboola.com","outbrain.com","popads.net","exoclick.com","trafficjunky.net"};

  int dp(int n){return (int)(n*getResources().getDisplayMetrics().density);}
  TextView btn(String t,Runnable r){TextView v=new TextView(this);v.setText(t);v.setTextColor(-1);v.setTextSize(13);v.setGravity(17);v.setPadding(dp(8),dp(10),dp(8),dp(10));v.setOnClickListener(w->r.run());return v;}

  @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(0xff090a0d);getWindow().setNavigationBarColor(0xff090a0d);readQueue();ui();web.loadUrl(HOME);}
  void ui(){
    root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(0xff090a0d);setContentView(root);
    top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);top.setPadding(dp(6),dp(4),dp(6),dp(4));
    top.addView(btn("R STREAM",()->showWeb()));
    search=new EditText(this);search.setSingleLine(true);search.setTextColor(-1);search.setHintTextColor(0xff999999);search.setHint("Search or enter URL");search.setTextSize(13);search.setBackgroundColor(0xff202127);
    top.addView(search,new LinearLayout.LayoutParams(0,dp(42),1));top.addView(btn("GO",()->go()));root.addView(top);
    status=new TextView(this);status.setTextColor(0xffbbbbbb);status.setTextSize(11);status.setPadding(dp(10),dp(3),dp(10),dp(3));status.setText("XHamster web source · basic blocking");root.addView(status);
    web=new WebView(this);web.setBackgroundColor(Color.BLACK);WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setMediaPlaybackRequiresUserGesture(true);s.setSupportMultipleWindows(false);s.setAllowFileAccess(false);s.setAllowContentAccess(false);s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
    web.addJavascriptInterface(new Object(){@JavascriptInterface public void video(String u,String t){runOnUiThread(()->{detectedUrl=u;detectedTitle=t;status.setText("HTML5 video detected · Add to queue");});}},"Rabbi");
    web.setWebViewClient(new WebViewClient(){
      @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){String p=r.getUrl().getScheme();return !("http".equals(p)||"https".equals(p));}
      @Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest r){String h=r.getUrl().getHost();if(block&&h!=null)for(String d:blocked)if(h.equals(d)||h.endsWith("."+d))return new WebResourceResponse("text/plain","utf-8",new java.io.ByteArrayInputStream(new byte[0]));return super.shouldInterceptRequest(v,r);}
      @Override public void onPageFinished(WebView v,String u){search.setText(u);v.evaluateJavascript("(function(){function f(){var x=document.querySelector('video');if(x){var u=x.currentSrc||x.src;if(u&&u.indexOf('http')===0)Rabbi.video(u,document.title||'Video')}}f();document.addEventListener('play',f,true);new MutationObserver(f).observe(document.documentElement,{childList:true,subtree:true,attributes:true});})();",null);}
    });
    root.addView(web,new LinearLayout.LayoutParams(-1,0,1));
    playerView=new PlayerView(this);playerView.setVisibility(View.GONE);player=new ExoPlayer.Builder(this).build();playerView.setPlayer(player);root.addView(playerView,new LinearLayout.LayoutParams(-1,0,1));
    player.addListener(new Player.Listener(){@Override public void onPlaybackStateChanged(int s){if(s==Player.STATE_ENDED)next();}});
    controls=new LinearLayout(this);controls.setGravity(Gravity.CENTER);controls.setBackgroundColor(0xff17181e);
    controls.addView(btn("−10s",()->seek(-10000)));controls.addView(btn("PLAY",()->playPause()));controls.addView(btn("+10s",()->seek(10000)));controls.addView(btn("SPEED",()->speed()));controls.addView(btn("FULL",()->fullscreen()));root.addView(controls);
    bottom=new LinearLayout(this);bottom.setGravity(Gravity.CENTER);bottom.setBackgroundColor(0xff101116);
    bottom.addView(btn("Home",()->{showWeb();web.loadUrl(HOME);}));bottom.addView(btn("Back",()->{if(nativeMode)showWeb();else if(web.canGoBack())web.goBack();}));
    bottom.addView(btn("Queue +",()->add()));bottom.addView(btn("Queue",()->queueDialog()));bottom.addView(btn("Download",()->download()));bottom.addView(btn("Block",()->{block=!block;status.setText("Basic blocklist "+(block?"ON":"OFF")+" (not uBlock Origin)");web.reload();}));root.addView(bottom);
    label=new TextView(this);label.setText("Rabbi Stream · Prototype");label.setTextColor(0xff999999);label.setTextSize(10);label.setPadding(dp(8),dp(2),dp(8),dp(2));root.addView(label);
    search.setOnEditorActionListener((v,a,e)->{go();return true;});
  }
  void go(){String q=search.getText().toString().trim();if(q.isEmpty())return;String u=q.startsWith("http")?q:(q.contains(".")&&!q.contains(" ")?"https://"+q:"https://xhamster.com/search/"+Uri.encode(q));showWeb();web.loadUrl(u);((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(search.getWindowToken(),0);}
  void showWeb(){nativeMode=false;playerView.setVisibility(View.GONE);web.setVisibility(View.VISIBLE);if(full)fullscreen();}
  void add(){if(detectedUrl.isEmpty()){Toast.makeText(this,"No direct HTML5 video URL detected on this page.",1).show();return;}queue.add(new String[]{detectedUrl,detectedTitle});saveQueue();if(current<0)current=0;Toast.makeText(this,"Added to queue",0).show();}
  void next(){if(queue.isEmpty())return;current=(current+1)%queue.size();playItem(current);}
  void playItem(int i){try{String[] q=queue.get(i);player.setMediaItem(MediaItem.fromUri(q[0]));player.prepare();player.play();current=i;nativeMode=true;web.setVisibility(View.GONE);playerView.setVisibility(View.VISIBLE);label.setText(q[1]);}catch(Exception e){Toast.makeText(this,"This stream is not supported.",1).show();}}
  void queueDialog(){if(queue.isEmpty()){Toast.makeText(this,"Queue is empty",0).show();return;}String[] a=new String[queue.size()];for(int i=0;i<a.length;i++)a[i]=queue.get(i)[1];new AlertDialog.Builder(this).setTitle("Play queue").setItems(a,(d,w)->playItem(w)).setNeutralButton("Clear",(d,w)->{queue.clear();current=-1;saveQueue();}).setNegativeButton("Close",null).show();}
  void playPause(){if(!nativeMode){Toast.makeText(this,"Add a detected video to queue first",0).show();return;}if(player.isPlaying())player.pause();else player.play();}
  void seek(long n){if(nativeMode)player.seekTo(Math.max(0,player.getCurrentPosition()+n));}
  void speed(){String[] a={"0.5×","0.75×","1×","1.25×","1.5×","1.75×","2×"};float[] f={.5f,.75f,1f,1.25f,1.5f,1.75f,2f};new AlertDialog.Builder(this).setTitle("Playback speed").setItems(a,(d,w)->{if(nativeMode)player.setPlaybackParameters(new PlaybackParameters(f[w]));}).show();}
  void fullscreen(){full=!full;getWindow().getDecorView().setSystemUiVisibility(full?5894:0);top.setVisibility(full?8:0);bottom.setVisibility(full?8:0);status.setVisibility(full?8:0);label.setVisibility(full?8:0);}
  void download(){if(detectedUrl.isEmpty()){Toast.makeText(this,"No direct media URL detected",0).show();return;}try{DownloadManager.Request r=new DownloadManager.Request(Uri.parse(detectedUrl));r.setTitle("Rabbi Stream download");r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"rabbi-stream-video");((DownloadManager)getSystemService(DOWNLOAD_SERVICE)).enqueue(r);}catch(Exception e){Toast.makeText(this,"Source does not expose a downloadable direct file.",1).show();}}
  void saveQueue(){try{JSONArray a=new JSONArray();for(String[] q:queue){JSONObject o=new JSONObject();o.put("url",q[0]);o.put("title",q[1]);a.put(o);}getPreferences(0).edit().putString("queue",a.toString()).apply();}catch(Exception e){}}
  void readQueue(){try{JSONArray a=new JSONArray(getPreferences(0).getString("queue","[]"));for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);queue.add(new String[]{o.optString("url"),o.optString("title","Video")});}}catch(Exception e){}}
  @Override public void onBackPressed(){if(full){fullscreen();return;}if(nativeMode){showWeb();return;}if(web!=null&&web.canGoBack())web.goBack();else super.onBackPressed();}
  @Override protected void onDestroy(){if(web!=null)web.destroy();if(player!=null)player.release();super.onDestroy();}
}
