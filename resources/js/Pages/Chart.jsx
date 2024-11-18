import React from 'react';
import {Link} from "@inertiajs/react";
import '../../css/chart.css';

const Chart = ({ chart }) => {
    return (
        <div className="chart-page">
            <h1>Vlasje24</h1>
            <ul className="song-list">
                {chart.songs.map((song, index) => (
                    <li key={song.id} className="song-item">
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
                        <button onClick={() => playSong(song.preview_url)} className="play-button">
                            ▶ Play
                        </button>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default Chart;
