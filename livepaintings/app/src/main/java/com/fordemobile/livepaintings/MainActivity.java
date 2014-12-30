package com.fordemobile.livepaintings;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import net.equasoft.ratingreminder.RatingReminder;
import net.equasoft.ratingreminder.type.AlgoType;
import net.equasoft.ratingreminder.type.RatingDialogType;
import net.equasoft.ratingreminder.type.StoreType;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements RatingReminder.RatingInterface {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RatingReminder reminder = new RatingReminder(this, this);
        reminder.setAppName(getString(R.string.app_name));
        reminder.setAlgoType(AlgoType.REGULAR_ALGO);
        reminder.setDialogType(RatingDialogType.IMAGE_DIALOG);
        reminder.setGap(4);

        ArrayList<StoreType> storeTypes = new ArrayList<StoreType>();
        storeTypes.add(StoreType.GOOGLE_PLAYSTORE);
        reminder.setStoreTypes(storeTypes);

        reminder.process();


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        DataManager.getInstance().track("App started");
    }

    @Override
    public void dialogShowed() {
        DataManager.getInstance().track("Rating dialog showed");
    }

    @Override
    public void dialogCanceled() {
        DataManager.getInstance().track("Rating dialog canceled");
    }

    @Override
    public void dialogGo() {
        DataManager.getInstance().track("Rating dialog confirmed");
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter  {

        private List<Fragment> fragments = new ArrayList<Fragment>();
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

            List<Painting> paintings = DataManager.getInstance().getPaintings();
            for (int i = 0; i < paintings.size(); i++) {
                Fragment fragment = new PaintingFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PaintingFragment.PAINTING_INDEX, i);
                fragment.setArguments(bundle);
                fragments.add(fragment);
            }

        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PaintingFragment extends Fragment  implements
            View.OnTouchListener, View.OnClickListener {
        private Painting painting;
        private MixpanelAPI mixpanelAPI;
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String PAINTING_INDEX = "painting_index";

        public PaintingFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            int index = getArguments().getInt(PAINTING_INDEX);
            List<Painting> paintings = DataManager.getInstance().getPaintings();
            painting = paintings.get(index);
            painting.loadPreview();
            View v = null;
            if (painting.isLandscape()) {
                v = inflater.inflate(R.layout.fragment_pager_list, null);
            } else {
                v = inflater.inflate(
                        R.layout.fragment_pager_list_portrait, null);
            }
            TextView descriptionTextView = (TextView) v
                    .findViewById(R.id.descriptionTextView);

            Typeface tf = Typeface
                    .createFromAsset(getActivity().getAssets(), "fonts/Poly-Regular.ttf");
            descriptionTextView.setTypeface(tf);
            ImageView previewImageView = (ImageView) v
                    .findViewById(R.id.previewImageView);
            if (painting.getArtistName().trim().isEmpty()) {
                descriptionTextView
                        .setText(getString(R.string.request_description));
            } else {
                descriptionTextView.setText(Html.fromHtml(painting
                        .getDescription()));
            }
            previewImageView.setImageBitmap(painting.getPreviewImage());
            ((RelativeLayout)rootView).addView(v, 0);

            ImageView buyFullVersionButton = (ImageView) v
                    .findViewById(R.id.buyFullVersionButton);
            buyFullVersionButton.setOnClickListener(this);

            //if full version
            View forsaleImage = v.findViewById(R.id.forSaleView);
            if (!BuildConfig.IS_PRO && painting.isForSale()) {
                // display sale views
                buyFullVersionButton.setVisibility(View.VISIBLE);
                forsaleImage.setVisibility(View.VISIBLE);
                ColorMatrix cm = new ColorMatrix();
                cm.setSaturation(0);
                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(
                        cm);
                previewImageView.setColorFilter(filter);
            } else {
                 // remove sale views
                forsaleImage.setVisibility(View.GONE);
                buyFullVersionButton.setVisibility(View.GONE);
            }
            previewImageView.setOnTouchListener(this);

            ImageView setAsWallpaperButton = (ImageView) v
                    .findViewById(R.id.setAsWallpaperButton);
            setAsWallpaperButton.setOnClickListener(this);

            v.findViewById(R.id.img_arrow_left).setVisibility((index != 0) ? View.VISIBLE : View.GONE);
            v.findViewById(R.id.img_arrow_right).setVisibility((index != paintings.size() - 1) ? View.VISIBLE : View.GONE);


            mixpanelAPI = DataManager.getInstance().getMixpanelAPI();

            return rootView;
        }


        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageView previewImageView = (ImageView) v;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    previewImageView.setColorFilter(new LightingColorFilter(
                            0xFFF1C40E, 0x00000000));
                    break;
                case MotionEvent.ACTION_UP:
                    if (painting.getArtistName().trim().isEmpty()) {
                        final Intent emailIntent = new Intent(
                                Intent.ACTION_SEND);

					/* Fill it with Data */
                        emailIntent.setType("plain/text");
                        emailIntent.putExtra(Intent.EXTRA_EMAIL,
                                new String[] { "livepaintings@fordemobile.com" });
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT,
                                getString(R.string.request_subject));
                        emailIntent.putExtra(Intent.EXTRA_TEXT,
                                getString(R.string.request_message));

					/* Send it off to the Activity-Chooser */
                        startActivity(Intent.createChooser(emailIntent,
                                getString(R.string.request_send)));

                        DataManager.getInstance().track("Request a new painting");
                    } else if (!BuildConfig.IS_PRO && painting.isForSale()) {
                        buyFullVersion();
                    } else {
                        try {
                            JSONObject props = new JSONObject();
                            props.put("painting", painting.getName());
                            DataManager.getInstance().track("Select painting", props);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        previewImageView.setColorFilter(new LightingColorFilter(
                                0xFFFFFFFF, 0x00000000));
                        SharedPreferences prefs = getActivity().getSharedPreferences(
                                Consts.SHARED_PREFS_NAME, 0);
                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putString(Consts.PREF_PAINTING,
                                painting.getDirectoryPath());
                        edit.commit();


                        Toast.makeText(
                                getActivity(),
                                painting.getName() + " "
                                        + getString(R.string.set_as_painting),
                                Toast.LENGTH_SHORT).show();

                        if (!isWallpaperSet()) {
                            setWallpaper();
                        }
                    }
                    break;
            }
            return true;
        }

        public boolean isWallpaperSet() {

            WallpaperManager wpm = WallpaperManager.getInstance(getActivity());
            WallpaperInfo info = wpm.getWallpaperInfo();

            String p = LivePaintingsWallpaper.class.getPackage().getName();
            return (info != null && info.getPackageName().equals(p));
        }

        @Override
        public void onStart() {
            super.onStart();
            try {
                JSONObject props = new JSONObject();
                props.put("painting", painting.getName());
                DataManager.getInstance().track("View painting", props);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                //need to change this
                case R.id.setAsWallpaperButton:
//                    setWallpaper();
                    String shareBody = "Hey, check out this new Live Wallpaper I'm using.<br/><br/>It's really cool. <a href='market://search?q=com.fordemobile.livepaintingspro'>Here is the link</a>";
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/html");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Live Paintings Is Awesome!!!");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(shareBody));
                    startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_to_friends)));

                    DataManager.getInstance().track("Share to friends");
                    break;
                case R.id.buyFullVersionButton:
                    buyFullVersion();
                    break;
                default:
                    break;
            }
        }

        private void buyFullVersion() {
            DataManager.getInstance().track("Clicked on buy full version");
            Intent goToMarket = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://search?q=com.fordemobile.livepaintingspro"));
            startActivity(goToMarket);
        }

        public void setWallpaper() {

            Intent i = new Intent();

            if(Build.VERSION.SDK_INT > 15){
                ComponentName component = new ComponentName(getActivity().getPackageName(), LivePaintingsWallpaper.class.getCanonicalName());
                i = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component);
            }
            else{
                // install button
                Toast toast = Toast.makeText(getActivity(),
                        R.string.choose_live_paintings, Toast.LENGTH_LONG);
                toast.show();
                i.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
            }
            getActivity().startActivityForResult(i, 0);
        }
    }

}
