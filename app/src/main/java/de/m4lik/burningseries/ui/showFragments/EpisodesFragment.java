package de.m4lik.burningseries.ui.showFragments;


import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.m4lik.burningseries.R;
import de.m4lik.burningseries.api.API;
import de.m4lik.burningseries.api.APIInterface;
import de.m4lik.burningseries.api.objects.EpisodeObj;
import de.m4lik.burningseries.api.objects.SeasonObj;
import de.m4lik.burningseries.api.objects.VideoObj;
import de.m4lik.burningseries.ui.ShowActivity;
import de.m4lik.burningseries.ui.listitems.EpisodeListItem;
import de.m4lik.burningseries.ui.viewAdapters.EpisodesRecyclerAdapter;
import de.m4lik.burningseries.util.Settings;
import de.m4lik.burningseries.util.listeners.RecyclerItemClickListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class EpisodesFragment extends Fragment implements Callback<SeasonObj> {

    View rootView;

    Integer selectedShow;
    Integer selectedSeason;

    String userSession;

    @BindView(R.id.episodesRecyclerView)
    RecyclerView episodesRecyclerView;

    List<EpisodeListItem> episodesList = new ArrayList<>();

    boolean loaded = false;

    public EpisodesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_episodes, container, false);

        ButterKnife.bind(this, rootView);

        selectedShow = ((ShowActivity) getActivity()).getSelectedShow();
        selectedSeason = ((ShowActivity) getActivity()).getSelectedSeason();

        userSession = Settings.of(getActivity()).getUserSession();

        API api = new API();
        api.setSession(userSession);
        api.generateToken("series/" + selectedShow + "/" + selectedSeason);
        APIInterface apiI = api.getInterface();
        Call<SeasonObj> call = apiI.getSeason(api.getToken(), api.getUserAgent(), selectedShow, selectedSeason, api.getSession());
        call.enqueue(this);

        return rootView;
    }

    @Override
    public void onResponse(Call<SeasonObj> call, Response<SeasonObj> response) {

        SeasonObj season = response.body();

        Integer i = 1;
        for (SeasonObj.Episode episode : season.getEpisodes()) {
            episodesList.add(new EpisodeListItem(episode.getGermanTitle(), episode.getEnglishTitle(), episode.getEpisodeID(), episode.isWatched() == 1));
            i++;
        }

        loaded = true;

        refreshList();
    }

    @Override
    public void onFailure(Call<SeasonObj> call, Throwable t) {
        Snackbar.make(rootView, "Fehler beim Laden der Episoden", Snackbar.LENGTH_SHORT);
    }

    private void refreshList() {

        episodesRecyclerView.setAdapter(new EpisodesRecyclerAdapter(getActivity(), episodesList));
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        episodesRecyclerView.setLayoutManager(llm);
        episodesRecyclerView.setHasFixedSize(true);
        episodesRecyclerView.setNestedScrollingEnabled(false);
        episodesRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), episodesRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {

                    @Override
                    public void onItemClick(View view, int position) {
                        Integer id = episodesList.get(position).getId();
                        String name = episodesList.get(position).getTitleGer().equals("") ? episodesList.get(position).getTitle() : episodesList.get(position).getTitleGer();
                        showEpisode(id, name);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        EpisodeListItem episode = episodesList.get(position);
                        Integer selectedEpisode = episode.getId();

                        final API api = new API();
                        api.setSession(userSession);
                        api.generateToken("series/" + selectedShow + "/" + selectedSeason + "/" + selectedEpisode);
                        APIInterface apii = api.getInterface();
                        Call<EpisodeObj> call = apii.getEpisode(api.getToken(), api.getUserAgent(), selectedShow, selectedSeason, selectedEpisode, api.getSession());
                        call.enqueue(new Callback<EpisodeObj>() {
                            @Override
                            public void onResponse(Call<EpisodeObj> call, Response<EpisodeObj> response) {

                                Integer episodeID = response.body().getEpisode().getEpisodeId();

                                api.generateToken("unwatch/" + episodeID);
                                APIInterface apii = api.getInterface();
                                Call<VideoObj> ucall = apii.unwatch(api.getToken(), api.getUserAgent(), episodeID, api.getSession());
                                ucall.enqueue(new Callback<VideoObj>() {
                                    @Override
                                    public void onResponse(Call<VideoObj> call, Response<VideoObj> response) {

                                        TextView titleGerView = view.findViewById(R.id.episodeTitleGer);
                                        titleGerView.setTextColor(ContextCompat.getColor(getActivity().getApplicationContext(), android.R.color.black));

                                        ImageView fav = view.findViewById(R.id.watchedImageView);
                                        fav.setImageDrawable(null);
                                    }

                                    @Override
                                    public void onFailure(Call<VideoObj> call, Throwable t) {

                                    }
                                });
                            }

                            @Override
                            public void onFailure(Call<EpisodeObj> call, Throwable t) {

                            }
                        });
                    }
                })
        );


    }

    private void showEpisode(Integer id, String name) {
        ((ShowActivity) getActivity()).setSelectedEpisode(id);
        ((ShowActivity) getActivity()).setEpisodeName(name);
        ((ShowActivity) getActivity()).switchEpisodesToHosters();
    }
}
