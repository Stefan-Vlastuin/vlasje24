import React from 'react';
import { Link } from "@inertiajs/react";
import '../../css/styles.css';
import useAudioPlayer from "../hooks/useAudioPlayer.js";

const Chart = ({ chart, previousChartId, nextChartId, chartDate }) => {
    return (
        <div className="page-container">
            <div className="chart-navigation">
                {previousChartId && (
                    <Link href={`/chart/${previousChartId}`} className="navigation-button">
                        &#9664;
                    </Link>
                )}
                <span className="chart-date">{chartDate}</span>
                {nextChartId && (
                    <Link href={`/chart/${nextChartId}`} className="navigation-button">
                        &#9654;
                    </Link>
                )}
            </div>
            <h1>Vlasje24</h1>
            <ul className="song-list">
                {chart.songs.map((song, index) => {
                    const { isPlaying, togglePlay } = useAudioPlayer(song.preview_url);
                    const isFirstWeek = song.nr_of_weeks === 1;
                    const positionChange = song.position_change;
                    return (
                        <li key={song.id} className={`song-container ${isFirstWeek ? 'first-week' : ''}`}>
                            <div className="song-number">{index + 1}</div>
                            <img src={song.image_url} alt={song.title} className="song-image" />
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
                                <span className="weeks-in-chart">{song.nr_of_weeks} {song.nr_of_weeks === 1 ? 'week' : 'weeks'}</span>
                            </div>
                            <button onClick={togglePlay} className="play-button">
                                {isPlaying ? '\u23F8' : '\u25B6'}
                            </button>
                        </li>
                    );
                })}
            </ul>
        </div>
    );
};

export default Chart;
