package de.m4lik.monarchcode.burningseries.objects;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Malik on 01.10.2016.
 */

public class SeasonObj {

    private FullShowObj series;
    private Integer season;
    @SerializedName("epi")
    private Episode[] episodes;

    public FullShowObj getSeries() {
        return series;
    }

    public Integer getSeason() {
        return season;
    }

    public Episode[] getEpisodes() {
        return episodes;
    }

    public class Episode {
        private String german;
        private String english;
        private Integer epi;
        private boolean watched;

        public Episode(String german, String english, Integer epi, boolean watched) {
            this.german = german;
            this.english = english;
            this.epi = epi;
            this.watched = watched;
        }

        public String getGermanTitle() {
            return german;
        }

        public String getEnglishTitle() {
            return english;
        }

        public Integer getEpisodeID() {
            return epi;
        }

        public boolean isWatched() {
            return watched;
        }
    }
}
