import React, { useEffect, useState } from 'react';
import '../../css/styles.css';
import useAudioPlayer from "../hooks/useAudioPlayer.js";
import { Link } from "@inertiajs/react";
import { Line } from 'react-chartjs-2';
import { Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend } from 'chart.js';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend);

const Song = ({ song }) => {
    const { isPlaying, togglePlay } = useAudioPlayer(song.preview_url);
    const [chartData, setChartData] = useState(null);

    useEffect(() => {
        const fetchChartData = async () => {
            const response = await fetch(`/api/song/${song.id}/chart-positions`);
            const data = await response.json();
            setChartData(data);
        };

        fetchChartData();
    }, [song.id]);

    const chartOptions = {
        responsive: true,
        scales: {
            y: {
                reverse: true,
                min: 1,
                max: 24,
                ticks: {
                    stepSize: 1
                }
            }
        },
        plugins: {
            tooltip: {
                callbacks: {
                    label: function (context) {
                        return `Position: ${context.raw}`;
                    }
                }
            }
        }
    };

    return (
        <div className="page-container">
            <div className="song-container">
                <img
                    className="song-image"
                    src={song.image_url}
                    alt={`${song.title} cover`}
                />
                <div className="song-details">
                    <h1 className="song-title">{song.title}</h1>
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
                <button
                    className="play-button"
                    onClick={togglePlay}
                >
                    {isPlaying ? '\u23F8' : '\u25B6'}
                </button>
            </div>
            {chartData && (
                <div className="chart-container">
                    <Line data={chartData} options={chartOptions} />
                </div>
            )}
        </div>
    );
};

export default Song;
