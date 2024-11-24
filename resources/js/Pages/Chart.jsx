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
                    return (
                        <li key={song.id} className="song-container">
                            <div className="song-number">{index + 1}</div>
                            <img src={song.image_url} alt={song.title} className="song-image"/>
                            <div className="song-details">
                                <Link href={`/song/${song.id}`} className="song-title">
                                    {song.title}
                                </Link>
                                <div className="song-artists">
                                    {song.artists.map(artist => artist.name).join(", ")}
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
