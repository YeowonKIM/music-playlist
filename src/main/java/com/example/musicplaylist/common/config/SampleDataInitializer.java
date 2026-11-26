package com.example.musicplaylist.common.config;

import com.example.musicplaylist.playlist.domain.Playlist;
import com.example.musicplaylist.playlist.repository.PlaylistRepository;
import com.example.musicplaylist.song.domain.Song;
import com.example.musicplaylist.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.sample-data", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class SampleDataInitializer implements ApplicationRunner {

    private final SongRepository songRepository;
    private final PlaylistRepository playlistRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Rock
        Song yellow = createSong("Yellow", "Coldplay", "Rock", 269, "yellow");
        Song clocks = createSong("Clocks", "Coldplay", "Rock", 307, "clocks");
        Song fixYou = createSong("Fix You", "Coldplay", "Rock", 295, "fix-you");
        Song theScientist = createSong("The Scientist", "Coldplay", "Rock", 309, "the-scientist");
        Song vivaLaVida = createSong("Viva La Vida", "Coldplay", "Rock", 242, "viva-la-vida");
        Song bohemianRhapsody = createSong("Bohemian Rhapsody", "Queen", "Rock", 355, "bohemian-rhapsody");
        Song dontStopMeNow = createSong("Don't Stop Me Now", "Queen", "Rock", 209, "dont-stop-me-now");
        Song comeTogether = createSong("Come Together", "The Beatles", "Rock", 259, "come-together");

        // Pop
        Song hello = createSong("Hello", "Adele", "Pop", 295, "hello");
        Song rollingInTheDeep = createSong("Rolling in the Deep", "Adele", "Pop", 228, "rolling-in-the-deep");
        Song shapeOfYou = createSong("Shape of You", "Ed Sheeran", "Pop", 233, "shape-of-you");
        Song perfect = createSong("Perfect", "Ed Sheeran", "Pop", 263, "perfect");
        Song blankSpace = createSong("Blank Space", "Taylor Swift", "Pop", 231, "blank-space");
        Song shakeItOff = createSong("Shake It Off", "Taylor Swift", "Pop", 219, "shake-it-off");

        // Jazz
        Song soWhat = createSong("So What", "Miles Davis", "Jazz", 562, "so-what");
        Song blueInGreen = createSong("Blue in Green", "Miles Davis", "Jazz", 337, "blue-in-green");
        Song takeFive = createSong("Take Five", "Dave Brubeck", "Jazz", 324, "take-five");
        Song strangeMeadowLark = createSong("Strange Meadow Lark", "Dave Brubeck", "Jazz", 442, "strange-meadow-lark");

        // Hip Hop
        Song loseYourself = createSong("Lose Yourself", "Eminem", "Hip Hop", 326, "lose-yourself");
        Song withoutMe = createSong("Without Me", "Eminem", "Hip Hop", 290, "without-me");
        Song humble = createSong("HUMBLE.", "Kendrick Lamar", "Hip Hop", 177, "humble");
        Song alright = createSong("Alright", "Kendrick Lamar", "Hip Hop", 219, "alright");

        // Electronic
        Song oneMoreTime = createSong("One More Time", "Daft Punk", "Electronic", 320, "one-more-time");
        Song harderBetterFasterStronger = createSong("Harder, Better, Faster, Stronger", "Daft Punk", "Electronic", 225, "harder-better-faster-stronger");
        Song levels = createSong("Levels", "Avicii", "Electronic", 339, "levels");
        Song wakeMeUp = createSong("Wake Me Up", "Avicii", "Electronic", 247, "wake-me-up");

        // Classical
        Song moonlightSonata = createSong("Moonlight Sonata", "Beethoven", "Classical", 360, "moonlight-sonata");
        Song furElise = createSong("Fur Elise", "Beethoven", "Classical", 180, "fur-elise");
        Song nocturne = createSong("Nocturne Op. 9 No. 2", "Chopin", "Classical", 270, "nocturne");
        Song clairDeLune = createSong("Clair de Lune", "Debussy", "Classical", 300, "clair-de-lune");

        List<Song> songs = List.of(
                yellow, clocks, fixYou, theScientist, vivaLaVida,
                bohemianRhapsody, dontStopMeNow, comeTogether,
                hello, rollingInTheDeep, shapeOfYou, perfect,
                blankSpace, shakeItOff,
                soWhat, blueInGreen, takeFive, strangeMeadowLark,
                loseYourself, withoutMe, humble, alright,
                oneMoreTime, harderBetterFasterStronger, levels, wakeMeUp,
                moonlightSonata, furElise, nocturne, clairDeLune
        );

        songRepository.saveAll(songs);

        Playlist myFavorites = createPlaylist(
                "My Favorites",
                "A collection of favorite songs across different genres",
                yellow, hello, bohemianRhapsody, shapeOfYou,
                takeFive, loseYourself, oneMoreTime, clairDeLune
        );

        Playlist rockClassics = createPlaylist(
                "Rock Classics",
                "A selection of classic rock songs",
                yellow, clocks, bohemianRhapsody, dontStopMeNow, comeTogether
        );

        Playlist coldplayCollection = createPlaylist(
                "Coldplay Collection",
                "A collection of songs by Coldplay",
                yellow, clocks, fixYou, theScientist, vivaLaVida
        );

        Playlist mixedGenres = createPlaylist(
                "Mixed Genres",
                "An unbalanced mix of genres and artists",
                yellow, clocks, fixYou, bohemianRhapsody,
                hello, rollingInTheDeep,
                soWhat, humble, levels, moonlightSonata
        );

        Playlist emptyPlaylist = createPlaylist(
                "Empty Playlist",
                "A playlist without songs"
        );

        List<Playlist> playlists = List.of(
                myFavorites,
                rockClassics,
                coldplayCollection,
                mixedGenres,
                emptyPlaylist
        );

        playlistRepository.saveAll(playlists);

        log.info("Initialized sample data: {} songs and {} playlists",
                songs.size(), playlists.size());
    }

    private Song createSong(String title, String artist, String genre, int durationSeconds, String fileName) {
        return Song.builder()
                .title(title)
                .artist(artist)
                .genre(genre)
                .durationSeconds(durationSeconds)
                .uri("music/" + fileName + ".mp3")
                .build();
    }

    private Playlist createPlaylist(String name, String description, Song... songs) {
        Playlist playlist = Playlist.of(name, description);

        for (Song song : songs) {
            playlist.addSong(song);
        }

        return playlist;
    }
}