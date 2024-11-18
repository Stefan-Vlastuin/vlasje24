import React from 'react';
import { Link } from '@inertiajs/inertia-react'
import '../../css/chart.css';

const Chart = ({ chart }) => {
    // if (!chart) {
    //     return <div>Loading chart...</div>;
    // }

    return (
        <div className="chart-page">
            <h1>Top Songs Chart</h1>
            <ul className="song-list">
                {chart.songs.map((song, index) => (
                    <li key={song.id} className="song-item">
                        <div className="song-number">{index + 1}.</div>
                        <Link href={`/song/${song.id}`} className="song-title">
                            {song.title}
                        </Link>
                        <div className="song-artists">
                            by {song.artists.map(artist => artist.name).join(", ")}
                        </div>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default Chart;
