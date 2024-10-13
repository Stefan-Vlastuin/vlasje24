<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsToMany;

/**
 * @property int $id
 */
class Song extends Model
{
    use HasFactory;

    public function artists() : BelongsToMany {
        return $this->belongsToMany(Artist::class, 'song_artist')->orderBy('order')->withTimestamps();
    }

    public function charts() : BelongsToMany {
        return $this->belongsToMany(Chart::class, 'chart_song')->withTimestamps();
    }
}
