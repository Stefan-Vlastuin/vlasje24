import React from 'react';
import '../../css/song.css';

const Song = ({ song }) => {
    return (
        <div className="song-page">
            <div className="song-container">
                <div className="song-number">
                    {/* Show a static or dynamic number if relevant */}
                    1
                </div>
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
                    onClick={() => window.open(song.preview_url, '_blank')}
                >
                    ▶ Play
                </button>
            </div>
        </div>
    );
};

export default Song;
