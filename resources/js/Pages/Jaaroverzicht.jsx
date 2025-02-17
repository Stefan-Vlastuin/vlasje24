import React, { useState } from 'react';
import { Link } from '@inertiajs/react';
import '../../css/styles.css';

const Jaaroverzicht = ({ rankedSongs, rankedArtists, year, rankingType }) => {
    const [selectedRankingType, setSelectedRankingType] = useState(rankingType);
    const [displayType, setDisplayType] = useState('songs');
    const [showAll, setShowAll] = useState(false);

    const handleRankingTypeChange = (event) => {
        setSelectedRankingType(event.target.value);
    };

    const handleDisplayTypeChange = (event) => {
        setDisplayType(event.target.value);
    };

    const handleShowAllClick = () => {
        setShowAll(true);
    };

    const renderSongs = () => {
        const songsToShow = showAll ? rankedSongs : rankedSongs.slice(0, 3);
        return (
            <>
                <h2>Songs</h2>
                <ul className="song-list">
                    {songsToShow.map((song, index) => (
                        <li key={song.id} className="song-container">
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
                            <div className="song-points">
                                {song.points} {selectedRankingType === 'weeks' ? 'weeks' : 'points'}
                            </div>
                        </li>
                    ))}
                </ul>
                {!showAll && (
                    <button onClick={handleShowAllClick} className="show-all-button">
                        Show All
                    </button>
                )}
            </>
        );
    };

    const renderArtists = () => {
        const artistsToShow = showAll ? rankedArtists : rankedArtists.slice(0, 3);
        return (
            <>
                <h2>Artists</h2>
                <ul className="artist-list">
                    {artistsToShow.map((artist, index) => (
                        <li key={artist.id} className="artist-container">
                            <div className="artist-number">{index + 1}</div>
                            <div className="artist-details">
                                <Link href={`/artist/${artist.id}`} className="artist-name">
                                    {artist.name}
                                </Link>
                            </div>
                            <div className="artist-points">
                                {artist.points} {selectedRankingType === 'weeks' ? 'weeks' : 'points'}
                            </div>
                        </li>
                    ))}
                </ul>
                {!showAll && (
                    <button onClick={handleShowAllClick} className="show-all-button">
                        Show All
                    </button>
                )}
            </>
        );
    };

    return (
        <div className="page-container">
            <h1>Jaaroverzicht {year ? year : 'All-Time'}</h1>
            <div className="ranking-options">
                <label>
                    <input
                        type="radio"
                        value="weeks"
                        checked={selectedRankingType === 'weeks'}
                        onChange={handleRankingTypeChange}
                    />
                    Number of Weeks
                </label>
                <label>
                    <input
                        type="radio"
                        value="highest"
                        checked={selectedRankingType === 'highest'}
                        onChange={handleRankingTypeChange}
                    />
                    Highest Position
                </label>
                <label>
                    <input
                        type="radio"
                        value="points"
                        checked={selectedRankingType === 'points'}
                        onChange={handleRankingTypeChange}
                    />
                    Number of Points
                </label>
            </div>
            <div className="display-options">
                <label>
                    <input
                        type="radio"
                        value="songs"
                        checked={displayType === 'songs'}
                        onChange={handleDisplayTypeChange}
                    />
                    Songs
                </label>
                <label>
                    <input
                        type="radio"
                        value="artists"
                        checked={displayType === 'artists'}
                        onChange={handleDisplayTypeChange}
                    />
                    Artists
                </label>
            </div>
            {displayType === 'songs' ? renderSongs() : renderArtists()}
        </div>
    );
};

export default Jaaroverzicht;
