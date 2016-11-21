package de.monarchcode.m4lik.burningseries.mainFragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import de.monarchcode.m4lik.burningseries.MainActivity;
import de.monarchcode.m4lik.burningseries.R;
import de.monarchcode.m4lik.burningseries.ShowActivity;
import de.monarchcode.m4lik.burningseries.database.MainDBHelper;
import de.monarchcode.m4lik.burningseries.objects.ShowListItem;

import static de.monarchcode.m4lik.burningseries.database.SeriesContract.seriesTable.COLUMN_NAME_GENRE;
import static de.monarchcode.m4lik.burningseries.database.SeriesContract.seriesTable.COLUMN_NAME_ID;
import static de.monarchcode.m4lik.burningseries.database.SeriesContract.seriesTable.COLUMN_NAME_ISFAV;
import static de.monarchcode.m4lik.burningseries.database.SeriesContract.seriesTable.COLUMN_NAME_TITLE;
import static de.monarchcode.m4lik.burningseries.database.SeriesContract.seriesTable.TABLE_NAME;


/**
 * A simple {@link Fragment} subclass.
 */
public class SeriesFragment extends Fragment {

    View rootView;

    ListView seriesListView;
    List<ShowListItem> seriesList = new ArrayList<>();

    ProgressDialog progressDialog;

    String requestStatus;


    public SeriesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_series, container, false);
        seriesListView = (ListView) rootView.findViewById(R.id.seriesListView);


        MenuItem searchItem = MainActivity.getMenu().findItem(R.id.action_search);
        searchItem.setVisible(true);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return false;
            }
        });


        fillList();


        return rootView;
    }

    public void filterList(String query) {

        List<ShowListItem> filteredList = new ArrayList<>();

        for (ShowListItem single : seriesList) {
            if (single.getTitle().toLowerCase().contains(query.toLowerCase()))
                filteredList.add(single);
        }

        refreshList(filteredList);
    }

    public void fillList() {

        seriesList.clear();

        MainDBHelper dbHelper = new MainDBHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                COLUMN_NAME_ID,
                COLUMN_NAME_TITLE,
                COLUMN_NAME_GENRE,
                COLUMN_NAME_ISFAV
        };

        String sortOrder =
                COLUMN_NAME_TITLE + " ASC";

        Cursor c = db.query(
                TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        while (c.moveToNext()) {
            seriesList.add(new ShowListItem(
                    c.getString(c.getColumnIndex(COLUMN_NAME_TITLE)),
                    c.getInt(c.getColumnIndex(COLUMN_NAME_ID)),
                    c.getString(c.getColumnIndex(COLUMN_NAME_GENRE)),
                    c.getInt(c.getColumnIndex(COLUMN_NAME_ISFAV)) == 1
            ));
        }

        refreshList();
    }

    public void refreshList() {
        refreshList(null);
    }

    private void refreshList(List<ShowListItem> inputList) {
        Log.d("BS", "Refreshing list..");

        if (inputList == null) {
            inputList = seriesList;
        }

        if (inputList.size() == 0)
            Log.w("BS", "List is empty");
        else
            Log.d("BS", "List containts " + inputList.size() + " elements");

        String[][] series = new String[inputList.size()][4];

        int i = 0;
        for (ShowListItem show : inputList) {
            series[i][0] = show.getTitle();
            series[i][1] = show.getId().toString();
            series[i][2] = show.getGenre();
            series[i][3] = show.isFav() ? "1" : "0";
            i++;
        }

        Arrays.sort(series, new Comparator<String[]>() {
            @Override
            public int compare(final String[] entry1, final String[] entry2) {
                final String show1 = entry1[0];
                final String show2 = entry2[0];
                return show1.compareTo(show2);
            }
        });

        List<ShowListItem> list = new ArrayList<>();
        i = 0;
        for (String[] show : series) {
            list.add(new ShowListItem(show[0], Integer.parseInt(show[1]), show[2], show[3].equals("1")));
            i++;
        }

        if (inputList.size() >= 1) {
            ArrayAdapter<ShowListItem> adapter = new seriesListAdapter(inputList);
            seriesListView.setAdapter(adapter);

            seriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView nameView = (TextView) view.findViewById(R.id.seriesTitle);
                    TextView idView = (TextView) view.findViewById(R.id.seriesId);
                    TextView genreView = (TextView) view.findViewById(R.id.seriesGenre);
                    showSeries(Integer.parseInt(idView.getText().toString()), nameView.getText().toString(), genreView.getText().toString());
                }
            });

            Log.d("BS", "Everything done.");
            Log.d("BS", "----------------------------");
        } else {
            seriesListView.setAdapter(null);
            Snackbar snackbar = Snackbar.make(rootView.findViewById(android.R.id.content), "Fehler beim Laden der Serien.", Snackbar.LENGTH_SHORT);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
            snackbar.show();
        }

        rootView.findViewById(R.id.avi).setVisibility(View.GONE);
    }

    private void showSeries(Integer id, String name, String genre) {
        Intent i = new Intent(getActivity(), ShowActivity.class);
        i.putExtra("ShowName", name);
        i.putExtra("ShowID", id);
        i.putExtra("ShowGenre", genre);
        startActivity(i);
    }

    class seriesListAdapter extends ArrayAdapter<ShowListItem> {

        private List<ShowListItem> list;

        seriesListAdapter(List<ShowListItem> list) {
            super(getActivity(), R.layout.list_item_series, seriesList);
            this.list = list;
        }

        @Override
        public View getView(int pos, View view, ViewGroup parent) {
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.list_item_series, parent, false);
            }

            ShowListItem current = list.get(pos);

            TextView title = (TextView) view.findViewById(R.id.seriesTitle);
            title.setText(current.getTitle());

            TextView genre = (TextView) view.findViewById(R.id.seriesGenre);
            genre.setText(current.getGenre());

            TextView id = (TextView) view.findViewById(R.id.seriesId);
            id.setText(current.getId().toString());

            ImageView fav = (ImageView) view.findViewById(R.id.favImageView);
            fav.setImageDrawable(ContextCompat.getDrawable(getContext(), current.isFav() ? R.drawable.ic_star : R.drawable.ic_star_border));

            return view;
        }

        @Override
        public int getCount() {
            return list != null ? list.size() : 0;
        }
    }
}