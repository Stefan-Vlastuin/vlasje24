import React from 'react';
import '../../css/styles.css';
import useAudioPlayer from "../hooks/useAudioPlayer.js";
import { Link } from "@inertiajs/react";
import { Line } from 'react-chartjs-2';
import { Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend } from 'chart.js';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend);

const Song = ({ song, chartPositions }) => {
    const { isPlaying, togglePlay } = useAudioPlayer(song.preview_url);

    const chartData = {
        labels: chartPositions.map(position => position.date),
        datasets: [{
            label: 'Chart Position',
            data: chartPositions.map(position => position.position),
            borderColor: 'rgba(0, 123, 255)',
            backgroundColor: 'rgba(0, 123, 255)',
        }]
    };

    const chartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
            x: {
                offset: true,
                grid: {
                    display: false
                }
            },
            y: {
                reverse: true,
                min: 1,
                max: 24,
                ticks: {
                    stepSize: 5
                },
                offset: true,
            }
        },
        plugins: {
            legend: {
                display: false
            },
            tooltip: {
                callbacks: {
                    label: function (context) {
                        return `Positie: ${context.raw}`;
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
            <div className="chart-container">
                <Line data={chartData} options={chartOptions} />
            </div>
        </div>
    );
};

export default Song;
