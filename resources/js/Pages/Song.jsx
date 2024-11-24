import React from 'react';
import '../../css/styles.css';

const Song = ({ song }) => {
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
                    onClick={() => window.open(song.preview_url, '_blank')}
                >
                    ▶ Play
                </button>
            </div>
        </div>
    );
};

export default Song;
