import React from 'react';
import {Link} from "@inertiajs/react";
import '../../css/styles.css';
import useAudioPlayer from "../hooks/useAudioPlayer.js";

const Chart = ({ chart }) => {
    return (
        <div className="page-container">
            <h1>Vlasje24</h1>
            <ul className="song-list">
                {chart.songs.map((song, index) => {
                    const {isPlaying, togglePlay} = useAudioPlayer(song.preview_url);
                    const isFirstWeek = song.weeks_in_chart === 1;
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
                                <div className="song-weeks">
                                    {positionChange !== null && (
                                        <span className={`position-change ${positionChange > 0 ? 'up' : 'down'}`}>
                                            {positionChange > 0 ? '↑' : '↓'} {Math.abs(positionChange)}
                                        </span>
                                    )}
                                    <span className="weeks-in-chart">{song.weeks_in_chart} weeks</span>
                                </div>
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
