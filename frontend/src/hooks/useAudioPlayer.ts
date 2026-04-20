import { createContext, useContext, useRef, useState, useCallback } from 'react'

interface AudioPlayerContext {
  playingId: number | null
  toggle: (songId: number, previewUrl: string) => void
}

export const AudioPlayerContext = createContext<AudioPlayerContext>({
  playingId: null,
  toggle: () => {},
})

export function useAudioPlayerState(): AudioPlayerContext {
  const [playingId, setPlayingId] = useState<number | null>(null)
  const audioRef = useRef<HTMLAudioElement | null>(null)

  const toggle = useCallback((songId: number, previewUrl: string) => {
    if (audioRef.current) {
      audioRef.current.pause()
      audioRef.current.onended = null
      audioRef.current = null
    }

    if (playingId === songId) {
      setPlayingId(null)
      return
    }

    const audio = new Audio(previewUrl)
    audio.play().catch(() => {})
    audio.onended = () => {
      setPlayingId(null)
      audioRef.current = null
    }
    audioRef.current = audio
    setPlayingId(songId)
  }, [playingId])

  return { playingId, toggle }
}

export function useAudioPlayer() {
  return useContext(AudioPlayerContext)
}
