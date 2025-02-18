import React, {useEffect, useState} from 'react';
import {Link, router} from '@inertiajs/react';
import '../../css/styles.css';

const Ranking = ({ songs, artists, year, rankingType, years }) => {
    const [selectedRankingType, setSelectedRankingType] = useState(rankingType);
    const [displayType, setDisplayType] = useState('songs');
    const [showAll, setShowAll] = useState(false);
    const [selectedYear, setSelectedYear] = useState(year);
    const [sortedSongs, setSortedSongs] = useState([]);
    const [sortedArtists, setSortedArtists] = useState([]);

    useEffect(() => {
        sortSongs();
    }, [selectedRankingType, songs]);

    useEffect(() => {
        sortArtists();
    }, [selectedRankingType, artists]);

    const handleRankingTypeChange = (event) => {
        setSelectedRankingType(event.target.value);
    };

    const handleDisplayTypeChange = (event) => {
        setDisplayType(event.target.value);
    };

    const handleShowAllClick = () => {
        setShowAll(true);
    };

    const handleYearChange = (event) => {
        const newYear = event.target.value;
        router.visit(`/ranking/${newYear}`);
    };

    const rankingNumber = (songOrArtist) => {
        if (selectedRankingType === 'weeks') {
            return songOrArtist.nrOfWeeks;
        } else if (selectedRankingType === 'highest') {
            return songOrArtist.highestPosition;
        } else {
            return songOrArtist.points;
        }
    }

    const sortSongs = () => {
        const sorted = [...songs].sort((a, b) => {
            if (selectedRankingType === 'weeks') {
                return b.nrOfWeeks - a.nrOfWeeks;
            } else if (selectedRankingType === 'highest') {
                return a.highestPosition - b.highestPosition;
            } else {
                return b.points - a.points;
            }
        });
        setSortedSongs(sorted);
    };

    const sortArtists = () => {
        const sorted = [...artists].sort((a, b) => {
            if (selectedRankingType === 'weeks') {
                return b.nrOfWeeks - a.nrOfWeeks;
            } else if (selectedRankingType === 'highest') {
                return a.highestPosition - b.highestPosition;
            } else {
                return b.points - a.points;
            }
        });
        setSortedArtists(sorted);
    };

    const renderSongs = () => {
        const songsToShow = showAll ? sortedSongs : sortedSongs.slice(0, 3);
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
                                {rankingNumber(song)}
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
        const artistsToShow = showAll ? sortedArtists : sortedArtists.slice(0, 3);
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
                                {rankingNumber(artist)}
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
            <h1>Ranking {selectedYear ? selectedYear : 'All-Time'}</h1>
            <div className="ranking-options">
                <label>
                    <input
                        type="radio"
                        value="points"
                        checked={selectedRankingType === 'points'}
                        onChange={handleRankingTypeChange}
                    />
                    Number of Points
                </label>
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
            </div>
            <div className="ranking-options">
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
            <div className="year-selection">
                <label>
                    Select Year:
                    <select value={selectedYear} onChange={handleYearChange}>
                        <option key="all" value="all">All-Time</option>
                        {years.map(year => {
                            return <option key={year} value={year}>{year}</option>;
                        })}
                    </select>
                </label>
            </div>
            {displayType === 'songs' ? renderSongs() : renderArtists()}
        </div>
    );
};

export default Ranking;
