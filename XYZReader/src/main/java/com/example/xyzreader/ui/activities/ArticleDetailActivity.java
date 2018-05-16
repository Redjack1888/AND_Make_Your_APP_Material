package com.example.xyzreader.ui.activities;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.ui.fragments.ArticleDetailFragment;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ArticleDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ArticleDetailActivity.class.getSimpleName();
    public static final String KEY_ITEM_POSITION = "item_position";

    final Context mContext = this;
    private DetailPagerAdapter mDetailPagerAdapter;
    private Cursor mCursor;
    private String mArticleTitle;
    private Animation mShowAnimation;
    private int mPosition;

    @BindView(R.id.viewpager)
    ViewPager mViewPager;
    @BindView(R.id.appbar_layout)
    AppBarLayout mAppbarLayout;
    @BindView(R.id.imageView_photo)
    ImageView mImageViewPhoto;
    @BindView(R.id.toolbar_detail_activity)
    Toolbar mToolbar;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbar;
    @BindView(R.id.textView_title)
    TextView mTextViewTitle;
    @BindView(R.id.textView_subtitle)
    TextView mTextViewSubtitle;
    @BindView(R.id.fab_share_article)
    FloatingActionButton mFabShareArticle;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss", Locale.ENGLISH);
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);
        ButterKnife.bind(this);

        mPosition = 0;

        // Prepare Toolbar
        setSupportActionBar(mToolbar);
        if (mToolbar != null) {
            Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        mShowAnimation = AnimationUtils.loadAnimation(mContext, R.anim.image_anim);

        getLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                if (getIntent().hasExtra(KEY_ITEM_POSITION)) {
                    mPosition = Objects.requireNonNull(getIntent().getExtras()).getInt(KEY_ITEM_POSITION, 0);
                }
            }
        }

        // OnPageChangeListener on ViewPager to handle swiping viewpager to change article
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                }
                mPosition = position;
                displayArticleData();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        // OffsetChanged Listener on Appbar to handle display of title in expanded and collapsed mode
        mAppbarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShowTitle = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                scrollRange = (scrollRange == -1) ? appBarLayout.getTotalScrollRange() : scrollRange;

                if ((scrollRange + verticalOffset) == 0) {
                    mCollapsingToolbar.setTitle(mArticleTitle);
                    isShowTitle = true;
                } else if (isShowTitle) {
                    mCollapsingToolbar.setTitle(" ");
                    isShowTitle = false;
                }
            }
        });

        // OnClickListener for FAB
        mFabShareArticle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareArticle();
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursor = cursor;

        displayArticleData();

        // Set ViewPager to Adapter
        mDetailPagerAdapter = new DetailPagerAdapter(getSupportFragmentManager(), mCursor);
        mViewPager.setAdapter(mDetailPagerAdapter);
        mViewPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mViewPager.setCurrentItem(mPosition);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursor = null;
        mDetailPagerAdapter.notifyDataSetChanged();
    }

    /**
     * Method to display article details (image, title, author, date)
     */
    public void displayArticleData() {
        mCursor.moveToPosition(mPosition);

        Picasso.get()
                .load(mCursor.getString(ArticleLoader.Query.PHOTO_URL))
                .placeholder(R.drawable.photo_background_protection)
                .into(mImageViewPhoto);

        mImageViewPhoto.startAnimation(mShowAnimation);

        mArticleTitle = mCursor.getString(ArticleLoader.Query.TITLE);
        mTextViewTitle.setText(mArticleTitle);

        String dateParsed = "";
        Date date = parsePublishedDate(mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE));

        if (!date.before(START_OF_EPOCH.getTime())) {
            dateParsed = DateUtils.getRelativeTimeSpanString(
                    date.getTime(),
                    System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_ALL).toString();
        }

        String subtitle = mCursor.getString(ArticleLoader.Query.AUTHOR) + " / " + dateParsed;
        mTextViewSubtitle.setText(subtitle);
    }

    /**
     * Inner Adapter class used for fragment that displays article content
     */
    private class DetailPagerAdapter extends FragmentStatePagerAdapter {

        private WeakReference<Cursor> mCursorWeakRef;

        public DetailPagerAdapter(android.support.v4.app.FragmentManager fm, Cursor cursor) {
            super(fm);
            mCursorWeakRef = new WeakReference<>(cursor);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            mCursorWeakRef.get().moveToPosition(position);
            return ArticleDetailFragment.newInstance(mCursorWeakRef.get().getString(ArticleLoader.Query.BODY));
        }

        @Override
        public int getCount() {
            return mCursorWeakRef.get().getCount();
        }
    }

    /**
     * Method to parse published date to a SimpleDateFormat date
     *
     * @return SimpleDateFormat date
     */
    private Date parsePublishedDate(String date) {
        try {
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, getString(R.string.info_date_parse));
            return new Date();
        }
    }

    /**
     * Method to share article when FAB is clicked
     */
    private void shareArticle() {
        startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(mArticleTitle)
                .getIntent(), getString(R.string.action_share)));

    }
}