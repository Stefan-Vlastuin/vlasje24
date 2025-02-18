<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Collection;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsToMany;

/**
 * @property int $id
 * @property Collection songs
 */
class Artist extends Model
{
    use HasFactory;

    public function songs() : BelongsToMany {
        return $this->belongsToMany(Song::class, 'song_artist')->withTimestamps();
    }

    public function getPoints(?int $year) : int {
        return $this->songs->map(function (Song $song) use ($year) {
            return $song->getPoints($year);
        })->sum();
    }

    public function getNrOfWeeks(?int $year) : int {
        return $this->songs->map(function (Song $song) use ($year) {
            return $song->getNrOfWeeks($year);
        })->sum();
    }

    public function getHighestPosition(?int $year) : int {
        return $this->songs->map(function (Song $song) use ($year) {
            return $song->getHighestPosition($year);
        })->min();
    }
}
