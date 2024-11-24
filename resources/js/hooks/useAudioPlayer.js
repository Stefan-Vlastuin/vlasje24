import { useState, useEffect } from 'react';

const useAudioPlayer = (previewUrl) => {
    const [isPlaying, setIsPlaying] = useState(false);
    const [audio] = useState(new Audio(previewUrl));

    useEffect(() => {
        // Add event listener for when the song ends
        const handleSongEnd = () => {
            setIsPlaying(false);  // Stop the playback when the song ends
        };

        // Listen for the 'ended' event
        audio.addEventListener('ended', handleSongEnd);

        // Cleanup the event listener when the component unmounts or audio changes
        return () => {
            audio.removeEventListener('ended', handleSongEnd);
            audio.pause(); // Stop audio when navigating away
        };
    }, [audio]);

    const togglePlay = () => {
        if (isPlaying) {
            audio.pause();
        } else {
            audio.play();
        }
        setIsPlaying(!isPlaying);
    };

    return { isPlaying, togglePlay };
};

export default useAudioPlayer;
