import React, { useState } from 'react';
import { Link } from "@inertiajs/react";
import '../../css/styles.css';
import useAudioPlayer from "../hooks/useAudioPlayer.js";
import SearchBar from '../components/SearchBar.jsx';

const Chart = ({ chart, previousChartId, nextChartId }) => {
    const [playingSongId, setPlayingSongId] = useState(null);

    return (
        <div className="page-container">
            <h1>Vlasje24</h1>
            <SearchBar />
            <div className="chart-navigation">
                <Link
                    href={previousChartId ? `/chart/${previousChartId}` : '#'}
                    className={`navigation-button ${!previousChartId ? 'hidden' : ''}`}
                    aria-disabled={!previousChartId}
                >
                    &#9664;
                </Link>
                <span className="chart-date">{chart.date}</span>
                <Link
                    href={nextChartId ? `/chart/${nextChartId}` : '#'}
                    className={`navigation-button ${!nextChartId ? 'hidden' : ''}`}
                    aria-disabled={!nextChartId}
                >
                    &#9654;
                </Link>
            </div>
            <ul className="song-list">
                {chart.songs.map((song, index) => {
                    // Only play if this song is the one marked as playing
                    const isGlobalPlaying = playingSongId === song.id;
                    const { isPlaying, play, pause } = useAudioPlayer(song.preview_url, isGlobalPlaying);

                    const handlePlayPause = () => {
                        if (isGlobalPlaying) {
                            setPlayingSongId(null);
                            pause();
                        } else {
                            setPlayingSongId(song.id);
                            play();
                        }
                    };

                    const isFirstWeek = song.nr_of_weeks === 1;
                    const positionChange = song.position_change;
                    return (
                        <li key={song.id} className={`song-container ${isFirstWeek ? 'first-week' : ''}`}>
                            <div className="song-number">{index + 1}</div>
                            <img src={song.image_url} alt={song.title} className="song-image"/>
                            <div className="song-details">
                                <Link href={`/song/${song.id}`} className="song-title">
                                    {song.title}
                                </Link>
                                <div className="song-artists">
                                    {song.artists.map(artist => (
                                        <span key={artist.id}>
                                            <Link href={`/artist/${artist.id}`} className="artist-link">
                                                {artist.name}
                                            </Link>
                                            {song.artists.indexOf(artist) !== song.artists.length - 1 && ", "}
                                        </span>
                                    ))}
                                </div>
                            </div>
                            <div className="song-weeks">
                                {(positionChange !== null && positionChange !== 0) && (
                                    <span className={`position-change ${positionChange > 0 ? 'up' : 'down'}`}>
                                        {positionChange > 0 ? '↑' : '↓'} {Math.abs(positionChange)}
                                    </span>
                                )}
                                <span
                                    className="weeks-in-chart">{song.nr_of_weeks} {song.nr_of_weeks === 1 ? 'week' : 'weeks'}</span>
                            </div>
                            <button onClick={handlePlayPause} className="play-button">
                                {isGlobalPlaying && isPlaying ? '\u23F8' : '\u25B6'}
                            </button>
                        </li>
                    );
                })}
            </ul>
        </div>
    );
};

export default Chart;
