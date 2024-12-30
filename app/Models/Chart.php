<?php

namespace App\Models;

use Carbon\Carbon;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsToMany;
use Illuminate\Support\Collection;

/**
 * @property int $id
 * @property Collection $songs
 * @property Carbon $date
 */
class Chart extends Model
{
    use HasFactory;

    protected $casts = [
        'date' => 'date'
    ];

    public function songs() : BelongsToMany {
        return $this->belongsToMany(Song::class, 'chart_song')->withPivot('order')->orderby('order')->withTimestamps();
    }
}
