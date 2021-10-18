package apps.vidstatus.android.shotvideo.adapter.pageradapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import apps.vidstatus.android.shotvideo.fragment.CategoryFragment;
import apps.vidstatus.android.shotvideo.fragment.NewFragment;
import apps.vidstatus.android.shotvideo.fragment.SearchFragment;
import apps.vidstatus.android.shotvideo.fragment.TrendingFragment;
import apps.vidstatus.android.shotvideo.utils.Constant;


public class ViewPagerAdapter extends FragmentPagerAdapter {
    private static final int NUM_ITEMS = 4;
    private final String[] tabTitles = new String[]{Constant.NAV_SELECTED_ITEM, Constant.NAV_SELECTED_ITEM, "Category", "Search"};

    public ViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: // Fragment # 0 - This will show FirstFragment
                return new NewFragment();
            case 1: // Fragment # 0 - This will show FirstFragment different title
                return new TrendingFragment();
            case 2: // Fragment # 1 - This will show SecondFragment
                return new CategoryFragment();
            case 3:
                return new SearchFragment();
            default:
                return null;
        }
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }


    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

}
