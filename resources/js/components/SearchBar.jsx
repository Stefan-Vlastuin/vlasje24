import React, { useState } from 'react';
import axios from 'axios';
import '../../css/SearchBar.css';

const SearchBar = () => {
    const [query, setQuery] = useState('');
    const [results, setResults] = useState([]);

    const handleInputChange = async (event) => {
        const value = event.target.value;
        setQuery(value);

        if (value.length > 0) {
            const response = await axios.get(`/search?q=${value}`);
            setResults(response.data);
        } else {
            setResults([]);
        }
    };

    const handleResultClick = (result) => {
        if (result.type === 'song') {
            window.location.href = `/song/${result.id}`;
        } else if (result.type === 'artist') {
            window.location.href = `/artist/${result.id}`;
        }
    };

    return (
        <div className="search-bar">
            <input
                type="text"
                value={query}
                onChange={handleInputChange}
                placeholder="Zoek op titel of artiest"
            />
            {results.length > 0 && (
                <ul className="search-results">
                    {results.map((result) => (
                        <li key={`${result.type}-${result.id}`} onClick={() => handleResultClick(result)}>
                            {result.text}
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
};

export default SearchBar;
