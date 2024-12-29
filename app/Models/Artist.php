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
}
