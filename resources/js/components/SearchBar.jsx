import React, { useState } from 'react';
import axios from 'axios';

const SearchBar = () => {
    const [query, setQuery] = useState('');
    const [results, setResults] = useState([]);

    const handleInputChange = async (event) => {
        const value = event.target.value;
        setQuery(value);

        if (value.length > 2) {
            const response = await axios.get(`/search?q=${value}`);
            setResults(response.data);
        } else {
            setResults([]);
        }
    };

    const handleResultClick = (result) => {
        window.location.href = `/song/${result.id}`;
    };

    return (
        <div className="search-bar">
            <input
                type="text"
                value={query}
                onChange={handleInputChange}
                placeholder="Search for songs..."
            />
            {results.length > 0 && (
                <ul className="search-results">
                    {results.map((result) => (
                        <li key={result.id} onClick={() => handleResultClick(result)}>
                            {result.title}
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
};

export default SearchBar;
