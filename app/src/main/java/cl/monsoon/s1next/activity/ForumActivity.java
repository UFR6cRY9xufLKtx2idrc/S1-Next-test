package cl.monsoon.s1next.activity;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import cl.monsoon.s1next.R;
import cl.monsoon.s1next.fragment.ForumFragment;
import cl.monsoon.s1next.singleton.Config;
import cl.monsoon.s1next.util.ObjectUtil;
import cl.monsoon.s1next.widget.StateListDrawableWithTint;

/**
 * This Activity has Spinner in Toolbar to switch between different forum groups.
 */
public final class ForumActivity extends BaseActivity
        implements AdapterView.OnItemSelectedListener,
        ToolbarInterface.SpinnerInteractionCallback {

    /**
     * The serialization (saved instance state) Bundle key representing
     * the position of the selected spinner item.
     */
    private static final String STATE_SPINNER_SELECTED_POSITION = "selected_position";

    private Spinner mSpinner;

    /**
     * Stores the selected Spinner position.
     */
    private int mSelectedPosition = 0;

    private ToolbarInterface.OnDropDownItemSelectedListener mOnToolbarDropDownItemSelectedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        enableWindowTranslucentStatus();
        setContentView(R.layout.activity_base);

        Fragment fragment;
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            fragment = new ForumFragment();

            fragmentManager.beginTransaction()
                    .replace(R.id.frame_layout, fragment, ForumFragment.TAG).commit();
        } else {
            mSelectedPosition = savedInstanceState.getInt(STATE_SPINNER_SELECTED_POSITION);
            fragment = fragmentManager.findFragmentByTag(ForumFragment.TAG);
        }

        mOnToolbarDropDownItemSelectedListener =
                ObjectUtil.cast(fragment, ToolbarInterface.OnDropDownItemSelectedListener.class);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(STATE_SPINNER_SELECTED_POSITION, mSelectedPosition);
    }

    /**
     * Implements {@link android.widget.AdapterView.OnItemSelectedListener}.
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mSelectedPosition = position;
        mOnToolbarDropDownItemSelectedListener.onToolbarDropDownItemSelected(mSelectedPosition);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     * Implements {@link ToolbarInterface.SpinnerInteractionCallback}.
     */
    @Override
    @SuppressWarnings("deprecation")
    public void setupToolbarDropDown(List<? extends CharSequence> dropDownItemList) {
        if (mSpinner == null) {
            setTitle(null);

            Toolbar toolbar = getToolbar();

            // add Spinner to Toolbar
            LayoutInflater.from(this).inflate(R.layout.toolbar_spinner, toolbar, true);
            mSpinner = (Spinner) toolbar.findViewById(R.id.spinner);

            if (!Config.isS1Theme() && !Config.isDarkTheme()) {
                int colorAccent = Config.getColorAccent();
                int[][] states =
                        new int[][]{
                                new int[]{android.R.attr.state_enabled, android.R.attr.state_focused},
                                new int[]{android.R.attr.state_enabled, android.R.attr.state_pressed},
                        };
                int[] colors = new int[]{colorAccent, colorAccent};
                ColorStateList colorStateList = new ColorStateList(states, colors);

                mSpinner.setBackgroundDrawable(
                        new StateListDrawableWithTint(
                                getResources().getDrawable(R.drawable.abc_spinner_mtrl_am_alpha),
                                colorStateList,
                                PorterDuff.Mode.SRC_ATOP));
            }
            mSpinner.setOnItemSelectedListener(this);

            // We disable clicking in Spinner
            // and let its parents LinearLayout to handle
            // clicking event in order to increase clickable area.
            View spinnerView = toolbar.findViewById(R.id.toolbar_layout);
            spinnerView.setOnClickListener(v -> mSpinner.performClick());
        }

        mSpinner.setAdapter(getSpinnerAdapter(dropDownItemList));
        // invalid index may occurs when user's login status has changed
        if (mSpinner.getAdapter().getCount() - 1 < mSelectedPosition) {
            mSpinner.setSelection(0, false);
        } else {
            mSpinner.setSelection(mSelectedPosition, false);
        }
    }

    BaseAdapter getSpinnerAdapter(List<? extends CharSequence> dropDownItemList) {
        // don't use dropDownItemList#add(int, E)
        // otherwise we will have multiple "全部"
        // if we invoke this method many times

        List<CharSequence> list = new ArrayList<>();
        // the first drop-down item is "全部"
        // and other items fetched from S1
        list.add(getResources().getString(R.string.toolbar_spinner_drop_down_all_forums_item_title));
        list.addAll(dropDownItemList);

        ArrayAdapter<CharSequence> arrayAdapter =
                new ArrayAdapter<>(this, R.layout.toolbar_spinner_item, list);
        arrayAdapter.setDropDownViewResource(R.layout.toolbar_spinner_dropdown_item);

        return arrayAdapter;
    }
}
