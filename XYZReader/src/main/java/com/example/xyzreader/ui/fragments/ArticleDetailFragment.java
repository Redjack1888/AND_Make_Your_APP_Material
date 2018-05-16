package com.example.xyzreader.ui.fragments;


import android.content.Context;

import android.graphics.Typeface;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.ui.activities.ArticleDetailActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ArticleDetailFragment extends Fragment {

    private static final String TAG = ArticleDetailFragment.class.getSimpleName();
    private static final String KEY_ARTICLE_CONTENT = "article_content";

    private Context mDetailFragmentContext;
    private Unbinder mUnbinder;
    private View mRootView;

    @BindView(R.id.text_view_article)
    TextView mTextViewArticle;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDetailFragmentContext = context;
    }

    /**
     * NewInstance constructor for creating fragment with arguments
     */
    public static ArticleDetailFragment newInstance(String article) {
        ArticleDetailFragment articleDetailFragment = new ArticleDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_ARTICLE_CONTENT, article);
        articleDetailFragment.setArguments(bundle);
        return articleDetailFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        String article = "";

        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        mUnbinder = ButterKnife.bind(this, mRootView);

        if (getArguments() != null && getArguments().containsKey(KEY_ARTICLE_CONTENT)) {
            article = getArguments().getString(KEY_ARTICLE_CONTENT);
        }

        prepareArticleText(article);
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Method to format raw article content before displaying on screen
     * based on the article os StackOverflow - How to make replaceAll working in java?
     * short url: http://bit.ly/2jTQqz4
     */
    private void prepareArticleText(String articleText) {
        String a = articleText.replaceAll(">", "&gt;");
        String a1 = a.replaceAll("(\r\n){2}(?!(&gt;))", "<br><br>");
        String a2 = a1.replaceAll("(\r\n)", " ");

        //remove all text between [ and ]
        String a3 = a2.replaceAll("\\[.*?\\]", "");

        //put new line after i.e 1. Ebooks aren't marketing.
        String a4 = a3.replaceAll("(\\d\\.\\s.*?\\.)", "$1<br>");

        //make text between * * bold
        String a5 = a4.replaceAll("\\*(.*?)\\*", "<b>$1</b>");

        //remove all '>' from text such as 'are >'  but leave the first '>' in tact
        String a6 = a5.replaceAll("(\\w\\s)&gt;", "$1");

        // replace double hyphen with single hyphen
        String a7 = a6.replaceAll("--", " - ");

        Spanned a8 = Html.fromHtml(a7);

        mTextViewArticle.setText(a8.toString());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}
