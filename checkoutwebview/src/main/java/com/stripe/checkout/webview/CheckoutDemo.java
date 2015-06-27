package com.stripe.checkout.webview;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.support.v4.view.ActionProvider;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;

public class CheckoutDemo extends ActionBarActivity {
    private static String TAG = "CheckoutDemo";

    private DemoViewFragment mDemoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout_demo);

        if (savedInstanceState == null) {
            mDemoView = new DemoViewFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mDemoView)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.checkout_demo, menu);

        MenuItem goItem = menu.findItem(R.id.action_go);
        GoActionProvider goActionProvider = (GoActionProvider) MenuItemCompat.getActionProvider(goItem);
        goActionProvider.setTarget(mDemoView);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(item.getItemId()){
            case R.id.action_go:
                return true;
            case R.id.action_allow_multiple_windows:
                boolean allow = !item.isChecked();
                item.setChecked(allow);
                mDemoView.setAllowMultipleWindows(allow);
                return true;
        }
        if (id == R.id.action_go) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class GoActionProvider extends ActionProvider {

        /** Context for accessing resources. */
        private final Context mContext;
        private DemoViewFragment mTarget;

        /**
         * Creates a new instance.
         *
         * @param context Context for accessing resources.
         */
        public GoActionProvider(Context context) {
            super(context);
            mContext = context;
        }

        public void setTarget(DemoViewFragment target){
            mTarget = target;
        }

        @Override
        public View onCreateActionView() {
            return null;
        }

        @Override
        public boolean hasSubMenu() {
            return true;
        }

        @Override
        public void onPrepareSubMenu(SubMenu submenu){
            submenu.clear();

            MenuItem item;
            String[] targets = mContext.getResources().getStringArray(R.array.url_array);
            String[] values = mContext.getResources().getStringArray(R.array.url_values_array);

            for(int i = 0; i < targets.length; i ++){
                item = submenu.add(targets[i]);
                final String value;
                if(i < values.length){
                    value = values[i];
                } else {
                    value = null;
                }
                item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if(value != null){
                            goToUrl(value);
                        } else {
                            showCustomUrlDialog();
                        }
                        return true;
                    }
                });
            }
        }

        public void showCustomUrlDialog(){
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Custom URL");

            final EditText input = new EditText(mContext);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("Go", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    mTarget.loadUrl(input.getText().toString());
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    dialog.cancel();
                }
            });

            builder.show();
        }

        public void goToUrl(String url){
            mTarget.loadUrl(url);
        }
    }

    public static class DemoViewFragment extends Fragment {
        WebView mWebView;
        WebSettings mWebSettings;

        public DemoViewFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_checkout_demo, container, false);

            mWebView = (WebView) rootView.findViewById(R.id.webView);
            final TextView currentUrl = (TextView) rootView.findViewById(R.id.currentUrl);
            final ViewGroup webViewContainer = (ViewGroup) rootView.findViewById(R.id.web_view_container);

            mWebSettings = mWebView.getSettings();
            mWebSettings.setJavaScriptEnabled(true);

            WebViewClient client = new WebViewClient(){
                public void onPageStarted(WebView view, String url, Bitmap favicon){
                    currentUrl.setText(url);
                }
            };
            mWebView.setWebViewClient(client);

            WebChromeClient webChromeClient = new WebChromeClient() {
                @Override public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
                    webViewContainer.removeAllViews();
                    WebView newWebView = new WebView(getActivity());
                    WebSettings newWebSettings = newWebView.getSettings();
                    newWebSettings.setJavaScriptEnabled(true);
                    webViewContainer.addView(newWebView);
                    WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                    transport.setWebView(newWebView);
                    resultMsg.sendToTarget();
                    return true;
                }
            };
            mWebView.setWebChromeClient(webChromeClient);

            return rootView;
        }

        public void loadUrl(String url) {
            mWebView.loadUrl(url);
        }

        public void setAllowMultipleWindows(boolean allow){
            mWebSettings.setSupportMultipleWindows(allow);
            mWebSettings.setJavaScriptCanOpenWindowsAutomatically(allow);
        }
    }

}
