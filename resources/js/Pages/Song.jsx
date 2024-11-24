import React, {useEffect, useState} from 'react';
import '../../css/styles.css';
import useAudioPlayer from "../hooks/useAudioPlayer.js";

const Song = ({ song }) => {
    const { isPlaying, togglePlay } = useAudioPlayer(song.preview_url);

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
                    <p className="song-artists">
                        {song.artists.map((artist, index) => (
                            <span key={index}>
                                {artist.name}
                                {index < song.artists.length - 1 && ', '}
                            </span>
                        ))}
                    </p>
                </div>
                <button
                    className="play-button"
                    onClick={togglePlay}
                >
                    {isPlaying ? '\u23F8' : '\u25B6'}
                </button>
            </div>
        </div>
    );
};

export default Song;
