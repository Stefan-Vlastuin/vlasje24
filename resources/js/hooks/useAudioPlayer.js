import { useState, useEffect, useRef } from 'react';

const useAudioPlayer = (previewUrl, shouldPlay = false) => {
    const [isPlaying, setIsPlaying] = useState(false);
    const audioRef = useRef(new Audio(previewUrl));

    useEffect(() => {
        audioRef.current.src = previewUrl;
        audioRef.current.load();
        setIsPlaying(false);
    }, [previewUrl]);

    useEffect(() => {
        const handleSongEnd = () => setIsPlaying(false);
        const audio = audioRef.current;
        audio.addEventListener('ended', handleSongEnd);

        return () => {
            audio.removeEventListener('ended', handleSongEnd);
            audio.pause();
        };
    }, []);

    useEffect(() => {
        if (shouldPlay) {
            audioRef.current.play();
            setIsPlaying(true);
        } else {
            audioRef.current.pause();
            setIsPlaying(false);
        }
    }, [shouldPlay]);

    const play = () => {
        audioRef.current.play();
        setIsPlaying(true);
    };

    const pause = () => {
        audioRef.current.pause();
        setIsPlaying(false);
    };

    return { isPlaying, play, pause };
};

export default useAudioPlayer;
